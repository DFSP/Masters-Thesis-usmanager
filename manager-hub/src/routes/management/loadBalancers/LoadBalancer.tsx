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
import Form, {IFields, required, requiredAndNumberAndMin, requiredAndTrimmed} from "../../../components/form/Form";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addLoadBalancers, loadLoadBalancers, loadNodes, loadRegions} from "../../../actions";
import {IRegion} from "../regions/Region";
import {IReply} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {IContainer} from "../containers/Container";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IHostAddress} from "../hosts/Hosts";
import {INode} from "../nodes/Node";
import {connect} from "react-redux";
import {IDatabaseData} from "../../../components/IData";

export interface ILoadBalancer extends IDatabaseData {
    container: IContainer,
    region: IRegion,
}

interface INewLoadBalancerRegion {
    regions: string[] | undefined
}

interface INewLoadBalancerHost {
    host: IHostAddress | undefined
}

const buildNewLoadBalancerRegion = (): INewLoadBalancerRegion => ({
    regions: undefined
});

const buildNewLoadBalancerHost = (): INewLoadBalancerHost => ({
    host: undefined
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newLoadBalancerRegion?: INewLoadBalancerRegion;
    newLoadBalancerHost?: INewLoadBalancerHost;
    loadBalancer?: ILoadBalancer;
    formLoadBalancer?: Partial<ILoadBalancer>;
    regions: { [key: string]: IRegion };
    nodes: { [key: string]: INode };
    loadBalancers: { [key: string]: ILoadBalancer };
}

interface DispatchToProps {
    loadLoadBalancers: (id?: string) => void;
    addLoadBalancers: (loadBalancers: ILoadBalancer[]) => void;
    loadRegions: () => void;
    loadNodes: () => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: ILoadBalancer,
    selected: 'load-balancer';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    loadBalancer?: ILoadBalancer,
    formLoadBalancer?: ILoadBalancer,
    currentForm: 'Num endereço' | 'Por regiões',
}

class LoadBalancer extends BaseComponent<Props, State> {

    state: State = {
        currentForm: 'Por regiões',
    };
    private mounted = false;

    public componentDidMount(): void {
        if (isNew(this.props.location.search)) {
            this.props.loadLoadBalancers();
        } else {
            const workerManagerId = this.props.match.params.id;
            this.props.loadLoadBalancers(workerManagerId);
        }
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
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private getLoadBalancer = () =>
        this.props.loadBalancer || this.state.loadBalancer;

    private getFormLoadBalancer = () =>
        this.props.formLoadBalancer || this.state.formLoadBalancer;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<ILoadBalancer[]>): void => {
        let loadBalancers = reply.data;
        if (loadBalancers.length === 1) {
            const loadBalancer = loadBalancers[0];
            super.toast(`<span class="green-text">O balanceador de carga ${this.mounted ? `<b class="white-text">${loadBalancer.id}</b>` : `<a href='/balanceamento de carga/${loadBalancer.id}'><b>${loadBalancer.id}</b></a>`} foi iniciado</span>`);
            if (this.mounted) {
                this.updateLoadBalancer(loadBalancer);
                this.props.history.replace(loadBalancer.id.toString())
            }
        } else {
            loadBalancers = loadBalancers.reverse();
            super.toast(`<span class="green-text">Foram iniciados ${loadBalancers.length} balanceadores de carga: <br/><b class="white-text">${loadBalancers.map(loadBalancer => `Contentor ${loadBalancer.container.id} => Host ${loadBalancer.container.publicIpAddress}`).join('<br/>')}</b></span>`);
            if (this.mounted) {
                this.props.history.push('/balanceamento de carga');
            }
        }
        this.props.addLoadBalancers(loadBalancers);
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Não foi possível lançar um novo balanceador de carga`, 10000, reason, true);

    private onDeleteSuccess = (loadBalancer: ILoadBalancer): void => {
        super.toast(`<span class="green-text">O balanceador de carga <b class="white-text">${loadBalancer.id}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/balanceamento de carga`)
        }
    };

    private onDeleteFailure = (reason: string, loadBalancer: ILoadBalancer): void =>
        super.toast(`Não foi possível parar o balanceador de carga ${this.mounted ? `<b>${loadBalancer.id}</b>` : `<a href='/balanceamento de carga/${loadBalancer.id}'><b>${loadBalancer.id}</b></a>`}`, 10000, reason, true);

    private updateLoadBalancer = (loadBalancer: ILoadBalancer) => {
        loadBalancer = Object.values(normalize(loadBalancer, Schemas.LOAD_BALANCER).entities.loadBalancers || {})[0];
        const formLoadBalancer = {...loadBalancer};
        this.setState({loadBalancer: loadBalancer, formLoadBalancer: formLoadBalancer});
    };

    private getFields = (loadBalancer: INewLoadBalancerRegion | INewLoadBalancerHost | ILoadBalancer): IFields => {
        if (this.isNew()) {
            if (this.state.currentForm === 'Por regiões') {
                return {
                    regions: {
                        id: 'regions',
                        label: 'regions',
                        validation: {rule: required}
                    },
                }
            } else {
                return {
                    hostAddress: {
                        id: 'hostAddress',
                        label: 'hostAddress',
                        validation: {rule: required}
                    },
                }
            }
        } else {
            return Object.entries(loadBalancer).map(([key, value]) => {
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

    private regionOption = (region: IRegion) =>
        region.region;

    private getSelectableRegions = (): string[] => {
        const deployedRegions = Object.values(this.props.loadBalancers).map(loadBalancer => loadBalancer.region.region);
        return Object.keys(this.props.regions).filter(region => !deployedRegions.includes(region));
    }

    private containerIdField = (container: IContainer) =>
        container.id.toString();

    private containerPublicIpAddressField = (container: IContainer) =>
        container.publicIpAddress;

    private formFields = (isNew: boolean, formLoadBalancer?: Partial<ILoadBalancer>) => {
        const {currentForm} = this.state;
        return (
            isNew ?
                currentForm === 'Por regiões'
                    ?
                    <Field key={'regions'}
                           id={'regions'}
                           label={'regions'}
                           type={'list'}
                           value={this.getSelectableRegions()}/>
                    :
                    <>
                        <Field<Partial<IHostAddress>> key={'hostAddress'}
                                                      id={'hostAddress'}
                                                      label={'hostAddress'}
                                                      type="dropdown"
                                                      dropdown={{
                                                          defaultValue: "Selecionar o endereço",
                                                          emptyMessage: 'Não há hosts disponíveis',
                                                          values: this.getSelectableHosts(),
                                                          optionToString: this.hostAddressesDropdown,
                                                      }}/>
                    </>
                : formLoadBalancer && Object.entries(formLoadBalancer).map((([key, value], index) =>
                key === 'container'
                    ? <>
                        <Field<IContainer> key={index}
                                           id={key}
                                           label={key + " id"}
                                           valueToString={this.containerIdField}
                                           icon={{linkedTo: '/contentores/' + (formLoadBalancer as Partial<ILoadBalancer>).container?.id}}/>
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
                                          values: [(formLoadBalancer as ILoadBalancer).region],
                                          optionToString: this.regionOption
                                      }}/>
                    : <Field key={index}
                             id={key}
                             label={key}/>))
        );
    };

    private switchForm = (formId: 'Por regiões' | 'Num endereço') =>
        this.setState({currentForm: formId});

    private onPost = (loadBalancers: ILoadBalancer[]) => {
        if (loadBalancers.length == 1) {
            return loadBalancers[0];
        }
        return loadBalancers;
    }

    private loadBalancer = () => {
        const {isLoading, error, newLoadBalancerRegion, newLoadBalancerHost} = this.props;
        const {currentForm} = this.state;
        const isNewLoadBalancer = this.isNew();
        const loadBalancer = isNewLoadBalancer ? (currentForm === 'Por regiões' ? newLoadBalancerRegion : newLoadBalancerHost) : this.getLoadBalancer();
        const formLoadBalancer = this.getFormLoadBalancer();
        // @ts-ignore
        const loadBalancerKey: (keyof ILoadBalancer) = formLoadBalancer && Object.keys(formLoadBalancer)[0];
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && loadBalancer && (
                    /*@ts-ignore*/
                    <Form id={loadBalancerKey}
                          fields={this.getFields(loadBalancer)}
                          values={loadBalancer}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={false}
                          post={{
                              textButton: 'Executar',
                              url: 'load-balancers',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure,
                              result: this.onPost
                          }}
                          delete={{
                              textButton: 'Parar',
                              url: `load-balancers/${(loadBalancer as ILoadBalancer).id}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          switchDropdown={isNewLoadBalancer ? {
                              options: currentForm === 'Por regiões' ? ['Num endereço'] : ['Por regiões'],
                              onSwitch: this.switchForm
                          } : undefined}>
                        {this.formFields(isNewLoadBalancer, formLoadBalancer)}
                    </Form>
                )}
            </>
        )
    };

    private tabs = (): Tab[] => [
        {
            title: 'Balanceamento de carga',
            id: 'loadBalancer',
            content: () => this.loadBalancer(),
            active: this.props.location.state?.selected === 'load-balancer'
        },
    ];

}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.loadBalancers.isLoadingLoadBalancers;
    const error = state.entities.loadBalancers.loadLoadBalancersError;
    const id = props.match.params.id;
    const newLoadBalancer = isNew(props.location.search);
    const newLoadBalancerRegion = newLoadBalancer ? buildNewLoadBalancerRegion() : undefined;
    const newLoadBalancerHost = newLoadBalancer ? buildNewLoadBalancerHost() : undefined;
    const loadBalancer = !newLoadBalancer ? state.entities.loadBalancers.data[id] : undefined;
    let formLoadBalancer;
    if (loadBalancer) {
        formLoadBalancer = {...loadBalancer};
    }
    const regions = state.entities.regions.data;
    const nodes = state.entities.nodes.data;
    const loadBalancers = state.entities.loadBalancers.data;
    return {
        isLoading,
        error,
        newLoadBalancerRegion,
        newLoadBalancerHost,
        loadBalancer,
        formLoadBalancer,
        regions,
        nodes,
        loadBalancers
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadLoadBalancers,
    addLoadBalancers,
    loadRegions,
    loadNodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(LoadBalancer);