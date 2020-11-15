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
import {
    addWorkerManagers,
    assignWorkerManagerHosts,
    loadNodes,
    loadRegions,
    loadWorkerManagers
} from "../../../actions";
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
    region: IRegion,
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
    addWorkerManagers: (workerManagers: IWorkerManager[]) => void;
    loadRegions: () => void;
    loadNodes: () => void;
    assignWorkerManagerHosts: (id: string, Hosts: string[]) => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: IWorkerManager,
    selected: 'worker-manager' | 'assignHosts';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    workerManager?: IWorkerManager,
    formWorkerManager?: IWorkerManager,
    unsavedHosts: string[],
    currentForm: 'Por regiões' | 'Num endereço',
}

class WorkerManager extends BaseComponent<Props, State> {

    state: State = {
        unsavedHosts: [],
        currentForm: 'Por regiões'
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
                <div className='container'>
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

    private onPostSuccess = (reply: IReply<IWorkerManager[]>): void => {
        let workerManagers = reply.data;
        if (workerManagers.length === 1) {
            const workerManager = workerManagers[0];
            const publicIpAddress = workerManager.container.publicIpAddress;
            super.toast(`<span class='green-text'>Worker-manager ${this.mounted ? `<b class='white-text'>${workerManager.id}</b>` : `<a href='/gestores locais/${workerManager.id}'><b>${workerManager.id}</b></a>`} launched at ${publicIpAddress}</span>`);
            this.saveEntities(workerManager);
            if (this.mounted) {
                this.updateWorkerManager(workerManager);
                this.props.history.replace(workerManager.id.toString());
            }
        } else {
            workerManagers = workerManagers.reverse();
            super.toast(`<span class='green-text'>Launched ${workerManagers.length} worker-managers:<br/><b class='white-text'>${workerManagers.map(workerManager => `${workerManager.id} => Host ${workerManager.container.publicIpAddress} => Contentor ${workerManager.container.containerId}`).join('<br/>')}</b></span>`);
            if (this.mounted) {
                this.props.history.push("/gestores locais");
            }
        }
        this.props.addWorkerManagers(workerManagers);
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Não foi possível lançar o gestor local`, 10000, reason, true);

    private onDeleteSuccess = (workerManager: IWorkerManager): void => {
        super.toast(`<span class='green-text'>O gestor local <b class='white-text'>${workerManager.id}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/gestores locais`)
        }
    };

    private onDeleteFailure = (reason: string, workerManager: IWorkerManager): void =>
        super.toast(`Não foi possível parar o gestor local ${this.mounted ? `<b>${workerManager.id}</b>` : `<a href='/gestores locais/${workerManager.id}'><b>${workerManager.id}</b></a>`}`, 10000, reason, true);

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
        super.toast(`Não foi possível guardar os hosts atribuídos ao gestor local ${this.mounted ? `<b>${workerManager.id}</b>` : `<a href='/gestores locais/${workerManager.id}'><b>${workerManager.id}</b></a>`}`, 10000, reason, true);

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
        hostAddress.publicIpAddress + (hostAddress.privateIpAddress ? ("/" + hostAddress.privateIpAddress) : '') + " - " + hostAddress.coordinates?.label;

    private containerIdField = (container: IContainer) =>
        container.containerId;

    private containerPublicIpAddressField = (container: IContainer) =>
        container.publicIpAddress;

    private regionOption = (region: IRegion) =>
        region.region;

    private formFields = (isNew: boolean, formWorkerManager?: Partial<IWorkerManager>) => {
        const {currentForm} = this.state;
        return (
            isNew ?
                currentForm === 'Por regiões'
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
                                                      type='dropdown'
                                                      dropdown={{
                                                          defaultValue: 'Selecionar endereço',
                                                          emptyMessage: 'Nenhum host disponível',
                                                          values: this.getSelectableHosts(),
                                                          optionToString: this.hostAddressesDropdown,
                                                      }}/>
                    </>
                : formWorkerManager && Object.entries(formWorkerManager).map((([key, value], index) =>
                key === 'container'
                    ? <>
                        <Field<IContainer> key={index}
                                           id={key}
                                           label={key + " id"}
                                           valueToString={this.containerIdField}
                                           icon={{linkedTo: `/contentores/${(formWorkerManager as Partial<IWorkerManager>).container?.containerId}`}}/>
                        <Field<IContainer>
                            key={5000}
                            id={key}
                            label={"host"}
                            valueToString={this.containerPublicIpAddressField}/>
                    </>
                    : key === 'region'
                    ? <Field<IRegion> key={index}
                                      id={key}
                                      type="dropdown"
                                      label={key}
                                      valueToString={this.regionOption}
                                      dropdown={{
                                          defaultValue: "Selecionar a região",
                                          emptyMessage: "Não há regiões disponíveis",
                                          values: Object.values(this.props.regions),
                                          optionToString: this.regionOption
                                      }}/>
                    : <Field key={index}
                             id={key}
                             label={key}/>))
        );
    };

    private switchForm = (formId: 'Por regiões' | 'Num endereço') =>
        this.setState({currentForm: formId});

    private onPost = (workerManagers: IWorkerManager[]) => {
        if (workerManagers.length == 1) {
            return workerManagers[0];
        }
        return workerManagers;
    }

    private workerManager = () => {
        const {isLoading, error, newWorkerManagerRegion, newWorkerManagerHost} = this.props;
        const {currentForm} = this.state;
        const isNewWorkerManager = this.isNew();
        const workerManager = isNewWorkerManager ? (currentForm === 'Por regiões' ? newWorkerManagerRegion : newWorkerManagerHost) : this.getWorkerManager();
        const formWorkerManager = this.getFormWorkerManager();
        // @ts-ignore
        const workerManagerKey: (keyof IWorkerManager) = formWorkerManager && Object.keys(formWorkerManager)[0];
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && workerManager && (
                    /*@ts-ignore*/
                    <Form id={workerManagerKey}
                          fields={this.getFields(workerManager)}
                          values={workerManager}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              textButton: isNew(this.props.location.search) ? 'Executar' : 'Guardar',
                              url: 'worker-managers',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure,
                              result: this.onPost,
                          }}
                          delete={{
                              textButton: 'Parar',
                              url: `worker-managers/${(workerManager as IWorkerManager).id}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}
                          switchDropdown={isNewWorkerManager ? {
                              options: currentForm === 'Por regiões' ? ['Num endereço'] : ['Por regiões'],
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
                           loadWorkerManagerError={this.props.error}
                           workerManager={this.getWorkerManager()}
                           unSavedHosts={this.state.unsavedHosts}
                           onAssignHost={this.assignHost}
                           onUnassignHosts={this.unassignHosts}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Gestor local',
            id: 'workerManager',
            content: () => this.workerManager(),
            active: this.props.location.state?.selected === 'worker-manager'
        },
        {
            title: 'Hosts geridos',
            id: 'assignHosts',
            content: () => this.assignHosts(),
            active: this.props.location.state?.selected === 'assignHosts'
        }
    ];

}

function removeFields(workerManager: Partial<IWorkerManager>) {
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
    addWorkerManagers,
    loadRegions,
    loadNodes,
    assignWorkerManagerHosts,
};

export default connect(mapStateToProps, mapDispatchToProps)(WorkerManager);
