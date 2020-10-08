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
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addEurekaServer, loadEurekaServers, loadRegions,} from "../../../actions";
import {connect} from "react-redux";
import {IRegion} from "../region/Region";
import {IReply} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {IContainer} from "../containers/Container";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";

export interface IEurekaServer extends IContainer {
}

interface INewEurekaServer {
    regions: string[] | undefined
}

const buildNewEurekaServer = (): INewEurekaServer => ({
    regions: undefined
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newEurekaServer?: INewEurekaServer;
    eurekaServer?: IEurekaServer;
    formEurekaServer?: Partial<IEurekaServer> | INewEurekaServer;
    regions: { [key: string]: IRegion };
}

interface DispatchToProps {
    loadEurekaServers: (id: string) => void;
    addEurekaServer: (eurekaServer: IContainer) => void;
    loadRegions: () => void;
}

interface MatchParams {
    id: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
    eurekaServer?: IEurekaServer,
    formEurekaServer?: IEurekaServer,
}

class EurekaServer extends BaseComponent<Props, State> {

    state: State = {};
    private mounted = false;

    public componentDidMount(): void {
        this.loadEurekaServer();
        this.props.loadRegions();
        this.mounted = true;
    }

    componentWillUnmount(): void {
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
        this.state.eurekaServer || this.props.eurekaServer;

    private getFormEurekaServer = () =>
        this.state.formEurekaServer || this.props.formEurekaServer;

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
        super.toast(`Unable to stop ${this.mounted ? `<b>${eurekaServer.containerId}</b>` : `<a href=/eureka-servers/${eurekaServer.containerId}><b>${eurekaServer.containerId}</b></a>`} eureka server`, 10000, reason, true);

    private updateEurekaServer = (eurekaServer: IEurekaServer) => {
        eurekaServer = Object.values(normalize(eurekaServer, Schemas.EUREKA_SERVER).entities.eurekaServers || {})[0];
        const formEurekaServer = {...eurekaServer};
        removeFields(formEurekaServer);
        this.setState({eurekaServer: eurekaServer, formEurekaServer: formEurekaServer});
    };

    private getFields = (eurekaServer: Partial<IEurekaServer> | INewEurekaServer): IFields =>
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

    private eurekaServer = () => {
        const {isLoading, error, newEurekaServer} = this.props;
        const eurekaServer = this.getEurekaServer();
        const formEurekaServer = this.getFormEurekaServer();
        // @ts-ignore
        const eurekaServerKey: (keyof IEurekaServer) = formEurekaServer && Object.keys(formEurekaServer)[0];
        const isNewEurekaServer = this.isNew();
        return (
            <>
                {!isNewEurekaServer && isLoading && <ListLoadingSpinner/>}
                {!isNewEurekaServer && !isLoading && error && <Error message={error}/>}
                {(isNewEurekaServer || !isLoading) && (isNewEurekaServer || !error) && formEurekaServer && (
                    <Form id={eurekaServerKey}
                          fields={this.getFields(formEurekaServer || {})}
                          values={eurekaServer || newEurekaServer || {}}
                          isNew={isNew(this.props.location.search)}
                          post={{
                              textButton: 'launch',
                              url: 'eureka-server',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          delete={{
                              textButton: 'Stop',
                              url: `containers/${eurekaServer?.containerId}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}>
                        {Object.entries(formEurekaServer).map((([key, value], index) =>
                                key === 'regions'
                                    ? <Field key={index}
                                             id={key}
                                             label={key}
                                             type={'list'}
                                             value={value}/>
                                    : <Field key={index}
                                             id={key}
                                             label={key}/>
                        ))}
                    </Form>
                )}
            </>
        )
    };

    private tabs = (): Tab[] => [
        {
            title: 'Eureka Server',
            id: 'eurekaServer',
            content: () => this.eurekaServer()
        },
    ];

}

function removeFields(eurekaServer: Partial<IEurekaServer>) {
    delete eurekaServer["id"];
    delete eurekaServer["ports"];
    delete eurekaServer["labels"];
    delete eurekaServer["logs"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.eurekaServers.isLoadingEurekaServers;
    const error = state.entities.eurekaServers.loadEurekaServersError;
    const id = props.match.params.id;
    const newEurekaServer = isNew(props.location.search) ? buildNewEurekaServer() : undefined;
    const eurekaServer = !isNew(props.location.search) ? state.entities.eurekaServers.data[id] : undefined;
    const regions = state.entities.regions.data;
    let formEurekaServer;
    if (newEurekaServer) {
        formEurekaServer = {...newEurekaServer, regions: Object.keys(regions)};
    }
    if (eurekaServer) {
        formEurekaServer = {...eurekaServer};
        removeFields(formEurekaServer);
    }
    return {
        isLoading,
        error,
        newEurekaServer,
        eurekaServer,
        formEurekaServer,
        regions
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadEurekaServers,
    addEurekaServer,
    loadRegions,
};

export default connect(mapStateToProps, mapDispatchToProps)(EurekaServer);
