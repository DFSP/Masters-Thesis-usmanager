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
import ListItem from "../../../components/list/ListItem";
import listItemStyles from "../../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadCloudHosts, loadEdgeHosts, loadWorkerManagerHosts, unassignWorkerManagerHosts,} from "../../../actions";
import {connect} from "react-redux";
import {IWorkerManager} from "./WorkerManager";
import {INode} from "../nodes/Node";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    nodes: { [key: string]: INode };
    assignedHosts: string[];
}

interface DispatchToProps {
    loadCloudHosts: () => void;
    loadEdgeHosts: () => void;
    loadWorkerManagerHosts: (id: string) => void;
    unassignWorkerManagerHosts: (id: string, assignedHosts: string[]) => void;
}

interface WorkerManagerHostListProps {
    isLoadingWorkerManager: boolean;
    loadWorkerManagerError?: string | null;
    workerManager: IWorkerManager | Partial<IWorkerManager> | undefined;
    unSavedHosts: string[];
    onAssignHost: (assignedHost: string) => void;
    onUnassignHosts: (assignedHosts: string[]) => void;
}

type Props = StateToProps & DispatchToProps & WorkerManagerHostListProps;

interface State {
    selectedHost?: string;
    entitySaved: boolean;
}

class AssignedHostsList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadCloudHosts();
        this.props.loadEdgeHosts();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.workerManager?.id && this.props.workerManager?.id) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList<string>
            isLoading={!isNew ? this.props.isLoadingWorkerManager || this.props.isLoading : undefined}
            error={!isNew ? this.props.loadWorkerManagerError || this.props.error : undefined}
            emptyMessage={`O gestor não tem hosts associados`}
            data={this.props.assignedHosts}
            dropdown={{
                id: 'workerManagerHosts',
                title: 'Atribuir o host',
                empty: 'Não existem hosts disponíveis',
                data: this.getSelectableHosts(),
            }}
            show={this.assignedHost}
            onAdd={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `worker-managers/${this.props.workerManager?.id}/assigned-hosts`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            removeButtonText={'Deixar de gerir'}
            entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.workerManager?.id) {
            const {id} = this.props.workerManager;
            this.props.loadWorkerManagerHosts(id.toString());
        }
    };

    private isNew = () =>
        this.props.workerManager?.id === undefined;

    private assignedHost = (index: number, assignedHost: string, separate: boolean, checked: boolean,
                            handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unSavedHosts.includes(assignedHost);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${listItemStyles.linkedItemContent}`}>
                    <label>
                        <input id={assignedHost}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? listItemStyles.unsavedItem : undefined}>
                                {assignedHost}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/nós/${assignedHost}`}
                          className={`${listItemStyles.link}`}>
                        <i className={`${listItemStyles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (assignedHost: string): void => {
        this.props.onAssignHost(assignedHost);
    };

    private onRemove = (assignedHosts: string[]): void => {
        this.props.onUnassignHosts(assignedHosts);
    };

    private onDeleteSuccess = (assignedHosts: string[]): void => {
        if (this.props.workerManager?.id) {
            const {id} = this.props.workerManager;
            this.props.unassignWorkerManagerHosts(id.toString(), assignedHosts);
        }
    };

    private onDeleteFailure = (reason: string, assignedHosts?: string[]): void =>
        super.toast(`Não foi possível desassociar o gestor local <b>${this.props.workerManager?.id}</b> from ${assignedHosts?.length === 1 ? 'do host ' + assignedHosts[0] : 'dos hosts'}`, 10000, reason, true);

    private getSelectableHosts = () => {
        const {assignedHosts, nodes, unSavedHosts} = this.props;
        return Object.entries(nodes)
            .filter(([_, node]) => node.state === 'ready'
                && !assignedHosts.includes(node.publicIpAddress)
                && !unSavedHosts.includes(node.publicIpAddress)
                && this.props.workerManager?.container?.publicIpAddress !== node.publicIpAddress)
            .map(([_, node]) => node.publicIpAddress)
    };

}

function mapStateToProps(state: ReduxState, ownProps: WorkerManagerHostListProps): StateToProps {
    const id = ownProps.workerManager?.id;
    const workerManager = id && state.entities.workerManagers.data[id];
    const assignedHosts = workerManager && workerManager.assignedHosts;
    return {
        isLoading: state.entities.workerManagers.isLoadingWorkerManagers,
        error: state.entities.workerManagers.loadWorkerManagersError,
        nodes: state.entities.nodes.data,
        assignedHosts: (assignedHosts && Object.values(assignedHosts)) || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadCloudHosts,
        loadEdgeHosts,
        loadWorkerManagerHosts,
        unassignWorkerManagerHosts,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(AssignedHostsList);