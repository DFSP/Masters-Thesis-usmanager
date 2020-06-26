import React from "react";
import {IField, IOperator, IValueMode} from "../Rule";
import IDatabaseData from "../../../components/IDatabaseData";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../components/BaseComponent";
import Form, {IFields, requiredAndNumberAndMin, requiredAndTrimmed} from "../../../components/form/Form";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
  addCondition,
  loadConditions,
  loadFields,
  loadOperators,
  loadValueModes,
  updateCondition
} from "../../../actions";
import {connect} from "react-redux";
import {IReply} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IRegion} from "../../region/Region";
import {INode} from "../../nodes/Node";

export interface IRuleCondition extends IDatabaseData {
  name: string;
  valueMode: IValueMode;
  field: IField;
  operator: IOperator;
  value: number;
}

const buildNewCondition = (): Partial<IRuleCondition> => ({
  name: undefined,
  valueMode: undefined,
  field: undefined,
  operator: undefined,
  value: undefined
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  condition: Partial<IRuleCondition>;
  formCondition?: Partial<IRuleCondition>,
  valueModes: { [key:string]: IValueMode };
  fields: { [key:string]: IField };
  operators: { [key:string]: IOperator };
}

interface DispatchToProps {
  loadConditions: (name: string) => void;
  addCondition: (condition: IRuleCondition) => void;
  updateCondition: (previousCondition: IRuleCondition, currentCondition: IRuleCondition) => void;
  loadValueModes: () => void;
  loadFields: () => void;
  loadOperators: () => void;
}

interface MatchParams {
  name: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

type State = {
  condition?: IRuleCondition,
  formCondition?: IRuleCondition,
}

class RuleCondition extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
  };

  public componentDidMount(): void {
    this.loadCondition();
    this.props.loadValueModes();
    this.props.loadFields();
    this.props.loadOperators();
    this.mounted = true;
  };

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadCondition = () => {
    if (!isNew(this.props.location.search)) {
      const conditionName = this.props.match.params.name;
      this.props.loadConditions(conditionName);
    }
  };

  private getCondition = () =>
    this.state.condition || this.props.condition;

  private getFormCondition = () =>
    this.state.formCondition || this.props.formCondition;

  private isNew = () =>
    isNew(this.props.location.search);

  private onPostSuccess = (reply: IReply<IRuleCondition>): void => {
    const condition = reply.data;
    super.toast(`<span class="green-text">Condition ${this.mounted ? `<b class="white-text">${condition.name}</b>` : `<a href=/rules/conditions/${condition.name}><b>${condition.name}</b></a>`} saved</span>`);
    this.props.addCondition(condition);
    if (this.mounted) {
      this.updateCondition(condition);
      this.props.history.replace(condition.name);
    }
  };

  private onPostFailure = (reason: string, condition: IRuleCondition): void =>
    super.toast(`Unable to save <b>${condition.name}</b> condition`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<IRuleCondition>): void => {
    const condition = reply.data;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${condition.name}</b>` : `<a href=/rules/conditions/${condition.name}><b>${condition.name}</b></a>`} condition have been saved</span>`);
    if (this.mounted) {
      this.updateCondition(condition);
      this.props.history.replace(condition.name);
    }
  };

  private onPutFailure = (reason: string, condition: IRuleCondition): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${condition.name}</b>` : `<a href=/rules/conditions/${condition.name}><b>${condition.name}</b></a>`} condition`, 10000, reason, true);

  private onDeleteSuccess = (condition: IRuleCondition): void => {
    super.toast(`<span class="green-text">Condition <b class="white-text">${condition.name}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/rules/conditions`)
    }
  };

  private onDeleteFailure = (reason: string, condition: IRuleCondition): void =>
    super.toast(`Unable to delete ${this.mounted ? <b>${condition.name}</b> : `<a href=/rules/conditions/${condition.name}><b>${condition.name}</b></a>`} condition`, 10000, reason, true);

  private updateCondition = (condition: IRuleCondition) => {
    condition = Object.values(normalize(condition, Schemas.RULE_CONDITION).entities.conditions || {})[0];
    const previousCondition = this.getCondition();
    if (previousCondition?.id) {
      this.props.updateCondition(previousCondition as IRuleCondition, condition)
    }
    const formCondition = { ...condition };
    removeFields(formCondition);
    this.setState({condition: condition, formCondition: formCondition});
  };

  private getFields = (condition: Partial<IRuleCondition>): IFields =>
    Object.entries(condition).map(([key, value]) => {
      return {
        [key]: {
          id: key,
          label: key,
          validation: getTypeFromValue(value) === 'number'
            ? { rule: requiredAndNumberAndMin, args: 0 }
            : { rule: requiredAndTrimmed }
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private fieldOption = (field: IField): string =>
    field.name;

  private operatorOption = (operator: IOperator): string =>
    operator.symbol;

  private valueModeOption = (valueMode: IValueMode): string =>
    valueMode.name;

  private condition = () => {
    const {isLoading, error} = this.props;
    const condition = this.getCondition();
    const formCondition = this.getFormCondition();
    // @ts-ignore
    const conditionKey: (keyof IRuleCondition) = formCondition && Object.keys(formCondition)[0];
    const isNewRuleCondition = this.isNew();
    return (
      <>
        {!isNewRuleCondition && isLoading && <ListLoadingSpinner/>}
        {!isNewRuleCondition && !isLoading && error && <Error message={error}/>}
        {(isNewRuleCondition || !isLoading) && (isNewRuleCondition || !error) && formCondition && (
          <Form id={conditionKey}
                fields={this.getFields(formCondition)}
                values={condition}
                isNew={isNew(this.props.location.search)}
                post={{
                  url: 'rules/conditions',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                put={{
                  url: `rules/conditions/${condition.name}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `rules/conditions/${condition.name}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}>
            <Field key='name' id={'name'} label='name'/>
            <Field<IField> key='fields'
                           id='field'
                           label='field'
                           type='dropdown'
                           dropdown={{
                             defaultValue: "Select field",
                             values: Object.values(this.props.fields),
                             optionToString: this.fieldOption}}/>
            <Field<IOperator> key='operators'
                              id='operator'
                              label='operator'
                              type='dropdown'
                              dropdown={{
                                defaultValue: "Select operator",
                                values: Object.values(this.props.operators),
                                optionToString: this.operatorOption}}/>
            <Field<IValueMode> key='valueModes'
                               id='valueMode'
                               label='valueMode'
                               type='dropdown'
                               dropdown={{
                                 defaultValue: 'Select value mode',
                                 values: Object.values(this.props.valueModes),
                                 optionToString: this.valueModeOption}}/>
            <Field key='value' id='value' label='value' type="number"/>
          </Form>
        )}
      </>
    )
  };

  private tabs = (): Tab[] => [
    {
      title: 'Condition',
      id: 'condition',
      content: () => this.condition()
    },
  ];

  public render() {
    return (
      <MainLayout>
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs()}/>
        </div>
      </MainLayout>
    );
  }

}

function removeFields(condition: Partial<IRuleCondition>) {
  delete condition["id"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.rules.conditions.isLoadingConditions;
  const error = state.entities.rules.conditions.loadConditionsError;
  const name = props.match.params.name;
  const condition = isNew(props.location.search) ? buildNewCondition() : state.entities.rules.conditions.data[name];
  let formCondition;
  if (condition) {
    formCondition = { ...condition };
    removeFields(formCondition);
  }
  const valueModes = state.entities.valueModes.data;
  const fields = state.entities.fields.data;
  const operators = state.entities.operators.data;
  return  {
    isLoading,
    error,
    condition,
    formCondition,
    valueModes,
    fields,
    operators,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadConditions,
  addCondition,
  updateCondition,
  loadValueModes,
  loadFields,
  loadOperators,
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleCondition);