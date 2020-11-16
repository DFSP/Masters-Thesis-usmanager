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

import React from 'react';
import MainLayout from '../../../views/mainLayout/MainLayout';
import NodeCard from './NodeCard';
import AddButton from "../../../components/form/AddButton";
import {connect} from "react-redux";
import {ReduxState} from "../../../reducers";
import CardList from "../../../components/list/CardList";
import {INode} from "./Node";
import styles from './Nodes.module.css'
import BaseComponent from "../../../components/BaseComponent";
import {loadNodes, syncNodes} from "../../../actions";
import ActionButton from "../../../components/list/ActionButton";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    nodes: INode[];
}

interface DispatchToProps {
    loadNodes: (id?: string) => any;
    syncNodes: () => void;
}

type Props = StateToProps & DispatchToProps;

class Nodes extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadNodes();
    }

    public render() {
        return (
            <MainLayout>
                <ActionButton icon={'sync'}
                              tooltip={{text: 'Sincronizar os nós na base de dados com o docker swarm', position: 'bottom'}}
                              clickCallback={this.syncNodes}/>
                <AddButton button={{text: 'Adicionar nó'}}
                           pathname={'/nós/novo nó?new'}/>
                <div className={`${styles.container}`}>
                    <CardList<INode>
                        isLoading={this.props.isLoading}
                        error={this.props.error}
                        emptyMessage={"Não existem nós conhecidos"}
                        list={this.props.nodes}
                        card={this.node}
                        predicate={this.predicate}/>
                </div>
            </MainLayout>
        );
    }

    private node = (node: INode): JSX.Element =>
        <NodeCard key={node.nodeId} node={node}/>;

    private predicate = (node: INode, search: string): boolean =>
        node.nodeId.toString().toLowerCase().includes(search)
        || node.publicIpAddress.toLowerCase().includes(search)
        || node.state.toLowerCase().includes(search)
        || node.role.toLowerCase().includes(search);

    private syncNodes = () => {
        this.props.syncNodes();
    };

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.nodes.isLoadingNodes,
        error: state.entities.nodes.loadNodesError,
        nodes: (state.entities.nodes.data && Object.values(state.entities.nodes.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadNodes,
    syncNodes
};

export default connect(mapStateToProps, mapDispatchToProps)(Nodes);
