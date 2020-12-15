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
import styles from "../../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import List from "../../../components/list/List";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import ListItem from "../../../components/list/ListItem";
import {loadSimulatedServiceMetrics} from "../../../actions";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    genericSimulatedServiceMetrics: string[];
}

interface DispatchToProps {
    loadSimulatedServiceMetrics: () => void;
}

type Props = StateToProps & DispatchToProps;

class GenericSimulatedServiceMetricList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadSimulatedServiceMetrics();
    }

    public render() {
        const SimulatedMetricsList = List<string>();
        return (
            <SimulatedMetricsList isLoading={this.props.isLoading}
                                  error={this.props.error}
                                  emptyMessage={`Não existem métricas simuladas que se apliquem`}
                                  list={this.props.genericSimulatedServiceMetrics}
                                  show={this.simulatedMetric}/>
        );
    }

    private simulatedMetric = (simulatedMetric: string, index: number): JSX.Element =>
        <ListItem key={index} separate={index !== this.props.genericSimulatedServiceMetrics.length - 1}>
            <Link to={`/métricas simuladas/serviços/${simulatedMetric}`}
                  className={`${styles.link}`}>
                <div className={`${styles.linkedItemContent}`}>
                    <span>{simulatedMetric}</span>
                    <i className={`${styles.linkIcon} material-icons right`}>link</i>
                </div>
            </Link>
        </ListItem>;

}

function mapStateToProps(state: ReduxState): StateToProps {
    return {
        isLoading: state.entities.simulatedMetrics.services.isLoadingSimulatedServiceMetrics,
        error: state.entities.simulatedMetrics.services.loadSimulatedServiceMetricsError,
        genericSimulatedServiceMetrics: Object.entries(state.entities.simulatedMetrics.services.data)
            .filter(([_, simulatedMetric]) => simulatedMetric.generic)
            .map(([simulatedMetricName, _]) => simulatedMetricName)
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadSimulatedServiceMetrics,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(GenericSimulatedServiceMetricList);