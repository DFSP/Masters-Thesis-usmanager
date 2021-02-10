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
import {ISimulatedContainerMetric} from "./SimulatedContainerMetric";
import BaseComponent from "../../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../../components/contextmenu/LinkedContextMenuItem";
import {deleteSimulatedContainerMetric} from "../../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface SimulatedContainerMetricCardProps {
    simulatedContainerMetric: ISimulatedContainerMetric;
}

interface DispatchToProps {
    deleteSimulatedContainerMetric: (simulatedContainerMetric: ISimulatedContainerMetric) => void;
}

type Props = DispatchToProps & SimulatedContainerMetricCardProps;

class SimulatedContainerMetricCard extends BaseComponent<Props, State> {

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

    private onDeleteSuccess = (simulatedMetric: ISimulatedContainerMetric): void => {
        super.toast(`<span class="green-text">A métrica simulada <b>${simulatedMetric.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteSimulatedContainerMetric(simulatedMetric);
    }

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedContainerMetric): void => {
        super.toast(`Não foi possível remover a métrica simulada <a href='/métricas simuladas/contentores/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {simulatedContainerMetric} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Alterar a lista de contentores'}
                pathname={`/métricas simuladas/contentores/${simulatedContainerMetric.name}`}
                selected={'containers'}
                state={simulatedContainerMetric}/>,
        ];
    }

    public render() {
        const {simulatedContainerMetric} = this.props;
        const {loading} = this.state;
        const CardSimulatedContainerMetric = Card<ISimulatedContainerMetric>();
        return <CardSimulatedContainerMetric id={`simulated-container-metric-${simulatedContainerMetric.id}`}
                                             title={simulatedContainerMetric.name}
                                             link={{
                                                 to: {
                                                     pathname: `/métricas simuladas/contentores/${simulatedContainerMetric.name}`,
                                                     state: simulatedContainerMetric
                                                 }
                                             }}
                                             height={'185px'}
                                             margin={'10px 0'}
                                             hoverable
                                             delete={{
                                                 url: `simulated-metrics/containers/${simulatedContainerMetric.name}`,
                                                 successCallback: this.onDeleteSuccess,
                                                 failureCallback: this.onDeleteFailure,
                                             }}
                                             loading={loading}
                                             bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'Field'}
                      label={'Field'}
                      value={`${simulatedContainerMetric.field.name}`}/>
            <CardItem key={'MinimumValue'}
                      label='Minimum value'
                      value={`${simulatedContainerMetric.minimumValue}`}/>
            <CardItem key={'MaximumValue'}
                      label='Maximum value'
                      value={`${simulatedContainerMetric.maximumValue}`}/>
            <CardItem key={'Override'}
                      label='Override'
                      value={`${simulatedContainerMetric.override}`}/>
            <CardItem key={'Active'}
                      label='Active'
                      value={`${simulatedContainerMetric.active}`}/>
        </CardSimulatedContainerMetric>;
    }
}

const mapDispatchToProps: DispatchToProps = {
    deleteSimulatedContainerMetric,
};

export default connect(null, mapDispatchToProps)(SimulatedContainerMetricCard);
