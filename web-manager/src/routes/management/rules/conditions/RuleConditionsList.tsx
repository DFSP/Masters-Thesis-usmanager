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

  private condition = (condition: IRuleCondition): JSX.Element =>
    <RuleConditionCard key={condition.id} condition={condition}/>;

  private predicate = (condition: IRuleCondition, search: string): boolean =>
    condition.name.toString().toLowerCase().includes(search);

  public render() {
    return (
      <CardList<IRuleCondition>
        isLoading={this.props.isLoading}
        error={this.props.error}
        emptyMessage={"No conditions to display"}
        list={this.props.conditions}
        card={this.condition}
        predicate={this.predicate}/>
    )
  }

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.rules.conditions.isLoadingConditions,
    error: state.entities.rules.conditions.loadConditionsError,
    conditions: (state.entities.rules.conditions.data && Object.values(state.entities.rules.conditions.data)) || [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadConditions,
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleConditionsList);
