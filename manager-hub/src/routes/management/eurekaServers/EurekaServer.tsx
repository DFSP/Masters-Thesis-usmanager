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
import {addEurekaServer, loadEurekaServers, loadNodes, loadRegions,} from "../../../actions";
import {connect} from "react-redux";
import {IRegion} from "../regions/Region";
import {IReply} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {IContainer} from "../containers/Container";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IHostAddress} from "../hosts/Hosts";
import {INode} from "../nodes/Node";

export interface IEurekaServer extends IContainer {
}

interface INewEurekaServerRegion {
    regions: string[] | undefined
}

interface INewEurekaServerHost {
    hostAddress: IHostAddress | undefined
}

const buildNewEurekaServerRegion = (): INewEurekaServerRegion => ({
    regions: undefined
});

const buildNewEurekaServerHost = (): INewEurekaServerHost => ({
    hostAddress: undefined
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newEurekaServerHost?: INewEurekaServerHost;
    newEurekaServerRegion?: INewEurekaServerRegion;
    eurekaServer?: IEurekaServer;
    formEurekaServer?: Partial<IEurekaServer>;
    regions: { [key: string]: IRegion };
    nodes: { [key: string]: INode };
}

interface DispatchToProps {
    loadEurekaServers: (id: string) => void;
    addEurekaServer: (eurekaServer: IContainer) => void;
    loadRegions: () => void;
    loadNodes: () => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: IEurekaServer,
    selected: 'eurekaServer';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    eurekaServer?: IEurekaServer,
    formEurekaServer?: IEurekaServer,
    currentForm: 'On regions' | 'On host',
}

class EurekaServer extends BaseComponent<Props, State> {

    state: State = {
        currentForm: 'On regions'
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadEurekaServer();
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

    private loadEurekaServer = () => {
        if (!isNew(this.props.location.search)) {
            const eurekaServerId = this.props.match.params.id;
            this.props.loadEurekaServers(eurekaServerId);
        }
    };

    private getEurekaServer = () =>
        this.props.eurekaServer || this.state.eurekaServer;

    private getFormEurekaServer = () =>
        this.props.formEurekaServer || this.state.formEurekaServer;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IEurekaServer[]>): void => {
        const eurekaServers = reply.data;
        eurekaServers.forEach(eurekaServer => {
            super.toast(`<span class="green-text">Eureka server ${this.mounted ? `<b class="white-text">${eurekaServer.containerId}</b>` : `<a href=/eureka-servers/${eurekaServer.containerId}><b>${eurekaServer.containerId}</b></a>`} launched</span>`);
            this.props.addEurekaServer(eurekaServer);
        });
        if (this.mounted) {
            if (eurekaServers.length === 1) {
                const eurekaServer = eurekaServers[0];
                this.updateEurekaServer(eurekaServer);
                this.props.history.replace(eurekaServer.containerId)
            } else {
                this.props.history.push('/eureka-servers');
            }
        }
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Unable to launch eureka server`, 10000, reason, true);

    private onDeleteSuccess = (eurekaServer: IEurekaServer): void => {
        super.toast(`<span class="green-text">Eureka server <b class="white-text">${eurekaServer.containerId}</b> successfully stopped</span>`);
        if (this.mounted) {
            this.props.history.push(`/eureka-servers`)
        }
    };

    private onDeleteFailure = (reason: string, eurekaServer: IEurekaServer): void =>
        super.toast(`Unable to stop eureka-server ${this.mounted ? `<b>${eurekaServer.containerId}</b>` : `<a href=/eureka-servers/${eurekaServer.containerId}><b>${eurekaServer.containerId}</b></a>`}`, 10000, reason, true);

    private updateEurekaServer = (eurekaServer: IEurekaServer) => {
        eurekaServer = Object.values(normalize(eurekaServer, Schemas.EUREKA_SERVER).entities.eurekaServers || {})[0];
        const formEurekaServer = {...eurekaServer};
        removeFields(formEurekaServer);
        this.setState({eurekaServer: eurekaServer, formEurekaServer: formEurekaServer});
    };

    private getFields = (eurekaServer: INewEurekaServerRegion | INewEurekaServerHost | IEurekaServer): IFields =>
        Object.entries(eurekaServer).map(([key, value]) => {
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

    private formFields = (isNew: boolean, formEurekaServer?: Partial<IEurekaServer>) => {
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
                : formEurekaServer && Object.entries(formEurekaServer).map((([key, value], index) =>
                key === 'containerId'
                    ? <Field key={index}
                             id={key}
                             label={key}
                             icon={{linkedTo: '/containers/' + (formEurekaServer as Partial<IEurekaServer>).containerId}}/>
                    : key === 'created'
                    ? <Field key={index}
                             id={key}
                             label={key}
                             type={"date"}/>
                    : <Field key={index}
                             id={key}
                             label={key}/>))
        );
    };

    private switchForm = (formId: 'On regions' | 'On host') =>
        this.setState({currentForm: formId});

    private eurekaServer = () => {
        const {isLoading, error, newEurekaServerRegion, newEurekaServerHost} = this.props;
        const {currentForm} = this.state;
        const isNewEurekaServer = this.isNew();
        const eurekaServer = isNewEurekaServer ? (currentForm === 'On regions' ? newEurekaServerRegion : newEurekaServerHost) : this.getEurekaServer();
        const formEurekaServer = this.getFormEurekaServer();
        // @ts-ignore
        const eurekaServerKey: (keyof IEurekaServer) = formEurekaServer && Object.keys(formEurekaServer)[0];
        return (
            <>
                {!isNewEurekaServer && isLoading && <LoadingSpinner/>}
                {!isNewEurekaServer && !isLoading && error && <Error message={error}/>}
                {(isNewEurekaServer || !isLoading) && (isNewEurekaServer || !error) && eurekaServer && (
                    /*@ts-ignore*/
                    <Form id={eurekaServerKey}
                          fields={this.getFields(eurekaServer)}
                          values={eurekaServer}
                          isNew={isNew(this.props.location.search)}
                          post={{
                              textButton: 'launch',
                              url: 'eureka-server',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          delete={{
                              textButton: 'Stop',
                              url: `containers/${(eurekaServer as IEurekaServer).containerId}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          switchDropdown={isNewEurekaServer ? {
                              options: currentForm === 'On regions' ? ['On host'] : ['On regions'],
                              onSwitch: this.switchForm
                          } : undefined}>
                        {this.formFields(isNewEurekaServer, formEurekaServer)}
                    </Form>
                )}
            </>
        )
    };

    private tabs = (): Tab[] => [
        {
            title: 'Eureka Server',
            id: 'eurekaServer',
            content: () => this.eurekaServer(),
            active: this.props.location.state?.selected === 'eurekaServer'
        },
    ];

}

function removeFields(eurekaServer: Partial<IEurekaServer>) {
    delete eurekaServer["id"];
    delete eurekaServer["ports"];
    delete eurekaServer["labels"];
    delete eurekaServer["logs"];
    delete eurekaServer["coordinates"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.eurekaServers.isLoadingEurekaServers;
    const error = state.entities.eurekaServers.loadEurekaServersError;
    const id = props.match.params.id;
    const newEurekaServer = isNew(props.location.search);
    const newEurekaServerRegion = newEurekaServer ? buildNewEurekaServerRegion() : undefined;
    const newEurekaServerHost = newEurekaServer ? buildNewEurekaServerHost() : undefined;
    const eurekaServer = !newEurekaServer ? state.entities.eurekaServers.data[id] : undefined;
    let formEurekaServer;
    if (eurekaServer) {
        formEurekaServer = {...eurekaServer};
        removeFields(formEurekaServer);
    }
    const regions = state.entities.regions.data;
    const nodes = state.entities.nodes.data;
    return {
        isLoading,
        error,
        newEurekaServerRegion,
        newEurekaServerHost,
        eurekaServer,
        formEurekaServer,
        regions,
        nodes
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadEurekaServers,
    addEurekaServer,
    loadRegions,
    loadNodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(EurekaServer);
