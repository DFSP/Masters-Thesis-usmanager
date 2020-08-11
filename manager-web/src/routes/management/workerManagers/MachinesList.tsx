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
import {
  loadCloudHosts,
  loadEdgeHosts,
  loadWorkerManagerMachines,
  unassignWorkerManagerMachines,
} from "../../../actions";
import {connect} from "react-redux";
import {ICloudHost} from "../hosts/cloud/CloudHost";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IWorkerManager} from "./WorkerManager";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  cloudHosts: { [key: string]: ICloudHost };
  edgeHosts: { [key: string]: IEdgeHost };
  machines: string[];
}

interface DispatchToProps {
  loadCloudHosts: () => void;
  loadEdgeHosts: () => void;
  loadWorkerManagerMachines: (id: string) => void;
  unassignWorkerManagerMachines: (id: string, machines: string[]) => void;
}

interface WorkerManagerMachineListProps {
  isLoadingWorkerManager: boolean;
  loadWorkerManagerError?: string | null;
  workerManager: IWorkerManager | Partial<IWorkerManager> | undefined;
  unSavedMachines: string[];
  onAssignMachine: (machine: string) => void;
  onUnassignMachines: (machines: string[]) => void;
}

type Props = StateToProps & DispatchToProps & WorkerManagerMachineListProps;

interface State {
  selectedMachine?: string;
  entitySaved: boolean;
}

class MachinesList extends BaseComponent<Props, State> {

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
      emptyMessage={`Machines list is empty`}
      data={this.props.machines}
      dropdown={{
        id: 'workerManagerMachines',
        title: 'Add machine',
        empty: 'No more machines to add',
        data: this.getSelectableMachines(),
      }}
      show={this.machine}
      onAdd={this.onAdd}
      onRemove={this.onRemove}
      onDelete={{
        url: `worker-managers/${this.props.workerManager?.id}/machines`,
        successCallback: this.onDeleteSuccess,
        failureCallback: this.onDeleteFailure
      }}
      entitySaved={this.state.entitySaved}/>;
  }

  private loadEntities = () => {
    if (this.props.workerManager?.id) {
      const {id} = this.props.workerManager;
      this.props.loadWorkerManagerMachines(id.toString());
    }
  };

  private isNew = () =>
    this.props.workerManager?.id === undefined;

  private machine = (index: number, machine: string, separate: boolean, checked: boolean,
                     handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const isNew = this.isNew();
    const unsaved = this.props.unSavedMachines.includes(machine);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${listItemStyles.linkedItemContent}`}>
          <label>
            <input id={machine}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? listItemStyles.unsavedItem : undefined}>
                {machine}
              </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={Object.keys(this.props.cloudHosts).includes(machine) ? `/hosts/cloud/${machine}` : `/hosts/edge/${machine}`}
                className={`${listItemStyles.link} waves-effect`}>
            <i className={`${listItemStyles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (machine: string): void => {
    this.props.onAssignMachine(machine);
  };

  private onRemove = (machines: string[]): void => {
    this.props.onUnassignMachines(machines);
  };

  private onDeleteSuccess = (machines: string[]): void => {
    if (this.props.workerManager?.id) {
      const {id} = this.props.workerManager;
      this.props.unassignWorkerManagerMachines(id.toString(), machines);
    }
  };

  private onDeleteFailure = (reason: string, machines: string[]): void =>
    super.toast(`Unable to remove ${machines.length === 1 ? machines[0] : 'machines'} from <b>${this.props.workerManager?.id}</b> worker-manager`, 10000, reason, true);

  private getSelectableMachines = () => {
    const {machines, cloudHosts, edgeHosts, unSavedMachines} = this.props;
    const cloud = Object.keys(cloudHosts).filter(host => !machines.includes(host) && !unSavedMachines.includes(host));
    const edge = Object.keys(edgeHosts).filter(host => !machines.includes(host) && !unSavedMachines.includes(host));
    return cloud.concat(edge);
  };

}

function mapStateToProps(state: ReduxState, ownProps: WorkerManagerMachineListProps): StateToProps {
  const id = ownProps.workerManager?.id;
  const workerManager = id && state.entities.workerManagers.data[id];
  const machines = workerManager && workerManager.machines;
  return {
    isLoading: state.entities.workerManagers.isLoadingWorkerManagers,
    error: state.entities.workerManagers.loadWorkerManagersError,
    cloudHosts: state.entities.hosts.cloud.data,
    edgeHosts: state.entities.hosts.edge.data,
    machines: (machines && Object.values(machines)) || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadCloudHosts,
    loadEdgeHosts,
    loadWorkerManagerMachines,
    unassignWorkerManagerMachines,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(MachinesList);