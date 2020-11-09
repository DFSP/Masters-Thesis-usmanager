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
import {IRuleCondition} from "./RuleCondition";
import BaseComponent from "../../../../components/BaseComponent";
import CardList from "../../../../components/list/CardList";
import {ReduxState} from "../../../../reducers";
import {connect} from "react-redux";
import RuleConditionCard from "./RuleConditionCard";
import {loadConditions} from "../../../../actions";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    conditions: IRuleCondition[];
}

interface DispatchToProps {
    loadConditions: (id?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class RuleConditionsList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadConditions();
    }

    public render() {
        return (
            <CardList<IRuleCondition>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não há nenhuma condição para mostrar"}
                list={this.props.conditions}
                card={this.condition}
                predicate={this.predicate}/>
        )
    }

    private condition = (condition: IRuleCondition): JSX.Element =>
        <RuleConditionCard key={condition.id} condition={condition}/>;

    private predicate = (condition: IRuleCondition, search: string): boolean =>
        condition.name.toString().toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.rules.conditions.isLoadingConditions,
        error: state.entities.rules.conditions.loadConditionsError,
        conditions: (state.entities.rules.conditions.data && Object.values(state.entities.rules.conditions.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadConditions,
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleConditionsList);
