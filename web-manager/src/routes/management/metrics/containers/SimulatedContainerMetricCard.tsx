/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import Card from "../../../../components/cards/Card";
import CardItem from "../../../../components/list/CardItem";
import React from "react";
import {ISimulatedContainerMetric} from "./SimulatedContainerMetric";

interface SimulatedContainerMetricCardProps {
  simulatedContainerMetric: ISimulatedContainerMetric;
}

type Props = SimulatedContainerMetricCardProps;

const CardSimulatedContainerMetric = Card<ISimulatedContainerMetric>();
const SimulatedContainerMetricCard = ({simulatedContainerMetric}: Props) => (
  <CardSimulatedContainerMetric title={simulatedContainerMetric.name}
                                link={{
                                  to: {
                                    pathname: `/simulated-metrics/containers/${simulatedContainerMetric.name}`,
                                    state: simulatedContainerMetric
                                  }
                                }}
                                height={'170px'}
                                margin={'10px 0'}
                                hoverable>
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
    <CardItem key={'Generic'}
              label='Generic'
              value={`${simulatedContainerMetric.generic}`}/>
  </CardSimulatedContainerMetric>
);

export default SimulatedContainerMetricCard;
