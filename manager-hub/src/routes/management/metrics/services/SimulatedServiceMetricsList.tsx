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
import {ISimulatedServiceMetric} from "./SimulatedServiceMetric";
import SimulatedServiceMetricCard from "./SimulatedServiceMetricCard";
import {loadSimulatedServiceMetrics} from "../../../../actions";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    simulatedServiceMetrics: ISimulatedServiceMetric[];
}

interface DispatchToProps {
    loadSimulatedServiceMetrics: () => void;
}

type Props = StateToProps & DispatchToProps;

class SimulatedServiceMetricsList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadSimulatedServiceMetrics();
    }

    public render() {
        return (
            <CardList<ISimulatedServiceMetric>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não existem métricas simuladas para serviços"}
                list={this.props.simulatedServiceMetrics}
                card={this.simulatedServiceMetric}
                predicate={this.predicate}/>
        );
    }

    private simulatedServiceMetric = (simulatedMetric: ISimulatedServiceMetric): JSX.Element =>
        <SimulatedServiceMetricCard key={simulatedMetric.id} simulatedServiceMetric={simulatedMetric}/>;

    private predicate = (simulatedMetric: ISimulatedServiceMetric, search: string): boolean =>
        simulatedMetric.name.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.simulatedMetrics.services.isLoadingSimulatedServiceMetrics,
        error: state.entities.simulatedMetrics.services.loadSimulatedServiceMetricsError,
        simulatedServiceMetrics: (state.entities.simulatedMetrics.services.data
            && Object.values(state.entities.simulatedMetrics.services.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedServiceMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedServiceMetricsList);
