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
import {RouteComponentProps} from "react-router";
import Form, {IFields, requiredAndNumberAndMin, requiredAndTrimmed} from "../../../components/form/Form";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
  addWorkerManager,
  loadWorkerManagers,
  assignWorkerManagerHosts, loadNodes
} from "../../../actions";
import {connect} from "react-redux";
import {IReply, postData} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {ICloudHost} from "../hosts/cloud/CloudHost";
import IDatabaseData from "../../../components/IDatabaseData";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {IContainer} from "../containers/Container";
import {INode} from "../nodes/Node";
import AssignedHostsList from "./AssignedHostsList";

export interface IWorkerManager extends IDatabaseData {
  startedAt: string,
  container: IContainer,
  assignedHosts?: string[],
}

interface INewWorkerManager {
  host: IEdgeHost | ICloudHost | undefined,
}

const buildNewWorkerManager = (): INewWorkerManager => ({
  host: undefined,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  newWorkerManager?: INewWorkerManager;
  workerManager?: IWorkerManager;
  formWorkerManager?: Partial<IWorkerManager> | INewWorkerManager;
  nodes: { [key: string]: INode };
}

interface DispatchToProps {
  loadWorkerManagers: (id: string) => void;
  addWorkerManager: (workerManager: IWorkerManager) => void;
  loadNodes: () => void;
  assignWorkerManagerHosts: (id: string, Hosts: string[]) => void;
}

interface MatchParams {
  id: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  workerManager?: IWorkerManager,
  formWorkerManager?: IWorkerManager,
  unsavedHosts: string[],
}

class WorkerManager extends BaseComponent<Props, State> {

  state: State = {
    unsavedHosts: [],
  };
  private mounted = false;

  public componentDidMount(): void {
    this.loadWorkerManager();
    this.props.loadNodes();
    this.mounted = true;
  }

  componentWillUnmount(): void {
    this.mounted = false;
  }

  public render() {
    return (
      <MainLayout>
        {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs()}/>
        </div>
      </MainLayout>
    );
  }

  private loadWorkerManager = () => {
    if (!isNew(this.props.location.search)) {
      const workerManagerId = this.props.match.params.id;
      this.props.loadWorkerManagers(workerManagerId);
    }
  };

  private getWorkerManager = () =>
    this.state.workerManager || this.props.workerManager;

  private getFormWorkerManager = () =>
    this.state.formWorkerManager || this.props.formWorkerManager;

  private isNew = () =>
    isNew(this.props.location.search);

  private onPostSuccess = (reply: IReply<IWorkerManager>): void => {
    const workerManager = reply.data;
    const hostname = workerManager.container.publicIpAddress;
    super.toast(`<span class="green-text">Worker-manager ${this.mounted ? `<b class="white-text">${workerManager.id}</b>` : `<a href=/worker-managers/${workerManager.id}><b>${workerManager.id}</b></a>`} launched at ${hostname}</span>`);
    this.props.addWorkerManager(workerManager);
    this.saveEntities(workerManager);
    if (this.mounted) {
      this.updateWorkerManager(workerManager);
    }
  };

  private onPostFailure = (reason: string): void =>
    super.toast(`Unable to launch worker-manager`, 10000, reason, true);

  private onDeleteSuccess = (workerManager: IWorkerManager): void => {
    super.toast(`<span class="green-text">Worker-manager <b class="white-text">${workerManager.id}</b> successfully stopped</span>`);
    if (this.mounted) {
      this.props.history.push(`/worker-managers`)
    }
  };

  private onDeleteFailure = (reason: string, workerManager: IWorkerManager): void =>
    super.toast(`Unable to stop ${this.mounted ? `<b>${workerManager.id}</b>` : `<a href=/worker-managers/${workerManager.id}><b>${workerManager.id}</b></a>`} worker-manager`, 10000, reason, true);

  private updateWorkerManager = (workerManager: IWorkerManager) => {
    workerManager = Object.values(normalize(workerManager, Schemas.WORKER_MANAGER).entities.workerManagers || {})[0];
    const formWorkerManager = {...workerManager};
    removeFields(formWorkerManager);
    this.setState({workerManager: workerManager, formWorkerManager: formWorkerManager},
      () => this.props.history.replace(workerManager.id.toString()));
  };

  private shouldShowSaveButton = () =>
    !!this.state.unsavedHosts.length;

  private saveEntities = (workerManager: IWorkerManager) => {
    this.saveHosts(workerManager);
  };

  private assignHost = (Host: string): void => {
    this.setState({
      unsavedHosts: this.state.unsavedHosts.concat(Host)
    });
  };

  private unassignHosts = (hosts: string[]): void => {
    this.setState({
      unsavedHosts: this.state.unsavedHosts.filter(Host => !hosts.includes(Host))
    });
  };

  private saveHosts = (workerManager: IWorkerManager): void => {
    const {unsavedHosts} = this.state;
    if (unsavedHosts.length) {
      postData(`worker-managers/${workerManager.id}/assigned-hosts`, unsavedHosts,
        () => this.onSaveHostsSuccess(workerManager),
        (reason) => this.onSaveHostsFailure(workerManager, reason));
    }
  };

  private onSaveHostsSuccess = (workerManager: IWorkerManager): void => {
    this.props.assignWorkerManagerHosts(workerManager.id.toString(), this.state.unsavedHosts);
    if (this.mounted) {
      this.setState({unsavedHosts: []});
    }
  };

  private onSaveHostsFailure = (workerManager: IWorkerManager, reason: string): void =>
    super.toast(`Unable to save assigned hosts of ${this.mounted ? `<b>${workerManager.id}</b>` : `<a href=/worker-managers/${workerManager.id}><b>${workerManager.id}</b></a>`} worker-manager`, 10000, reason, true);

  private getFields = (workerManager: Partial<IWorkerManager> | INewWorkerManager): IFields =>
    Object.entries(workerManager).map(([key, value]) => {
      return {
        [key]: {
          id: key,
          label: key,
          validation: getTypeFromValue(value) === 'number'
            ? {rule: requiredAndNumberAndMin, args: 0}
            : {rule: requiredAndTrimmed}
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private getSelectableHosts = () =>
    Object.entries(this.props.nodes)
          .filter(([_, node]) => node.state === 'ready')
          .map(([_, node]) => node.hostname)

  private containerIdField = (container: IContainer) =>
    container.containerId;

  private containerHostnameField = (container: IContainer) =>
    container.publicIpAddress;

  private workerManager = () => {
    const {isLoading, error, newWorkerManager} = this.props;
    const workerManager = this.getWorkerManager();
    const formWorkerManager = this.getFormWorkerManager();
    // @ts-ignore
    const workerManagerKey: (keyof IWorkerManager) = workerManager && Object.keys(workerManager)[0];
    const isNewWorkerManager = this.isNew();
    return (
      <>
        {!isNewWorkerManager && isLoading && <ListLoadingSpinner/>}
        {!isNewWorkerManager && !isLoading && error && <Error message={error}/>}
        {(isNewWorkerManager || !isLoading) && (isNewWorkerManager || !error) && formWorkerManager && (
          <Form id={workerManagerKey}
                fields={this.getFields(formWorkerManager || {})}
                values={workerManager || newWorkerManager || {}}
                isNew={isNew(this.props.location.search)}
                showSaveButton={this.shouldShowSaveButton()}
                post={{
                  textButton: isNew(this.props.location.search) ? 'launch' : 'save',
                  url: 'worker-managers',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                delete={{
                  textButton: 'Stop',
                  url: `worker-managers/${workerManager?.id}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}
                saveEntities={this.saveEntities}>
            {Object.entries(formWorkerManager).map((([key, value], index) =>
                key === 'host'
                  ? <Field key={index}
                           id={key}
                           label={key}
                           type={'dropdown'}
                           dropdown={{
                             defaultValue: "Select host",
                             values: this.getSelectableHosts()
                           }}/>
                  : key === 'container'
                  ? <>
                    <Field<IContainer> key={index}
                                       id={key}
                                       label={key + " id"}
                                       valueToString={this.containerIdField}
                                       icon={{linkedTo: `/containers`}}/>
                    <Field<IContainer> key={index + 1} // index + 1 is ok unless there are more fields after this one
                                       id={key}
                                       label={"hostname"}
                                       valueToString={this.containerHostnameField}/>
                  </>
                  : <Field key={index}
                           id={key}
                           label={key}/>
            ))}
          </Form>
        )}
      </>
    )
  };

  private assignHosts = (): JSX.Element =>
    <AssignedHostsList isLoadingWorkerManager={this.props.isLoading}
                       loadWorkerManagerError={!this.isNew() ? this.props.error : undefined}
                       workerManager={this.getWorkerManager()}
                       unSavedHosts={this.state.unsavedHosts}
                       onAssignHost={this.assignHost}
                       onUnassignHosts={this.unassignHosts}/>;

  private tabs = (): Tab[] => [
    {
      title: 'Worker manager',
      id: 'workerManager',
      content: () => this.workerManager()
    },
    {
      title: 'Assigned hosts',
      id: 'assignHosts',
      content: () => this.assignHosts()
    }
  ];

}

function removeFields(workerManager: Partial<IWorkerManager>) {
  delete workerManager["id"];
  delete workerManager["assignedHosts"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.workerManagers.isLoadingWorkerManagers;
  const error = state.entities.workerManagers.loadWorkerManagersError;
  const id = props.match.params.id;
  const newWorkerManager = isNew(props.location.search) ? buildNewWorkerManager() : undefined;
  const workerManager = !isNew(props.location.search) ? state.entities.workerManagers.data[id] : undefined;
  let formWorkerManager;
  if (newWorkerManager) {
    formWorkerManager = {...newWorkerManager};
  }
  if (workerManager) {
    formWorkerManager = {...workerManager};
    removeFields(formWorkerManager);
  }
  const nodes = state.entities.nodes.data;
  return {
    isLoading,
    error,
    newWorkerManager,
    workerManager,
    formWorkerManager,
    nodes
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadWorkerManagers,
  addWorkerManager,
  loadNodes,
  assignWorkerManagerHosts
};

export default connect(mapStateToProps, mapDispatchToProps)(WorkerManager);
