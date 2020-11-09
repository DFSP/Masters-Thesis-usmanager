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
import {ISimulatedAppMetric} from "./SimulatedAppMetric";
import BaseComponent from "../../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../../components/contextmenu/LinkedContextMenuItem";
import {deleteSimulatedAppMetric} from "../../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface SimulatedAppMetricCardProps {
    simulatedAppMetric: ISimulatedAppMetric;
}

interface DispatchToProps {
    deleteSimulatedAppMetric: (simulatedAppMetric: ISimulatedAppMetric) => void;
}

type Props = DispatchToProps & SimulatedAppMetricCardProps;

class SimulatedAppMetricCard extends BaseComponent<Props, State> {

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

    private onDeleteSuccess = (simulatedMetric: ISimulatedAppMetric): void => {
        super.toast(`<span class="green-text">A métrica simulada <b class="white-text">${simulatedMetric.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteSimulatedAppMetric(simulatedMetric);
    }

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedAppMetric): void => {
        super.toast(`Não foi possível remover a métrica simulada <a href='/métricas simuladas/aplicações/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {simulatedAppMetric} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Alterar a lista de aplicações'}
                pathname={`/métricas simuladas/aplicações/${simulatedAppMetric.name}`}
                selected={'apps'}
                state={simulatedAppMetric}/>,
        ];
    }

    public render() {
        const {simulatedAppMetric} = this.props;
        const {loading} = this.state;
        const CardSimulatedAppMetric = Card<ISimulatedAppMetric>();
        return <CardSimulatedAppMetric id={`simulated-app-metric-${simulatedAppMetric.id}`}
                                       title={simulatedAppMetric.name}
                                       link={{
                                           to: {
                                               pathname: `/métricas simuladas/aplicações/${simulatedAppMetric.name}`,
                                               state: simulatedAppMetric
                                           }
                                       }}
                                       height={'185px'}
                                       margin={'10px 0'}
                                       hoverable
                                       delete={{
                                           url: `simulated-metrics/apps/${simulatedAppMetric.name}`,
                                           successCallback: this.onDeleteSuccess,
                                           failureCallback: this.onDeleteFailure,
                                       }}
                                       loading={loading}
                                       bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'Field'}
                      label={'Field'}
                      value={`${simulatedAppMetric.field.name}`}/>
            <CardItem key={'MinimumValue'}
                      label='Minimum value'
                      value={`${simulatedAppMetric.minimumValue}`}/>
            <CardItem key={'MaximumValue'}
                      label='Maximum value'
                      value={`${simulatedAppMetric.maximumValue}`}/>
            <CardItem key={'Override'}
                      label='Override'
                      value={`${simulatedAppMetric.override}`}/>
            <CardItem key={'Active'}
                      label='Active'
                      value={`${simulatedAppMetric.active}`}/>
        </CardSimulatedAppMetric>;
    }
}

const mapDispatchToProps: DispatchToProps = {
    deleteSimulatedAppMetric,
};

export default connect(null, mapDispatchToProps)(SimulatedAppMetricCard);
