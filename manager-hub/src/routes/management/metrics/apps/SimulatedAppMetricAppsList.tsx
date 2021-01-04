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
import {loadApps, loadSimulatedAppMetricApps, removeSimulatedAppMetricApps,} from "../../../../actions";
import {IApp} from "../../apps/App";
import {ISimulatedAppMetric} from "./SimulatedAppMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    apps: { [key: string]: IApp },
    simulatedMetricApps: string[];
}

interface DispatchToProps {
    loadApps: () => void;
    loadSimulatedAppMetricApps: (name: string) => void;
    removeSimulatedAppMetricApps: (name: string, apps: string[]) => void;
}

interface SimulatedAppMetricAppListProps {
    isLoadingSimulatedAppMetric: boolean;
    loadSimulatedAppMetricError?: string | null;
    simulatedAppMetric: ISimulatedAppMetric | Partial<ISimulatedAppMetric> | null;
    unsavedApps: string[];
    onAddApp: (app: string) => void;
    onRemoveApps: (app: string[]) => void;
}

type Props = StateToProps & DispatchToProps & SimulatedAppMetricAppListProps;

interface State {
    entitySaved: boolean;
}

class SimulatedAppMetricAppList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadApps();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.simulatedAppMetric?.name && this.props.simulatedAppMetric?.name) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList
            isLoading={!isNew ? this.props.isLoadingSimulatedAppMetric || this.props.isLoading : undefined}
            error={this.props.loadSimulatedAppMetricError || (isNew ? undefined : this.props.error)}
            emptyMessage={`Sem aplicações associadas`}
            data={this.props.simulatedMetricApps}
            dropdown={{
                id: 'apps',
                title: 'Selecionar a aplicação',
                empty: 'Não há aplicações disponíveis',
                data: this.getSelectableApps()
            }}
            show={this.app}
            onAdd={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `simulated-metrics/apps/${this.props.simulatedAppMetric?.name}/apps`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.simulatedAppMetric?.name) {
            const {name} = this.props.simulatedAppMetric;
            this.props.loadSimulatedAppMetricApps(name);
        }
    };

    private isNew = () =>
        this.props.simulatedAppMetric?.name === undefined;

    private app = (index: number, app: string, separate: boolean, checked: boolean,
                   handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedApps.includes(app);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${styles.linkedItemContent}`}>
                    <label>
                        <input id={app}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                                {app}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/aplicações/${app}`}
                          className={`${styles.link}`}>
                        <i className={`link-icon material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (app: string): void =>
        this.props.onAddApp(app);

    private onRemove = (app: string[]) =>
        this.props.onRemoveApps(app);

    private onDeleteSuccess = (app: string[]): void => {
        if (this.props.simulatedAppMetric?.name) {
            const {name} = this.props.simulatedAppMetric;
            this.props.removeSimulatedAppMetricApps(name, app);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remover a aplicação`, 10000, reason, true);

    private getSelectableApps = () => {
        const {apps, simulatedMetricApps, unsavedApps} = this.props;
        return Object.keys(apps).filter(app => !simulatedMetricApps.includes(app) && !unsavedApps.includes(app));
    };

}

function mapStateToProps(state: ReduxState, ownProps: SimulatedAppMetricAppListProps): StateToProps {
    const name = ownProps.simulatedAppMetric?.name;
    const simulatedMetric = name && state.entities.simulatedMetrics.apps.data[name];
    const simulatedMetricApps = simulatedMetric && simulatedMetric.apps;
    return {
        isLoading: state.entities.simulatedMetrics.apps.isLoadingApps,
        error: state.entities.simulatedMetrics.apps.loadAppsError,
        apps: state.entities.apps.data,
        simulatedMetricApps: simulatedMetricApps || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadApps,
        loadSimulatedAppMetricApps,
        removeSimulatedAppMetricApps,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedAppMetricAppList);
