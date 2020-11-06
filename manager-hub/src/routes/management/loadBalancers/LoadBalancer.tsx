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

export interface ILoadBalancer extends IContainer {
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
}

interface DispatchToProps {
    loadLoadBalancers: (id: string) => void;
    addLoadBalancers: (loadBalancers: ILoadBalancer[]) => void;
    loadRegions: () => void;
    loadNodes: () => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: ILoadBalancer,
    selected: 'loadBalancer';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    loadBalancer?: ILoadBalancer,
    formLoadBalancer?: ILoadBalancer,
    currentForm: 'On regions' | 'On host',
}

class LoadBalancer extends BaseComponent<Props, State> {

    state: State = {
        currentForm: 'On regions'
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadLoadBalancer();
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

    private loadLoadBalancer = () => {
        if (!isNew(this.props.location.search)) {
            const loadBalancerId = this.props.match.params.id;
            this.props.loadLoadBalancers(loadBalancerId);
        }
    };

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
            super.toast(`<span class="green-text">Load-balancer ${this.mounted ? `<b class="white-text">${loadBalancer.containerId}</b>` : `<a href=/load-balancers/${loadBalancer.containerId}><b>${loadBalancer.containerId}</b></a>`} launched</span>`);
            if (this.mounted) {
                this.updateLoadBalancer(loadBalancer);
                this.props.history.replace(loadBalancer.containerId)
            }
        } else {
            loadBalancers = loadBalancers.reverse();
            super.toast(`<span class="green-text">Launched ${loadBalancers.length} load-balancers:<br/><b class="white-text">${loadBalancers.map(loadBalancer => `Container ${loadBalancer.containerId} => Host ${loadBalancer.publicIpAddress}`).join('<br/>')}</b></span>`);
            if (this.mounted) {
                this.props.history.push('/load-balancers');
            }
        }
        this.props.addLoadBalancers(loadBalancers);
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Unable to launch load-balancer`, 10000, reason, true);

    private onDeleteSuccess = (loadBalancer: ILoadBalancer): void => {
        super.toast(`<span class="green-text">Load-balancer <b class="white-text">${loadBalancer.containerId}</b> successfully stopped</span>`);
        if (this.mounted) {
            this.props.history.push(`/load-balancers`)
        }
    };

    private onDeleteFailure = (reason: string, loadBalancer: ILoadBalancer): void =>
        super.toast(`Unable to stop load-balancer ${this.mounted ? `<b>${loadBalancer.containerId}</b>` : `<a href=/load-balancers/${loadBalancer.containerId}><b>${loadBalancer.containerId}</b></a>`}`, 10000, reason, true);

    private updateLoadBalancer = (loadBalancer: ILoadBalancer) => {
        loadBalancer = Object.values(normalize(loadBalancer, Schemas.LOAD_BALANCER).entities.loadBalancers || {})[0];
        const formLoadBalancer = {...loadBalancer};
        removeFields(formLoadBalancer);
        this.setState({loadBalancer: loadBalancer, formLoadBalancer: formLoadBalancer});
    };

    private getFields = (loadBalancer: INewLoadBalancerRegion | INewLoadBalancerHost | ILoadBalancer): IFields => {
        if (this.isNew()) {
            if (this.state.currentForm === 'On regions') {
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

    private formFields = (isNew: boolean, formLoadBalancer?: Partial<ILoadBalancer>) => {
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
                : formLoadBalancer && Object.entries(formLoadBalancer).map((([key, value], index) =>
                key === 'containerId'
                    ? <Field key={index}
                             id={key}
                             label={key}
                             icon={{linkedTo: '/containers/' + (formLoadBalancer as Partial<ILoadBalancer>).containerId}}/>
                    : key === 'created'
                    ? <Field key={index}
                             id={key}
                             label={key}
                             type={"date"}/>
                    : key === 'region'
                        ? <Field<IRegion> key={index}
                                          id={key}
                                          type="dropdown"
                                          label={key}
                                          valueToString={this.regionOption}
                                          dropdown={{
                                              defaultValue: "Select region",
                                              emptyMessage: "No regions to select",
                                              values: [(formLoadBalancer as ILoadBalancer).region],
                                              optionToString: this.regionOption
                                          }}/>
                        : <Field key={index}
                                 id={key}
                                 label={key}/>))
        );
    };

    private switchForm = (formId: 'On regions' | 'On host') =>
        this.setState({currentForm: formId});

    private loadBalancer = () => {
        const {isLoading, error, newLoadBalancerRegion, newLoadBalancerHost} = this.props;
        const {currentForm} = this.state;
        const isNewLoadBalancer = this.isNew();
        const loadBalancer = isNewLoadBalancer ? (currentForm === 'On regions' ? newLoadBalancerRegion : newLoadBalancerHost) : this.getLoadBalancer();
        const formLoadBalancer = this.getFormLoadBalancer();
        // @ts-ignore
        const loadBalancerKey: (keyof ILoadBalancer) = formLoadBalancer && Object.keys(formLoadBalancer)[0];
        return (
            <>
                {!isNewLoadBalancer && isLoading && <LoadingSpinner/>}
                {!isNewLoadBalancer && !isLoading && error && <Error message={error}/>}
                {(isNewLoadBalancer || !isLoading) && (isNewLoadBalancer || !error) && loadBalancer && (
                    /*@ts-ignore*/
                    <Form id={loadBalancerKey}
                          fields={this.getFields(loadBalancer)}
                          values={loadBalancer}
                          isNew={isNew(this.props.location.search)}
                          post={{
                              textButton: 'launch',
                              url: 'load-balancers',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          delete={{
                              textButton: 'Stop',
                              url: `containers/${(loadBalancer as ILoadBalancer).containerId}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          switchDropdown={isNewLoadBalancer ? {
                              options: currentForm === 'On regions' ? ['On host'] : ['On regions'],
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
            title: 'Load balancer',
            id: 'loadBalancer',
            content: () => this.loadBalancer(),
            active: this.props.location.state?.selected === 'loadBalancer'
        },
    ];

}

function removeFields(loadBalancer: Partial<ILoadBalancer>) {
    delete loadBalancer["id"];
    delete loadBalancer["ports"];
    delete loadBalancer["labels"];
    delete loadBalancer["logs"];
    delete loadBalancer["coordinates"];
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
        removeFields(formLoadBalancer);
    }
    const regions = state.entities.regions.data;
    const nodes = state.entities.nodes.data;
    return {
        isLoading,
        error,
        newLoadBalancerRegion,
        newLoadBalancerHost,
        loadBalancer,
        formLoadBalancer,
        regions,
        nodes
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadLoadBalancers,
    addLoadBalancers,
    loadRegions,
    loadNodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(LoadBalancer);