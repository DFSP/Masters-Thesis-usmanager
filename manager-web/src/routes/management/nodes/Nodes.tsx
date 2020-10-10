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
import {loadNodes} from "../../../actions";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    nodes: INode[];
}

interface DispatchToProps {
    loadNodes: (id?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class Nodes extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadNodes();
    }

    public render() {
        return (
            <MainLayout>
                <AddButton tooltip={{text: 'Add node', position: 'left'}}
                           pathname={'/nodes/new_node?new=true'}/>
                <div className={`${styles.container}`}>
                    <CardList<INode>
                        isLoading={this.props.isLoading}
                        error={this.props.error}
                        emptyMessage={"No nodes to display"}
                        list={this.props.nodes}
                        card={this.node}
                        predicate={this.predicate}/>
                </div>
            </MainLayout>
        );
    }

    private node = (node: INode): JSX.Element =>
        <NodeCard key={node.id} node={node}/>;

    private predicate = (node: INode, search: string): boolean =>
        node.id.toString().toLowerCase().includes(search)
        || node.hostname.toLowerCase().includes(search)
        || node.state.toLowerCase().includes(search)
        || node.role.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.nodes.isLoadingNodes,
        error: state.entities.nodes.loadNodesError,
        nodes: (state.entities.nodes.data && Object.values(state.entities.nodes.data)) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadNodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(Nodes);
