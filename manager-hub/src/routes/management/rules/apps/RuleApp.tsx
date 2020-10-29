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

import {componentTypes, IDecision, IRule} from "../Rule";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../../components/BaseComponent";
import Form, {IFields, requiredAndNumberAndMinAndMax, requiredAndTrimmed} from "../../../../components/form/Form";
import LoadingSpinner from "../../../../components/list/LoadingSpinner";
import {Error} from "../../../../components/errors/Error";
import Field from "../../../../components/form/Field";
import Tabs, {Tab} from "../../../../components/tabs/Tabs";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../../reducers";
import {
    addRuleApp,
    addRuleAppConditions,
    addRuleApps,
    loadDecisions,
    loadRulesApp,
    updateRuleApp,
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../../utils/api";
import RuleAppConditionList from "./RuleAppConditionsList";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import RuleAppAppsList from "./RuleAppAppsList";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";

export interface IRuleApp extends IRule {
    apps?: string[]
}

const buildNewAppRule = (): Partial<IRuleApp> => ({
    name: undefined,
    priority: 0,
    decision: undefined,
});


interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    ruleApp: Partial<IRuleApp>;
    formRuleApp?: Partial<IRuleApp>,
    decisions: { [key: string]: IDecision },
}

interface DispatchToProps {
    loadRulesApp: (name: string) => void;
    addRuleApp: (ruleApp: IRuleApp) => void;
    updateRuleApp: (previousRuleApp: IRuleApp, currentRuleApp: IRuleApp) => void;
    loadDecisions: () => void;
    addRuleAppConditions: (ruleName: string, conditions: string[]) => void;
    addRuleApps: (ruleName: string, apps: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IRuleApp,
    selected: 'appRule' | 'ruleConditions' | 'apps'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

type State = {
    ruleApp?: IRuleApp,
    formRuleApp?: IRuleApp,
    unsavedConditions: string[],
    unsavedApps: string[],
}

class RuleApp extends BaseComponent<Props, State> {

    state: State = {
        unsavedConditions: [],
        unsavedApps: [],
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadRuleApp();
        this.props.loadDecisions();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadRuleApp = () => {
        if (!isNew(this.props.location.search)) {
            const ruleName = this.props.match.params.name;
            this.props.loadRulesApp(ruleName);
        }
    };

    private getRuleApp = () =>
        this.state.ruleApp || this.props.ruleApp;

    private getFormRuleApp = () =>
        this.state.formRuleApp || this.props.formRuleApp;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IRuleApp>): void => {
        const ruleApp = reply.data;
        super.toast(`<span class="green-text">App rule ${this.mounted ? `<b class="white-text">${ruleApp.name}</b>` : `<a href=/rules/apps/${ruleApp.name}><b>${ruleApp.name}</b></a>`} saved</span>`);
        this.props.addRuleApp(ruleApp);
        this.saveEntities(reply.data);
        if (this.mounted) {
            this.updateRuleApp(ruleApp);
            this.props.history.replace(ruleApp.name);
        }
    };

    private onPostFailure = (reason: string, ruleApp: IRuleApp): void =>
        super.toast(`Unable to save app rule <b>${ruleApp.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<IRuleApp>): void => {
        const ruleApp = reply.data;
        super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${ruleApp.name}</b>` : `<a href=/rules/apps/${ruleApp.name}><b>${ruleApp.name}</b></a>`} app rule have been saved</span>`);
        this.saveEntities(ruleApp);
        const previousRuleApp = this.getRuleApp();
        if (previousRuleApp?.id) {
            this.props.updateRuleApp(previousRuleApp as IRuleApp, ruleApp)
        }
        if (this.mounted) {
            this.updateRuleApp(ruleApp);
            this.props.history.replace(ruleApp.name);
        }
    };

    private onPutFailure = (reason: string, ruleApp: IRuleApp): void =>
        super.toast(`Unable to update app rule ${this.mounted ? `<b>${ruleApp.name}</b>` : `<a href=/rules/apps/${ruleApp.name}><b>${ruleApp.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (ruleApp: IRuleApp): void => {
        super.toast(`<span class="green-text">App rule <b class="white-text">${ruleApp.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.props.history.push(`/rules/apps`);
        }
    };

    private onDeleteFailure = (reason: string, ruleApp: IRuleApp): void =>
        super.toast(`Unable to delete app rule ${this.mounted ? `<b>${ruleApp.name}</b>` : `<a href=/rules/apps/${ruleApp.name}><b>${ruleApp.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedConditions.length
        || !!this.state.unsavedApps.length;

    private saveEntities = (rule: IRuleApp) => {
        this.saveRuleConditions(rule);
        this.saveRuleApps(rule);
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

    private saveRuleConditions = (rule: IRuleApp): void => {
        const {unsavedConditions} = this.state;
        if (unsavedConditions.length) {
            postData(`rules/apps/${rule.name}/conditions`, unsavedConditions,
                () => this.onSaveConditionsSuccess(rule),
                (reason) => this.onSaveConditionsFailure(rule, reason));
        }
    };

    private onSaveConditionsSuccess = (rule: IRuleApp): void => {
        this.props.addRuleAppConditions(rule.name, this.state.unsavedConditions);
        if (this.mounted) {
            this.setState({unsavedConditions: []});
        }
    };

    private onSaveConditionsFailure = (ruleApp: IRuleApp, reason: string): void =>
        super.toast(`Unable to save conditions of app rule ${this.mounted ? `<b>${ruleApp.name}</b>` : `<a href=/rules/apps/${ruleApp.name}><b>${ruleApp.name}</b></a>`}`, 10000, reason, true);

    private addRuleApp = (app: string): void =>
        this.setState({
            unsavedApps: this.state.unsavedApps.concat(app)
        });

    private removeRuleApps = (apps: string[]): void => {
        this.setState({
            unsavedApps: this.state.unsavedApps.filter(app => !apps.includes(app))
        });
    };

    private saveRuleApps = (rule: IRuleApp): void => {
        const {unsavedApps} = this.state;
        if (unsavedApps.length) {
            postData(`rules/apps/${rule.name}/apps`, unsavedApps,
                () => this.onSaveAppsSuccess(rule),
                (reason) => this.onSaveAppsFailure(rule, reason));
        }
    };

    private onSaveAppsSuccess = (rule: IRuleApp): void => {
        this.props.addRuleApps(rule.name, this.state.unsavedApps);
        if (this.mounted) {
            this.setState({unsavedApps: []});
        }
    };

    private onSaveAppsFailure = (ruleApp: IRuleApp, reason: string): void =>
        super.toast(`Unable to save apps of app rule ${this.mounted ? `<b>${ruleApp.name}</b>` : `<a href=/rules/apps/${ruleApp.name}><b>${ruleApp.name}</b></a>`}`, 10000, reason, true);

    private updateRuleApp = (ruleApp: IRuleApp) => {
        ruleApp = Object.values(normalize(ruleApp, Schemas.RULE_APP).entities.appRules || {})[0];
        const formRuleApp = {...ruleApp};
        removeFields(formRuleApp);
        this.setState({ruleApp: ruleApp, formRuleApp: formRuleApp});
    };

    private getFields = (appRule: Partial<IRule>): IFields =>
        Object.entries(appRule).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation:
                        key === 'priority'
                            ? {rule: requiredAndNumberAndMinAndMax, args: [0, 2147483647]}
                            : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private decisionDropdownOption = (decision: IDecision): string =>
        decision.ruleDecision;

    private getSelectableDecisions = () =>
        Object.values(this.props.decisions)
            .filter(decision => decision.componentType.type.toLowerCase() === componentTypes.SERVICE.type.toLowerCase());

    private appRule = () => {
        const {isLoading, error} = this.props;
        const ruleApp = this.getRuleApp();
        const formRuleApp = this.getFormRuleApp();
        // @ts-ignore
        const ruleKey: (keyof IRuleApp) = formRuleApp && Object.keys(formRuleApp)[0];
        const isNewRuleApp = this.isNew();
        return (
            <>
                {!isNewRuleApp && isLoading && <LoadingSpinner/>}
                {!isNewRuleApp && !isLoading && error && <Error message={error}/>}
                {(isNewRuleApp || !isLoading) && (isNewRuleApp || !error) && formRuleApp && (
                    /*@ts-ignore*/
                    <Form id={ruleKey}
                          fields={this.getFields(formRuleApp)}
                          values={ruleApp}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'rules/apps',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `rules/apps/${ruleApp.name}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `rules/apps/${ruleApp.name}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.entries(formRuleApp).map(([key, value], index) =>
                            key === 'decision'
                                ? <Field<IDecision> key={index}
                                                    id={key}
                                                    label={key}
                                                    type="dropdown"
                                                    dropdown={{
                                                        defaultValue: "Choose decision",
                                                        values: this.getSelectableDecisions(),
                                                        optionToString: this.decisionDropdownOption,
                                                        emptyMessage: 'No decisions available'
                                                    }}/>
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
        <RuleAppConditionList isLoadingRuleApp={this.props.isLoading}
                              loadRuleAppError={!this.isNew() ? this.props.error : undefined}
                              ruleApp={this.getRuleApp()}
                              unsavedConditions={this.state.unsavedConditions}
                              onAddRuleCondition={this.addRuleCondition}
                              onRemoveRuleConditions={this.removeRuleConditions}/>;

    private apps = (): JSX.Element =>
        <RuleAppAppsList isLoadingRuleApp={this.props.isLoading}
                         loadRuleAppError={!this.isNew() ? this.props.error : undefined}
                         ruleApp={this.getRuleApp()}
                         unsavedApps={this.state.unsavedApps}
                         onAddRuleApp={this.addRuleApp}
                         onRemoveRuleApps={this.removeRuleApps}/>;

    private tabs = (): Tab[] => [
        {
            title: 'App rule',
            id: 'appRule',
            content: () => this.appRule(),
            active: this.props.location.state?.selected === 'appRule'
        },
        {
            title: 'Conditions',
            id: 'ruleConditions',
            content: () => this.conditions(),
            active: this.props.location.state?.selected === 'ruleConditions'
        },
        {
            title: 'Apps',
            id: 'apps',
            content: () => this.apps(),
            active: this.props.location.state?.selected === 'apps'
        }
    ];

}

function removeFields(ruleApp: Partial<IRuleApp>) {
    delete ruleApp["id"];
    delete ruleApp["conditions"];
    delete ruleApp["apps"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.rules.apps.isLoadingRules;
    const error = state.entities.rules.apps.loadRulesError;
    const name = props.match.params.name;
    const ruleApp = isNew(props.location.search) ? buildNewAppRule() : state.entities.rules.apps.data[name];
    let formRuleApp;
    if (ruleApp) {
        formRuleApp = {...ruleApp};
        removeFields(formRuleApp);
    }
    const decisions = state.entities.decisions.data;
    return {
        isLoading,
        error,
        ruleApp,
        formRuleApp,
        decisions,
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadRulesApp,
    addRuleApp,
    updateRuleApp,
    loadDecisions,
    addRuleAppConditions,
    addRuleApps,
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleApp);
