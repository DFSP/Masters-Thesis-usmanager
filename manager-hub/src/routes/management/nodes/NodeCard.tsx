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
import {deleteNode} from "../../../actions";

interface State {
    loading: boolean;
}

interface NodeCardProps {
    node: INode;
}

interface DispatchToProps {
    deleteNode: (node: INode) => EntitiesAction;
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

    private onDeleteSuccess = (node: INode): void => {
        super.toast(`<span class="green-text">O nó <b class="white-text">${node.publicIpAddress}</b> ${node.state === 'down' ? 'foi removido com sucesso do swarm' : 'saiu do swarm.'}</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteNode(node);
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

    private contextMenu = (): JSX.Element[] => {
        const {node} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Ver as labels associadas'}
                pathname={`/nós/${node.id}`}
                selected={'nodeLabels'}
                state={node}/>,
        ];
    }

    public render() {
        const {node} = this.props;
        const {loading} = this.state;
        const CardNode = Card<INode>();
        return <CardNode id={`node-${node.id}`}
                         title={node.id.toString()}
                         link={{to: {pathname: `/nós/${node.id}`, state: node}}}
                         height={'150px'}
                         margin={'10px 0'}
                         hoverable
                         delete={{
                             textButton: (node as INode).state === 'down' ? 'Remover do swarm' : 'Sair do swarm',
                             url: (node as INode).state === 'down' ? `nodes/${(node as INode).id}` : `nodes/${(node as INode).publicIpAddress}/leave`,
                             confirmMessage: (node as INode).state === 'down' ? `remover o nó ${node.id} do swarm` : `${node.id} irá sair permanentemente do swarm`,
                             successCallback: this.onDeleteSuccess,
                             failureCallback: this.onDeleteFailure
                         }}
                         loading={loading}
                         bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'hostName'}
                      label={'Hostname'}
                      value={node.publicIpAddress}/>
            <CardItem key={'state'}
                      label={'State'}
                      value={node.state}/>
            <CardItem key={'availability'}
                      label={'Availability'}
                      value={node.availability}/>
            <CardItem key={'role'}
                      label={'Role'}
                      value={node.role}/>
        </CardNode>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteNode
};

export default connect(null, mapDispatchToProps)(NodeCard);
