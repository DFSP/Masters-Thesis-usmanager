import {IRuleContainer} from "./RuleContainer";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import React from "react";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {
  loadConditions,
  loadRuleContainerConditions,
  removeRuleContainerConditions
} from "../../../actions";
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
  loadRuleContainerConditions: (ruleName: string) => void;
  removeRuleContainerConditions: (ruleName: string, conditions: string[]) => void;
}

interface ContainerRuleConditionListProps {
  isLoadingRuleContainer: boolean;
  loadRuleContainerError?: string | null;
  ruleContainer: IRuleContainer | Partial<IRuleContainer> | null;
  unsavedConditions: string[];
  onAddRuleCondition: (condition: string) => void;
  onRemoveRuleConditions: (condition: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ContainerRuleConditionListProps

interface State {
  entitySaved: boolean;
}

class RuleContainerConditionList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { entitySaved: !this.isNew() };
  }

  public componentDidMount(): void {
    this.props.loadConditions();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevProps.ruleContainer?.name !== this.props.ruleContainer?.name) {
      this.loadEntities();
    }
    if (!prevProps.ruleContainer?.name && this.props.ruleContainer?.name) {
      this.setState({entitySaved: true});
    }
  }

  private loadEntities = () => {
    if (this.props.ruleContainer?.name) {
      const {name} = this.props.ruleContainer;
      this.props.loadRuleContainerConditions(name);
    }
  };

  private isNew = () =>
    this.props.ruleContainer?.name === undefined;

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
    if (this.props.ruleContainer?.name) {
      const {name} = this.props.ruleContainer;
      this.props.removeRuleContainerConditions(name, conditions);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to delete condition`, 10000, reason, true);

  private getSelectableConditionNames = () => {
    const {conditions, ruleConditions, unsavedConditions} = this.props;
    return Object.keys(conditions)
                 .filter(condition => !ruleConditions.includes(condition) && !unsavedConditions.includes(condition));
  };

  public render() {
    return <ControlledList isLoading={this.props.isLoadingRuleContainer || this.props.isLoading}
                           error={this.props.loadRuleContainerError || this.props.error}
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
                             url: `rules/containers/${this.props.ruleContainer?.name}/conditions`,
                             successCallback: this.onDeleteSuccess,
                             failureCallback: this.onDeleteFailure
                           }}
                           entitySaved={this.state.entitySaved}/>;
  }

}

function mapStateToProps(state: ReduxState, ownProps: ContainerRuleConditionListProps): StateToProps {
  const ruleName = ownProps.ruleContainer?.name;
  const rule = ruleName && state.entities.rules.containers.data[ruleName];
  const ruleConditions = rule && rule.conditions;
  return {
    isLoading: state.entities.rules.containers.isLoadingConditions,
    error: state.entities.rules.containers.loadConditionsError,
    ruleConditions: ruleConditions || [],
    conditions: state.entities.rules.conditions.data,
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadRuleContainerConditions,
    removeRuleContainerConditions,
    loadConditions,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(RuleContainerConditionList);