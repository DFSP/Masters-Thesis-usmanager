/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import React from "react";
import {IRuleContainer} from "./RuleContainer";
import Card from "../../../components/cards/Card";
import CardItem from "../../../components/list/CardItem";
import {IRuleCondition} from "../conditions/RuleCondition";

interface ContainerRuleCardProps {
  rule: IRuleContainer;
}

type Props = ContainerRuleCardProps;

const CardRuleContainer = Card<IRuleContainer>();
const RuleContainerCard = ({rule}: Props) => (
  <CardRuleContainer title={rule.name}
                     link={{to: {pathname: `/rules/containers/${rule.name}`, state: rule}}}
                     height={'125px'}
                     margin={'10px 0'}
                     hoverable>
    <CardItem key={'priority'}
              label={'Priority'}
              value={`${rule.priority}`}/>
    <CardItem key={'decision'}
              label={'Decision'}
              value={`${rule.decision.value}`}/>
    <CardItem key={'generic'}
              label={'Generic'}
              value={`${rule.generic}`}/>
  </CardRuleContainer>
);

export default RuleContainerCard;