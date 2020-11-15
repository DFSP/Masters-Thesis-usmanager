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

import IDatabaseData from "../../../../components/IDatabaseData";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../../components/BaseComponent";
import Form, {
    IFields,
    requiredAndTrimmed,
    requiredAndTrimmedAndNotValidIpAddress
} from "../../../../components/form/Form";
import LoadingSpinner from "../../../../components/list/LoadingSpinner";
import {Error} from "../../../../components/errors/Error";
import Field from "../../../../components/form/Field";
import Tabs, {Tab} from "../../../../components/tabs/Tabs";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../../reducers";
import {
    addEdgeHost,
    addEdgeHostRules,
    addEdgeHostSimulatedMetrics,
    loadEdgeHosts,
    loadRegions,
    updateEdgeHost
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import EdgeHostRuleList from "./EdgeHostRuleList";
import {IReply, postData} from "../../../../utils/api";
import GenericHostRuleList from "../GenericHostRuleList";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import EdgeHostSimulatedMetricList from "./EdgeHostSimulatedMetricList";
import GenericSimulatedHostMetricList from "../GenericSimulatedHostMetricList";
import {IRegion} from "../../regions/Region";
import {IWorkerManager} from "../../workerManagers/WorkerManager";
import {ICoordinates} from "../../../../components/map/LocationMap";
import EdgeHostSshCommand from "./EdgeHostSshCommand";
import EdgeHostSshFileTransfer from "./EdgeHostSshFileTransfer";

export interface IEdgeHost extends IDatabaseData {
    username: string;
    publicIpAddress: string;
    privateIpAddress: string;
    publicDnsName: string;
    region: IRegion;
    coordinates: ICoordinates;
    worker: IWorkerManager;
    managedByWorker: IWorkerManager;
    hostRules?: string[];
    hostSimulatedMetrics?: string[];
}

interface INewEdgeHost extends IEdgeHost {
    password: string;
}

const buildNewEdgeHost = (): Partial<INewEdgeHost> => ({
    username: undefined,
    password: undefined,
    privateIpAddress: undefined,
    publicIpAddress: undefined,
    publicDnsName: undefined,
    coordinates: undefined,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    edgeHost: Partial<IEdgeHost>;
    formEdgeHost?: Partial<IEdgeHost>;
    regions: { [key: string]: IRegion };
}

interface DispatchToProps {
    loadEdgeHosts: (hostname: string) => void;
    addEdgeHost: (edgeHost: IEdgeHost) => void;
    updateEdgeHost: (previousEdgeHost: IEdgeHost, currentEdgeHost: IEdgeHost) => void;
    addEdgeHostRules: (hostname: string, rules: string[]) => void;
    addEdgeHostSimulatedMetrics: (hostname: string, simulatedMetrics: string[]) => void;
    loadRegions: () => void;
}

interface MatchParams {
    hostname: string;
}

interface LocationState {
    data: IEdgeHost,
    selected: 'edge-host' | 'rules' | 'genericRules' | 'simulatedMetrics' | 'genericSimulatedMetrics' | 'ssh' | 'sftp',
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    edgeHost?: IEdgeHost,
    formEdgeHost?: IEdgeHost,
    unsavedRules: string[],
    unsavedSimulatedMetrics: string[],
}

class EdgeHost extends BaseComponent<Props, State> {

    state: State = {
        unsavedRules: [],
        unsavedSimulatedMetrics: [],
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadEdgeHost();
        this.props.loadRegions();
        this.mounted = true;
    };

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

    private loadEdgeHost = () => {
        if (!isNew(this.props.location.search)) {
            const hostname = this.props.match.params.hostname;
            this.props.loadEdgeHosts(hostname);
        }
    };

    private getEdgeHost = () =>
        this.props.edgeHost || this.state.edgeHost;

    private getFormEdgeHost = () =>
        this.props.formEdgeHost || this.state.formEdgeHost;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IEdgeHost>): void => {
        const edgeHost = reply.data;
        const hostname = edgeHost.publicIpAddress + "-" + edgeHost.privateIpAddress;
        super.toast(`<span class="green-text">O host ${this.mounted ? `<b class="white-text">${hostname}</b>` : `<a href='/hosts/edge/${hostname}'><b>${hostname}</b></a>`} foi configurado com sucesso</span>`);
        this.props.addEdgeHost(edgeHost);
        this.saveEntities(edgeHost);
        if (this.mounted) {
            this.updateEdgeHost(edgeHost);
            this.props.history.replace(hostname);
        }
    };

    private onPostFailure = (reason: string, edgeHost: IEdgeHost): void =>
        super.toast(`Não foi possível guardar o host <b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b>`, 10000, reason, true);

    /*private onPutSuccess = (reply: IReply<IEdgeHost>): void => {
        const edgeHost = reply.data;
        const hostname = edgeHost.publicIpAddress + '-' + edgeHost.privateIpAddress;
        super.toast(`<span class="green-text">As alterações ao host ${this.mounted ? `<b class="white-text">${hostname}</b>` : `<a href='/hosts/edge/${hostname}'><b>${hostname}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(edgeHost);
        const previousEdgeHost = this.getEdgeHost();
        if (previousEdgeHost.id) {
            this.props.updateEdgeHost(previousEdgeHost as IEdgeHost, edgeHost);
        }
        if (this.mounted) {
            this.updateEdgeHost(edgeHost);
            this.props.history.replace(hostname);
        }
    };*/

    /*private onPutFailure = (reason: string, edgeHost: IEdgeHost): void =>
        super.toast(`Não foi possível atualizar o host ${this.mounted ? `<b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b>` : `<a href='/hosts/edge/${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}'><b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b></a>`}`, 10000, reason, true);*/

    private onDeleteSuccess = (edgeHost: IEdgeHost): void => {
        super.toast(`<span class="green-text">O edge host <b class="white-text">${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b> foi removido com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/hosts/edge`)
        }
    };

    private onDeleteFailure = (reason: string, edgeHost: IEdgeHost): void =>
        super.toast(`Não foi possível remover o edge host ${this.mounted ? `<b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b>` : `<a href='/hosts/edge/${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}'><b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedRules.length
        || !!this.state.unsavedSimulatedMetrics.length;

    private saveEntities = (edgeHost: IEdgeHost) => {
        this.saveEdgeHostRules(edgeHost);
        this.saveEdgeHostSimulatedMetrics(edgeHost);
    };

    private addEdgeHostRule = (rule: string): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.concat(rule)
        });
    };

    private removeEdgeHostRules = (rules: string[]): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
        });
    };

    private saveEdgeHostRules = (edgeHost: IEdgeHost): void => {
        const {unsavedRules} = this.state;
        if (unsavedRules.length) {
            postData(`hosts/edge/${edgeHost.publicIpAddress}/${edgeHost.privateIpAddress}/rules`, unsavedRules,
                () => this.onSaveRulesSuccess(edgeHost),
                (reason) => this.onSaveRulesFailure(edgeHost, reason));
        }
    };

    private onSaveRulesSuccess = (edgeHost: IEdgeHost): void => {
        const hostname = edgeHost.publicIpAddress + '-' + edgeHost.privateIpAddress;
        this.props.addEdgeHostRules(hostname, this.state.unsavedRules);
        if (this.mounted) {
            this.setState({unsavedRules: []});
        }
    };

    private onSaveRulesFailure = (edgeHost: IEdgeHost, reason: string): void =>
        super.toast(`Não foi possível guardar as regras associadas ao host ${this.mounted ? `<b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b>` : `<a href='/hosts/edge/${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}'><b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b></a>`}`, 10000, reason, true);

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

    private saveEdgeHostSimulatedMetrics = (edgeHost: IEdgeHost): void => {
        const {unsavedSimulatedMetrics} = this.state;
        if (unsavedSimulatedMetrics.length) {
            postData(`hosts/edge/${edgeHost.publicIpAddress}/${edgeHost.privateIpAddress}/simulated-metrics`, unsavedSimulatedMetrics,
                () => this.onSaveSimulatedMetricsSuccess(edgeHost),
                (reason) => this.onSaveSimulatedMetricsFailure(edgeHost, reason));
        }
    };

    private onSaveSimulatedMetricsSuccess = (edgeHost: IEdgeHost): void => {
        const hostname = edgeHost.publicIpAddress + '-' + edgeHost.privateIpAddress;
        this.props.addEdgeHostSimulatedMetrics(hostname, this.state.unsavedSimulatedMetrics);
        if (this.mounted) {
            this.setState({unsavedSimulatedMetrics: []});
        }
    };

    private onSaveSimulatedMetricsFailure = (edgeHost: IEdgeHost, reason: string): void =>
        super.toast(`Não foi possível guardar as métricas simuladas associadas ao host ${this.mounted ? `<b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b>` : `<a href='/hosts/edge/${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}'><b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b></a>`}`, 10000, reason, true);

    private updateEdgeHost = (edgeHost: IEdgeHost) => {
        edgeHost = Object.values(normalize(edgeHost, Schemas.EDGE_HOST).entities.edgeHosts || {})[0];
        const formEdgeHost = {...edgeHost};
        removeFields(formEdgeHost);
        this.setState({edgeHost: edgeHost, formEdgeHost: formEdgeHost});
    };

    private getFields = (edgeHost: Partial<IEdgeHost>): IFields =>
        Object.entries(edgeHost).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation:
                        key.toLowerCase().includes('address')
                            ? {rule: requiredAndTrimmedAndNotValidIpAddress}
                            : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private managedByWorker = (worker: IWorkerManager) =>
        worker.id.toString();

    private regionOption = (region: IRegion) =>
        region.region;

    private edgeHost = () => {
        const {isLoading, error} = this.props;
        const edgeHost = this.getEdgeHost();
        const formEdgeHost = this.getFormEdgeHost();
        // @ts-ignore
        const edgeHostKey: (keyof IEdgeHost) = formEdgeHost && Object.keys(formEdgeHost)[0];
        const isNewEdgeHost = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formEdgeHost && (
                    /*@ts-ignore*/
                    <Form id={edgeHostKey}
                          fields={this.getFields(formEdgeHost)}
                          values={edgeHost}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'hosts/edge',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                        /*put={{
                            url: `hosts/edge/${edgeHost.publicIpAddress}/${edgeHost.privateIpAddress}`,
                            successCallback: this.onPutSuccess,
                            failureCallback: this.onPutFailure
                        }}*/
                          delete={{
                              confirmMessage: `apagar host ${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}`,
                              url: `hosts/edge/${edgeHost.publicIpAddress}/${edgeHost.privateIpAddress}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.keys(formEdgeHost).map((key, index) =>
                            key === 'password'
                                ? <Field key={index}
                                         id={key}
                                         label={key}
                                         hidden={true}/>
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
                                : key === 'coordinates'
                                    ? <Field key={index} id='coordinates' label='position' type='map'
                                             map={{
                                                 loading: isLoading,
                                                 editable: this.isNew(),
                                                 singleMarker: true,
                                                 zoomable: true,
                                                 labeled: true
                                             }}/>
                                    : key === 'managedByWorker'
                                        ? <Field<IWorkerManager> key={index}
                                                                 id={key}
                                                                 label={key}
                                                                 valueToString={this.managedByWorker}/>
                                        : <Field key={index}
                                                 id={key}
                                                 label={key}/>
                        )}
                    </Form>
                )}
            </>
        )
    };

    private rules = (): JSX.Element =>
        <EdgeHostRuleList isLoadingEdgeHost={this.props.isLoading}
                          loadEdgeHostError={this.props.error}
                          edgeHost={this.getEdgeHost()}
                          unsavedRules={this.state.unsavedRules}
                          onAddHostRule={this.addEdgeHostRule}
                          onRemoveHostRules={this.removeEdgeHostRules}/>;

    private genericRules = (): JSX.Element =>
        <GenericHostRuleList/>;

    private simulatedMetrics = (): JSX.Element =>
        <EdgeHostSimulatedMetricList isLoadingEdgeHost={this.props.isLoading}
                                     loadEdgeHostError={this.props.error}
                                     edgeHost={this.getEdgeHost()}
                                     unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                     onAddSimulatedHostMetric={this.addHostSimulatedMetric}
                                     onRemoveSimulatedHostMetrics={this.removeHostSimulatedMetrics}/>;

    private genericSimulatedMetrics = (): JSX.Element =>
        <GenericSimulatedHostMetricList/>;

    private ssh = (): JSX.Element =>
        <EdgeHostSshCommand edgeHost={this.getEdgeHost() as IEdgeHost}/>;

    private sftp = (): JSX.Element =>
        <EdgeHostSshFileTransfer edgeHost={this.getEdgeHost() as IEdgeHost}/>;

    private tabs = (): Tab[] => {
        const tabs = [
            {
                title: 'Host',
                id: 'edgeHost',
                content: () => this.edgeHost(),
                active: this.props.location.state?.selected === 'edge-host'
            },
            {
                title: 'Regras',
                id: 'rules',
                content: () => this.rules(),
                active: this.props.location.state?.selected === 'rules'
            },
            {
                title: 'Regras genéricas',
                id: 'genericRules',
                content: () => this.genericRules(),
                active: this.props.location.state?.selected === 'genericRules'
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
            },
        ];
        if (!this.isNew()) {
            tabs.push({
                title: 'Executar comandos',
                id: 'ssh',
                content: () => this.ssh(),
                active: this.props.location.state?.selected === 'ssh'
            });
            tabs.push({
                title: 'Carregar ficheiros',
                id: 'sftp',
                content: () => this.sftp(),
                active: this.props.location.state?.selected === 'sftp'
            });
        }
        return tabs;
    }

}

function removeFields(edgeHost: Partial<IEdgeHost>) {
    delete edgeHost["id"];
    delete edgeHost["worker"];
    if (!edgeHost.managedByWorker) {
        delete edgeHost["managedByWorker"];
    }
    delete edgeHost["hostRules"];
    delete edgeHost["hostSimulatedMetrics"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.hosts.edge.isLoadingHosts;
    const error = state.entities.hosts.edge.loadHostsError;
    const hostname = props.match.params.hostname;
    const edgeHost = isNew(props.location.search) ? buildNewEdgeHost() : state.entities.hosts.edge.data[hostname];
    let formEdgeHost;
    if (edgeHost) {
        formEdgeHost = {...edgeHost};
        removeFields(formEdgeHost);
    }
    return {
        isLoading,
        error,
        edgeHost,
        formEdgeHost,
        regions: state.entities.regions.data,
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadEdgeHosts,
    addEdgeHost,
    updateEdgeHost,
    addEdgeHostRules,
    addEdgeHostSimulatedMetrics,
    loadRegions
};

export default connect(mapStateToProps, mapDispatchToProps)(EdgeHost);