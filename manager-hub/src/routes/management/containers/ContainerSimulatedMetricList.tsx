/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React from "react";
import {IContainer} from "./Container";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {
    loadContainerSimulatedMetrics,
    loadSimulatedContainerMetrics,
    removeContainerSimulatedMetrics
} from "../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {ISimulatedContainerMetric} from "../metrics/containers/SimulatedContainerMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    simulatedMetrics: { [key: string]: ISimulatedContainerMetric },
    simulatedMetricsName: string[];
}

interface DispatchToProps {
    loadSimulatedContainerMetrics: (name?: string) => any;
    loadContainerSimulatedMetrics: (containerId: string) => void;
    removeContainerSimulatedMetrics: (containerId: string, simulatedMetrics: string[]) => void;
}

interface ContainerSimulatedMetricListProps {
    isLoadingContainer: boolean;
    loadContainerError?: string | null;
    container?: IContainer | Partial<IContainer> | null;
    unsavedSimulatedMetrics: string[];
    onAddSimulatedContainerMetric: (simulatedMetric: string) => void;
    onRemoveSimulatedContainerMetrics: (simulatedMetric: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ContainerSimulatedMetricListProps;

interface State {
    entitySaved: boolean;
}

class ContainerSimulatedMetricList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadSimulatedContainerMetrics();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.container?.containerId && this.props.container?.containerId) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingContainer || this.props.isLoading : undefined}
                               error={!isNew ? this.props.loadContainerError || this.props.error : undefined}
                               emptyMessage={`Sem métricas métricas simuladas associadas`}
                               data={this.props.simulatedMetricsName}
                               dropdown={{
                                   id: 'simulatedMetrics',
                                   title: 'Selecionar a métrica simulada',
                                   empty: 'Não há métricas simuladas disponíveis',
                                   data: this.getSelectableSimulatedMetrics()
                               }}
                               show={this.simulatedMetric}
                               onAdd={this.onAdd}
                               onRemove={this.onRemove}
                               onDelete={{
                                   url: `containers/${this.props.container?.containerId}/simulated-metrics`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.container?.containerId) {
            const {containerId} = this.props.container;
            this.props.loadContainerSimulatedMetrics(containerId);
        }
    };

    private isNew = () =>
        this.props.container?.containerId === undefined;

    private simulatedMetric = (index: number, simulatedMetric: string, separate: boolean, checked: boolean,
                               handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedSimulatedMetrics.includes(simulatedMetric);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${styles.linkedItemContent}`}>
                    <label>
                        <input id={simulatedMetric}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                                {simulatedMetric}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/métricas simuladas/contentores/${simulatedMetric}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (simulatedMetric: string): void =>
        this.props.onAddSimulatedContainerMetric(simulatedMetric);

    private onRemove = (simulatedMetrics: string[]) =>
        this.props.onRemoveSimulatedContainerMetrics(simulatedMetrics);

    private onDeleteSuccess = (simulatedMetrics: string[]): void => {
        if (this.props.container?.containerId) {
            const {containerId} = this.props.container;
            this.props.removeContainerSimulatedMetrics(containerId, simulatedMetrics);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remover a métrica simulada`, 10000, reason, true);

    private getSelectableSimulatedMetrics = () => {
        const {simulatedMetrics, simulatedMetricsName, unsavedSimulatedMetrics} = this.props;
        return Object.keys(simulatedMetrics).filter(name => !simulatedMetricsName.includes(name) && !unsavedSimulatedMetrics.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: ContainerSimulatedMetricListProps): StateToProps {
    const containerId = ownProps.container?.containerId;
    const container = containerId && state.entities.containers.data[containerId];
    const simulatedMetricsName = container && container.containerSimulatedMetrics;
    return {
        isLoading: state.entities.containers.isLoadingSimulatedMetrics,
        error: state.entities.containers.loadSimulatedMetricsError,
        simulatedMetrics: Object.entries(state.entities.simulatedMetrics.containers.data)
            .map(([key, value]) => ({[key]: value}))
            .reduce((fields, field) => {
                for (let key in field) {
                    fields[key] = field[key];
                }
                return fields;
            }, {}),
        simulatedMetricsName: simulatedMetricsName || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadSimulatedContainerMetrics,
        loadContainerSimulatedMetrics,
        removeContainerSimulatedMetrics,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ContainerSimulatedMetricList);