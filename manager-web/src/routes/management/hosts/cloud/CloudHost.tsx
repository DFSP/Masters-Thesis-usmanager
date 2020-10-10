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

import {RouteComponentProps} from "react-router";
import IDatabaseData from "../../../../components/IDatabaseData";
import BaseComponent from "../../../../components/BaseComponent";
import Form, {ICustomButton, IFormLoading} from "../../../../components/form/Form";
import ListLoadingSpinner from "../../../../components/list/ListLoadingSpinner";
import {Error} from "../../../../components/errors/Error";
import Field from "../../../../components/form/Field";
import Tabs, {Tab} from "../../../../components/tabs/Tabs";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../../reducers";
import {
    addCloudHost,
    addCloudHostRule,
    addCloudHostSimulatedMetrics,
    loadCloudHosts,
    updateCloudHost
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {deleteData, IReply, postData} from "../../../../utils/api";
import GenericHostRuleList from "../GenericHostRuleList";
import CloudHostRuleList from "./CloudHostRuleList";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {Schemas} from "../../../../middleware/api";
import {normalize} from "normalizr";
import {isNew} from "../../../../utils/router";
import GenericSimulatedHostMetricList from "../GenericSimulatedHostMetricList";
import CloudHostSimulatedMetricList from "./CloudHostSimulatedMetricList";
import formStyles from "../../../../components/form/Form.module.css";
import {IWorkerManager} from "../../workerManagers/WorkerManager";
import {IApp} from "../../apps/App";

export interface ICloudHost extends IDatabaseData {
    instanceId: string;
    imageId: string;
    instanceType: string;
    state: {
        code: number,
        name: string
    };
    publicDnsName: string;
    publicIpAddress: string;
    privateIpAddress: string;
    placement: IPlacement;
    worker: IWorkerManager
    managedByWorker: IWorkerManager;
    hostRules?: string[];
    hostSimulatedMetrics?: string[];
}

export interface IState {
    code: number,
    name: string
}

export interface IPlacement {
    affinity: any;
    availabilityZone: string;
    groupName: string;
    hostId: string;
    spreadDomain: string;
    tenancy: string;
}

const buildNewCloudHost = (): Partial<ICloudHost> => ({});

export const awsInstanceStates = {
    PENDING: {name: "pending", code: 0},
    RUNNING: {name: "running", code: 16},
    SHUTTING_DOWN: {name: "shutting-down", code: 32},
    TERMINATED: {name: "terminated", code: 48},
    STOPPING: {name: "stopping", code: 64},
    STOPPED: {name: "stopped", code: 80}
};

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    cloudHost: Partial<ICloudHost>;
    formCloudHost?: Partial<ICloudHost>;
}

interface DispatchToProps {
    loadCloudHosts: (instanceId: string) => void;
    addCloudHost: (cloudHost: ICloudHost) => void;
    updateCloudHost: (previousCloudHost: ICloudHost, currentCloudHost: ICloudHost) => void;
    addCloudHostRule: (cloudHost: string, ruleName: string) => void;
    addCloudHostSimulatedMetrics: (cloudHost: string, simulatedMetrics: string[]) => void;
}

interface MatchParams {
    instanceId: string;
}

interface LocationState {
    data: ICloudHost,
    selected: 'cloudHost' | 'rules' | 'genericRules' | 'simulatedMetrics' | 'genericSimulatedMetrics'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    cloudHost?: ICloudHost,
    formCloudHost?: ICloudHost,
    unsavedRules: string[],
    unsavedSimulatedMetrics: string[],
    loading: IFormLoading,
}

class CloudHost extends BaseComponent<Props, State> {

    state: State = {
        unsavedRules: [],
        unsavedSimulatedMetrics: [],
        loading: undefined,
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadCloudHost();
        this.mounted = true;
    };

    componentWillUnmount(): void {
        this.mounted = false;
    }

    render() {
        return (
            <MainLayout>
                {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadCloudHost = () => {
        if (!isNew(this.props.location.search)) {
            const instanceId = this.props.match.params.instanceId;
            this.props.loadCloudHosts(instanceId);
        }
    };

    private getCloudHost = () =>
        this.state.cloudHost || this.props.cloudHost;

    private getFormCloudHost = () =>
        this.state.formCloudHost || this.props.formCloudHost;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<ICloudHost>): void => {
        const cloudHost = reply.data;
        super.toast(`<span class="green-text">Cloud instance ${this.mounted ? `<b class="white-text">${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} has started</span>`);
        this.props.addCloudHost(cloudHost);
        this.saveEntities(cloudHost);
        if (this.mounted) {
            this.updateCloudHost(cloudHost);
            this.props.history.replace(cloudHost.instanceId);
        }
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Unable to start a new cloud instance`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedRules.length
        || !!this.state.unsavedSimulatedMetrics.length;

    private saveEntities = (cloudHost: ICloudHost) => {
        this.saveCloudHostRules(cloudHost);
        this.saveCloudHostSimulatedMetrics(cloudHost);
    };

    private addCloudHostRule = (rule: string): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.concat(rule)
        });
    };

    private removeCloudHostRules = (rules: string[]): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
        });
    };

    private saveCloudHostRules = (cloudHost: ICloudHost): void => {
        const {unsavedRules} = this.state;
        if (unsavedRules.length) {
            postData(`hosts/cloud/${cloudHost.instanceId}/rules`, unsavedRules,
                () => this.onSaveRulesSuccess(cloudHost),
                (reason) => this.onSaveRulesFailure(cloudHost, reason));
        }
    };

    private onSaveRulesSuccess = (cloudHost: ICloudHost): void => {
        this.state.unsavedRules.forEach(rule => this.props.addCloudHostRule(cloudHost.publicIpAddress || cloudHost.instanceId, rule));
        if (this.mounted) {
            this.setState({unsavedRules: []});
        }
    };

    private onSaveRulesFailure = (cloudHost: ICloudHost, reason: string): void =>
        super.toast(`Unable to save rules of ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance`, 10000, reason, true);

    private removeHostSimulatedMetrics = (simulatedMetrics: string[]): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.filter(metric => !simulatedMetrics.includes(metric))
        });
    };

    private addHostSimulatedMetric = (simulatedMetric: string): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.concat(simulatedMetric)
        });
    };

    private saveCloudHostSimulatedMetrics = (cloudHost: ICloudHost): void => {
        const {unsavedSimulatedMetrics} = this.state;
        if (unsavedSimulatedMetrics.length) {
            postData(`hosts/cloud/${cloudHost.instanceId}/simulated-metrics`, unsavedSimulatedMetrics,
                () => this.onSaveSimulatedMetricsSuccess(cloudHost),
                (reason) => this.onSaveSimulatedMetricsFailure(cloudHost, reason));
        }
    };

    private onSaveSimulatedMetricsSuccess = (cloudHost: ICloudHost): void => {
        this.props.addCloudHostSimulatedMetrics(cloudHost.publicIpAddress || cloudHost.instanceId, this.state.unsavedSimulatedMetrics);
        if (this.mounted) {
            this.setState({unsavedSimulatedMetrics: []});
        }
    };

    private onSaveSimulatedMetricsFailure = (cloudHost: ICloudHost, reason: string): void =>
        super.toast(`Unable to save simulated metrics of ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} cloud host`, 10000, reason, true);

    private startStopTerminateButtons = (): ICustomButton[] => {
        const buttons: ICustomButton[] = [];
        const cloudHost = this.getCloudHost();
        const state = this.getCloudHost().state?.name;
        if (state?.includes(awsInstanceStates.STOPPED.name)) {
            buttons.push({
                button:
                    <button className={`btn-flat btn-small waves-effect waves-light blue-text ${formStyles.formButton}`}
                            onClick={this.startCloudHost}>
                        Start
                    </button>
            });
        }
        if (state?.includes(awsInstanceStates.RUNNING.name)) {
            buttons.push({
                button:
                    <button className={`btn-flat btn-small waves-effect waves-light blue-text ${formStyles.formButton}`}
                            onClick={this.stopCloudHost}>
                        Stop
                    </button>
            });
        }
        if (!state?.includes(awsInstanceStates.TERMINATED.name)
            && !state?.includes(awsInstanceStates.SHUTTING_DOWN.name)) {
            buttons.push({
                button:
                    <button
                        className={`modal-trigger btn-flat btn-small waves-effect waves-light red-text ${formStyles.formButton}`}
                        data-target='terminate-cloudHost'>
                        Terminate
                    </button>,
                confirm: {
                    id: 'terminate-cloudHost',
                    message: `terminate instance ${cloudHost.instanceId}`,
                    onClickConfirm: this.terminateCloudHost
                }
            });
        }
        return buttons;
    };

    private startCloudHost = () => {
        const cloudHost = this.getCloudHost();
        const url = `hosts/cloud/${cloudHost.instanceId}/state`;
        this.setState({loading: {method: 'post', url: url}});
        postData(url, 'start',
            (reply: IReply<ICloudHost>) => this.onStartSuccess(reply.data),
            (reason) => this.onStartFailure(reason, cloudHost));
    };

    private onStartSuccess = (cloudHost: ICloudHost) => {
        super.toast(`<span class="green-text">Successfully started ${this.mounted ? `<b class="white-text">${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance</span>`, 15000);
        const previousCloudHost = this.getCloudHost();
        if (previousCloudHost?.id) {
            this.props.updateCloudHost(previousCloudHost as ICloudHost, cloudHost)
        }
        if (this.mounted) {
            this.updateCloudHost(cloudHost);
        }
    };

    private onStartFailure = (reason: string, cloudHost: Partial<ICloudHost>) => {
        super.toast(`Failed to start ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private stopCloudHost = () => {
        const cloudHost = this.getCloudHost();
        const url = `hosts/cloud/${cloudHost.instanceId}/state`;
        this.setState({loading: {method: 'post', url: url}});
        postData(url, 'stop',
            (reply: IReply<ICloudHost>) => this.onStopSuccess(reply.data),
            (reason) => this.onStopFailure(reason, cloudHost));
    };

    private onStopSuccess = (cloudHost: ICloudHost) => {
        super.toast(`<span class="green-text">Successfully stopped ${this.mounted ? `<b class="white-text">${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance</span>`, 15000);
        const previousCloudHost = this.getCloudHost();
        if (previousCloudHost?.id) {
            this.props.updateCloudHost(previousCloudHost as ICloudHost, cloudHost)
        }
        if (this.mounted) {
            this.updateCloudHost(cloudHost);
        }
    };

    private onStopFailure = (reason: string, cloudHost: Partial<ICloudHost>) => {
        super.toast(`Failed to stop ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private terminateCloudHost = () => {
        const cloudHost = this.getCloudHost();
        const url = `hosts/cloud/${cloudHost.instanceId}`;
        this.setState({loading: {method: 'delete', url: url}});
        deleteData(url,
            () => this.onTerminateSuccess(cloudHost),
            (reason) => this.onTerminateFailure(reason, cloudHost));
    };

    private onTerminateSuccess = (cloudHost: Partial<ICloudHost>) => {
        super.toast(`<span class="green-text">Successfully terminated <b class="white-text">${cloudHost.instanceId}</b> instance</span>`, 15000);

        if (this.mounted) {
            this.props.history.push('/hosts/cloud');
        }
    };

    private onTerminateFailure = (reason: string, cloudHost: Partial<ICloudHost>) => {
        super.toast(`Failed to terminate ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private updateCloudHost = (cloudHost: ICloudHost) => {
        cloudHost = Object.values(normalize(cloudHost, Schemas.CLOUD_HOST).entities.cloudHosts || {})[0];
        const formCloudHost = {...cloudHost};
        removeFields(formCloudHost);
        this.setState({cloudHost: cloudHost, formCloudHost: formCloudHost, loading: undefined});
    };

    private cloudHostState = (state: IState) =>
        state.name;

    private cloudHostPlacement = (placement: IPlacement) =>
        placement.availabilityZone;

    private managedByWorker = (worker: IWorkerManager) =>
        worker.id.toString();

    private cloudHost = () => {
        const {isLoading, error} = this.props;
        const cloudHost = this.getCloudHost();
        const formCloudHost = this.getFormCloudHost();
        // @ts-ignore
        const cloudHostKey: (keyof ICloudHost) = formCloudHost && Object.keys(formCloudHost)[0];
        const isNewCloudHost = this.isNew();
        return (
            <>
                {!isNewCloudHost && isLoading && <ListLoadingSpinner/>}
                {!isNewCloudHost && !isLoading && error && <Error message={error}/>}
                {(isNewCloudHost || !isLoading) && (isNewCloudHost || !error) && formCloudHost && (
                    /*@ts-ignore*/
                    <Form id={cloudHostKey}
                          fields={{}}
                          values={cloudHost}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'hosts/cloud',
                              textButton: 'launch',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          customButtons={this.startStopTerminateButtons()}
                          saveEntities={this.saveEntities}
                          loading={this.state.loading}>
                        {Object.keys(formCloudHost).map((key, index) =>
                            key === 'state'
                                ? <Field<IState> key={index}
                                                 id={key}
                                                 label={key}
                                                 valueToString={this.cloudHostState}/>
                                : key === 'placement'
                                ? <Field<IPlacement> key={index}
                                                     id={key}
                                                     label={key}
                                                     valueToString={this.cloudHostPlacement}/>
                                : key === 'managedByWorker'
                                    ? <Field<IWorkerManager> key={index}
                                                             id={key}
                                                             label={key}
                                                             valueToString={this.managedByWorker}/>
                                    : <Field key={index}
                                             id={key}
                                             label={key}/>)}
                    </Form>
                )}
            </>
        )
    };

    private rules = (): JSX.Element =>
        <CloudHostRuleList isLoadingCloudHost={this.props.isLoading}
                           loadCloudHostError={!this.isNew() ? this.props.error : undefined}
                           cloudHost={this.getCloudHost()}
                           unsavedRules={this.state.unsavedRules}
                           onAddHostRule={this.addCloudHostRule}
                           onRemoveHostRules={this.removeCloudHostRules}/>;

    private genericRules = (): JSX.Element =>
        <GenericHostRuleList/>;

    private simulatedMetrics = (): JSX.Element =>
        <CloudHostSimulatedMetricList isLoadingCloudHost={this.props.isLoading}
                                      loadCloudHostError={!this.isNew() ? this.props.error : undefined}
                                      cloudHost={this.getCloudHost()}
                                      unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                      onAddSimulatedHostMetric={this.addHostSimulatedMetric}
                                      onRemoveSimulatedHostMetrics={this.removeHostSimulatedMetrics}/>;

    private genericSimulatedMetrics = (): JSX.Element =>
        <GenericSimulatedHostMetricList/>;

    private tabs = (): Tab[] => [
        {
            title: 'Cloud host',
            id: 'cloudHost',
            content: () => this.cloudHost(),
            active: this.props.location.state.selected === 'cloudHost'
        },
        {
            title: 'Rules',
            id: 'rules',
            content: () => this.rules(),
            active: this.props.location.state.selected === 'rules'
        },
        {
            title: 'Generic rules',
            id: 'genericRules',
            content: () => this.genericRules(),
            active: this.props.location.state.selected === 'genericRules'
        },
        {
            title: 'Simulated metrics',
            id: 'simulatedMetrics',
            content: () => this.simulatedMetrics(),
            active: this.props.location.state.selected === 'simulatedMetrics'
        },
        {
            title: 'Generic simulated metrics',
            id: 'genericSimulatedMetrics',
            content: () => this.genericSimulatedMetrics(),
            active: this.props.location.state.selected === 'genericSimulatedMetrics'
        },
    ];

}

function removeFields(cloudHost: Partial<ICloudHost>) {
    delete cloudHost["id"];
    if (!cloudHost.publicDnsName) {
        delete cloudHost["publicDnsName"];
    }
    if (!cloudHost.publicIpAddress) {
        delete cloudHost["publicIpAddress"];
    }
    if (!cloudHost.privateIpAddress) {
        delete cloudHost["privateIpAddress"];
    }
    delete cloudHost["worker"];
    if (!cloudHost.managedByWorker) {
        delete cloudHost["managedByWorker"];
    }
    delete cloudHost["hostRules"];
    delete cloudHost["hostSimulatedMetrics"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.hosts.cloud.isLoadingHosts;
    const error = state.entities.hosts.cloud.loadHostsError;
    const instanceId = props.match.params.instanceId;
    const cloudHost = isNew(props.location.search) ? buildNewCloudHost() : state.entities.hosts.cloud.data[instanceId];
    let formCloudHost;
    if (cloudHost) {
        formCloudHost = {...cloudHost};
        removeFields(formCloudHost);
    }
    return {
        isLoading,
        error,
        cloudHost,
        formCloudHost
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadCloudHosts,
    addCloudHost,
    updateCloudHost,
    addCloudHostRule,
    addCloudHostSimulatedMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(CloudHost);
