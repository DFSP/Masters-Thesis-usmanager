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
    requiredAndNumberAndMin,
    requiredAndTrimmed,
    trimmed
} from "../../../components/form/Form";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addNode, loadCloudHosts, loadEdgeHosts, loadNodes, loadRegions, updateNode} from "../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IRegion} from "../region/Region";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IReply, postData} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {ICloudHost} from "../hosts/cloud/CloudHost";
import NodeLabelsList from "./NodeLabelList";
import formStyles from "../../../components/form/Form.module.css";
import IDatabaseData from "../../../components/IDatabaseData";
import LocationSelectorMap, {ICoordinates} from "../../../components/map/LocationSelectorMap";

export interface INode extends IDatabaseData {
    publicIpAddress: string;
    state: string;
    availability: string;
    role: string;
    version: number;
    labels: INodeLabel;
}

export interface INodeLabel {
    [key: string]: string
}

interface INewNodeHost {
    host?: string;
    role?: string;
}

interface INewNodeLocation {
    region?: IRegion,
    country?: string,
    city?: string,
    role?: string;
    quantity: number,
}

const buildNewNodeHost = (): INewNodeHost => ({
    host: undefined,
    role: undefined,
});

const buildNewNodeLocation = (): INewNodeLocation => ({
    region: undefined,
    country: undefined,
    city: undefined,
    role: undefined,
    quantity: 1,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newNodeHost?: INewNodeHost;
    newNodeLocation?: INewNodeLocation;
    node?: INode;
    formNode?: Partial<INode>;
    cloudHosts: { [key: string]: ICloudHost };
    edgeHosts: { [key: string]: IEdgeHost };
    regions: { [key: string]: IRegion };
    nodes: { [key: string]: INode };
}

interface DispatchToProps {
    loadNodes: (nodeId: string) => void;
    addNode: (node: INode) => void;
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
    selected: 'newNode' | 'node' | 'nodeLabels'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    node?: INode,
    formNode?: INode,
    loading: IFormLoading,
    currentForm: 'On host' | 'On location',
}

class Node extends BaseComponent<Props, State> {

    state: State = {
        loading: undefined,
        currentForm: 'On host'
    };

    private mounted = false;

    public componentDidMount(): void {
        this.loadNode();
        this.props.loadEdgeHosts();
        this.props.loadCloudHosts();
        this.props.loadRegions();
        this.mounted = true;
    };

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

    private loadNode = () => {
        if (!isNew(this.props.location.search)) {
            const nodeId = this.props.match.params.id;
            this.props.loadNodes(nodeId);
        }
    };

    private getNode = () =>
        this.state.node || this.props.node;

    private getFormNode = () =>
        this.state.formNode || this.props.formNode;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<INode[]>): void => {
        const nodes = reply.data;
        if (nodes.length === 1) {
            const node = nodes[0];
            super.toast(`<span class="green-text">Node ${this.mounted ? `<b class="white-text">${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`} at ${node.publicIpAddress} has joined the swarm</span>`);
            this.props.addNode(node);
            if (this.mounted) {
                this.updateNode(node);
                this.props.history.replace(node.id.toString());
            }
        } else {
            super.toast(`<span class="green-text">Nodes <b class="white-text">${nodes.map(node => `${node.publicIpAddress} => ${node.id}`)}</b> have joined the swarm</span>`);
            this.props.history.push("/nodes");
        }

    };

    private onPostFailure = (reason: string, place: INewNodeHost | INewNodeLocation): void => {
        let message;
        if ("host" in place && place.host) {
            message = `Unable to start node at ${place.host}`;
        } else if ("city" in place) {
            message = `Unable to start node at ${place.city}`;
        } else {
            message = `Unable to start node`;
        }
        super.toast(message, 10000, reason, true);
    };

    private onPutSuccess = (reply: IReply<INode>): void => {
        const node = reply.data;
        const previousNode = this.getNode();
        const previousAvailability = previousNode?.availability;
        const previousRole = previousNode?.role;
        if (node.availability !== previousAvailability) {
            super.toast(`<span class="green-text">Node ${this.mounted ? `<b class="white-text">${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`} availability has been changed to ${node.availability}</span>`);
        } else if (node.role !== previousRole) {
            super.toast(`<span class="green-text">Node ${this.mounted ? `<b class="white-text">${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`} has been ${previousRole === 'MANAGER' ? 'demoted' : 'promoted'} to ${node.role}</span>`);
        } else {
            super.toast(`<span class="green-text">Changes to node ${this.mounted ? `<b class="white-text">${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`} have been saved</span>`);
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
        super.toast(`Unable to change role of node ${this.mounted ? `<b>${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (node: INode): void => {
        super.toast(`<span class="green-text">Host <b class="white-text">${node.publicIpAddress}</b> ${node.state === 'down' ? 'successfully removed from the swarm' : 'left the swarm. Takes a few seconds to update.'}</span>`);
        if (this.mounted) {
            this.props.history.push(`/nodes`);
        }
    };

    private onDeleteFailure = (reason: string, node: INode): void => {
        if (node.state === 'active') {
            super.toast(`Node ${this.mounted ? `<b>${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`} was unable to leave the swarm`, 10000, reason, true);
        } else if (node.state === 'down') {
            super.toast(`Unable to remove node ${this.mounted ? `<b>${node.id}</b>` : `<a href=/nodes/${node.id}><b>${node.id}</b></a>`} from the swarm`, 10000, reason, true);
        }
    }

    private rejoinSwarmButton = (): ICustomButton[] => {
        const buttons: ICustomButton[] = [];
        buttons.push({
            button:
                <button className={`btn-flat btn-small waves-effect waves-light green-text ${formStyles.formButton}`}
                        onClick={this.rejoinSwarm}>
                    Rejoin swarm
                </button>
        });
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
        super.toast(`<span class="green-text">Host</span> <b>${node?.publicIpAddress}</b> <span class="green-text">successfully rejoined the swarm as node</span> ${this.mounted ? `<b>${node?.id}</b>` : `<a href=/nodes/${node?.id}><b>${node?.id}</b></a>`}`);
        if (this.mounted) {
            this.setState({loading: undefined});
            this.updateNode(node);
            this.props.history.replace(node.id.toString());
        }
    };

    private onRejoinSwarmFailure = (reason: string, node?: INode) => {
        super.toast(`Node ${this.mounted ? `<b>${node?.id}</b>` : `<a href=/nodes/${node?.id}><b>${node?.id}</b></a>`} failed to rejoin the swarm`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private updateNode = (node: INode) => {
        node = Object.values(normalize(node, Schemas.NODE).entities.nodes || {})[0];
        const formNode = {...node};
        removeFields(formNode);
        this.setState({node: node, formNode: formNode});
    };

    private getFields = (node: INewNodeHost | INewNodeLocation | INode): IFields =>
        Object.entries(node).map(([key, value]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: getTypeFromValue(value) === 'number'
                        ? {rule: requiredAndNumberAndMin, args: 1}
                        : key === 'country' || key === 'city'
                            ? {rule: trimmed}
                            : {rule: requiredAndTrimmed}
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

    private regionDropdownOption = (region: IRegion) =>
        region.name;

    private hostnameLink = (hostname: string) => {
        if (Object.values(this.props.cloudHosts).map(c => c.publicIpAddress).includes(hostname)) {
            return '/hosts/cloud';
        }
        if (Object.values(this.props.edgeHosts).map(e => e.publicIpAddress).includes(hostname)) {
            return '/hosts/edge';
        }
        return null;
    }

    private formFields = (isNew: boolean) => {
        const formNode = this.getFormNode();
        const {currentForm} = this.state;
        return (
            isNew ?
                currentForm === 'On host'
                    ?
                    <>
                        <Field<string> key={'host'}
                                       id={'host'}
                                       label={'host'}
                                       type="dropdown"
                                       dropdown={{
                                           defaultValue: "Select host",
                                           values: this.getSelectableHosts()
                                       }}/>
                        <Field key={'role'}
                               id={'role'}
                               label={'role'}
                               type="dropdown"
                               dropdown={{
                                   defaultValue: "Select role",
                                   values: ['MANAGER', 'WORKER']
                               }}/>
                    </>
                    :
                    <>
                        <Field<IRegion> key={'region'}
                                        id={'region'}
                                        label={'region'}
                                        type="dropdown"
                                        dropdown={{
                                            defaultValue: "Select region",
                                            values: Object.values(this.props.regions),
                                            optionToString: this.regionDropdownOption
                                        }}/>
                        <Field key={'country'}
                               id={'country'}
                               label={'country'}/>
                        <Field key={'city'}
                               id={'city'}
                               label={'city'}/>
                        <Field key={'role'}
                               id={'role'}
                               label={'role'}
                               type="dropdown"
                               dropdown={{
                                   defaultValue: "Select role",
                                   values: ['MANAGER', 'WORKER']
                               }}/>
                        <Field key={'quantity'}
                               id={'quantity'}
                               label={'quantity'}
                               type={"number"}/>
                    </>
                :
                formNode && Object.entries(formNode).map(([key, value], index) =>
                    key === 'availability'
                        ? <Field key={'availability'}
                                 id={'availability'}
                                 label={'availability'}
                                 type="dropdown"
                                 dropdown={{
                                     defaultValue: "Select availability",
                                     values: ['ACTIVE', 'PAUSE', 'DRAIN']
                                 }}/>
                        : key === 'role' && formNode.state !== 'down'
                        ? <Field key={'role'}
                                 id={'role'}
                                 label={'role'}
                                 type="dropdown"
                                 dropdown={{
                                     defaultValue: "Select role",
                                     values: ['MANAGER', 'WORKER']
                                 }}/>
                        : key === 'hostname'
                            ? <Field key={index}
                                     id={key}
                                     label={key}
                                     icon={{linkedTo: this.hostnameLink}}/>
                            : <Field key={index}
                                     id={key}
                                     label={key}
                                     disabled={true}/>)
        );
    };

    private switchForm = (formId: 'On host' | 'On location') =>
        this.setState({currentForm: formId});

    private showRejoinSwarmButton = (node: INode): boolean =>
        !this.isNew()
        && node.state === 'down'
        && Object.values(this.props.cloudHosts)
            .filter(instance => instance.state.name === 'running')
            .map(instance => instance.publicIpAddress)
            .includes(node.publicIpAddress);

    private node = () => {
        const {isLoading, error, newNodeHost, newNodeLocation} = this.props;
        const {currentForm, loading} = this.state;
        const isNewNode = this.isNew();
        const node = isNewNode ? (currentForm === 'On host' ? newNodeHost : newNodeLocation) : this.getNode();
        // @ts-ignore
        const nodeKey: (keyof INode) = node && Object.keys(node)[0];
        return (
            <>
                {!isNewNode && isLoading && <ListLoadingSpinner/>}
                {!isNewNode && !isLoading && error && <Error message={error}/>}
                {(isNewNode || !isLoading) && (isNewNode || !error) && node && (
                    <>
                        {/*@ts-ignore*/}
                        <Form id={nodeKey}
                              fields={this.getFields(node)}
                              values={node}
                              isNew={isNewNode}
                              loading={loading}
                              post={{
                                  textButton: isNewNode ? 'Join swarm' : 'Save',
                                  url: 'nodes',
                                  successCallback: this.onPostSuccess,
                                  failureCallback: this.onPostFailure
                              }}
                            // modify button is never present on new nodes, so a type cast is safe
                              put={{
                                  url: `nodes/${(node as INode).id}`,
                                  successCallback: this.onPutSuccess,
                                  failureCallback: this.onPutFailure
                              }}
                            // delete button is never present on new nodes, so a type cast is safe
                              delete={{
                                  textButton: (node as INode).state === 'down' ? 'Remove from swarm' : 'Leave swarm',
                                  url: (node as INode).state === 'down' ? `nodes/${(node as INode).id}` : `nodes/${(node as INode).publicIpAddress}/leave`,
                                  successCallback: this.onDeleteSuccess,
                                  failureCallback: this.onDeleteFailure
                              }}
                              switchDropdown={isNewNode ? {
                                  options: ['On host', 'On location'],
                                  onSwitch: this.switchForm
                              } : undefined}
                              customButtons={this.showRejoinSwarmButton(node as INode) ? this.rejoinSwarmButton() : undefined}>
                            {this.formFields(isNewNode)}
                        </Form>
                        {isNewNode && currentForm === 'On location' && <LocationSelectorMap onSelect={this.onSelectCoordinates}/>}
                    </>
                )}
            </>
        )
    };

    private onSelectCoordinates = (coordinates: ICoordinates): void => {
        /*this.setState({selectedCoordinates: coordinates});*/
        M.toast({html: 'latitude: ' + coordinates.latitude + ' longitude: ' + coordinates.longitude});
    }

    private labels = (): JSX.Element =>
        <NodeLabelsList isLoadingNode={this.props.isLoading}
                        loadNodeError={!this.isNew() ? this.props.error : undefined}
                        node={this.getNode()}/>;

    private tabs = (): Tab[] => ([
        {
            title: 'Node',
            id: 'newNode',
            content: () => this.node(),
            hidden: !this.isNew(),
            active: this.props.location.state?.selected === 'newNode'
        },
        {
            title: 'Node',
            id: 'node',
            content: () => this.node(),
            hidden: this.isNew(),
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
    if (node) {
        delete node["labels"];
    }
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.nodes.isLoadingNodes;
    const error = state.entities.nodes.loadNodesError;
    const id = props.match.params.id.split('#')[0];
    const newNodeHost = isNew(props.location.search) ? buildNewNodeHost() : undefined;
    const newNodeLocation = isNew(props.location.search) ? buildNewNodeLocation() : undefined;
    const node = !isNew(props.location.search) ? state.entities.nodes.data[id] : undefined;
    let formNode;
    if (node) {
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
    addNode,
    updateNode,
    loadCloudHosts,
    loadEdgeHosts,
    loadRegions,
};

export default connect(mapStateToProps, mapDispatchToProps)(Node);