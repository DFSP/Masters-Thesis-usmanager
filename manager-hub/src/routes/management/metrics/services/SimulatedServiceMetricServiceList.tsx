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
import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {
    loadServices,
    loadSimulatedServiceMetricServices,
    removeSimulatedServiceMetricServices,
} from "../../../../actions";
import {IService} from "../../services/Service";
import {ISimulatedServiceMetric} from "./SimulatedServiceMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    services: { [key: string]: IService },
    simulatedMetricServices: string[];
}

interface DispatchToProps {
    loadServices: () => void;
    loadSimulatedServiceMetricServices: (name: string) => void;
    removeSimulatedServiceMetricServices: (name: string, services: string[]) => void;
}

interface SimulatedServiceMetricServiceListProps {
    isLoadingSimulatedServiceMetric: boolean;
    loadSimulatedServiceMetricError?: string | null;
    simulatedServiceMetric: ISimulatedServiceMetric | Partial<ISimulatedServiceMetric> | null;
    unsavedServices: string[];
    onAddService: (service: string) => void;
    onRemoveServices: (service: string[]) => void;
}

type Props = StateToProps & DispatchToProps & SimulatedServiceMetricServiceListProps;

interface State {
    entitySaved: boolean;
}

class SimulatedServiceMetricServiceList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadServices();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.simulatedServiceMetric?.name && this.props.simulatedServiceMetric?.name) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList
            isLoading={!isNew ? this.props.isLoadingSimulatedServiceMetric || this.props.isLoading : undefined}
            error={!isNew ? this.props.loadSimulatedServiceMetricError || this.props.error : undefined}
            emptyMessage={`Services list is empty`}
            data={this.props.simulatedMetricServices}
            dropdown={{
                id: 'services',
                title: 'Add service',
                empty: 'No services to add',
                data: this.getSelectableServices()
            }}
            show={this.service}
            onAdd={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `simulated-metrics/services/${this.props.simulatedServiceMetric?.name}/services`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.simulatedServiceMetric?.name) {
            const {name} = this.props.simulatedServiceMetric;
            this.props.loadSimulatedServiceMetricServices(name);
        }
    };

    private isNew = () =>
        this.props.simulatedServiceMetric?.name === undefined;

    private service = (index: number, service: string, separate: boolean, checked: boolean,
                       handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedServices.includes(service);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${styles.linkedItemContent}`}>
                    <label>
                        <input id={service}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
               <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                 {service}
               </div>
            </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/services/${service}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (service: string): void =>
        this.props.onAddService(service);

    private onRemove = (service: string[]) =>
        this.props.onRemoveServices(service);

    private onDeleteSuccess = (service: string[]): void => {
        if (this.props.simulatedServiceMetric?.name) {
            const {name} = this.props.simulatedServiceMetric;
            this.props.removeSimulatedServiceMetricServices(name, service);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Unable to remove service`, 10000, reason, true);

    private getSelectableServices = () => {
        const {services, simulatedMetricServices, unsavedServices} = this.props;
        return Object.keys(services).filter(service => !simulatedMetricServices.includes(service)
            && !unsavedServices.includes(service));
    };

}

function mapStateToProps(state: ReduxState, ownProps: SimulatedServiceMetricServiceListProps): StateToProps {
    const name = ownProps.simulatedServiceMetric?.name;
    const simulatedMetric = name && state.entities.simulatedMetrics.services.data[name];
    const simulatedMetricServices = simulatedMetric && simulatedMetric.services;
    return {
        isLoading: state.entities.simulatedMetrics.services.isLoadingServices,
        error: state.entities.simulatedMetrics.services.loadServicesError,
        services: state.entities.services.data,
        simulatedMetricServices: simulatedMetricServices || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadServices,
        loadSimulatedServiceMetricServices,
        removeSimulatedServiceMetricServices,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedServiceMetricServiceList);
