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
import {IApp} from "./App";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadAppSimulatedMetrics, loadSimulatedAppMetrics, removeAppSimulatedMetrics} from "../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {ISimulatedAppMetric} from "../metrics/apps/SimulatedAppMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    simulatedMetrics: { [key: string]: ISimulatedAppMetric },
    simulatedMetricsName: string[];
}

interface DispatchToProps {
    loadSimulatedAppMetrics: (name?: string) => any;
    loadAppSimulatedMetrics: (name: string) => void;
    removeAppSimulatedMetrics: (name: string, simulatedMetrics: string[]) => void;
}

interface AppSimulatedMetricListProps {
    isLoadingApp: boolean;
    loadAppError?: string | null;
    app?: IApp | Partial<IApp> | null;
    unsavedSimulatedMetrics: string[];
    onAddSimulatedAppMetric: (simulatedMetric: string) => void;
    onRemoveSimulatedAppMetrics: (simulatedMetric: string[]) => void;
}

type Props = StateToProps & DispatchToProps & AppSimulatedMetricListProps;

interface State {
    entitySaved: boolean;
}

class AppSimulatedMetricList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadSimulatedAppMetrics();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.app?.name && this.props.app?.name) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingApp || this.props.isLoading : undefined}
                               error={!isNew ? this.props.loadAppError || this.props.error : undefined}
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
                                   url: `apps/${this.props.app?.name}/simulated-metrics`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.app?.name) {
            const {name} = this.props.app;
            this.props.loadAppSimulatedMetrics(name);
        }
    };

    private isNew = () =>
        this.props.app?.name === undefined;

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
                    <Link to={`/simulated-metrics/apps/${simulatedMetric}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (simulatedMetric: string): void =>
        this.props.onAddSimulatedAppMetric(simulatedMetric);

    private onRemove = (simulatedMetrics: string[]) =>
        this.props.onRemoveSimulatedAppMetrics(simulatedMetrics);

    private onDeleteSuccess = (simulatedMetrics: string[]): void => {
        if (this.props.app?.name) {
            const {name} = this.props.app;
            this.props.removeAppSimulatedMetrics(name, simulatedMetrics);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Unable to delete simulated metric`, 10000, reason, true);

    private getSelectableSimulatedMetrics = () => {
        const {simulatedMetrics, simulatedMetricsName, unsavedSimulatedMetrics} = this.props;
        return Object.keys(simulatedMetrics).filter(name => !simulatedMetricsName.includes(name) && !unsavedSimulatedMetrics.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: AppSimulatedMetricListProps): StateToProps {
    const name = ownProps.app?.name;
    const app = name && state.entities.apps.data[name];
    const simulatedMetricsName = app && app.appSimulatedMetrics;
    return {
        isLoading: state.entities.apps.isLoadingSimulatedMetrics,
        error: state.entities.apps.loadSimulatedMetricsError,
        simulatedMetrics: Object.entries(state.entities.simulatedMetrics.apps.data)
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
        loadSimulatedAppMetrics,
        loadAppSimulatedMetrics,
        removeAppSimulatedMetrics,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(AppSimulatedMetricList);