/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import {IDecision, IRule} from "../Rule";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../components/BaseComponent";
import Form, {
  IFields,
  requiredAndNumberAndMinAndMax,
  requiredAndTrimmed
} from "../../../components/form/Form";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {connect} from "react-redux";
import React from "react";
import {
  addRuleCloudHosts,
  addRuleHostConditions,
  addRuleEdgeHosts,
  loadDecisions,
  loadRulesHost, addRuleHost,
} from "../../../actions";
import {IReply, postData} from "../../../utils/api";
import HostRuleConditionList from "./RuleHostConditionList";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import HostRuleCloudHostsList from "./RuleHostCloudHostsList";
import HostRuleEdgeHostsList from "./RuleHostEdgeHostsList";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";

export interface IRuleHost extends IRule {
  cloudHosts?: string[],
  edgeHosts?: string[],
}

const buildNewHostRule = (): Partial<IRuleHost> => ({
  name: undefined,
  priority: undefined,
  generic: undefined,
  decision: undefined,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  ruleHost: Partial<IRuleHost>;
  formRuleHost?: Partial<IRuleHost>,
  decisions: { [key: string]: IDecision },
}

interface DispatchToProps {
  loadRulesHost: (name: string) => void;
  addRuleHost: (ruleHost: IRuleHost) => void;
  //TODO updateRuleHost: (previousRuleHost: Partial<IRuleHost>, ruleHost: IRuleHost) => void;
  loadDecisions: () => void;
  addRuleHostConditions: (ruleName: string, conditions: string[]) => void;
  addRuleCloudHosts: (ruleName: string, cloudHosts: string[]) => void;
  addRuleEdgeHosts: (ruleName: string, edgeHosts: string[]) => void;
}

interface MatchParams {
  name: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

type State = {
  ruleHost?: IRuleHost,
  formRuleHost?: IRuleHost,
  unsavedConditions: string[],
  unsavedCloudHosts: string[],
  unsavedEdgeHosts: string[],
  isGeneric: boolean,
}

class RuleHost extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
    unsavedConditions: [],
    unsavedCloudHosts: [],
    unsavedEdgeHosts: [],
    isGeneric: this.props.ruleHost?.generic || false,
  };

  public componentDidMount(): void {
    this.loadRuleHost();
    this.props.loadDecisions();
    this.mounted = true;
  };

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadRuleHost = () => {
    if (!isNew(this.props.location.search)) {
      const ruleName = this.props.match.params.name;
      this.props.loadRulesHost(ruleName);
    }
  };

  private getRuleHost = () =>
    this.state.ruleHost || this.props.ruleHost;

  private getFormRuleHost = () =>
    this.state.formRuleHost || this.props.formRuleHost;

  private onPostSuccess = (reply: IReply<IRuleHost>): void => {
    const ruleHost = reply.data;
    super.toast(`<span class="green-text">Host rule ${this.mounted ? `<b class="white-text">${ruleHost.name}</b>` : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} saved</span>`);
    this.props.addRuleHost(ruleHost);
    this.saveEntities(reply.data);
    if (this.mounted) {
      this.updateRuleHost(ruleHost);
      this.props.history.replace(ruleHost.name);
    }
  };

  private onPostFailure = (reason: string, ruleHost: IRuleHost): void =>
    super.toast(`Unable to save <b>${ruleHost.name}</b> host rule`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<IRuleHost>): void => {
    const ruleHost = reply.data;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${ruleHost.name}</b>` : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} host rule have been saved</span>`);
    this.saveEntities(ruleHost);
    if (this.mounted) {
      this.updateRuleHost(ruleHost);
      this.props.history.replace(ruleHost.name);
    }
  };

  private onPutFailure = (reason: string, ruleHost: IRuleHost): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} host rule`, 10000, reason, true);

  private onDeleteSuccess = (ruleHost: IRuleHost): void => {
    super.toast(`<span class="green-text">Host rule <b class="white-text">${ruleHost.name}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/rules/hosts`)
    }
  };

  private onDeleteFailure = (reason: string, ruleHost: IRuleHost): void =>
    super.toast(`Unable to delete ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} host rule`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedConditions.length
    || !!this.state.unsavedCloudHosts.length
    || !!this.state.unsavedEdgeHosts.length;

  private saveEntities = (rule: IRuleHost) => {
    this.saveRuleConditions(rule);
    this.saveRuleCloudHosts(rule);
    this.saveRuleEdgeHosts(rule);
  };

  private addRuleCondition = (condition: string): void => {
    this.setState({
      unsavedConditions: this.state.unsavedConditions.concat(condition)
    });
  };

  private removeRuleConditions = (conditions: string[]): void => {
    this.setState({
      unsavedConditions: this.state.unsavedConditions.filter(condition => !conditions.includes(condition))
    });
  };

  private saveRuleConditions = (rule: IRuleHost): void => {
    const {unsavedConditions} = this.state;
    if (unsavedConditions.length) {
      postData(`rules/hosts/${rule.name}/conditions`, unsavedConditions,
        () => this.onSaveConditionsSuccess(rule),
        (reason) => this.onSaveConditionsFailure(rule, reason));
    }
  };

  private onSaveConditionsSuccess = (rule: IRuleHost): void => {
    this.props.addRuleHostConditions(rule.name, this.state.unsavedConditions);
    if (this.mounted) {
      this.setState({ unsavedConditions: [] });
    }
  };

  private onSaveConditionsFailure = (ruleHost: IRuleHost, reason: string): void =>
    super.toast(`Unable to save conditions of ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} host rule`, 10000, reason, true);

  private addRuleCloudHost = (cloudHost: string): void =>
    this.setState({
      unsavedCloudHosts: this.state.unsavedCloudHosts.concat(cloudHost)
    });

  private removeRuleCloudHosts = (cloudHosts: string[]): void => {
    this.setState({
      unsavedCloudHosts: this.state.unsavedCloudHosts.filter(cloudHost => !cloudHosts.includes(cloudHost))
    });
  };

  private saveRuleCloudHosts = (rule: IRuleHost): void => {
    const {unsavedCloudHosts} = this.state;
    if (unsavedCloudHosts.length) {
      postData(`rules/hosts/${rule.name}/cloud-hosts`, unsavedCloudHosts,
        () => this.onSaveCloudHostsSuccess(rule),
        (reason) => this.onSaveCloudHostsFailure(rule, reason));
    }
  };

  private onSaveCloudHostsSuccess = (rule: IRuleHost): void => {
    this.props.addRuleCloudHosts(rule.name, this.state.unsavedCloudHosts);
    if (this.mounted) {
      this.setState({ unsavedCloudHosts: [] });
    }
  };

  private onSaveCloudHostsFailure = (ruleHost: IRuleHost, reason: string): void =>
    super.toast(`Unable to save cloud hosts of ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} host rule`, 10000, reason, true);

  private addRuleEdgeHost = (edgeHost: string): void =>
    this.setState({
      unsavedEdgeHosts: this.state.unsavedEdgeHosts.concat(edgeHost)
    });

  private removeRuleEdgeHosts = (edgeHosts: string[]): void => {
    this.setState({
      unsavedEdgeHosts: this.state.unsavedEdgeHosts.filter(edgeHost => !edgeHosts.includes(edgeHost))
    });
  };

  private saveRuleEdgeHosts = (rule: IRuleHost): void => {
    const {unsavedEdgeHosts} = this.state;
    if (unsavedEdgeHosts.length) {
      postData(`rules/hosts/${rule.name}/edge-hosts`, unsavedEdgeHosts,
        () => this.onSaveEdgeHostsSuccess(rule),
        (reason) => this.onSaveEdgeHostsFailure(rule, reason));
    }
  };

  private onSaveEdgeHostsSuccess = (rule: IRuleHost): void => {
    this.props.addRuleEdgeHosts(rule.name, this.state.unsavedEdgeHosts);
    if (this.mounted) {
      this.setState({ unsavedEdgeHosts: [] });
    }
  };

  private onSaveEdgeHostsFailure = (ruleHost: IRuleHost, reason: string): void =>
    super.toast(`Unable to save edge hosts of ${this.mounted ? <b>${ruleHost.name}</b> : `<a href=/rules/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} host rule`, 10000, reason, true);

  private updateRuleHost = (ruleHost: IRuleHost) => {
    //const previousRuleHost = this.getRuleHost();
    ruleHost = Object.values(normalize(ruleHost, Schemas.RULE_HOST).entities.hostRules || {})[0];
    //TODO this.props.updateRuleHost(previousRuleHost, ruleHost);
    const formRuleHost = { ...ruleHost };
    removeFields(formRuleHost);
    this.setState({ruleHost: ruleHost, formRuleHost: formRuleHost});
  };

  private getFields = (hostRule: Partial<IRuleHost>): IFields =>
    Object.entries(hostRule).map(([key, _]) => {
      return {
        [key]: {
          id: key,
          label: key,
          validation:
            key == 'priority'
              ? { rule: requiredAndNumberAndMinAndMax, args: [0, 2147483647] }
              : { rule: requiredAndTrimmed }
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private decisionDropdownOption = (decision: IDecision) =>
    decision.name;

  private isGenericSelected = (value: string) =>
    this.setState({isGeneric: value.toLowerCase() === 'true'});

  private getSelectableDecisions = () =>
    Object.values(this.props.decisions).filter(decision => decision.componentType.name.toLowerCase() == 'host');

  private hostRule = () => {
    const {isLoading, error} = this.props;
    const ruleHost = this.getRuleHost();
    const formRuleHost = this.getFormRuleHost();
    // @ts-ignore
    const ruleKey: (keyof IRuleHost) = formRuleHost && Object.keys(formRuleHost)[0];
    return (
      <>
        {isLoading && <ListLoadingSpinner/>}
        {!isLoading && error && <Error message={error}/>}
        {!isLoading && !error && formRuleHost && (
          <Form id={ruleKey}
                fields={this.getFields(formRuleHost)}
                values={ruleHost}
                isNew={isNew(this.props.location.search)}
                showSaveButton={this.shouldShowSaveButton()}
                post={{
                  url: 'rules/hosts',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                put={{
                  url: `rules/hosts/${ruleHost.name}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `rules/hosts/${ruleHost.name}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}
                saveEntities={this.saveEntities}>
            {Object.entries(formRuleHost).map(([key, value], index) =>
              key === 'decision'
                ? <Field<IDecision> key={index}
                                    id={key}
                                    label={key}
                                    type="dropdown"
                                    dropdown={{
                                      defaultValue: "Choose decision",
                                      values: this.getSelectableDecisions(),
                                      optionToString: this.decisionDropdownOption}}/>
                : key === 'generic'
                ? <Field key={index}
                         id={key}
                         label={key}
                         type="dropdown"
                         dropdown={{
                           selectCallback: this.isGenericSelected,
                           defaultValue: "Apply to all hosts?",
                           values: ['True', 'False']}}/>
                : <Field key={index}
                         id={key}
                         label={key}
                         type={key === 'priority' ? "number" : "text"}/>
            )}
          </Form>
        )}
      </>
    )
  };

  private conditions = (): JSX.Element =>
    <HostRuleConditionList isLoadingHostRule={this.props.isLoading}
                           loadHostRuleError={this.props.error}
                           ruleHost={this.props.ruleHost}
                           unsavedConditions={this.state.unsavedConditions}
                           onAddRuleCondition={this.addRuleCondition}
                           onRemoveRuleConditions={this.removeRuleConditions}/>;

  private cloudHosts = (): JSX.Element =>
    <HostRuleCloudHostsList isLoadingHostRule={this.props.isLoading}
                            loadHostRuleError={this.props.error}
                            ruleHost={this.props.ruleHost}
                            unsavedCloudHosts={this.state.unsavedCloudHosts}
                            onAddRuleCloudHost={this.addRuleCloudHost}
                            onRemoveRuleCloudHosts={this.removeRuleCloudHosts}/>;

  private edgeHosts = (): JSX.Element =>
    <HostRuleEdgeHostsList isLoadingHostRule={this.props.isLoading}
                           loadHostRuleError={this.props.error}
                           ruleHost={this.props.ruleHost}
                           unsavedEdgeHosts={this.state.unsavedEdgeHosts}
                           onAddRuleEdgeHost={this.addRuleEdgeHost}
                           onRemoveRuleEdgeHosts={this.removeRuleEdgeHosts}/>;

  private tabs = () => [
    {
      title: 'Host rule',
      id: 'hostRule',
      content: () => this.hostRule(),
    },
    {
      title: 'Conditions',
      id: 'ruleConditions',
      content: () => this.conditions(),
    },
    {
      title: 'Cloud hosts',
      id: 'cloudHosts',
      content: () => this.cloudHosts(),
      disabled: this.state.isGeneric,
    },
    {
      title: 'Edge hosts',
      id: 'edgeHosts',
      content: () => this.edgeHosts(),
      disabled: this.state.isGeneric
    }
  ];

  render() {
    return (
      <MainLayout>
        {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs()}/>
        </div>
      </MainLayout>
    );
  }

}

function removeFields(ruleHost: Partial<IRuleHost>) {
  delete ruleHost["id"];
  delete ruleHost["conditions"];
  delete ruleHost["cloudHosts"];
  delete ruleHost["edgeHosts"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.rules.hosts.isLoadingRules;
  const error = state.entities.rules.hosts.loadRulesError;
  const name = props.match.params.name;
  const ruleHost = isNew(props.location.search) ? buildNewHostRule() : state.entities.rules.hosts.data[name];
  let formRuleHost;
  if (ruleHost) {
    formRuleHost = { ...ruleHost };
    removeFields(formRuleHost);
  }
  const decisions = state.entities.decisions.data;
  return  {
    isLoading,
    error,
    ruleHost,
    formRuleHost,
    decisions,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadRulesHost,
  addRuleHost,
  //TODO updateRuleHost,
  loadDecisions,
  addRuleHostConditions,
  addRuleCloudHosts,
  addRuleEdgeHosts
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleHost);
