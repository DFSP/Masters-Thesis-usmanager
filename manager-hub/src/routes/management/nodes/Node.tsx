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
    ICustomButton,
    IFields,
    IFormLoading,
    required,
    requireGreaterOrEqualSize
} from "../../../components/form/Form";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addNodes, loadCloudHosts, loadEdgeHosts, loadNodes, loadRegions, updateNode} from "../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IRegion} from "../regions/Region";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IReply, postData, putData} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {ICloudHost} from "../hosts/cloud/CloudHost";
import NodeLabelsList from "./NodeLabelList";
import formStyles from "../../../components/form/Form.module.css";
import IDatabaseData from "../../../components/IDatabaseData";
import {Point} from "react-simple-maps";
import {ICoordinates} from "../../../components/map/LocationMap";
import {IMarker} from "../../../components/map/Marker";

export interface INode extends IDatabaseData {
    publicIpAddress: string;
    state: string;
    availability: string;
    role: string;
    version: number;
    coordinates?: ICoordinates
    labels: INodeLabel;
    managerStatus: IManagerStatus;
    managerId: string | null;
}

export interface INodeLabel {
    [key: string]: string
}

export interface IManagerStatus {
    leader: boolean;
    reachability: string;
    addr: string;
}

interface INewNodeHost {
    role?: string;
    host?: string;
}

interface INewNodeLocation {
    role?: string;
    coordinates?: Point[];
}

const buildNewNodeLocation = (): INewNodeLocation => ({
    role: undefined,
    coordinates: undefined,
});

const buildNewNodeHost = (): INewNodeHost => ({
    role: undefined,
    host: undefined,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newNodeHost?: INewNodeHost;
    newNodeLocation?: INewNodeLocation;
    node?: INode;
    formNode?: Partial<INode> & { coordinates?: ICoordinates };
    cloudHosts: { [key: string]: ICloudHost };
    edgeHosts: { [key: string]: IEdgeHost };
    regions: { [key: string]: IRegion };
    nodes: { [key: string]: INode };
}

interface DispatchToProps {
    loadNodes: (nodeId?: string) => void;
    addNodes: (nodes: INode[]) => void;
    updateNode: (previousNode: INode, currentNode: INode) => void;
    loadEdgeHosts: () => void;
    loadCloudHosts: () => void;
    loadRegions: () => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: INode,
    selected: 'node' | 'nodeLabels'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    node?: INode,
    formNode?: INode,
    loading: IFormLoading,
    currentForm: 'Num endereço' | 'Escolher localização',
    locations: Point[],
}

class Node extends BaseComponent<Props, State> {

    state: State = {
        loading: undefined,
        currentForm: 'Num endereço',
        locations: []
    };

    private mounted = false;

    public componentDidMount(): void {
        this.loadNode();
        this.props.loadNodes();
        this.props.loadEdgeHosts();
        this.props.loadCloudHosts();
        this.props.loadRegions();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                <div className='container'>
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadNode = () => {
        if (!isNew(this.props.location.search)) {
            const nodeId = this.props.match.params.id;
            this.props.loadNodes(nodeId);
        }
    };

    private getNode = () =>
        this.props.node || this.state.node;

    private getFormNode = () =>
        this.props.formNode || this.state.formNode;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<INode[]>): void => {
        let nodes = reply.data;
        nodes = nodes.map(node => addCoordinates(node));
        if (nodes.length === 1) {
            let node = nodes[0];
            super.toast(`<span class='green-text'>O host ${node.publicIpAddress} entrou no swarm com o id ${this.mounted ? `<b class='white-text'>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`}</span>`);
            if (this.mounted) {
                this.updateNode(node);
                this.props.history.replace(node.id.toString());
            }
        } else {
            super.toast(`<span class='green-text'>Os nós <b class='white-text'>${nodes.map(node => `${node.publicIpAddress} => ${node.id}`)}</b> entraram no swarm</span>`);
            this.props.history.push("/nós");
        }
        this.props.addNodes(nodes);
    };

    private onPostFailure = (reason: string, node: INewNodeHost | INewNodeLocation): void => {
        let message;
        if ("host" in node && node.host) {
            message = `Não foi possível começar o nó no host ${node.host}`;
        } else if ("coordinates" in node) {
            message = `Não foi possível começar um nó na localização ${node.coordinates}`;
        } else {
            message = `Não foi possível começar o nó`;
        }
        super.toast(message, 10000, reason, true);
    };

    private onPutSuccess = (reply: IReply<INode>): void => {
        let node = reply.data;
        node = addCoordinates(node);
        const previousNode = this.getNode();
        const previousAvailability = previousNode?.availability;
        const previousRole = previousNode?.role;
        if (node.availability !== previousAvailability) {
            super.toast(`<span class='green-text'>A disponibilidade do nó ${this.mounted ? `<b class='white-text'>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`} foi alterada para ${node.availability}</span>`);
        } else if (node.role !== previousRole) {
            super.toast(`<span class='green-text'>O nó ${this.mounted ? `<b class='white-text'>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`} foi ${previousRole === 'MANAGER' ? 'despromovido' : 'promovido'} a ${node.role}</span>`);
        } else {
            super.toast(`<span class='green-text'>As alterações ao nó ${this.mounted ? `<b class='white-text'>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`} foram guardadas com sucesso</span>`);
        }
        if (previousNode?.id) {
            this.props.updateNode(previousNode as INode, node)
        }
        if (this.mounted) {
            this.updateNode(node);
            this.props.history.replace(node.id.toString());
        }
    };

    private onPutFailure = (reason: string, node: INode): void =>
        super.toast(`Não foi possível mudar o cargo do nó ${this.mounted ? `<b>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (node: INode): void => {
        super.toast(`<span class='green-text'>O nó <b class='white-text'>${node.id}</b> ${node.state === 'down' ? 'foi removido com sucesso do swarm' : 'saiu do swarm com sucesso.'}</span>`);
        if (this.mounted) {
            this.props.history.push(`/nós`);
        }
    };

    private onDeleteFailure = (reason: string, node: INode): void => {
        if (node.state === 'active') {
            super.toast(`O nó ${this.mounted ? `<b>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`} não conseguiu sair do swarm`, 10000, reason, true);
        } else if (node.state === 'down') {
            super.toast(`Não foi possível remover o nó ${this.mounted ? `<b>${node.id}</b>` : `<a href='nós/${node.id}'><b>${node.id}</b></a>`} do swarm`, 10000, reason, true);
        }
    }

    private swarmButtons = (): ICustomButton[] => {
        const buttons: ICustomButton[] = [];
        const node = this.getNode();
        if (!this.isNew() && node
            && Object.values(this.props.cloudHosts)
                .filter(instance => instance.state.name === 'running')
                .map(instance => instance.publicIpAddress)
                .includes(node.publicIpAddress)
            && !((node as INode).labels?.['masterManager'] === 'true')) {
            if (node.state === 'down') {
                buttons.push({
                    button:
                        <button className={`btn-flat btn-small green-text ${formStyles.formButton}`}
                                onClick={this.rejoinSwarm}>
                            Re-entrar no swarm
                        </button>
                });
            } else if (node.state === 'ready') {
                buttons.push({
                    button:
                        <button className={`btn-flat btn-small blue-text ${formStyles.formButton}`}
                                onClick={this.leaveSwarm}>
                            Sair do swarm
                        </button>
                });
            }
        }
        return buttons;
    };

    private rejoinSwarm = () => {
        const node = this.getNode();
        const url = `nodes/${node?.id}/join`;
        this.setState({loading: {method: 'post', url: url}});
        postData(url, {},
            (reply: IReply<INode>) => this.onRejoinSwarmSuccess(reply.data),
            (reason: string) => this.onRejoinSwarmFailure(reason, node));
    };

    private onRejoinSwarmSuccess = (node: INode) => {
        node = addCoordinates(node);
        super.toast(`<span class='green-text'>O host </span> <b>${node?.publicIpAddress}</b> <span class='green-text'>re-entrou no swarm com o id </span> ${this.mounted ? `<b>${node?.id}</b>` : `<a href='/nós/${node?.id}'><b>${node?.id}</b></a>`}`);
        if (this.mounted) {
            this.setState({loading: undefined});
            this.updateNode(node);
            this.props.history.replace(node.id.toString());
        }
    };

    private onRejoinSwarmFailure = (reason: string, node?: INode) => {
        super.toast(`O nó ${this.mounted ? `<b>${node?.id}</b>` : `<a href='/nós/${node?.id}'><b>${node?.id}</b></a>`} não conseguiu re-entrar no swarm`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private leaveSwarm = () => {
        const node = this.getNode();
        const url = `nodes/${node?.publicIpAddress}/${node?.labels['privateIpAddress']}/leave`;
        this.setState({loading: {method: 'post', url: url}});
        putData(url, undefined,
            (reply: IReply<INode[]>) => this.onLeaveSuccess(reply.data),
            (reason) => this.onLeaveFailure(reason, node));
    };

    private onLeaveSuccess = (nodes: INode[]) => {
        let node = nodes[0];
        node = addCoordinates(node);
        super.toast(`<span class='green-text'>O nó <b class='white-text'>${node.id}</b> saiu com sucesso do swarm</span>`);
        const previousNode = this.getNode();
        if (previousNode?.id) {
            this.props.updateNode(previousNode as INode, node)
        }
        if (this.mounted) {
            this.updateNode(node);
        }
    };

    private onLeaveFailure = (reason: string, node?: INode) => {
        super.toast(`O nó ${this.mounted ? `<b>${node?.id}</b>` : `<a href='/nós/${node?.id}'><b>${node?.id}</b></a>`} não conseguiu sair do swarm`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private updateNode = (node: INode) => {
        node = Object.values(normalize(node, Schemas.NODE).entities.nodes || {})[0];
        const formNode = {...node};
        removeFields(formNode);
        this.setState({node: node, formNode: formNode, loading: undefined});
    };

    private getFields = (node: INewNodeHost | INewNodeLocation | INode): IFields =>
        Object.keys(node).map(key => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: key === 'coordinates'
                        ? {rule: requireGreaterOrEqualSize, args: 1}
                        : {rule: required}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private getSelectableHosts = () => {
        const nodesHostname = Object.values(this.props.nodes).map(node => node.publicIpAddress);
        const cloudHosts = Object.values(this.props.cloudHosts)
            .filter(instance => !nodesHostname.includes(instance.publicIpAddress))
            .filter(instance => instance.state.name === 'running' || instance.state.name === 'stopped')
            .map(instance => instance.publicIpAddress || instance.instanceId);
        const edgeHosts = Object.entries(this.props.edgeHosts)
            .filter(([_, edgeHost]) => !nodesHostname.includes(edgeHost.publicIpAddress))
            .map(([hostname, _]) => hostname);
        return cloudHosts.concat(edgeHosts);
    };

    private getNodesMarkers = (): IMarker[] => {
        const nodes: INode[] = Object.values(this.props.nodes);
        const markers = new Map<string, IMarker>();
        nodes.forEach(node => {
            const id = node.id.toString();
            const markerId = node.labels['coordinates'];
            const coordinates = JSON.parse(node.labels['coordinates']) as ICoordinates;
            const marker = markers.get(markerId) || {title: '', label: '', latitude: 0, longitude: 0};
            if (marker.title === '') {
                marker.title += coordinates.label + '<br/>';
            }
            marker.title += id + '<br/>' + node.publicIpAddress + '/' + node.labels['privateIpAddress'] + '<br/>';
            marker.label = id;
            marker.latitude = coordinates.latitude;
            marker.longitude = coordinates.longitude;
            marker.color = 'green';
            markers.set(markerId, marker);
        });
        return Array.from(markers.values());
    }

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

    private formFields = (isNew: boolean, formNode?: Partial<INode>, node?: INode | INewNodeHost | INewNodeLocation) => {
        const {currentForm} = this.state;
        return (
            isNew ?
                currentForm === 'Num endereço'
                    ?
                    <>
                        <Field key={'role'}
                               id={'role'}
                               label={'role'}
                               type='dropdown'
                               dropdown={{
                                   defaultValue: "Selecionar o cargo",
                                   values: ['MANAGER', 'WORKER']
                               }}/>
                        <Field<string> key={'host'}
                                       id={'host'}
                                       label={'host'}
                                       type='dropdown'
                                       dropdown={{
                                           defaultValue: "Selecionar o host",
                                           values: this.getSelectableHosts(),
                                           emptyMessage: 'Náo há hosts disponíveis'
                                       }}/>
                    </>
                    :
                    <>
                        <Field key={'role'}
                               id={'role'}
                               label={'role'}
                               type='dropdown'
                               dropdown={{
                                   defaultValue: "Selecionar o cargo",
                                   values: ['MANAGER', 'WORKER']
                               }}/>
                        <Field key='coordinates' id='coordinates' label='select position(s)' type='map'
                               map={{
                                   loading: this.props.isLoading,
                                   editable: true,
                                   labeled: false,
                                   markers: this.getNodesMarkers()
                               }}/>
                    </>
                :
                formNode && Object.entries(formNode).map(([key, value], index) =>
                    key === 'availability'
                        ? <Field key={'availability'}
                                 id={'availability'}
                                 label={'availability'}
                                 type='dropdown'
                                 dropdown={{
                                     defaultValue: "Selecionar a disponibilidade",
                                     values: ['ACTIVE', 'PAUSE', 'DRAIN']
                                 }}/>
                        : key === 'role' && formNode.state !== 'down'
                        ? <Field key={'role'}
                                 id={'role'}
                                 label={'role'}
                                 type='dropdown'
                                 dropdown={{
                                     defaultValue: "Selecionar o cargo",
                                     values: ['MANAGER', 'WORKER']
                                 }}
                                 disabled={Object.values(this.props.nodes).filter(node => node.role === 'MANAGER').length === 1 && formNode.role === 'MANAGER'
                                 || (node && 'labels' in node && (node as INode).labels?.['masterManager'] === 'true')}/>
                        : key === 'publicIpAddress'
                            ? <Field key={index}
                                     id={key}
                                     label={key}
                                     icon={{linkedTo: this.hostLink}}
                                     disabled={true}/>
                            : key === 'coordinates'
                                ? <Field key={index} id='coordinates' label='location' type='map'
                                         map={{
                                             loading: this.props.isLoading,
                                             editable: false,
                                             zoomable: true,
                                             labeled: true
                                         }}/>
                                : key === 'managerId'
                                    ? <Field key={index}
                                             id={key}
                                             label={key}
                                             icon={{linkedTo: this.managerLink}}/>
                                    : <Field key={index}
                                             id={key}
                                             label={key}
                                             disabled={true}/>)
        );
    };

    private switchForm = (formId: 'Num endereço' | 'Escolher localização') =>
        this.setState({currentForm: formId});

    private onPostReply = (nodes: INode[]) => {
        let node = nodes[0];
        node = addCoordinates(node);
        return node;
    }

    private onPutReply = (node: INode) => {
        return addCoordinates(node);
    }

    private managerLink = (managerId: string) => {
        if (!!managerId && managerId !== 'manager-master') {
            return `/gestores locais/${managerId}`
        }
        return null;
    }

    private node = () => {
        const {isLoading, error, newNodeHost, newNodeLocation} = this.props;
        const {currentForm, loading} = this.state;
        const isNewNode = this.isNew();
        const node = isNewNode ? (currentForm === 'Num endereço' ? newNodeHost : newNodeLocation) : this.getNode();
        const formNode = this.getFormNode();
        // @ts-ignore
        const nodeKey: (keyof INode) = formNode && Object.keys(formNode)[0];
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && node && (
                    <>
                        {/*@ts-ignore*/}
                        <Form id={nodeKey}
                              fields={this.getFields(formNode || node)}
                              values={node}
                              isNew={isNewNode}
                              loading={loading}
                              post={{
                                  textButton: isNewNode ? 'Entrar no swarm' : 'Guardar',
                                  url: 'nodes',
                                  successCallback: this.onPostSuccess,
                                  failureCallback: this.onPostFailure,
                                  result: this.onPostReply,
                              }}
                            // modify button is never present on new nodes, so a type cast is safe
                              put={(node as INode).state === 'down'
                                  ? undefined
                                  : {
                                      url: `nodes/${(node as INode).id}`,
                                      successCallback: this.onPutSuccess,
                                      failureCallback: this.onPutFailure,
                                      result: this.onPutReply,
                                  }}
                            // delete button is never present on new nodes, so a type cast is safe
                              delete={Object.values(this.props.nodes).filter(node => node.role === 'MANAGER').length === 1 && node.role === 'MANAGER'
                              || (node as INode).labels?.['masterManager'] === 'true' || (node as INode).state !== 'down'
                                  ? undefined
                                  : {
                                      textButton: 'Remover do swarm',
                                      confirmMessage: `remover o nó ${(node as INode).id} do swarm`,
                                      url: `nodes/${(node as INode).id}`,
                                      successCallback: this.onDeleteSuccess,
                                      failureCallback: this.onDeleteFailure
                                  }}
                              switchDropdown={isNewNode ? {
                                  options: currentForm === 'Num endereço' ? ['Escolher localização'] : ['Num endereço'],
                                  onSwitch: this.switchForm
                              } : undefined}
                              customButtons={this.swarmButtons()}>
                            {this.formFields(isNewNode, formNode, node)}
                        </Form>
                    </>
                )}
            </>
        )
    };

    private labels = (): JSX.Element =>
        <NodeLabelsList isLoadingNode={this.props.isLoading}
                        loadNodeError={this.props.error}
                        node={this.getNode()}/>;

    private tabs = (): Tab[] => ([
        {
            title: 'Nó',
            id: 'node',
            content: () => this.node(),
            active: this.props.location.state?.selected === 'node'
        },
        {
            title: 'Labels',
            id: 'nodeLabels',
            content: () => this.labels(),
            hidden: this.isNew(),
            active: this.props.location.state?.selected === 'nodeLabels'
        }
    ]);

}

function removeFields(node: Partial<INode>) {
    delete node["id"];
    delete node["labels"];
    delete node["managerStatus"];
}

function addCoordinates(node: INode) {
    if (node.labels['coordinates']) {
        node = {...node, coordinates: JSON.parse(node.labels['coordinates'])};
    }
    return node;
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.nodes.isLoadingNodes;
    const error = state.entities.nodes.loadNodesError;
    const id = props.match.params.id.split('#')[0];
    const newNodeHost = isNew(props.location.search) ? buildNewNodeHost() : undefined;
    const newNodeLocation = isNew(props.location.search) ? buildNewNodeLocation() : undefined;
    let node: INode & { coordinates?: ICoordinates } | undefined = !isNew(props.location.search) ? state.entities.nodes.data[id] : undefined;
    let formNode;
    if (node) {
        node = addCoordinates(node);
        formNode = {...node};
        removeFields(formNode);
    }
    const nodes = state.entities.nodes.data;
    const cloudHosts = state.entities.hosts.cloud.data;
    const edgeHosts = state.entities.hosts.edge.data;
    const regions = state.entities.regions.data;
    return {
        isLoading,
        error,
        newNodeHost,
        newNodeLocation,
        node,
        formNode,
        nodes,
        cloudHosts,
        edgeHosts,
        regions
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadNodes,
    addNodes,
    updateNode,
    loadCloudHosts,
    loadEdgeHosts,
    loadRegions,
};

export default connect(mapStateToProps, mapDispatchToProps)(Node);