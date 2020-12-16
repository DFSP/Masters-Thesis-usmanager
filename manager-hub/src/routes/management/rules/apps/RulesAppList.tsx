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

import {IRuleApp} from "./RuleApp";
import BaseComponent from "../../../../components/BaseComponent";
import React from "react";
import RuleAppCard from "./RuleAppCard";
import CardList from "../../../../components/list/CardList";
import {ReduxState} from "../../../../reducers";
import {connect} from "react-redux";
import {loadRulesApp} from "../../../../actions";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    appRules: IRuleApp[];
}

interface DispatchToProps {
    loadRulesApp: () => any;
}

type Props = StateToProps & DispatchToProps;

class RulesAppList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadRulesApp();
    }

    public render() {
        return (
            <CardList<IRuleApp>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não estão definidas regras aplicadas a aplicações"}
                list={this.props.appRules}
                card={this.rule}
                predicate={this.predicate}/>
        );
    }

    private rule = (rule: IRuleApp): JSX.Element =>
        <RuleAppCard key={rule.id} rule={rule}/>;

    private predicate = (rule: IRuleApp, search: string): boolean =>
        rule.name.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.rules.apps.isLoadingRules,
        error: state.entities.rules.apps.loadRulesError,
        appRules: (state.entities.rules.apps.data && Object.values(state.entities.rules.apps.data).reverse()) || []
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadRulesApp,
};

export default connect(mapStateToProps, mapDispatchToProps)(RulesAppList);