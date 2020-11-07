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
import ListItem from "../../../components/list/ListItem";
import styles from "./ContainerLogsList.module.css";
import List from "../../../components/list/List";
import React from "react";
import {IContainer} from "./Container";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {loadContainerLogs} from "../../../actions";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    logs?: string;
}

interface DispatchToProps {
    loadContainerLogs: (containerId: string) => void;
}

interface PortsListProps {
    isLoadingContainer: boolean;
    loadContainerError?: string | null;
    container?: IContainer | null;
}

type Props = StateToProps & DispatchToProps & PortsListProps;

class ContainerLogsList extends BaseComponent<Props, {}> {

    componentDidMount(): void {
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        if (prevProps.container?.containerId !== this.props.container?.containerId) {
            this.loadEntities();
        }
    }

    public render() {
        const logs = this.logs();
        const LogsList = List<string>();
        return (
            <>
                {!!logs.length && (
                    <div className='smallControlsContainer'>
                        <button className='btn-floating btn-flat btn-small right'
                                data-for='dark-tooltip' data-tip="Reload" data-place='bottom'
                                type="button"
                                onClick={this.reloadLogs}>
                            <i className="large material-icons">sync</i>
                        </button>
                    </div>)}
                <div className={styles.logsListContainer}>
                    <LogsList isLoading={this.props.isLoadingContainer || this.props.isLoading}
                              error={this.props.loadContainerError || this.props.error}
                              emptyMessage={`No logs available`}
                              list={logs}
                              show={this.log}
                              paginate={{
                                  pagesize: {initial: 50},
                                  page: {last: true},
                                  position: 'top-bottom',
                              }}/>
                </div>
            </>

        );
    }

    private loadEntities = () => {
        this.reloadLogs();
    };

    private reloadLogs = () => {
        if (this.props.container?.containerId) {
            const {containerId} = this.props.container;
            this.props.loadContainerLogs(containerId);
        }
    };

    private logs = () => {
        let logs = this.props.container?.logs?.split("\n");
        if (logs) {
            // pop 1 to remove the last \n
            logs.pop();
        } else {
            logs = [];
        }
        return logs;
    };

    private log = (logs: string, index: number): JSX.Element =>
        <ListItem key={index}>
            <div className={styles.listItemContent}>
                <span>{logs}</span>
            </div>
        </ListItem>;

}

function mapStateToProps(state: ReduxState, ownProps: PortsListProps): StateToProps {
    const containerId = ownProps.container?.containerId;
    const container = containerId && state.entities.containers.data[containerId];
    const logs = container && container.logs;
    return {
        isLoading: state.entities.containers.isLoadingLogs,
        error: state.entities.containers.loadLogsError,
        logs: logs
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadContainerLogs
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ContainerLogsList);