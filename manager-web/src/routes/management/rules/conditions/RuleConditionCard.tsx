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
import {IRuleCondition} from "./RuleCondition";

interface ConditionCardProps {
  condition: IRuleCondition;
}

type Props = ConditionCardProps;

const CardRuleCondition = Card<IRuleCondition>();
const RuleConditionCard = ({condition}: Props) => (
  <CardRuleCondition title={condition.name.toString()}
                     link={{to: {pathname: `/rules/conditions/${condition.name}`, state: condition}}}
                     height={'150px'}
                     margin={'10px 0'}
                     hoverable>
    <CardItem key={'valueMode'}
              label={'Value mode'}
              value={condition.valueMode.name}/>
    <CardItem key={'field'}
              label={'Field'}
              value={condition.field.name}/>
    <CardItem key={'operator'}
              label={'Operator'}
              value={condition.operator.symbol}/>
    <CardItem key={'value'}
              label={'Value'}
              value={condition.value.toString()}/>
  </CardRuleCondition>
);

export default RuleConditionCard;
