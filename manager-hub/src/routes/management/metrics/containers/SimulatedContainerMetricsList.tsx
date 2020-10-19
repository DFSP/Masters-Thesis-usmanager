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
import {ReduxState} from "../../../../reducers";
import CardList from "../../../../components/list/CardList";
import BaseComponent from "../../../../components/BaseComponent";
import {ISimulatedContainerMetric} from "./SimulatedContainerMetric";
import SimulatedContainerMetricCard from "./SimulatedContainerMetricCard";
import {loadSimulatedContainerMetrics} from "../../../../actions";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    simulatedContainerMetrics: ISimulatedContainerMetric[];
}

interface DispatchToProps {
    loadSimulatedContainerMetrics: () => void;
}

type Props = StateToProps & DispatchToProps;

class SimulatedContainerMetricsList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadSimulatedContainerMetrics();
    }

    public render() {
        return (
            <CardList<ISimulatedContainerMetric>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"No simulated container metrics to display"}
                list={this.props.simulatedContainerMetrics}
                card={this.simulatedContainerMetric}
                predicate={this.predicate}/>
        );
    }

    private simulatedContainerMetric = (simulatedMetric: ISimulatedContainerMetric): JSX.Element =>
        <SimulatedContainerMetricCard key={simulatedMetric.id} simulatedContainerMetric={simulatedMetric}/>;

    private predicate = (simulatedMetric: ISimulatedContainerMetric, search: string): boolean =>
        simulatedMetric.name.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.simulatedMetrics.containers.isLoadingSimulatedContainerMetrics,
        error: state.entities.simulatedMetrics.containers.loadSimulatedContainerMetricsError,
        simulatedContainerMetrics: (state.entities.simulatedMetrics.containers.data
            && Object.values(state.entities.simulatedMetrics.containers.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedContainerMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedContainerMetricsList);
