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
import {ISimulatedServiceMetric} from "./SimulatedServiceMetric";

interface SimulatedServiceMetricCardProps {
  simulatedServiceMetric: ISimulatedServiceMetric;
}

type Props = SimulatedServiceMetricCardProps;

const CardSimulatedServiceMetric = Card<ISimulatedServiceMetric>();
const SimulatedServiceMetricCard = ({simulatedServiceMetric}: Props) => (
  <CardSimulatedServiceMetric title={simulatedServiceMetric.name}
                              link={{
                                to: {
                                  pathname: `/simulated-metrics/services/${simulatedServiceMetric.name}`,
                                  state: simulatedServiceMetric
                                }
                              }}
                              height={'180px'}
                              margin={'10px 0'}
                              hoverable>
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
);

export default SimulatedServiceMetricCard;
