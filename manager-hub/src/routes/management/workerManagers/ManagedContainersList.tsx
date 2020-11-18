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
import styles from "../../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadContainers,} from "../../../actions";
import {connect} from "react-redux";
import {IWorkerManager} from "./WorkerManager";
import {IContainer} from "../containers/Container";
import List from "../../../components/list/List";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    containers: { [key: string]: IContainer };
}

interface DispatchToProps {
    loadContainers: () => void;
}

interface WorkerManagerHostListProps {
    isLoadingWorkerManager: boolean;
    loadWorkerManagerError?: string | null;
    workerManager: IWorkerManager | Partial<IWorkerManager> | undefined;
}

type Props = StateToProps & DispatchToProps & WorkerManagerHostListProps;

interface State {
}

class ManagedHostsList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
    }

    public componentDidMount(): void {
        this.props.loadContainers();
    }

    private containers = (): IContainer[] =>
        Object.values(this.props.containers).filter(container => container.managerId === this.props.workerManager?.id)

    private container = (container: IContainer, index: number): JSX.Element => {
        return (
            <ListItem key={index} separate={index !== this.containers().length - 1}>
                <Link to={`/contentores/${container.id}`}
                      className={`${listItemStyles.link}`}>
                    <div className={`${styles.listItemContent}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                        <span>{container.id.toString().substr(0, 10)} - {container.names.join(', ')}</span>
                    </div>
                </Link>
            </ListItem>
        );
    };

    public render() {
        const ContainersList = List<IContainer>();
        return <ContainersList isLoading={this.props.isLoadingWorkerManager}
                               error={this.props.loadWorkerManagerError}
                               emptyMessage={`O gestor não gere nenhum contentor`}
                               list={this.containers()}
                               show={this.container}/>
    }

}

function mapStateToProps(state: ReduxState): StateToProps {
    return {
        isLoading: state.entities.workerManagers.isLoadingWorkerManagers,
        error: state.entities.workerManagers.loadWorkerManagersError,
        containers: state.entities.containers.data,
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadContainers,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ManagedHostsList);