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

import React from "react";
import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {
  loadEdgeHosts,
  loadSimulatedHostMetricEdgeHosts,
  removeSimulatedHostMetricEdgeHosts,
} from "../../../../actions";
import {IEdgeHost} from "../../hosts/edge/EdgeHost";
import {ISimulatedHostMetric} from "./SimulatedHostMetric";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  edgeHosts: { [key: string]: IEdgeHost },
  simulatedMetricEdgeHosts: string[];
}

interface DispatchToProps {
  loadEdgeHosts: () => void;
  loadSimulatedHostMetricEdgeHosts: (name: string) => void;
  removeSimulatedHostMetricEdgeHosts: (name: string, edgeHosts: string[]) => void;
}

interface SimulatedHostMetricEdgeHostListProps {
  isLoadingSimulatedHostMetric: boolean;
  loadSimulatedHostMetricError?: string | null;
  simulatedHostMetric: ISimulatedHostMetric | Partial<ISimulatedHostMetric> | null;
  unsavedEdgeHosts: string[];
  onAddEdgeHost: (edgeHost: string) => void;
  onRemoveEdgeHosts: (edgeHost: string[]) => void;
}

type Props = StateToProps & DispatchToProps & SimulatedHostMetricEdgeHostListProps;

interface State {
  entitySaved: boolean;
}

class SimulatedHostMetricEdgeHostList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {entitySaved: !this.isNew()};
  }

  public componentDidMount(): void {
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevProps.simulatedHostMetric?.name !== this.props.simulatedHostMetric?.name) {
      this.loadEntities();
    }
    if (!prevProps.simulatedHostMetric?.name && this.props.simulatedHostMetric?.name) {
      this.setState({entitySaved: true});
    }
  }

  public render() {
    const isNew = this.isNew();
    return <ControlledList
      isLoading={!isNew ? this.props.isLoadingSimulatedHostMetric || this.props.isLoading : undefined}
      error={!isNew ? this.props.loadSimulatedHostMetricError || this.props.error : undefined}
      emptyMessage={`Edge hosts list is empty`}
      data={this.props.simulatedMetricEdgeHosts}
      dropdown={{
        id: 'edgeHosts',
        title: 'Add edge host',
        empty: 'No more edge hosts to add',
        data: this.getSelectableEdgeHosts()
      }}
      show={this.edgeHost}
      onAdd={this.onAdd}
      onRemove={this.onRemove}
      onDelete={{
        url: `simulated-metrics/hosts/${this.props.simulatedHostMetric?.name}/edge-hosts`,
        successCallback: this.onDeleteSuccess,
        failureCallback: this.onDeleteFailure
      }}
      entitySaved={this.state.entitySaved}/>;
  }

  private loadEntities = () => {
    this.props.loadEdgeHosts();
    if (this.props.simulatedHostMetric?.name) {
      const {name} = this.props.simulatedHostMetric;
      this.props.loadSimulatedHostMetricEdgeHosts(name);
    }
  };

  private isNew = () =>
    this.props.simulatedHostMetric?.name === undefined;

  private edgeHost = (index: number, edgeHost: string, separate: boolean, checked: boolean,
                      handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const isNew = this.isNew();
    const unsaved = this.props.unsavedEdgeHosts.includes(edgeHost);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${styles.linkedItemContent}`}>
          <label>
            <input id={edgeHost}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
               <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                 {edgeHost}
               </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/hosts/edge/${edgeHost}`}
                className={`${styles.link} waves-effect`}>
            <i className={`${styles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (edgeHost: string): void =>
    this.props.onAddEdgeHost(edgeHost);

  private onRemove = (edgeHost: string[]) =>
    this.props.onRemoveEdgeHosts(edgeHost);

  private onDeleteSuccess = (edgeHost: string[]): void => {
    if (this.props.simulatedHostMetric?.name) {
      const {name} = this.props.simulatedHostMetric;
      this.props.removeSimulatedHostMetricEdgeHosts(name, edgeHost);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to remove edge host`, 10000, reason, true);

  private getSelectableEdgeHosts = () => {
    const {edgeHosts, simulatedMetricEdgeHosts, unsavedEdgeHosts} = this.props;
    return Object.keys(edgeHosts).filter(edgeHost => !simulatedMetricEdgeHosts.includes(edgeHost)
                                                     && !unsavedEdgeHosts.includes(edgeHost));
  };

}

function mapStateToProps(state: ReduxState, ownProps: SimulatedHostMetricEdgeHostListProps): StateToProps {
  const name = ownProps.simulatedHostMetric?.name;
  const simulatedMetric = name && state.entities.simulatedMetrics.hosts.data[name];
  const simulatedMetricEdgeHosts = simulatedMetric && simulatedMetric.edgeHosts;
  return {
    isLoading: state.entities.simulatedMetrics.hosts.isLoadingEdgeHosts,
    error: state.entities.simulatedMetrics.hosts.loadEdgeHostsError,
    edgeHosts: state.entities.hosts.edge.data,
    simulatedMetricEdgeHosts: simulatedMetricEdgeHosts || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadEdgeHosts,
    loadSimulatedHostMetricEdgeHosts,
    removeSimulatedHostMetricEdgeHosts,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedHostMetricEdgeHostList);
