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
    loadCloudHosts,
    loadSimulatedHostMetricCloudHosts,
    removeSimulatedHostMetricCloudHosts,
} from "../../../../actions";
import {ICloudHost} from "../../hosts/cloud/CloudHost";
import {ISimulatedHostMetric} from "./SimulatedHostMetric";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    cloudHosts: { [key: string]: ICloudHost },
    simulatedMetricCloudHosts: string[];
}

interface DispatchToProps {
    loadCloudHosts: () => void;
    loadSimulatedHostMetricCloudHosts: (name: string) => void;
    removeSimulatedHostMetricCloudHosts: (name: string, cloudHosts: string[]) => void;
}

interface SimulatedHostMetricCloudHostListProps {
    isLoadingSimulatedHostMetric: boolean;
    loadSimulatedHostMetricError?: string | null;
    simulatedHostMetric: ISimulatedHostMetric | Partial<ISimulatedHostMetric> | null;
    unsavedCloudHosts: string[];
    onAddCloudHost: (cloudHost: string) => void;
    onRemoveCloudHosts: (cloudHost: string[]) => void;
}

type Props = StateToProps & DispatchToProps & SimulatedHostMetricCloudHostListProps;

interface State {
    entitySaved: boolean;
}

class SimulatedHostMetricCloudHostList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.simulatedHostMetric?.name && this.props.simulatedHostMetric?.name) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList
            isLoading={!isNew ? this.props.isLoadingSimulatedHostMetric || this.props.isLoading : undefined}
            error={!isNew ? this.props.loadSimulatedHostMetricError || this.props.error : undefined}
            emptyMessage={`Sem hosts cloud associados`}
            data={this.props.simulatedMetricCloudHosts}
            dropdown={{
                id: 'cloudHosts',
                title: 'Selecionar o host',
                empty: 'Não há hosts cloud disponíveis',
                data: this.getSelectableCloudHosts()
            }}
            show={this.cloudHost}
            onAdd={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `simulated-metrics/hosts/${this.props.simulatedHostMetric?.name}/cloud-hosts`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        this.props.loadCloudHosts();
        if (this.props.simulatedHostMetric?.name) {
            const {name} = this.props.simulatedHostMetric;
            this.props.loadSimulatedHostMetricCloudHosts(name);
        }
    };

    private isNew = () =>
        this.props.simulatedHostMetric?.name === undefined;

    private cloudHost = (index: number, cloudHost: string, separate: boolean, checked: boolean,
                         handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedCloudHosts.includes(cloudHost);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${styles.linkedItemContent}`}>
                    <label>
                        <input id={cloudHost}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                                {cloudHost}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/hosts/cloud/${cloudHost}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (cloudHost: string): void =>
        this.props.onAddCloudHost(cloudHost);

    private onRemove = (cloudHost: string[]) =>
        this.props.onRemoveCloudHosts(cloudHost);

    private onDeleteSuccess = (cloudHost: string[]): void => {
        if (this.props.simulatedHostMetric?.name) {
            const {name} = this.props.simulatedHostMetric;
            this.props.removeSimulatedHostMetricCloudHosts(name, cloudHost);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remove a instância cloud`, 10000, reason, true);

    private getSelectableCloudHosts = () => {
        const {cloudHosts, simulatedMetricCloudHosts, unsavedCloudHosts} = this.props;
        return Object.keys(cloudHosts).filter(cloudHost => !simulatedMetricCloudHosts.includes(cloudHost) && !unsavedCloudHosts.includes(cloudHost));
    };

}

function mapStateToProps(state: ReduxState, ownProps: SimulatedHostMetricCloudHostListProps): StateToProps {
    const name = ownProps.simulatedHostMetric?.name;
    const simulatedMetric = name && state.entities.simulatedMetrics.hosts.data[name];
    const simulatedMetricCloudHosts = simulatedMetric && simulatedMetric.cloudHosts;
    return {
        isLoading: state.entities.simulatedMetrics.hosts.isLoadingCloudHosts,
        error: state.entities.simulatedMetrics.hosts.loadCloudHostsError,
        cloudHosts: state.entities.hosts.cloud.data,
        simulatedMetricCloudHosts: simulatedMetricCloudHosts || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadCloudHosts,
        loadSimulatedHostMetricCloudHosts,
        removeSimulatedHostMetricCloudHosts,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedHostMetricCloudHostList);
