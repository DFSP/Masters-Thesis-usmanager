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
import {connect} from "react-redux";
import {IWorkerManager} from "./WorkerManager";
import BaseComponent from "../../../components/BaseComponent";
import CardList from "../../../components/list/CardList";
import {ReduxState} from "../../../reducers";
import {loadWorkerManagers} from "../../../actions";
import WorkerManagerCard from "./WorkerManagerCard";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    workerManagers: IWorkerManager[];
}

interface DispatchToProps {
    loadWorkerManagers: () => void;
}

type Props = StateToProps & DispatchToProps;

class WorkerManagersList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadWorkerManagers();
    }

    public render() {
        return (
            <CardList<IWorkerManager>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não existem gestores locais a executar"}
                list={this.props.workerManagers}
                card={this.workerManager}
                predicate={this.predicate}/>
        );
    }

    private workerManager = (workerManager: IWorkerManager): JSX.Element =>
        <WorkerManagerCard key={workerManager.id} workerManager={workerManager}/>;

    private predicate = (workerManager: IWorkerManager, search: string): boolean =>
        workerManager.id.toString().toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.workerManagers.isLoadingWorkerManagers,
        error: state.entities.workerManagers.loadWorkerManagersError,
        workerManagers: (state.entities.workerManagers.data && Object.values(state.entities.workerManagers.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadWorkerManagers,
};

export default connect(mapStateToProps, mapDispatchToProps)(WorkerManagersList);
