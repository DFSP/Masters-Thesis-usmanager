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
import {ISimulatedAppMetric} from "./SimulatedAppMetric";
import SimulatedAppMetricCard from "./SimulatedAppMetricCard";
import {loadSimulatedAppMetrics} from "../../../../actions";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    simulatedAppMetrics: ISimulatedAppMetric[];
}

interface DispatchToProps {
    loadSimulatedAppMetrics: () => void;
}

type Props = StateToProps & DispatchToProps;

class SimulatedAppMetricsList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadSimulatedAppMetrics();
    }

    public render() {
        return (
            <CardList<ISimulatedAppMetric>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"No simulated app metrics to display"}
                list={this.props.simulatedAppMetrics}
                card={this.simulatedAppMetric}
                predicate={this.predicate}/>
        );
    }

    private simulatedAppMetric = (simulatedMetric: ISimulatedAppMetric): JSX.Element =>
        <SimulatedAppMetricCard key={simulatedMetric.id} simulatedAppMetric={simulatedMetric}/>;

    private predicate = (simulatedMetric: ISimulatedAppMetric, search: string): boolean =>
        simulatedMetric.name.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.simulatedMetrics.apps.isLoadingSimulatedAppMetrics,
        error: state.entities.simulatedMetrics.apps.loadSimulatedAppMetricsError,
        simulatedAppMetrics: (state.entities.simulatedMetrics.apps.data
            && Object.values(state.entities.simulatedMetrics.apps.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedAppMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedAppMetricsList);
