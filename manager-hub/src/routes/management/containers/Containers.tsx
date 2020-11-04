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
import ContainerCard from './ContainerCard';
import AddButton from "../../../components/form/AddButton";
import {connect} from "react-redux";
import {ReduxState} from "../../../reducers";
import CardList from "../../../components/list/CardList";
import {IContainer} from "./Container";
import styles from './Containers.module.css'
import BaseComponent from "../../../components/BaseComponent";
import {loadContainers, loadNodes, reloadContainers} from "../../../actions";
import ActionButton from "../../../components/list/ActionButton";
import {INode} from "../nodes/Node";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    containers: IContainer[];
    isLoadingNodes: boolean;
    loadNodesError?: string | null;
    nodes: { [key: string]: INode };
}

interface DispatchToProps {
    loadContainers: () => void;
    reloadContainers: () => void;
    loadNodes: () => void;
}

type Props = StateToProps & DispatchToProps;

class Containers extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadContainers();
        this.props.loadNodes();
    }

    public render() {
        return (
            <MainLayout>
                <AddButton tooltip={{text: 'Start container', position: 'left'}}
                           pathname={'/containers/start_container?new'}
                           offset={1}/>
                <ActionButton icon={'sync'}
                              tooltip={{text: 'Sync containers', position: 'bottom'}}
                              clickCallback={this.reloadContainers}
                              offset={0}/>
                <div className={`${styles.container}`}>
                    <CardList<IContainer>
                        isLoading={this.props.isLoading}
                        error={this.props.error}
                        emptyMessage={"No containers to display"}
                        list={this.props.containers}
                        card={this.container}
                        predicate={this.predicate}/>
                </div>
            </MainLayout>
        );
    }

    private container = (container: IContainer): JSX.Element =>
        <ContainerCard key={container.containerId} container={container}
                       nodes={{
                           data: Object.values(this.props.nodes),
                           isLoading: this.props.isLoadingNodes,
                           error: this.props.loadNodesError
                       }}/>;

    private predicate = (container: IContainer, search: string): boolean =>
        container.containerId.toString().toLowerCase().includes(search)
        || container.type.toLowerCase().includes(search)
        || container.names.filter(name => name.includes(search)).length > 0
        || container.publicIpAddress.toLowerCase().includes(search)
        || container.labels['serviceType'].toLowerCase().includes(search);

    private reloadContainers = () => {
        this.props.reloadContainers();
    };

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.containers.isLoadingContainers,
        error: state.entities.containers.loadContainersError,
        containers: (state.entities.containers.data && Object.values(state.entities.containers.data).reverse()) || [],
        nodes: state.entities.nodes.data,
        isLoadingNodes: state.entities.nodes.isLoadingNodes,
        loadNodesError: state.entities.nodes.loadNodesError,
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadContainers,
    reloadContainers,
    loadNodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(Containers);
