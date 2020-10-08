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
import {IService} from "./Service";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {
    loadServiceSimulatedMetrics,
    loadSimulatedServiceMetrics,
    removeServiceSimulatedMetrics
} from "../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {ISimulatedServiceMetric} from "../metrics/services/SimulatedServiceMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    simulatedMetrics: { [key: string]: ISimulatedServiceMetric },
    simulatedMetricsName: string[];
}

interface DispatchToProps {
    loadSimulatedServiceMetrics: (name?: string) => any;
    loadServiceSimulatedMetrics: (serviceName: string) => void;
    removeServiceSimulatedMetrics: (serviceName: string, simulatedMetrics: string[]) => void;
}

interface ServiceSimulatedMetricListProps {
    isLoadingService: boolean;
    loadServiceError?: string | null;
    service: IService | Partial<IService> | null;
    unsavedSimulatedMetrics: string[];
    onAddSimulatedServiceMetric: (simulatedMetric: string) => void;
    onRemoveSimulatedServiceMetrics: (simulatedMetric: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceSimulatedMetricListProps;

interface State {
    entitySaved: boolean;
}

class ServiceSimulatedMetricList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadSimulatedServiceMetrics();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.service?.serviceName && this.props.service?.serviceName) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingService || this.props.isLoading : undefined}
                               error={!isNew ? this.props.loadServiceError || this.props.error : undefined}
                               emptyMessage={`Simulated metrics list is empty`}
                               data={this.props.simulatedMetricsName}
                               dropdown={{
                                   id: 'simulatedMetrics',
                                   title: 'Add simulated metric',
                                   empty: 'No simulated metrics to add',
                                   data: this.getSelectableSimulatedMetrics()
                               }}
                               show={this.simulatedMetric}
                               onAdd={this.onAdd}
                               onRemove={this.onRemove}
                               onDelete={{
                                   url: `services/${this.props.service?.serviceName}/simulated-metrics`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.service?.serviceName) {
            const {serviceName} = this.props.service;
            this.props.loadServiceSimulatedMetrics(serviceName);
        }
    };

    private isNew = () =>
        this.props.service?.serviceName === undefined;

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
                    <Link to={`/simulated-metrics/services/${simulatedMetric}`}
                          className={`${styles.link} waves-effect`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (simulatedMetric: string): void =>
        this.props.onAddSimulatedServiceMetric(simulatedMetric);

    private onRemove = (simulatedMetrics: string[]) =>
        this.props.onRemoveSimulatedServiceMetrics(simulatedMetrics);

    private onDeleteSuccess = (simulatedMetrics: string[]): void => {
        if (this.props.service?.serviceName) {
            const {serviceName} = this.props.service;
            this.props.removeServiceSimulatedMetrics(serviceName, simulatedMetrics);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Unable to delete simulated metric`, 10000, reason, true);

    private getSelectableSimulatedMetrics = () => {
        const {simulatedMetrics, simulatedMetricsName, unsavedSimulatedMetrics} = this.props;
        return Object.keys(simulatedMetrics).filter(name => !simulatedMetricsName.includes(name) && !unsavedSimulatedMetrics.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: ServiceSimulatedMetricListProps): StateToProps {
    const serviceName = ownProps.service?.serviceName;
    const service = serviceName && state.entities.services.data[serviceName];
    const simulatedMetricsName = service && service.serviceSimulatedMetrics;
    return {
        isLoading: state.entities.services.isLoadingSimulatedMetrics,
        error: state.entities.services.loadSimulatedMetricsError,
        simulatedMetrics: Object.entries(state.entities.simulatedMetrics.services.data)
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
        loadSimulatedServiceMetrics,
        loadServiceSimulatedMetrics,
        removeServiceSimulatedMetrics,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ServiceSimulatedMetricList);