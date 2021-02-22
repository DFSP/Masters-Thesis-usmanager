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

import Card from "../../../components/cards/Card";
import React from "react";
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {EntitiesAction} from "../../../reducers/entities";
import {connect} from "react-redux";
import {INode} from "./Node";
import CardItem from "../../../components/list/CardItem";
import {deleteNode, updateNode} from "../../../actions";
import ActionContextMenuItem from "../../../components/contextmenu/ActionContextMenuItem";
import {IReply, postData, putData} from "../../../utils/api";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";

interface State {
    loading: boolean;
    node?: INode,
}

interface NodeCardProps {
    node: INode;
    manager?: string;
}

interface DispatchToProps {
    deleteNode: (node: INode) => EntitiesAction;
    updateNode: (previousNode: INode, currentNode: INode) => void;
}

type Props = DispatchToProps & NodeCardProps;

class NodeCard extends BaseComponent<Props, State> {

    private mounted = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            loading: false
        }
    }

    public componentDidMount(): void {
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    private getNode = () =>
        this.props.node || this.state.node;

    private onDeleteSuccess = (node: INode): void => {
        super.toast(`<span class='green-text'>O nó <b>${node.id}</b> ${node.state === 'down' ? 'foi removido com sucesso do swarm' : 'saiu do swarm'}</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteNode(node)
    }

    private onDeleteFailure = (reason: string, node: INode): void => {
        if (node.state === 'active') {
            super.toast(`O nó <a href='/nós/${node.id}'><b>${node.id}</b></a> não conseguiu sair do swarm`, 10000, reason, true);
        } else if (node.state === 'down') {
            super.toast(`Não foi possível remover o nó <a href='/nós/${node.id}'><b>${node.id}</b></a> do swarm`, 10000, reason, true);
        }
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private topContextMenu = (): JSX.Element[] => {
        const node = this.getNode();
        const menus = [];
        if (!((node as INode).labels?.['managerNode'] === 'true')) {
            if ((node as INode).state !== 'down') {
                menus.push(<ActionContextMenuItem
                    className='blue-text'
                    option='Sair do swarm'
                    state={node}
                    onClick={this.leaveSwarm}/>);
            } else if (this.props.manager !== undefined) {
                menus.push(<ActionContextMenuItem
                    className='green-text'
                    option='Re-entrar no swarm'
                    state={node}
                    onClick={this.rejoinSwarm}/>);
            }
        }
        return menus;
    }

    private contextMenu = (): JSX.Element[] => {
        const node = this.getNode();
        return [
            <LinkedContextMenuItem
                option={'Ver as labels associadas'}
                pathname={`/nós/${node.id}`}
                selected={'nodeLabels'}
                state={node}/>,
        ];
    }

    private leaveSwarm = () => {
        const node = this.getNode();
        const manager = this.props.manager;
        const url = `${manager ? `${manager}/api/` : ''}nodes/${node?.publicIpAddress}/${node?.labels['privateIpAddress']}/leave`;
        this.setState({loading: true});
        putData(url, undefined,
            (reply: IReply<INode[]>) => this.onLeaveSuccess(reply.data),
            (reason) => this.onLeaveFailure(reason, node));
    };

    private onLeaveSuccess = (nodes: INode[]) => {
        const node = nodes[0];
        super.toast(`<span class='green-text'>O nó <b>${node.id}</b> saiu do swarm</span>`);
        const previousNode = this.getNode();
        if (previousNode?.id) {
            this.props.updateNode(previousNode as INode, node)
        }
        if (this.mounted) {
            this.updateNode(node);
        }
    };

    private onLeaveFailure = (reason: string, node: Partial<INode>) => {
        super.toast(`O nó ${this.mounted ? `<b>${node.id}</b>` : `<a href='/nós/${node.id}'><b>${node.id}</b></a>`} não conseguiu sair do swarm`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private rejoinSwarm = () => {
        const node = this.getNode();
        const manager = this.props.manager;
        const url = `${manager ? `${manager}/api/` : ''}nodes/${node?.id}/join`;
        this.setState({loading: true});
        postData(url, {},
            (reply: IReply<INode>) => this.onRejoinSwarmSuccess(reply.data),
            (reason: string) => this.onRejoinSwarmFailure(reason, node));
    };

    private onRejoinSwarmSuccess = (node: INode) => {
        super.toast(`<span class='green-text'>O host </span> <b>${node?.publicIpAddress}</b> <span class='green-text'>re-entrou no swarm com o id </span> ${this.mounted ? `<b>${node?.id}</b>` : `<a href='/nós/${node?.id}'><b>${node?.id}</b></a>`}`);
        const previousNode = this.getNode();
        if (previousNode?.id) {
            this.props.updateNode(previousNode as INode, node)
        }
        if (this.mounted) {
            this.updateNode(node);
        }
    };

    private onRejoinSwarmFailure = (reason: string, node?: INode) => {
        super.toast(`O nó ${this.mounted ? `<b>${node?.id}</b>` : `<a href='/nós/${node?.id}'><b>${node?.id}</b></a>`} não conseguiu re-entrar no swarm`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private updateNode = (node: INode) => {
        node = Object.values(normalize(node, Schemas.NODE).entities.nodes || {})[0];
        this.setState({node: node});
    };

    public render() {
        const node = this.getNode();
        const {loading} = this.state;
        const CardNode = Card<INode>();
        const manager = this.props.manager;
        return <CardNode id={`node-${node.id}`}
                         title={node.id.toString()}
                         link={{to: {pathname: `/nós/${node.id}`, state: node}}}
                         height={'179px'}
                         margin={'10px 0'}
                         hoverable
                         loading={loading}
                         topContextMenuItems={this.topContextMenu()}
                         bottomContextMenuItems={this.contextMenu()}
                         delete={((node as INode).labels?.['managerNode'] === 'true' && (node as INode).managerId === 'master-manager') || (node as INode).state !== 'down'
                             ? undefined
                             : {
                                 textButton: this.props.manager === undefined ? 'Apagar' : 'Remover do swarm',
                                 url: `${manager ? `${manager}/api/` : ''}nodes/${(node as INode).id}`,
                                 confirmMessage: this.props.manager === undefined ? `Apagar o nó ${node?.id}` : `remover o nó ${node?.id} do swarm`,
                                 successCallback: this.onDeleteSuccess,
                                 failureCallback: this.onDeleteFailure
                             }}>
            {!!node?.managerId &&
            <CardItem key={'managerId'}
                      label={'Manager'}
                      value={`${node.managerId}`}/>}
            <CardItem key={'state'}
                      label={'State'}
                      value={node.state}/>
            <CardItem key={'hostName'}
                      label={'Hostname'}
                      value={node.publicIpAddress + '/' + node?.labels['privateIpAddress']}/>
            <CardItem key={'availability'}
                      label={'Availability'}
                      value={node.availability}/>
            <CardItem key={'role'}
                      label={'Swarm role'}
                      value={node.role}/>
        </CardNode>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteNode,
    updateNode,
};

export default connect(null, mapDispatchToProps)(NodeCard);
