/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import React from "react";
import {IEdgeHost} from "./EdgeHost";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {
  loadSimulatedHostMetrics,
  loadEdgeHostSimulatedMetrics,
  removeEdgeHostSimulatedMetrics
} from "../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {ISimulatedHostMetric} from "../../metrics/hosts/SimulatedHostMetric";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  simulatedMetrics: { [key: string]: ISimulatedHostMetric },
  simulatedMetricsName: string[];
}

interface DispatchToProps {
  loadSimulatedHostMetrics: (name?: string) => any;
  loadEdgeHostSimulatedMetrics: (hostname: string) => void;
  removeEdgeHostSimulatedMetrics: (hostname: string, simulatedMetrics: string[]) => void;
}

interface HostSimulatedMetricListProps {
  isLoadingEdgeHost: boolean;
  loadEdgeHostError?: string | null;
  edgeHost: IEdgeHost | Partial<IEdgeHost> | null;
  unsavedSimulatedMetrics: string[];
  onAddSimulatedHostMetric: (simulatedMetric: string) => void;
  onRemoveSimulatedHostMetrics: (simulatedMetric: string[]) => void;
}

type Props = StateToProps & DispatchToProps & HostSimulatedMetricListProps;

interface State {
  entitySaved: boolean;
}

class EdgeHostSimulatedMetricList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { entitySaved: !this.isNew() };
  }

  public componentDidMount(): void {
    this.props.loadSimulatedHostMetrics();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevProps.edgeHost?.hostname !== this.props.edgeHost?.hostname) {
      this.loadEntities();
    }
    if (!prevProps.edgeHost?.hostname && this.props.edgeHost?.hostname) {
      this.setState({entitySaved: true});
    }
  }

  private loadEntities = () => {
    if (this.props.edgeHost?.hostname) {
      const {hostname} = this.props.edgeHost;
      this.props.loadEdgeHostSimulatedMetrics(hostname);
    }
  };

  private isNew = () =>
    this.props.edgeHost?.hostname === undefined;

  private simulatedMetric = (index: number, simulatedMetric: string, separate: boolean, checked: boolean,
                             handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const isNew = this.isNew();
    const unsaved = this.props.unsavedSimulatedMetrics.includes(simulatedMetric);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${styles.linkedItemContent}`}>
          <label>
            <input id={simulatedMetric}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                 {simulatedMetric}
               </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/simulated-metrics/hosts/${simulatedMetric}`}
                className={`${styles.link} waves-effect`}>
            <i className={`${styles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (simulatedMetric: string): void =>
    this.props.onAddSimulatedHostMetric(simulatedMetric);

  private onRemove = (simulatedMetrics: string[]) =>
    this.props.onRemoveSimulatedHostMetrics(simulatedMetrics);

  private onDeleteSuccess = (simulatedMetrics: string[]): void => {
    if (this.props.edgeHost?.hostname) {
      const {hostname} = this.props.edgeHost;
      this.props.removeEdgeHostSimulatedMetrics(hostname, simulatedMetrics);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to delete simulated metric`, 10000, reason, true);

  private getSelectableSimulatedMetrics = () => {
    const {simulatedMetrics, simulatedMetricsName, unsavedSimulatedMetrics} = this.props;
    return Object.keys(simulatedMetrics).filter(name => !simulatedMetricsName.includes(name) && !unsavedSimulatedMetrics.includes(name));
  };

  public render() {
    return <ControlledList isLoading={this.props.isLoadingEdgeHost || this.props.isLoading}
                           error={this.props.loadEdgeHostError || this.props.error}
                           emptyMessage={`Simulated metrics list is empty`}
                           data={this.props.simulatedMetricsName}
                           dropdown={{
                             id: 'simulatedMetrics',
                             title: 'Add simulated metric',
                             empty: 'No more simulated metrics to add',
                             data: this.getSelectableSimulatedMetrics()
                           }}
                           show={this.simulatedMetric}
                           onAdd={this.onAdd}
                           onRemove={this.onRemove}
                           onDelete={{
                             url: `hosts/edge/${this.props.edgeHost?.hostname}/simulated-metrics`,
                             successCallback: this.onDeleteSuccess,
                             failureCallback: this.onDeleteFailure
                           }}
                           entitySaved={this.state.entitySaved}/>;
  }

}

function mapStateToProps(state: ReduxState, ownProps: HostSimulatedMetricListProps): StateToProps {
  const hostname = ownProps.edgeHost?.hostname;
  const host = hostname && state.entities.hosts.edge.data[hostname];
  const simulatedMetricsName = host && host.hostSimulatedMetrics;
  return {
    isLoading: state.entities.hosts.edge.isLoadingSimulatedMetrics,
    error: state.entities.hosts.edge.loadSimulatedMetricsError,
    simulatedMetrics: Object.entries(state.entities.simulatedMetrics.hosts.data)
                            .filter(([_, simulatedMetric]) => !simulatedMetric.generic)
                            .map(([key, value]) => ({[key]: value}))
                            .reduce((fields, field) => {
                              for (let key in field) {
                                fields[key] = field[key];
                              }
                              return fields;
                            }, {}),
    simulatedMetricsName: simulatedMetricsName || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadSimulatedHostMetrics,
    loadEdgeHostSimulatedMetrics,
    removeEdgeHostSimulatedMetrics,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(EdgeHostSimulatedMetricList);