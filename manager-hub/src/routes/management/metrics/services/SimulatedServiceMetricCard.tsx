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

import Card from "../../../../components/cards/Card";
import CardItem from "../../../../components/list/CardItem";
import React from "react";
import {deleteSimulatedServiceMetric} from "../../../../actions";
import {ISimulatedServiceMetric} from "./SimulatedServiceMetric";
import BaseComponent from "../../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../../components/contextmenu/LinkedContextMenuItem";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface SimulatedServiceMetricCardProps {
    simulatedServiceMetric: ISimulatedServiceMetric;
}

interface DispatchToProps {
    deleteSimulatedServiceMetric: (simulatedServiceMetric: ISimulatedServiceMetric) => void;
}

type Props = DispatchToProps & SimulatedServiceMetricCardProps;

class SimulatedServiceMetricCard extends BaseComponent<Props, State> {

    private mounted = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            loading: false
        }
    }

    public componentDidMount(): void {
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    private onDeleteSuccess = (simulatedMetric: ISimulatedServiceMetric): void => {
        super.toast(`<span class="green-text">Simulated service metric <b class="white-text">${simulatedMetric.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteSimulatedServiceMetric(simulatedMetric);
    }

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedServiceMetric): void => {
        super.toast(`Unable to delete <a href=/simulated-metrics/Services/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a> simulated service metric`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {simulatedServiceMetric} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Modify services'}
                pathname={`/simulated-metrics/services/${simulatedServiceMetric.name}`}
                selected={'services'}
                state={simulatedServiceMetric}/>,
        ];
    }

    public render() {
        const {simulatedServiceMetric} = this.props;
        const {loading} = this.state;
        const CardSimulatedServiceMetric = Card<ISimulatedServiceMetric>();
        return <CardSimulatedServiceMetric id={`simulated-service-metric-${simulatedServiceMetric.id}`}
                                           title={simulatedServiceMetric.name}
                                           link={{
                                               to: {
                                                   pathname: `/simulated-metrics/services/${simulatedServiceMetric.name}`,
                                                   state: simulatedServiceMetric
                                               }
                                           }}
                                           height={'180px'}
                                           margin={'10px 0'}
                                           hoverable
                                           delete={{
                                               url: `simulated-metrics/services/${simulatedServiceMetric.name}`,
                                               successCallback: this.onDeleteSuccess,
                                               failureCallback: this.onDeleteFailure,
                                           }}
                                           loading={loading}
                                           bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'Field'}
                      label={'Field'}
                      value={`${simulatedServiceMetric.field.name}`}/>
            <CardItem key={'MinimumValue'}
                      label='Minimum value'
                      value={`${simulatedServiceMetric.minimumValue}`}/>
            <CardItem key={'MaximumValue'}
                      label='Maximum value'
                      value={`${simulatedServiceMetric.maximumValue}`}/>
            <CardItem key={'Override'}
                      label='Override'
                      value={`${simulatedServiceMetric.override}`}/>
            <CardItem key={'Generic'}
                      label='Generic'
                      value={`${simulatedServiceMetric.generic}`}/>
        </CardSimulatedServiceMetric>
    }
}

const mapDispatchToProps: DispatchToProps = {
    deleteSimulatedServiceMetric,
};

export default connect(null, mapDispatchToProps)(SimulatedServiceMetricCard);
