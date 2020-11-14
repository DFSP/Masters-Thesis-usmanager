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
import {connect} from "react-redux";
import React from "react";
import {
    addRuleCloudHosts,
    addRuleEdgeHosts,
    addRuleHost,
    addRuleHostConditions,
    loadDecisions,
    loadRulesHost,
    updateRuleHost,
} from "../../../../actions";
import {IReply, postData} from "../../../../utils/api";
import HostRuleConditionList from "./RuleHostConditionList";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import HostRuleCloudHostsList from "./RuleHostCloudHostsList";
import HostRuleEdgeHostsList from "./RuleHostEdgeHostsList";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";

export interface IRuleHost extends IRule {
    cloudHosts?: string[],
    edgeHosts?: string[],
}

const buildNewHostRule = (): Partial<IRuleHost> => ({
    name: undefined,
    priority: 0,
    decision: undefined,
    generic: undefined,
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
    updateRuleHost: (previousRuleHost: IRuleHost, currentRuleHost: IRuleHost) => void;
    loadDecisions: () => void;
    addRuleHostConditions: (ruleName: string, conditions: string[]) => void;
    addRuleCloudHosts: (ruleName: string, cloudHosts: string[]) => void;
    addRuleEdgeHosts: (ruleName: string, edgeHosts: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IRuleHost,
    selected: 'rule' | 'ruleConditions' | 'cloudHosts' | 'edgeHosts'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

type State = {
    ruleHost?: IRuleHost,
    formRuleHost?: IRuleHost,
    unsavedConditions: string[],
    unsavedCloudHosts: string[],
    unsavedEdgeHosts: string[],
    isGeneric: boolean,
}

class RuleHost extends BaseComponent<Props, State> {

    state: State = {
        unsavedConditions: [],
        unsavedCloudHosts: [],
        unsavedEdgeHosts: [],
        isGeneric: this.props.ruleHost?.generic || false,
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadRuleHost();
        this.props.loadDecisions();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
        if (prevProps.ruleHost?.generic !== this.props.ruleHost?.generic) {
            this.setState({isGeneric: this.props.ruleHost?.generic || false})
        }
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

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IRuleHost>): void => {
        const ruleHost = reply.data;
        super.toast(`<span class="green-text">A regra ${this.mounted ? `<b class="white-text">${ruleHost.name}</b>` : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addRuleHost(ruleHost);
        this.saveEntities(reply.data);
        if (this.mounted) {
            this.updateRuleHost(ruleHost);
            this.props.history.replace(ruleHost.name);
        }
    };

    private onPostFailure = (reason: string, ruleHost: IRuleHost): void =>
        super.toast(`Não foi possível guardar a regra <b>${ruleHost.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<IRuleHost>): void => {
        const ruleHost = reply.data;
        super.toast(`<span class="green-text">As alterações à regra ${this.mounted ? `<b class="white-text">${ruleHost.name}</b>` : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(ruleHost);
        const previousRuleHost = this.getRuleHost();
        if (previousRuleHost.id) {
            this.props.updateRuleHost(previousRuleHost as IRuleHost, ruleHost);
        }
        if (this.mounted) {
            this.updateRuleHost(ruleHost);
            this.props.history.replace(ruleHost.name);
        }
    };

    private onPutFailure = (reason: string, ruleHost: IRuleHost): void =>
        super.toast(`Não foi possível atualizar a regra ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (ruleHost: IRuleHost): void => {
        super.toast(`<span class="green-text">A regra <b class="white-text">${ruleHost.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/regras/hosts`)
        }
    };

    private onDeleteFailure = (reason: string, ruleHost: IRuleHost): void =>
        super.toast(`Não foi possível remover a regra ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`}`, 10000, reason, true);

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
            this.setState({unsavedConditions: []});
        }
    };

    private onSaveConditionsFailure = (ruleHost: IRuleHost, reason: string): void =>
        super.toast(`Não foi possível guardar as condições associados à regra ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`}`, 10000, reason, true);

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
            this.setState({unsavedCloudHosts: []});
        }
    };

    private onSaveCloudHostsFailure = (ruleHost: IRuleHost, reason: string): void =>
        super.toast(`Não foi possível guardar as instâncias associadas à regra ${this.mounted ? `<b>${ruleHost.name}</b>` : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`}`, 10000, reason, true);

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
            this.setState({unsavedEdgeHosts: []});
        }
    };

    private onSaveEdgeHostsFailure = (ruleHost: IRuleHost, reason: string): void =>
        super.toast(`Não foi possível guardar os hosts associados à regra ${this.mounted ?
            <b>${ruleHost.name}</b> : `<a href=/regras/hosts/${ruleHost.name}><b>${ruleHost.name}</b></a>`}`, 10000, reason, true);

    private updateRuleHost = (ruleHost: IRuleHost) => {
        ruleHost = Object.values(normalize(ruleHost, Schemas.RULE_HOST).entities.hostRules || {})[0];
        const formRuleHost = {...ruleHost};
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

    private decisionDropdownOption = (decision: IDecision) =>
        decision.ruleDecision;

    private isGenericSelected = (generic: boolean) =>
        this.setState({isGeneric: generic});

    private getSelectableDecisions = () =>
        Object.values(this.props.decisions).filter(decision =>
            decision.componentType.type.toLocaleLowerCase() === componentTypes.HOST.type.toLocaleLowerCase());

    private hostRule = () => {
        const {isLoading, error} = this.props;
        const ruleHost = this.getRuleHost();
        const formRuleHost = this.getFormRuleHost();
        // @ts-ignore
        const ruleKey: (keyof IRuleHost) = formRuleHost && Object.keys(formRuleHost)[0];
        const isNewRuleHost = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formRuleHost && (
                    /*@ts-ignore*/
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
                                                        defaultValue: "Selecionar a decisão",
                                                        values: this.getSelectableDecisions(),
                                                        optionToString: this.decisionDropdownOption,
                                                        emptyMessage: 'Não há decisões disponíveis'
                                                    }}/>
                                : key === 'generic'
                                ? <Field key={index}
                                        id={key}
                                        type='checkbox'
                                        checkbox={{label: 'Aplicar a todos os hosts', checkCallback: this.isGenericSelected}}/>
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
                               ruleHost={this.getRuleHost()}
                               unsavedConditions={this.state.unsavedConditions}
                               onAddRuleCondition={this.addRuleCondition}
                               onRemoveRuleConditions={this.removeRuleConditions}/>;

    private cloudHosts = (): JSX.Element =>
        <HostRuleCloudHostsList isLoadingHostRule={this.props.isLoading}
                                loadHostRuleError={this.props.error}
                                ruleHost={this.getRuleHost()}
                                unsavedCloudHosts={this.state.unsavedCloudHosts}
                                onAddRuleCloudHost={this.addRuleCloudHost}
                                onRemoveRuleCloudHosts={this.removeRuleCloudHosts}/>;

    private edgeHosts = (): JSX.Element =>
        <HostRuleEdgeHostsList isLoadingHostRule={this.props.isLoading}
                               loadHostRuleError={this.props.error}
                               ruleHost={this.getRuleHost()}
                               unsavedEdgeHosts={this.state.unsavedEdgeHosts}
                               onAddRuleEdgeHost={this.addRuleEdgeHost}
                               onRemoveRuleEdgeHosts={this.removeRuleEdgeHosts}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Regra',
            id: 'hostRule',
            content: () => this.hostRule(),
            active: this.props.location.state?.selected === 'rule'
        },
        {
            title: 'Condições',
            id: 'ruleConditions',
            content: () => this.conditions(),
            active: this.props.location.state?.selected === 'ruleConditions'
        },
        {
            title: 'Instâncias cloud',
            id: 'cloudHosts',
            content: () => this.cloudHosts(),
            disabled: this.state.isGeneric,
            active: this.props.location.state?.selected === 'cloudHosts'
        },
        {
            title: 'Hosts edge',
            id: 'edgeHosts',
            content: () => this.edgeHosts(),
            disabled: this.state.isGeneric,
            active: this.props.location.state?.selected === 'edgeHosts'
        }
    ];

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
        formRuleHost = {...ruleHost};
        removeFields(formRuleHost);
    }
    const decisions = state.entities.decisions.data;
    return {
        isLoading,
        error,
        ruleHost,
        formRuleHost,
        decisions,
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadRulesHost,
    updateRuleHost,
    addRuleHost,
    loadDecisions,
    addRuleHostConditions,
    addRuleCloudHosts,
    addRuleEdgeHosts
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleHost);
