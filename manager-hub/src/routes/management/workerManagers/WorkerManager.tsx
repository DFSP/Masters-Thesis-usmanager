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

import BaseComponent from "../../../components/BaseComponent";
import React from "react";
import {RouteComponentProps} from "react-router";
import Form, {IFields, requiredAndNumberAndMin, requiredAndTrimmed} from "../../../components/form/Form";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addWorkerManager, assignWorkerManagerHosts, loadNodes, loadRegions, loadWorkerManagers} from "../../../actions";
import {connect} from "react-redux";
import {IReply, postData} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import IDatabaseData from "../../../components/IDatabaseData";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {IContainer} from "../containers/Container";
import {INode} from "../nodes/Node";
import AssignedHostsList from "./AssignedHostsList";
import {IHostAddress} from "../hosts/Hosts";
import {IRegion} from "../regions/Region";

export interface IWorkerManager extends IDatabaseData {
    container: IContainer,
    assignedHosts?: string[],
}

interface INewWorkerManagerRegion {
    regions: string[] | undefined
}

interface INewWorkerManagerHost {
    hostAddress: IHostAddress | undefined
}

const buildNewWorkerManagerRegion = (): INewWorkerManagerRegion => ({
    regions: undefined
});

const buildNewWorkerManagerHost = (): INewWorkerManagerHost => ({
    hostAddress: undefined,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newWorkerManagerRegion?: INewWorkerManagerRegion;
    newWorkerManagerHost?: INewWorkerManagerHost;
    workerManager?: IWorkerManager;
    formWorkerManager?: Partial<IWorkerManager>;
    regions: { [key: string]: IRegion };
    nodes: { [key: string]: INode };
}

interface DispatchToProps {
    loadWorkerManagers: (id: string) => void;
    addWorkerManager: (workerManager: IWorkerManager) => void;
    loadRegions: () => void;
    loadNodes: () => void;
    assignWorkerManagerHosts: (id: string, Hosts: string[]) => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: IWorkerManager,
    selected: 'workerManager' | 'assignHosts';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    workerManager?: IWorkerManager,
    formWorkerManager?: IWorkerManager,
    unsavedHosts: string[],
    currentForm: 'On regions' | 'On host',
}

class WorkerManager extends BaseComponent<Props, State> {

    state: State = {
        unsavedHosts: [],
        currentForm: 'On regions'
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadWorkerManager();
        this.props.loadRegions();
        this.props.loadNodes();
        this.mounted = true;
    }

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadWorkerManager = () => {
        if (!isNew(this.props.location.search)) {
            const workerManagerId = this.props.match.params.id;
            this.props.loadWorkerManagers(workerManagerId);
        }
    };

    private getWorkerManager = () =>
        this.state.workerManager || this.props.workerManager;

    private getFormWorkerManager = () =>
        this.state.formWorkerManager || this.props.formWorkerManager;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IWorkerManager>): void => {
        const workerManager = reply.data;
        const hostname = workerManager.container.publicIpAddress;
        super.toast(`<span class="green-text">Worker-manager ${this.mounted ? `<b class="white-text">${workerManager.id}</b>` : `<a href=/worker-managers/${workerManager.id}><b>${workerManager.id}</b></a>`} launched at ${hostname}</span>`);
        this.props.addWorkerManager(workerManager);
        this.saveEntities(workerManager);
        if (this.mounted) {
            this.updateWorkerManager(workerManager);
        }
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Unable to launch worker-manager`, 10000, reason, true);

    private onDeleteSuccess = (workerManager: IWorkerManager): void => {
        super.toast(`<span class="green-text">Worker-manager <b class="white-text">${workerManager.id}</b> successfully stopped</span>`);
        if (this.mounted) {
            this.props.history.push(`/worker-managers`)
        }
    };

    private onDeleteFailure = (reason: string, workerManager: IWorkerManager): void =>
        super.toast(`Unable to stop ${this.mounted ? `<b>${workerManager.id}</b>` : `<a href=/worker-managers/${workerManager.id}><b>${workerManager.id}</b></a>`} worker-manager`, 10000, reason, true);

    private updateWorkerManager = (workerManager: IWorkerManager) => {
        workerManager = Object.values(normalize(workerManager, Schemas.WORKER_MANAGER).entities.workerManagers || {})[0];
        const formWorkerManager = {...workerManager};
        removeFields(formWorkerManager);
        this.setState({workerManager: workerManager, formWorkerManager: formWorkerManager},
            () => this.props.history.replace(workerManager.id.toString()));
    };

    private shouldShowSaveButton = () =>
        !!this.state.unsavedHosts.length;

    private saveEntities = (workerManager: IWorkerManager) => {
        this.saveHosts(workerManager);
    };

    private assignHost = (Host: string): void => {
        this.setState({
            unsavedHosts: this.state.unsavedHosts.concat(Host)
        });
    };

    private unassignHosts = (hosts: string[]): void => {
        this.setState({
            unsavedHosts: this.state.unsavedHosts.filter(Host => !hosts.includes(Host))
        });
    };

    private saveHosts = (workerManager: IWorkerManager): void => {
        const {unsavedHosts} = this.state;
        if (unsavedHosts.length) {
            postData(`worker-managers/${workerManager.id}/assigned-hosts`, unsavedHosts,
                () => this.onSaveHostsSuccess(workerManager),
                (reason) => this.onSaveHostsFailure(workerManager, reason));
        }
    };

    private onSaveHostsSuccess = (workerManager: IWorkerManager): void => {
        this.props.assignWorkerManagerHosts(workerManager.id.toString(), this.state.unsavedHosts);
        if (this.mounted) {
            this.setState({unsavedHosts: []});
        }
    };

    private onSaveHostsFailure = (workerManager: IWorkerManager, reason: string): void =>
        super.toast(`Unable to save assigned hosts of worker-manager ${this.mounted ? `<b>${workerManager.id}</b>` : `<a href=/worker-managers/${workerManager.id}><b>${workerManager.id}</b></a>`}`, 10000, reason, true);

    private getFields = (workerManager: INewWorkerManagerRegion | INewWorkerManagerHost | IWorkerManager): IFields =>
        Object.entries(workerManager).map(([key, value]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: getTypeFromValue(value) === 'number'
                        ? {rule: requiredAndNumberAndMin, args: 0}
                        : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private getSelectableHosts = (): Partial<IHostAddress>[] =>
        Object.entries(this.props.nodes)
            .filter(([_, node]) => node.state === 'ready')
            .map(([_, node]) =>
                ({
                    username: node.labels['username'],
                    publicIpAddress: node.publicIpAddress,
                    privateIpAddress: node.labels['privateIpAddress'],
                    coordinates: node.labels['coordinates'] ? JSON.parse(node.labels['coordinates']) : undefined,
                }))

    private hostAddressesDropdown = (hostAddress: Partial<IHostAddress>): string =>
        hostAddress.publicIpAddress + (hostAddress.privateIpAddress ? " (" + hostAddress.privateIpAddress + ")" : '');

    private containerIdField = (container: IContainer) =>
        container.containerId;

    private containerPublicIpAddressField = (container: IContainer) =>
        container.publicIpAddress;

    private formFields = (isNew: boolean, formWorkerManager?: Partial<IWorkerManager>) => {
        const {currentForm} = this.state;
        return (
            isNew ?
                currentForm === 'On regions'
                    ?
                    <Field key={'regions'}
                           id={'regions'}
                           label={'regions'}
                           type={'list'}
                           value={Object.keys(this.props.regions)}/>
                    :
                    <>
                        <Field<Partial<IHostAddress>> key={'hostAddress'}
                                                      id={'hostAddress'}
                                                      label={'hostAddress'}
                                                      type="dropdown"
                                                      dropdown={{
                                                          defaultValue: "Select host address",
                                                          values: this.getSelectableHosts(),
                                                          optionToString: this.hostAddressesDropdown,
                                                          emptyMessage: 'No hosts to select'
                                                      }}/>
                    </>
                : formWorkerManager && Object.entries(formWorkerManager).map((([key, value], index) =>
                key === 'container'
                    ? <>
                        <Field<IContainer> key={index}
                                           id={key}
                                           label={key + " id"}
                                           valueToString={this.containerIdField}
                                           icon={{linkedTo: `/containers/${(formWorkerManager as Partial<IWorkerManager>).container?.containerId}`}}/>
                        <Field<IContainer>
                            key={index + 1} // index + 1 is ok unless there are more fields after this one
                            id={key}
                            label={"host"}
                            valueToString={this.containerPublicIpAddressField}/>
                    </>
                    : <Field key={index}
                             id={key}
                             label={key}/>))
        );
    };

    private switchForm = (formId: 'On regions' | 'On host') =>
        this.setState({currentForm: formId});

    private workerManager = () => {
        const {isLoading, error, newWorkerManagerRegion, newWorkerManagerHost} = this.props;
        const {currentForm} = this.state;
        const isNewWorkerManager = this.isNew();
        const workerManager = isNewWorkerManager ? (currentForm === 'On regions' ? newWorkerManagerRegion : newWorkerManagerHost) : this.getWorkerManager();
        const formWorkerManager = this.getFormWorkerManager();
        // @ts-ignore
        const workerManagerKey: (keyof IWorkerManager) = workerManager && Object.keys(workerManager)[0];
        return (
            <>
                {!isNewWorkerManager && isLoading && <LoadingSpinner/>}
                {!isNewWorkerManager && !isLoading && error && <Error message={error}/>}
                {(isNewWorkerManager || !isLoading) && (isNewWorkerManager || !error) && workerManager && (
                    /*@ts-ignore*/
                    <Form id={workerManagerKey}
                          fields={this.getFields(workerManager)}
                          values={workerManager}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              textButton: isNew(this.props.location.search) ? 'launch' : 'save',
                              url: 'worker-managers',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          delete={{
                              textButton: 'Stop',
                              url: `worker-managers/${(workerManager as IWorkerManager).id}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}
                          switchDropdown={isNewWorkerManager ? {
                              options: currentForm === 'On regions' ? ['On host'] : ['On regions'],
                              onSwitch: this.switchForm
                          } : undefined}>
                        {this.formFields(isNewWorkerManager, formWorkerManager)}
                    </Form>
                )}
            </>
        )
    };

    private assignHosts = (): JSX.Element =>
        <AssignedHostsList isLoadingWorkerManager={this.props.isLoading}
                           loadWorkerManagerError={!this.isNew() ? this.props.error : undefined}
                           workerManager={this.getWorkerManager()}
                           unSavedHosts={this.state.unsavedHosts}
                           onAssignHost={this.assignHost}
                           onUnassignHosts={this.unassignHosts}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Worker manager',
            id: 'workerManager',
            content: () => this.workerManager(),
            active: this.props.location.state?.selected === 'workerManager'
        },
        {
            title: 'Assigned hosts',
            id: 'assignHosts',
            content: () => this.assignHosts(),
            active: this.props.location.state?.selected === 'assignHosts'
        }
    ];

}

function removeFields(workerManager: Partial<IWorkerManager>) {
    delete workerManager["id"];
    delete workerManager["assignedHosts"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.workerManagers.isLoadingWorkerManagers;
    const error = state.entities.workerManagers.loadWorkerManagersError;
    const id = props.match.params.id;
    const newWorkerManager = isNew(props.location.search);
    const newWorkerManagerRegion = newWorkerManager ? buildNewWorkerManagerRegion() : undefined;
    const newWorkerManagerHost = newWorkerManager ? buildNewWorkerManagerHost() : undefined;
    const workerManager = !newWorkerManager ? state.entities.workerManagers.data[id] : undefined;
    let formWorkerManager;
    if (workerManager) {
        formWorkerManager = {...workerManager};
        removeFields(formWorkerManager);
    }
    const regions = state.entities.regions.data;
    const nodes = state.entities.nodes.data;
    return {
        isLoading,
        error,
        newWorkerManagerRegion,
        newWorkerManagerHost,
        workerManager,
        formWorkerManager,
        regions,
        nodes
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadWorkerManagers,
    addWorkerManager,
    loadRegions,
    loadNodes,
    assignWorkerManagerHosts,
};

export default connect(mapStateToProps, mapDispatchToProps)(WorkerManager);
