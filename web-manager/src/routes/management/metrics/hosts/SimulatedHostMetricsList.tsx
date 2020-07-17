/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import React from 'react';
import {connect} from "react-redux";
import {ReduxState} from "../../../../reducers";
import CardList from "../../../../components/list/CardList";
import BaseComponent from "../../../../components/BaseComponent";
import {loadSimulatedHostMetrics} from "../../../../actions";
import {ISimulatedHostMetric} from "./SimulatedHostMetric";
import SimulatedHostMetricCard from "./SimulatedHostMetricCard";

interface StateToProps {
  isLoading: boolean
  error?: string | null;
  simulatedHostMetrics: ISimulatedHostMetric[];
}

interface DispatchToProps {
  loadSimulatedHostMetrics: () => void;
}

type Props = StateToProps & DispatchToProps;

class SimulatedHostMetricsList extends BaseComponent<Props, {}> {

  public componentDidMount(): void {
    this.props.loadSimulatedHostMetrics();
  }

  private simulatedHostMetric = (simulatedMetric: ISimulatedHostMetric): JSX.Element =>
    <SimulatedHostMetricCard key={simulatedMetric.id} simulatedHostMetric={simulatedMetric}/>;

  private predicate = (simulatedMetric: ISimulatedHostMetric, search: string): boolean =>
    simulatedMetric.name.toLowerCase().includes(search);

  public render() {
    return (
      <CardList<ISimulatedHostMetric>
        isLoading={this.props.isLoading}
        error={this.props.error}
        emptyMessage={"No simulated host metrics to display"}
        list={this.props.simulatedHostMetrics}
        card={this.simulatedHostMetric}
        predicate={this.predicate}/>
    );
  }

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.simulatedMetrics.hosts.isLoadingSimulatedHostMetrics,
    error: state.entities.simulatedMetrics.hosts.loadSimulatedHostMetricsError,
    simulatedHostMetrics: (state.entities.simulatedMetrics.hosts.data
                           && Object.values(state.entities.simulatedMetrics.hosts.data)) || [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadSimulatedHostMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedHostMetricsList);
