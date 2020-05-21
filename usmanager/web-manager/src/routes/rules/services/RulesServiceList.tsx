/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import {IRuleService} from "./RuleService";
import BaseComponent from "../../../components/BaseComponent";
import React from "react";
import RuleServiceCard from "./RuleServiceCard";
import CardList from "../../../components/list/CardList";
import {ReduxState} from "../../../reducers";
import {connect} from "react-redux";
import {loadRulesService} from "../../../actions";

interface StateToProps {
  isLoading: boolean
  error?: string | null;
  serviceRules: IRuleService[];
}

interface DispatchToProps {
  loadRulesService: () => any;
}

type Props = StateToProps & DispatchToProps;

class RulesServiceList extends BaseComponent<Props, {}> {

  componentDidMount(): void {
    this.props.loadRulesService();
  }

  private rule = (rule: IRuleService): JSX.Element =>
    <RuleServiceCard key={rule.id} rule={rule}/>;

  private predicate = (rule: IRuleService, search: string): boolean =>
    rule.name.toLowerCase().includes(search);

  render() {
    return (
      <CardList<IRuleService>
        isLoading={this.props.isLoading}
        error={this.props.error}
        emptyMessage={"No service rules to display"}
        list={this.props.serviceRules}
        card={this.rule}
        predicate={this.predicate}/>
    );
  }

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.rules.services.isLoadingRules,
    error: state.entities.rules.services.loadRulesError,
    serviceRules: Object.values(state.entities.rules.services.data)
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadRulesService,
};

export default connect(mapStateToProps, mapDispatchToProps)(RulesServiceList);