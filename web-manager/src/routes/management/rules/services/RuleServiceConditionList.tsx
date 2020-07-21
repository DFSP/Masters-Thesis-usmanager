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

import {IRuleService} from "./RuleService";
import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import React from "react";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {loadConditions, loadRuleServiceConditions, removeRuleServiceConditions} from "../../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {IRuleCondition} from "../conditions/RuleCondition";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  ruleConditions: string[];
  conditions: { [key: string]: IRuleCondition };
}

interface DispatchToProps {
  loadConditions: () => void;
  loadRuleServiceConditions: (ruleName: string) => void;
  removeRuleServiceConditions: (ruleName: string, conditions: string[]) => void;
}

interface ServiceRuleConditionListProps {
  isLoadingRuleService: boolean;
  loadRuleServiceError?: string | null;
  ruleService: IRuleService | Partial<IRuleService> | null;
  unsavedConditions: string[];
  onAddRuleCondition: (condition: string) => void;
  onRemoveRuleConditions: (condition: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceRuleConditionListProps

interface State {
  entitySaved: boolean;
}

class RuleServiceConditionList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {entitySaved: !this.isNew()};
  }

  public componentDidMount(): void {
    this.props.loadConditions();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevProps.ruleService?.name !== this.props.ruleService?.name) {
      this.loadEntities();
    }
    if (!prevProps.ruleService?.name && this.props.ruleService?.name) {
      this.setState({entitySaved: true});
    }
  }

  public render() {
    const isNew = this.isNew();
    return <ControlledList isLoading={!isNew ? this.props.isLoadingRuleService || this.props.isLoading : undefined}
                           error={!isNew ? this.props.loadRuleServiceError || this.props.error : undefined}
                           emptyMessage={`Conditions list is empty`}
                           data={this.props.ruleConditions}
                           dropdown={{
                             id: 'conditions',
                             title: 'Add condition',
                             empty: 'No more conditions to add',
                             data: this.getSelectableConditionNames()
                           }}
                           show={this.condition}
                           onAdd={this.onAdd}
                           onRemove={this.onRemove}
                           onDelete={{
                             url: `rules/services/${this.props.ruleService?.name}/conditions`,
                             successCallback: this.onDeleteSuccess,
                             failureCallback: this.onDeleteFailure
                           }}
                           entitySaved={this.state.entitySaved}/>;
  }

  private loadEntities = () => {
    if (this.props.ruleService?.name) {
      const {name} = this.props.ruleService;
      this.props.loadRuleServiceConditions(name);
    }
  };

  private isNew = () =>
    this.props.ruleService?.name === undefined;

  private condition = (index: number, condition: string, separate: boolean, checked: boolean,
                       handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const isNew = this.isNew();
    const unsaved = this.props.unsavedConditions.includes(condition);
    return (
      <ListItem key={index} separate={separate}>
        <div className={styles.linkedItemContent}>
          <label>
            <input id={condition}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                {condition}
              </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/rules/conditions/${condition}`}
                className={`${styles.link} waves-effect`}>
            <i className={`${styles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (condition: string): void =>
    this.props.onAddRuleCondition(condition);

  private onRemove = (conditions: string[]) =>
    this.props.onRemoveRuleConditions(conditions);

  private onDeleteSuccess = (conditions: string[]): void => {
    if (this.props.ruleService?.name) {
      const {name} = this.props.ruleService;
      this.props.removeRuleServiceConditions(name, conditions);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to delete condition`, 10000, reason, true);

  private getSelectableConditionNames = () => {
    const {conditions, ruleConditions, unsavedConditions} = this.props;
    return Object.keys(conditions)
                 .filter(condition => !ruleConditions.includes(condition) && !unsavedConditions.includes(condition));
  };

}

function mapStateToProps(state: ReduxState, ownProps: ServiceRuleConditionListProps): StateToProps {
  const ruleName = ownProps.ruleService?.name;
  const rule = ruleName && state.entities.rules.services.data[ruleName];
  const ruleConditions = rule && rule.conditions;
  return {
    isLoading: state.entities.rules.services.isLoadingConditions,
    error: state.entities.rules.services.loadConditionsError,
    ruleConditions: ruleConditions || [],
    conditions: state.entities.rules.conditions.data,
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadRuleServiceConditions,
    removeRuleServiceConditions,
    loadConditions,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(RuleServiceConditionList);