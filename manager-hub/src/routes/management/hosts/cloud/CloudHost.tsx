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
import Form, {ICustomButton, IFields, IFormLoading, required} from "../../../../components/form/Form";
import LoadingSpinner from "../../../../components/list/LoadingSpinner";
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
    loadCloudRegions,
    updateCloudHost
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {deleteData, IReply, postData, putData} from "../../../../utils/api";
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
import {ICoordinates} from "../../../../components/map/LocationMap";
import {IMarker} from "../../../../components/map/Marker";
import CloudHostSshCommand from "./CloudHostSshCommand";
import CloudHostSshFileTransfer from "./CloudHostSshFileTransfer";
import { isEqual } from "lodash";

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
    awsRegion: IAwsRegion;
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

interface INewCloudHost {
    coordinates?: ICoordinates,
}

const buildNewCloudHost = (): INewCloudHost => ({
    coordinates: undefined,
});

export const awsInstanceStates = {
    PENDING: {name: "pending", code: 0},
    RUNNING: {name: "running", code: 16},
    SHUTTING_DOWN: {name: "shutting-down", code: 32},
    TERMINATED: {name: "terminated", code: 48},
    STOPPING: {name: "stopping", code: 64},
    STOPPED: {name: "stopped", code: 80}
};

export interface IAwsRegion {
    zone: string,
    name: string,
    coordinates: ICoordinates,
    available?: boolean,
}

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    cloudHost?: Partial<ICloudHost>;
    formCloudHost?: Partial<ICloudHost>;
    newCloudHost?: INewCloudHost;
    cloudRegions: IAwsRegion[];
}

interface DispatchToProps {
    loadCloudHosts: (instanceId: string) => void;
    loadCloudRegions: () => void;
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
    selected: 'cloudHost' | 'rules' | 'genericRules' | 'simulatedMetrics' | 'genericSimulatedMetrics' | 'ssh' | 'sftp'
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
        this.props.loadCloudRegions();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
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
        this.props.cloudHost || this.state.cloudHost || {};

    private getFormCloudHost = () =>
        this.props.formCloudHost || this.state.formCloudHost;

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
        this.state.unsavedRules.forEach(rule => this.props.addCloudHostRule(cloudHost.instanceId, rule));
        if (this.mounted) {
            this.setState({unsavedRules: []});
        }
    };

    private onSaveRulesFailure = (cloudHost: ICloudHost, reason: string): void =>
        super.toast(`Unable to save rules of instance ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} `, 10000, reason, true);

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
        this.props.addCloudHostSimulatedMetrics(cloudHost.instanceId, this.state.unsavedSimulatedMetrics);
        if (this.mounted) {
            this.setState({unsavedSimulatedMetrics: []});
        }
    };

    private onSaveSimulatedMetricsFailure = (cloudHost: ICloudHost, reason: string): void =>
        super.toast(`Unable to save simulated metrics of instance ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`}`, 10000, reason, true);

    private startStopTerminateButtons = (): ICustomButton[] => {
        const buttons: ICustomButton[] = [];
        const cloudHost = this.getCloudHost();
        const state = this.getCloudHost()?.state;
        if (isEqual(state, awsInstanceStates.STOPPED)) {
            buttons.push({
                button:
                    <button className={`btn-flat btn-small waves-effect waves-light blue-text ${formStyles.formButton}`}
                            onClick={this.startCloudHost}>
                        Start
                    </button>
            });
        }
        if (isEqual(state, awsInstanceStates.RUNNING)) {
            buttons.push({
                button:
                    <button className={`btn-flat btn-small waves-effect waves-light blue-text ${formStyles.formButton}`}
                            onClick={this.stopCloudHost}>
                        Stop
                    </button>
            });
        }
        if (!isEqual(state, awsInstanceStates.TERMINATED)
            && !isEqual(state, awsInstanceStates.SHUTTING_DOWN)) {
            buttons.push({
                button:
                    <button
                        className={`modal-trigger btn-flat btn-small waves-effect waves-light red-text ${formStyles.formButton}`}
                        data-target='terminate-cloudHost'>
                        Terminate
                    </button>,
                confirm: {
                    id: 'terminate-cloudHost',
                    message: `terminate instance ${cloudHost?.instanceId}`,
                    onClickConfirm: this.terminateCloudHost
                }
            });
        }
        return buttons;
    };

    private startCloudHost = () => {
        const cloudHost = this.getCloudHost();
        const url = `hosts/cloud/${cloudHost.instanceId}/start`;
        this.setState({loading: {method: 'put', url: url}});
        putData(url, undefined,
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
        const url = `hosts/cloud/${cloudHost.instanceId}/stop`;
        this.setState({loading: {method: 'put', url: url}});
        putData(url, undefined,
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

    private getFields = (cloudHost: Partial<ICloudHost> | INewCloudHost): IFields =>
        Object.entries(cloudHost).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: {rule: required}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private cloudHostState = (state: IState) =>
        state.name;

    private cloudHostPlacement = (placement: IPlacement) =>
        placement.availabilityZone;

    private managedByWorker = (worker: IWorkerManager) =>
        worker.id.toString();

    private cloudHost = () => {
        const {isLoading, error, newCloudHost} = this.props;
        const isNewCloudHost = this.isNew();
        const cloudHost = isNewCloudHost ? newCloudHost : this.getCloudHost();
        const formCloudHost = this.getFormCloudHost();
        // @ts-ignore
        const cloudHostKey: (keyof ICloudHost) = formCloudHost && Object.keys(formCloudHost)[0];
        return (
            <>
                {!isNewCloudHost && isLoading && <LoadingSpinner/>}
                {!isNewCloudHost && !isLoading && error && <Error message={error}/>}
                {(isNewCloudHost || !isLoading) && (isNewCloudHost || !error) && cloudHost && (
                    /*@ts-ignore*/
                    <Form id={cloudHostKey}
                          fields={this.getFields(cloudHost)}
                          values={cloudHost}
                          isNew={isNewCloudHost}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'hosts/cloud',
                              textButton: isNewCloudHost ? 'launch' : 'save',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          customButtons={this.startStopTerminateButtons()}
                          saveEntities={this.saveEntities}
                          loading={this.state.loading}>
                        {isNewCloudHost ?
                            <Field key='coordinates' id='coordinates' type='map' label='select position'
                                   map={{
                                       editable: this.isNew(),
                                       singleMarker: true,
                                       zoomable: true,
                                       labeled: true,
                                       loading: isLoading,
                                       markers: this.props.cloudRegions.filter(region => region.available).map(region => ({
                                           title: region.zone + " | " + region.name,
                                           latitude: region.coordinates.latitude,
                                           longitude: region.coordinates.longitude,
                                           color: 'green'
                                       }))
                                   }}/>
                            : formCloudHost && Object.keys(formCloudHost).map((key, index) =>
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
                                : key === 'awsRegion'
                                    ? <Field<IAwsRegion> key='awsRegion' id='awsRegion' type='map'
                                                         map={{
                                                             editable: false,
                                                             singleMarker: true,
                                                             zoomable: true,
                                                             labeled: true,
                                                             loading: isLoading,
                                                             valueToMarkers: (regions: IAwsRegion[]): IMarker[] =>
                                                                 regions.map(region => ({
                                                                     title: region.zone + " | " + region.name,
                                                                     label: formCloudHost?.instanceId,
                                                                     latitude: region.coordinates.latitude,
                                                                     longitude: region.coordinates.longitude,
                                                                 }))
                                                         }}/>
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

    private ssh = (): JSX.Element =>
        <CloudHostSshCommand cloudHost={this.getCloudHost()}/>;

    private sftp = (): JSX.Element =>
        <CloudHostSshFileTransfer cloudHost={this.getCloudHost()}/>;

    private tabs = (): Tab[] => {
        const tabs = [
            {
                title: 'Cloud host',
                id: 'cloudHost',
                content: () => this.cloudHost(),
                active: this.props.location.state?.selected === 'cloudHost'
            },
            {
                title: 'Rules',
                id: 'rules',
                content: () => this.rules(),
                active: this.props.location.state?.selected === 'rules'
            },
            {
                title: 'Generic rules',
                id: 'genericRules',
                content: () => this.genericRules(),
                active: this.props.location.state?.selected === 'genericRules'
            },
            {
                title: 'Simulated metrics',
                id: 'simulatedMetrics',
                content: () => this.simulatedMetrics(),
                active: this.props.location.state?.selected === 'simulatedMetrics'
            },
            {
                title: 'Generic simulated metrics',
                id: 'genericSimulatedMetrics',
                content: () => this.genericSimulatedMetrics(),
                active: this.props.location.state?.selected === 'genericSimulatedMetrics'
            },
        ];
        if (isEqual(this.getCloudHost().state, awsInstanceStates.RUNNING)) {
            tabs.push({
                title: 'Execute command',
                id: 'ssh',
                content: () => this.ssh(),
                active: this.props.location.state?.selected === 'ssh'
            });
            tabs.push({
                title: 'Upload file',
                id: 'sftp',
                content: () => this.sftp(),
                active: this.props.location.state?.selected === 'sftp'
            });
        }
        return tabs;
    }

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
    const isLoading = state.entities.hosts.cloud.isLoadingHosts || state.entities.hosts.cloud.regions.isLoadingRegions;
    const error = state.entities.hosts.cloud.loadHostsError || state.entities.hosts.cloud.regions.loadRegionsError;
    const instanceId = props.match.params.instanceId;
    const cloudHost = !isNew(props.location.search) ? state.entities.hosts.cloud.data[instanceId] : undefined;
    const newCloudHost = isNew(props.location.search) ? buildNewCloudHost() : undefined;
    let formCloudHost;
    if (cloudHost) {
        formCloudHost = {...cloudHost};
        removeFields(formCloudHost);
    }
    return {
        isLoading,
        error,
        cloudHost,
        newCloudHost,
        formCloudHost,
        cloudRegions: state.entities.hosts.cloud.regions.data,
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadCloudHosts,
    loadCloudRegions,
    addCloudHost,
    updateCloudHost,
    addCloudHostRule,
    addCloudHostSimulatedMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(CloudHost);
