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
import {ICloudHost} from "./CloudHost";
import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {
    loadCloudHostSimulatedMetrics,
    loadSimulatedHostMetrics,
    removeCloudHostSimulatedMetrics
} from "../../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {ISimulatedHostMetric} from "../../metrics/hosts/SimulatedHostMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    simulatedMetrics: { [key: string]: ISimulatedHostMetric },
    simulatedMetricsName: string[];
}

interface DispatchToProps {
    loadSimulatedHostMetrics: (name?: string) => any;
    loadCloudHostSimulatedMetrics: (cloudHost: string) => void;
    removeCloudHostSimulatedMetrics: (cloudHost: string, simulatedMetrics: string[]) => void;
}

interface HostSimulatedMetricListProps {
    isLoadingCloudHost: boolean;
    loadCloudHostError?: string | null;
    cloudHost: ICloudHost | Partial<ICloudHost> | null;
    unsavedSimulatedMetrics: string[];
    onAddSimulatedHostMetric: (simulatedMetric: string) => void;
    onRemoveSimulatedHostMetrics: (simulatedMetric: string[]) => void;
}

type Props = StateToProps & DispatchToProps & HostSimulatedMetricListProps;

interface State {
    entitySaved: boolean;
}

class CloudHostSimulatedMetricList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadSimulatedHostMetrics();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.cloudHost?.instanceId && this.props.cloudHost?.instanceId) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingCloudHost || this.props.isLoading : undefined}
                               error={this.props.loadCloudHostError || (isNew ? undefined : this.props.error)}
                               emptyMessage={`Sem métricas simuladas associadas`}
                               data={this.props.simulatedMetricsName}
                               dropdown={{
                                   id: 'simulatedMetrics',
                                   title: 'Selecionar métrica simulada',
                                   empty: 'Não há métricas simulada disponíveis',
                                   data: this.getSelectableSimulatedMetrics()
                               }}
                               show={this.simulatedMetric}
                               onAdd={this.onAdd}
                               onRemove={this.onRemove}
                               onDelete={{
                                   url: `hosts/cloud/${this.props.cloudHost?.instanceId}/simulated-metrics`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        const instanceId = this.props.cloudHost?.instanceId;
        if (instanceId) {
            this.props.loadCloudHostSimulatedMetrics(instanceId);
        }
    };

    private isNew = () =>
        this.props.cloudHost?.instanceId === undefined;

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
                    <Link to={`/métricas simuladas/hosts/${simulatedMetric}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (simulatedMetric: string): void =>
        this.props.onAddSimulatedHostMetric(simulatedMetric);

    private onRemove = (simulatedMetrics: string[]) =>
        this.props.onRemoveSimulatedHostMetrics(simulatedMetrics);

    private onDeleteSuccess = (simulatedMetrics: string[]): void => {
        const instanceId = this.props.cloudHost?.instanceId;
        if (instanceId) {
            this.props.removeCloudHostSimulatedMetrics(instanceId, simulatedMetrics);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remover a métrica simulada`, 10000, reason, true);

    private getSelectableSimulatedMetrics = () => {
        const {simulatedMetrics, simulatedMetricsName, unsavedSimulatedMetrics} = this.props;
        return Object.keys(simulatedMetrics).filter(name => !simulatedMetricsName.includes(name) && !unsavedSimulatedMetrics.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: HostSimulatedMetricListProps): StateToProps {
    const instanceId = ownProps.cloudHost?.instanceId;
    const host = instanceId && state.entities.hosts.cloud.data[instanceId];
    const simulatedMetricsName = host && host.hostSimulatedMetrics;
    return {
        isLoading: state.entities.hosts.cloud.isLoadingSimulatedMetrics,
        error: state.entities.hosts.cloud.loadSimulatedMetricsError,
        simulatedMetrics: Object.entries(state.entities.simulatedMetrics.hosts.data)
            .filter(([_, simulatedMetric]) => !simulatedMetric.generic)
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
        loadSimulatedHostMetrics,
        loadCloudHostSimulatedMetrics,
        removeCloudHostSimulatedMetrics,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CloudHostSimulatedMetricList);