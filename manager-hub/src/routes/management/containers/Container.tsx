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
import BaseComponent from "../../../components/BaseComponent";
import Form, {
    IFields,
    IFormLoading,
    required,
    requiredAndNumberAndMin,
    requiredAndTrimmed,
    requireGreaterOrEqualSize
} from "../../../components/form/Form";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
    addContainerRules,
    addContainers,
    addContainerSimulatedMetrics, loadCloudHosts,
    loadContainers, loadEdgeHosts,
    loadNodes,
    loadServices
} from "../../../actions";
import {connect} from "react-redux";
import React from "react";
import {ICloudHost} from "../hosts/cloud/CloudHost";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IService} from "../services/Service";
import ContainerPortsList from "./ContainerPortsList";
import ContainerLabelsList from "./ContainerLabelsList";
import ContainerLogsList from "./ContainerLogsList";
import PerfectScrollbar from "react-perfect-scrollbar";
import ScrollBar from "react-perfect-scrollbar";
import M from "materialize-css";
import styles from "../../../components/list/ControlledList.module.css";
import {decodeHTML} from "../../../utils/text";
import {IReply, postData} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import IDatabaseData from "../../../components/IDatabaseData";
import ContainerRuleList from "./ContainerRuleList";
import ContainerSimulatedMetricList from "./ContainerSimulatedMetricList";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import formStyles from "../../../components/form/Form.module.css";
import {INode} from "../nodes/Node";
import {IHostAddress} from "../hosts/Hosts";
import {ICoordinates} from "../../../components/map/LocationMap";
import GenericSimulatedServiceMetricList from "../services/GenericSimulatedServiceMetricList";
import GenericServiceRuleList from "../services/GenericServiceRuleList";
import {IRegion} from "../regions/Region";
import {Point} from "react-simple-maps";
import {IMarker} from "../../../components/map/Marker";
import ReactTooltip from "react-tooltip";

export interface IContainer extends IDatabaseData {
    type: ContainerType;
    created: number;
    name: string;
    image: string;
    command: string;
    network: string;
    publicIpAddress: string;
    privateIpAddress: string;
    mounts: string[];
    ports: IContainerPort[];
    labels: IContainerLabel;
    region: IRegion;
    coordinates: ICoordinates;
    managerId: string | null;
    logs?: string;
    containerRules?: string[];
    containerSimulatedMetrics?: string[];
}

export type ContainerType = 'SINGLETON' | 'BY_REQUEST'

export interface IContainerPort {
    publicPort: number;
    privatePort: number;
    type: string;
    ip: string;
}

export interface IContainerLabel {
    [key: string]: string
}

interface INewContainerHost {
    service?: string,
    externalPort?: number,
    internalPort?: number,
    hostAddress?: IHostAddress,
}

interface INewContainerLocation {
    service?: string,
    externalPort?: number,
    internalPort?: number,
    coordinates?: Point[];
}

const buildNewContainerHost = (): INewContainerHost => ({
    service: undefined,
    externalPort: undefined,
    internalPort: undefined,
    hostAddress: undefined,
});

const buildNewContainerLocation = (): INewContainerLocation => ({
    service: undefined,
    externalPort: undefined,
    internalPort: undefined,
    coordinates: undefined,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newContainerHost?: INewContainerHost;
    newContainerLocation?: INewContainerLocation;
    container?: IContainer;
    formContainer?: Partial<IContainer> | INewContainerHost;
    nodes: { [key: string]: INode };
    services: { [key: string]: IService };
    cloudHosts: { [key: string]: ICloudHost };
    edgeHosts: { [key: string]: IEdgeHost };
    containers: { [key: string]: IContainer };
}

interface DispatchToProps {
    loadContainers: (containerId?: string) => void;
    loadNodes: () => void;
    loadServices: () => void;
    loadCloudHosts: () => void;
    loadEdgeHosts: () => void;
    addContainers: (containers: IContainer[]) => void;
    addContainerRules: (containerId: string, rules: string[]) => void;
    addContainerSimulatedMetrics: (containerId: string, simulatedMetrics: string[]) => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: IContainer,
    selected: 'container' | 'ports' | 'containerLabels' | 'logs' | 'rules' | 'genericServiceRules'
        | 'simulatedMetrics' | 'genericSimulatedMetrics';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    container?: IContainer,
    formContainer?: IContainer,
    loading: IFormLoading,
    defaultInternalPort: number,
    defaultExternalPort: number,
    unsavedRules: string[],
    unsavedSimulatedMetrics: string[],
    currentForm: 'Usando o endereço' | 'Usando a localização',
}

class Container extends BaseComponent<Props, State> {

    state: State = {
        loading: undefined,
        defaultInternalPort: 0,
        defaultExternalPort: 0,
        unsavedRules: [],
        unsavedSimulatedMetrics: [],
        currentForm: 'Usando a localização',
    };
    private mounted = false;
    private scrollbar: (ScrollBar | null) = null;

    public componentDidMount(): void {
        this.loadContainer();
        this.props.loadNodes();
        this.props.loadServices();
        this.props.loadCloudHosts();
        this.props.loadEdgeHosts();
        this.mounted = true;
    };

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

    private initDropdown = (dropdown: HTMLButtonElement | null) => {
        if (dropdown) {
            M.Dropdown.init(dropdown,
                {
                    onOpenEnd: this.onOpenDropdown
                });
        }
    };

    private onOpenDropdown = () =>
        this.scrollbar?.updateScroll();

    private loadContainer = () => {
        if (!this.isNew()) {
            const containerId = this.props.match.params.id;
            this.props.loadContainers(containerId);
        } else {
            this.props.loadContainers();
        }
    };

    private getContainer = () =>
        this.props.container || this.state.container;

    private getFormContainer = () =>
        this.props.formContainer || this.state.formContainer;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IContainer[]>): void => {
        let containers = reply.data;
        if (containers.length === 1) {
            const container = containers[0];
            super.toast(`<span class='green-text'>O contentor ${this.mounted ? `<b class='white-text'>${container.id}</b>` : `<a href='/contentores/${container.id}'><b>${container.id}</b></a>`} começou a sua execução com sucesso no host ${container.publicIpAddress}</span>`);
            this.saveEntities(container);
            if (this.mounted) {
                this.updateContainer(container);
                this.props.history.replace(container.id.toString());
            }
        } else {
            containers = containers.reverse();
            super.toast(`<span class='green-text'>Foram lançados ${containers.length} contentores novos:<br/><b class='white-text'>${containers.map(container => `${container.id} => Host ${container.publicIpAddress}`).join('<br/>')}</b></span>`);
            if (this.mounted) {
                this.props.history.push("/contentores");
            }
        }
        this.props.addContainers(containers);
    };

    private onPostFailure = (reason: string, request: INewContainerHost | INewContainerLocation): void => {
        super.toast(`Erro ao iniciar o contentor</b>`, 10000, reason, true);
    }

    private onDeleteSuccess = (container: IContainer): void => {
        super.toast(`<span class='green-text'>O contentor <b class='white-text'>${container.id}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/contentores`);
        }
    };

    private onDeleteFailure = (reason: string, container: IContainer): void =>
        super.toast(`Erro ao parar o contentor ${this.mounted ? `<b>${container.id}</b>` : `<a href='/contentores/${container.id}'><b>${container.id}</b></a>`}`, 10000, reason, true);

    private replicateButton = () =>
        <>
            <button
                className={`btn-flat btn-small blue-text dropdown-trigger ${formStyles.formButton}`}
                data-target={`replicate-dropdown-host-address`}
                ref={(ref) => this.initDropdown(ref)}>
                Replicar
            </button>
            {this.chooseHostAddressDropdown('replicate-dropdown-host-address', this.replicate)}
        </>

    private migrateButton = () =>
        <>
            <button
                className={`btn-flat btn-small blue-text dropdown-trigger ${formStyles.formButton}`}
                data-target={`migrate-dropdown-host-address`}
                ref={(ref) => this.initDropdown(ref)}>
                Migrar
            </button>
            {this.chooseHostAddressDropdown('migrate-dropdown-host-address', this.migrate)}
        </>

    private chooseHostAddressDropdown = (id: string, onClick: (event: any) => void) => {
        const nodes = Object.values(this.props.nodes)
            .filter(node => node.state === 'ready' && (!id.includes('migrate') || node.publicIpAddress !== this.getContainer()?.publicIpAddress));
        return <ul id={id}
                   className={`dropdown-content ${styles.dropdown}`}>
            <li className={`${styles.disabled}`}>
                <a className={`${!nodes.length ? 'dropdown-empty' : ''}`}>
                    {!nodes.length ? 'Sem nós disponíveis' : 'Selecionar o endereço'}
                </a>
            </li>
            <PerfectScrollbar ref={(ref) => this.scrollbar = ref}>
                {nodes.map((node, index) =>
                    <li key={index} onClick={onClick}>
                        <a>
                            {`${node.publicIpAddress + (node.labels['privateIpAddress'] ? " (" + node.labels['privateIpAddress'] + ")" : '')}`}
                        </a>
                    </li>
                )}
            </PerfectScrollbar>
        </ul>;
    }

    private replicate = (event: any) => {
        const container = this.getContainer();
        const hostAddress = decodeHTML((event.target as HTMLLIElement).innerHTML).split(' (');
        const publicIpAddress = hostAddress[0];
        const privateIpAddress = hostAddress[1]?.substr(0, hostAddress[1].length - 1);
        const url = `containers/${container?.id}/replicate`;
        this.setState({loading: {method: 'post', url: url}});
        postData(url, {publicIpAddress: publicIpAddress, privateIpAddress: privateIpAddress},
            (reply: IReply<IContainer>) => this.onReplicateSuccess(reply.data),
            (reason: string) => this.onReplicateFailure(reason, container));
    };

    private onReplicateSuccess = (container: IContainer) => {
        super.toast(`<span class='green-text'>Replicated ${container.image.split('/').splice(1)} to container </span><a href='/contentores/${container.id}'><b>${container.id}</b></a>`, 15000);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private onReplicateFailure = (reason: string, container?: IContainer) => {
        super.toast(`Erro ao replicar o contentor ${this.mounted ? `<b>${container?.id}</b>` : `<a href='/contentores/${container?.id}'><b>${container?.id}</b></a>`}`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private migrate = (event: any) => {
        const container = this.getContainer();
        const hostAddress = decodeHTML((event.target as HTMLLIElement).innerHTML).split(' (');
        const publicIpAddress = hostAddress[0];
        const privateIpAddress = hostAddress[1]?.substr(0, hostAddress[1].length - 1)
        const url = `containers/${container?.id}/migrate`;
        this.setState({loading: {method: 'post', url: url}});
        postData(url, {publicIpAddress: publicIpAddress, privateIpAddress: privateIpAddress},
            (reply: IReply<IContainer>) => this.onMigrateSuccess(reply.data),
            (reason) => this.onMigrateFailure(reason, container));
    };

    private onMigrateSuccess = (container: IContainer) => {
        const parentContainer = this.getContainer();
        super.toast(`<span class='green-text'>Migrated ${this.mounted ? parentContainer?.id : `<a href='/contentores/${parentContainer?.id}'>${parentContainer?.id}</a>`} to container </span><a href='/contentores/'${container.id}>${container.id}</a>`, 15000);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private onMigrateFailure = (reason: string, container?: IContainer) => {
        super.toast(`Erro ao migrar o contentor ${this.mounted ? `<b>${container?.id}</b>` : `<a href='/contentores/${container?.id}'><b>${container?.id}</b></a>`}`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private addContainerRule = (rule: string): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.concat(rule)
        });
    };

    private removeContainerRules = (rules: string[]): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
        });
    };

    private saveContainerRules = (container: IContainer): void => {
        const {unsavedRules} = this.state;
        if (unsavedRules.length) {
            postData(`containers/${container.id}/rules`, unsavedRules,
                () => this.onSaveRulesSuccess(container),
                (reason) => this.onSaveRulesFailure(container, reason));
        }
    };

    private onSaveRulesSuccess = (container: IContainer): void => {
        this.props.addContainerRules(container.id.toString(), this.state.unsavedRules);
        if (this.mounted) {
            this.setState({unsavedRules: []});
        }
    };

    private onSaveRulesFailure = (container: IContainer, reason: string): void =>
        super.toast(`Não foi possível guardar as regras associadas ao contentor ${this.mounted ? `<b>${container.id}</b>` : `<a href='/contentores/${container.id}'><b>${container.id}</b></a>`}`, 10000, reason, true);

    private addContainerSimulatedMetric = (simulatedMetric: string): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.concat(simulatedMetric)
        });
    };

    private removeContainerSimulatedMetrics = (simulatedMetrics: string[]): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.filter(metric => !simulatedMetrics.includes(metric))
        });
    };

    private saveContainerSimulatedMetrics = (container: IContainer): void => {
        const {unsavedSimulatedMetrics} = this.state;
        if (unsavedSimulatedMetrics.length) {
            postData(`containers/${container.id}/simulated-metrics`, unsavedSimulatedMetrics,
                () => this.onSaveSimulatedMetricsSuccess(container),
                (reason) => this.onSaveSimulatedMetricsFailure(container, reason));
        }
    };

    private onSaveSimulatedMetricsSuccess = (container: IContainer): void => {
        this.props.addContainerSimulatedMetrics(container.id.toString(), this.state.unsavedSimulatedMetrics);
        if (this.mounted) {
            this.setState({unsavedSimulatedMetrics: []});
        }
    };

    private onSaveSimulatedMetricsFailure = (container: IContainer, reason: string): void =>
        super.toast(`Não foi possível guardar as métricas simuladas associadas ao contentor ${this.mounted ? `<b>${container.id}</b>` : `<a href='/contentores/${container.id}'><b>${container.id}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedRules.length
        || !!this.state.unsavedSimulatedMetrics.length;

    private saveEntities = (container: IContainer) => {
        this.saveContainerRules(container);
        this.saveContainerSimulatedMetrics(container);
    };

    private updateContainer = (container: IContainer) => {
        container = Object.values(normalize(container, Schemas.CONTAINER).entities.containers || {})[0];
        const formContainer = {...container};
        removeFields(formContainer);
        this.setState({container: container, formContainer: formContainer, loading: undefined});
    };

    private commonFields = (): IFields => {
        return ['service', 'externalPort', 'internalPort'].map(field => ({
            [field]: {
                id: field,
                label: field,
                validation: {rule: required}
            }
        })).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});
    }

    private getFields = (container: INewContainerHost | INewContainerLocation | IContainer): IFields => {
        if (this.isNew()) {
            return this.state.currentForm === 'Usando o endereço'
                ? ({
                    ...this.commonFields(),
                    hostAddress: {
                        id: 'hostAddress',
                        label: 'hostAddress',
                        validation: {rule: required}
                    },
                }) : ({
                    ...this.commonFields(),
                    coordinates: {
                        id: 'coordinates',
                        label: 'coordinates',
                        validation: {rule: requireGreaterOrEqualSize, args: 1}
                    },
                })
        } else {
            return Object.entries(container).map(([key, value]) => {
                return {
                    [key]: {
                        id: key,
                        label: key,
                        validation: getTypeFromValue(value) === 'number'
                            ? {rule: requiredAndNumberAndMin, args: 0}
                            : key === 'coordinates'
                                ? {rule: requireGreaterOrEqualSize, args: 1}
                                : {rule: requiredAndTrimmed}
                    }
                };
            }).reduce((fields, field) => {
                for (let key in field) {
                    fields[key] = field[key];
                }
                return fields;
            }, {});
        }
    }

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

    //TODO get apps' services instead (in case a service is associated to more than 1 app)
    private getSelectableServices = () =>
        Object.values(this.props.services).filter(service => service.serviceType !== 'SYSTEM')
            .map(service => service.serviceName).sort();
    /*Object.entries(this.props.services)
        .filter(([_, service]) => service.serviceType.toLowerCase() !== 'system')
        .map(([serviceName, _]) => serviceName);*/

    private setDefaultPorts = (serviceName: string) => {
        const service = this.props.services[serviceName];
        this.setState({
            defaultExternalPort: service.defaultExternalPort,
            defaultInternalPort: service.defaultInternalPort
        });
    };

    private hostLink = (publicIpAddress: string) => {
        const cloudHost = Object.values(this.props.cloudHosts).filter(c => c.publicIpAddress === publicIpAddress)[0];
        if (cloudHost) {
            return '/hosts/cloud/' + cloudHost.instanceId;
        }
        const edgeHost = Object.values(this.props.edgeHosts).filter(e => e.publicIpAddress === publicIpAddress)[0];
        if (edgeHost) {
            return '/hosts/edge/' + edgeHost.publicIpAddress + "-" + edgeHost.privateIpAddress;
        }
        return null;
    }

    private managerLink = (managerId: string) => {
        if (!!managerId && managerId !== 'manager-master') {
            return `/gestores locais/${managerId}`
        }
        return null;
    }

    private regionOption = (region: IRegion) =>
        region.region;

    private switchForm = (formId: 'Usando o endereço' | 'Usando a localização') =>
        this.setState({currentForm: formId});

    private getContainersMarkers = (): IMarker[] => {
        const containers: IContainer[] = Object.values(this.props.containers);
        const markers = new Map<String, IMarker>();
        containers
            .forEach(container => {
                const publicIpAddress = container.publicIpAddress;
                const marker = markers.get(publicIpAddress) || {title: '', label: '', latitude: 0, longitude: 0};
                if (marker.title === '') {
                    marker.title += container.coordinates.label + '<br/>';
                }
                marker.title += container.id.toString().substr(0, 5) + ' - ' + container.labels['serviceName'] + '<br/>';
                marker.label = publicIpAddress;
                marker.latitude = container.coordinates.latitude;
                marker.longitude = container.coordinates.longitude;
                marker.color = 'green';
                markers.set(publicIpAddress, marker);
            });
        return Array.from(markers.values());
    }

    private formFields = (formContainer: INewContainerHost | Partial<IContainer>, isNew: boolean): JSX.Element => {
        const {currentForm} = this.state;
        return isNew ?
            currentForm === 'Usando o endereço'
                ?
                <>
                    <Field key={'service'}
                           id={'service'}
                           label={'service'}
                           type={'dropdown'}
                           dropdown={{
                               defaultValue: "Selecionar o serviço",
                               emptyMessage: 'Não há serviços disponíveis',
                               values: this.getSelectableServices(),
                               selectCallback: this.setDefaultPorts,
                           }}/>
                    <Field key={'externalPort'}
                           id={'externalPort'}
                           label={'externalPort'}
                           type={'number'}/>
                    <Field key={'internalPort'}
                           id={'internalPort'}
                           label={'internalPort'}
                           type={'number'}/>
                    <Field<Partial<IHostAddress>> key={'hostAddress'}
                                                  id={'hostAddress'}
                                                  label={'hostAddress'}
                                                  type={'dropdown'}
                                                  dropdown={{
                                                      defaultValue: "Selecionar o endereço",
                                                      emptyMessage: 'Não há hosts disponíveis',
                                                      values: this.getSelectableHosts(),
                                                      optionToString: this.hostAddressesDropdown,
                                                  }}/>
                </>
                :
                <>
                    <Field key={'service'}
                           id={'service'}
                           label={'service'}
                           type={'dropdown'}
                           dropdown={{
                               defaultValue: "Selecionar o serviço",
                               emptyMessage: 'Não há serviços disponíveis',
                               values: this.getSelectableServices(),
                               selectCallback: this.setDefaultPorts,
                           }}/>
                    <Field key={'externalPort'}
                           id={'externalPort'}
                           label={'externalPort'}
                           type={'number'}/>
                    <Field key={'internalPort'}
                           id={'internalPort'}
                           label={'internalPort'}
                           type={'number'}/>
                    <Field key='coordinates' id='coordinates' label='select position(s)' type='map'
                           map={{
                               loading: this.props.isLoading,
                               editable: true,
                               labeled: false,
                               markers: this.getContainersMarkers()
                           }}/>
                </>
            :
            <>
                {Object.entries(formContainer).map(([key, value], index) =>
                    key === 'created'
                        ? <Field key={index}
                                 id={key}
                                 label={key}
                                 type={"date"}/>
                        : key === 'publicIpAddress'
                        ? <Field key={index}
                                 id={key}
                                 label={key}
                                 icon={{linkedTo: this.hostLink}}/>
                        : key === 'region'
                            ? <Field<IRegion> key={index}
                                              id={key}
                                              type='dropdown'
                                              label={key}
                                              valueToString={this.regionOption}
                                              dropdown={{
                                                  defaultValue: "Selecionar a região",
                                                  emptyMessage: "Não há regiões disponíveis",
                                                  values: [(formContainer as IContainer).region],
                                                  optionToString: this.regionOption
                                              }}/>
                            : key === 'coordinates'
                                ? <Field key={index} id='coordinates' label='location' type='map'
                                         map={{
                                             loading: this.props.isLoading,
                                             editable: false,
                                             zoomable: true,
                                             labeled: true
                                         }}/>
                                : key === 'command'
                                    ? <Field key={index}
                                             id={key}
                                             label={key}
                                             type='multilinetext'/>
                                    : key === 'managerId'
                                        ? <Field key={index}
                                                 id={key}
                                                 label={key}
                                                 icon={{linkedTo: this.managerLink}}/>
                                        : <Field key={index}
                                                 id={key}
                                                 label={key}/>
                )}
            </>;
    }

    private container = () => {
        const {isLoading, error} = this.props;
        let {newContainerHost, newContainerLocation} = this.props;
        const {currentForm} = this.state;
        const isNewContainer = this.isNew();
        if (isNewContainer) {
            newContainerHost = {
                ...newContainerHost,
                internalPort: this.state.defaultInternalPort,
                externalPort: this.state.defaultExternalPort
            };
            newContainerLocation = {
                ...newContainerHost,
                internalPort: this.state.defaultInternalPort,
                externalPort: this.state.defaultExternalPort
            };
        }
        const container = isNewContainer ? (currentForm === 'Usando o endereço' ? newContainerHost : newContainerLocation) : this.getContainer();
        const formContainer = this.getFormContainer();
        // @ts-ignore
        const containerKey: (keyof IContainer) = formContainer && Object.keys(formContainer)[0];
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && container && (
                    /*@ts-ignore*/
                    <Form id={containerKey}
                          fields={this.getFields(container)}
                          values={container}
                          isNew={isNewContainer}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              textButton: 'Iniciar',
                              url: 'containers',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                        // delete button is never present on new nodes, so a type cast is safe
                          delete={{
                              textButton: 'Parar',
                              url: `containers/${(container as IContainer).id}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          customButtons={!isNewContainer && container && (container as IContainer).labels?.['serviceType'] !== 'SYSTEM'
                              ? [{button: this.replicateButton()}, {button: this.migrateButton()}]
                              : undefined}
                          loading={this.state.loading}
                          saveEntities={this.saveEntities}
                          switchDropdown={isNewContainer ? {
                              options: currentForm === 'Usando o endereço' ? ['Usando a localização'] : ['Usando o endereço'],
                              onSwitch: this.switchForm
                          } : undefined}>
                        {this.formFields(formContainer || {}, isNewContainer)}
                    </Form>
                )}
            </>
        )
    };

    private ports = (): JSX.Element =>
        <ContainerPortsList isLoadingContainer={this.props.isLoading}
                            loadContainerError={this.props.error}
                            container={this.getContainer()}/>;

    private labels = (): JSX.Element =>
        <ContainerLabelsList isLoadingContainer={this.props.isLoading}
                             loadContainerError={this.props.error}
                             container={this.getContainer()}/>;

    private logs = (): JSX.Element =>
        <ContainerLogsList isLoadingContainer={this.props.isLoading}
                           loadContainerError={this.props.error}
                           container={this.getContainer()}/>;

    private rules = (): JSX.Element =>
        <ContainerRuleList isLoadingContainer={this.props.isLoading}
                           loadContainerError={this.props.error}
                           container={this.getContainer()}
                           unsavedRules={this.state.unsavedRules}
                           onAddContainerRule={this.addContainerRule}
                           onRemoveContainerRules={this.removeContainerRules}/>;

    private genericRules = (): JSX.Element =>
        <GenericServiceRuleList/>;

    private simulatedMetrics = (): JSX.Element =>
        <ContainerSimulatedMetricList isLoadingContainer={this.props.isLoading}
                                      loadContainerError={this.props.error}
                                      container={this.getContainer()}
                                      unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                      onAddSimulatedContainerMetric={this.addContainerSimulatedMetric}
                                      onRemoveSimulatedContainerMetrics={this.removeContainerSimulatedMetrics}/>;

    private genericSimulatedMetrics = (): JSX.Element =>
        <GenericSimulatedServiceMetricList/>;

    private tabs = (): Tab[] => ([
        {
            title: 'Contentor',
            id: 'container',
            content: () => this.container(),
            active: this.props.location.state?.selected === 'container'
        },
        {
            title: 'Portas',
            id: 'ports',
            content: () => this.ports(),
            hidden: this.isNew(),
            active: this.props.location.state?.selected === 'ports'
        },
        {
            title: 'Labels',
            id: 'containerLabels',
            content: () => this.labels(),
            hidden: this.isNew(),
            active: this.props.location.state?.selected === 'containerLabels'
        },
        {
            title: 'Logs',
            id: 'logs',
            content: () => this.logs(),
            hidden: this.isNew(),
            active: this.props.location.state?.selected === 'logs'
        },
        {
            title: 'Regras',
            id: 'rules',
            content: () => this.rules(),
            active: this.props.location.state?.selected === 'rules'
        },
        {
            title: 'Regras genéricas',
            id: 'genericContainerRules',
            content: () => this.genericRules(),
            active: this.props.location.state?.selected === 'genericServiceRules'
        },
        {
            title: 'Métricas simuladas',
            id: 'simulatedMetrics',
            content: () => this.simulatedMetrics(),
            active: this.props.location.state?.selected === 'simulatedMetrics'
        },
        {
            title: 'Métricas simuladas genéricas',
            id: 'genericSimulatedMetrics',
            content: () => this.genericSimulatedMetrics(),
            active: this.props.location.state?.selected === 'genericSimulatedMetrics'
        }
    ]);

}

function removeFields(container: IContainer) {
    if (container.labels?.['serviceType'] === 'SYSTEM') {
        delete container["network"];
    }
    delete container["ports"];
    delete container["labels"];
    delete container["logs"];
    delete container["containerRules"];
    delete container["containerSimulatedMetrics"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.containers.isLoadingContainers;
    const error = state.entities.containers.loadContainersError;
    const id = props.match.params.id;
    const newContainerHost = isNew(props.location.search) ? buildNewContainerHost() : undefined;
    const newContainerLocation = isNew(props.location.search) ? buildNewContainerLocation() : undefined;
    const container = !isNew(props.location.search) ? state.entities.containers.data[id] : undefined;
    let formContainer;
    if (container) {
        formContainer = {...container};
        removeFields(formContainer);
    }
    const nodes = state.entities.nodes.data;
    const services = state.entities.services.data;
    const cloudHosts = state.entities.hosts.cloud.data;
    const edgeHosts = state.entities.hosts.edge.data;
    const containers = state.entities.containers.data;
    return {
        isLoading,
        error,
        newContainerHost,
        newContainerLocation,
        container,
        formContainer,
        nodes,
        services,
        cloudHosts,
        edgeHosts,
        containers
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadContainers,
    addContainers,
    loadNodes,
    loadServices,
    loadCloudHosts,
    loadEdgeHosts,
    addContainerRules,
    addContainerSimulatedMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(Container);