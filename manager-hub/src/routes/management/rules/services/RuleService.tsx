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
    addRuleService,
    addRuleServiceConditions,
    addRuleServices,
    loadDecisions,
    loadRulesService,
    updateRuleService,
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../../utils/api";
import RuleServiceConditionList from "./RuleServiceConditionList";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import RuleServiceServicesList from "./RuleServiceServicesList";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";

export interface IRuleService extends IRule {
    services?: string[]
}

const buildNewServiceRule = (): Partial<IRuleService> => ({
    name: undefined,
    priority: 0,
    decision: undefined,
    generic: undefined,
});


interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    ruleService: Partial<IRuleService>;
    formRuleService?: Partial<IRuleService>,
    decisions: { [key: string]: IDecision },
}

interface DispatchToProps {
    loadRulesService: (name: string) => void;
    addRuleService: (ruleService: IRuleService) => void;
    updateRuleService: (previousRuleService: IRuleService, currentRuleService: IRuleService) => void;
    loadDecisions: () => void;
    addRuleServiceConditions: (ruleName: string, conditions: string[]) => void;
    addRuleServices: (ruleName: string, services: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IRuleService,
    selected: 'rule' | 'ruleConditions' | 'services'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

type State = {
    ruleService?: IRuleService,
    formRuleService?: IRuleService,
    unsavedConditions: string[],
    unsavedServices: string[],
    isGeneric: boolean,
}

class RuleService extends BaseComponent<Props, State> {

    state: State = {
        unsavedConditions: [],
        unsavedServices: [],
        isGeneric: this.props.ruleService?.generic || false,
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadRuleService();
        this.props.loadDecisions();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
        if (prevProps.ruleService?.generic !== this.props.ruleService?.generic) {
            this.setState({isGeneric: this.props.ruleService?.generic || false})
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

    private loadRuleService = () => {
        if (!isNew(this.props.location.search)) {
            const ruleName = this.props.match.params.name;
            this.props.loadRulesService(ruleName);
        }
    };

    private getRuleService = () =>
        this.state.ruleService || this.props.ruleService;

    private getFormRuleService = () =>
        this.state.formRuleService || this.props.formRuleService;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IRuleService>): void => {
        const ruleService = reply.data;
        super.toast(`<span class="green-text">A regra ${this.mounted ? `<b class="white-text">${ruleService.name}</b>` : `<a href='/regras/serviços/${ruleService.name}'><b>${ruleService.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addRuleService(ruleService);
        this.saveEntities(reply.data);
        if (this.mounted) {
            this.updateRuleService(ruleService);
            this.props.history.replace(ruleService.name);
        }
    };

    private onPostFailure = (reason: string, ruleService: IRuleService): void =>
        super.toast(`Não foi possível guardar a regra <b>${ruleService.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<IRuleService>): void => {
        const ruleService = reply.data;
        super.toast(`<span class="green-text">As alterações à regra ${this.mounted ? `<b class="white-text">${ruleService.name}</b>` : `<a href='/regras/serviços/${ruleService.name}'><b>${ruleService.name}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(ruleService);
        const previousRuleService = this.getRuleService();
        if (previousRuleService?.id) {
            this.props.updateRuleService(previousRuleService as IRuleService, ruleService);
        }
        if (this.mounted) {
            this.updateRuleService(ruleService);
            this.props.history.replace(ruleService.name);
        }
    };

    private onPutFailure = (reason: string, ruleService: IRuleService): void =>
        super.toast(`Não foi possível guardar as alterações feitas à regra ${this.mounted ? `<b>${ruleService.name}</b>` : `<a href='/regras/serviços/${ruleService.name}'><b>${ruleService.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (ruleService: IRuleService): void => {
        super.toast(`<span class="green-text">A regra <b class="white-text">${ruleService.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/regras/serviços`);
        }
    };

    private onDeleteFailure = (reason: string, ruleService: IRuleService): void =>
        super.toast(`Não foi possível remover a regra ${this.mounted ? `<b>${ruleService.name}</b>` : `<a href='/regras/serviços/${ruleService.name}'><b>${ruleService.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedConditions.length
        || !!this.state.unsavedServices.length;

    private saveEntities = (rule: IRuleService) => {
        this.saveRuleConditions(rule);
        this.saveRuleServices(rule);
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

    private saveRuleConditions = (rule: IRuleService): void => {
        const {unsavedConditions} = this.state;
        if (unsavedConditions.length) {
            postData(`rules/services/${rule.name}/conditions`, unsavedConditions,
                () => this.onSaveConditionsSuccess(rule),
                (reason) => this.onSaveConditionsFailure(rule, reason));
        }
    };

    private onSaveConditionsSuccess = (rule: IRuleService): void => {
        this.props.addRuleServiceConditions(rule.name, this.state.unsavedConditions);
        if (this.mounted) {
            this.setState({unsavedConditions: []});
        }
    };

    private onSaveConditionsFailure = (ruleService: IRuleService, reason: string): void =>
        super.toast(`Não foi possível guardar as condições associadas à regra ${this.mounted ? `<b>${ruleService.name}</b>` : `<a href='/regras/serviços/${ruleService.name}'><b>${ruleService.name}</b></a>`}`, 10000, reason, true);

    private addRuleService = (service: string): void =>
        this.setState({
            unsavedServices: this.state.unsavedServices.concat(service)
        });

    private removeRuleServices = (services: string[]): void => {
        this.setState({
            unsavedServices: this.state.unsavedServices.filter(service => !services.includes(service))
        });
    };

    private saveRuleServices = (rule: IRuleService): void => {
        const {unsavedServices} = this.state;
        if (unsavedServices.length) {
            postData(`rules/services/${rule.name}/services`, unsavedServices,
                () => this.onSaveServicesSuccess(rule),
                (reason) => this.onSaveServicesFailure(rule, reason));
        }
    };

    private onSaveServicesSuccess = (rule: IRuleService): void => {
        this.props.addRuleServices(rule.name, this.state.unsavedServices);
        if (this.mounted) {
            this.setState({unsavedServices: []});
        }
    };

    private onSaveServicesFailure = (ruleService: IRuleService, reason: string): void =>
        super.toast(`Não foi possível guardar os serviços associados à regra ${this.mounted ? `<b>${ruleService.name}</b>` : `<a href='/regras/serviços/${ruleService.name}'><b>${ruleService.name}</b></a>`}`, 10000, reason, true);

    private updateRuleService = (ruleService: IRuleService) => {
        ruleService = Object.values(normalize(ruleService, Schemas.RULE_SERVICE).entities.serviceRules || {})[0];
        const formRuleService = {...ruleService};
        removeFields(formRuleService);
        this.setState({ruleService: ruleService, formRuleService: formRuleService});
    };

    private getFields = (serviceRule: Partial<IRule>): IFields =>
        Object.entries(serviceRule).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation:
                        key === 'priority'
                            ? {rule: requiredAndNumberAndMinAndMax, args: [0, 2147483647]}
                            : key !== 'generic'
                            ? {rule: requiredAndTrimmed}
                            : undefined
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

    private isGenericSelected = (generic: boolean) =>
        this.setState({isGeneric: generic});

    private getSelectableDecisions = () =>
        Object.values(this.props.decisions).filter(decision =>
            decision.componentType.type.toLocaleLowerCase() === componentTypes.SERVICE.type.toLocaleLowerCase());

    private serviceRule = () => {
        const {isLoading, error} = this.props;
        const ruleService = this.getRuleService();
        const formRuleService = this.getFormRuleService();
        // @ts-ignore
        const ruleKey: (keyof IRuleService) = formRuleService && Object.keys(formRuleService)[0];
        const isNewRuleService = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formRuleService && (
                    /*@ts-ignore*/
                    <Form id={ruleKey}
                          fields={this.getFields(formRuleService)}
                          values={ruleService}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'rules/services',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `rules/services/${ruleService.name}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `rules/services/${ruleService.name}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.entries(formRuleService).map(([key, value], index) =>
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
                                         checkbox={{
                                             label: 'Aplicar a todos os serviços',
                                             checkCallback: this.isGenericSelected
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
        <RuleServiceConditionList isLoadingRuleService={this.props.isLoading}
                                  loadRuleServiceError={this.props.error}
                                  ruleService={this.getRuleService()}
                                  unsavedConditions={this.state.unsavedConditions}
                                  onAddRuleCondition={this.addRuleCondition}
                                  onRemoveRuleConditions={this.removeRuleConditions}/>;

    private services = (): JSX.Element =>
        <RuleServiceServicesList isLoadingRuleService={this.props.isLoading}
                                 loadRuleServiceError={this.props.error}
                                 ruleService={this.getRuleService()}
                                 unsavedServices={this.state.unsavedServices}
                                 onAddRuleService={this.addRuleService}
                                 onRemoveRuleServices={this.removeRuleServices}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Regra',
            id: 'serviceRule',
            content: () => this.serviceRule(),
            active: this.props.location.state?.selected === 'rule'
        },
        {
            title: 'Condições',
            id: 'ruleConditions',
            content: () => this.conditions(),
            active: this.props.location.state?.selected === 'ruleConditions'
        },
        {
            title: 'Serviços',
            id: 'services',
            content: () => this.services(),
            disabled: this.state.isGeneric,
            active: this.props.location.state?.selected === 'services'
        }
    ];

}

function removeFields(ruleService: Partial<IRuleService>) {
    delete ruleService["id"];
    delete ruleService["conditions"];
    delete ruleService["services"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.rules.services.isLoadingRules;
    const error = state.entities.rules.services.loadRulesError;
    const name = props.match.params.name;
    const ruleService = isNew(props.location.search) ? buildNewServiceRule() : state.entities.rules.services.data[name];
    let formRuleService;
    if (ruleService) {
        formRuleService = {...ruleService};
        removeFields(formRuleService);
    }
    const decisions = state.entities.decisions.data;
    return {
        isLoading,
        error,
        ruleService,
        formRuleService,
        decisions,
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadRulesService,
    addRuleService,
    updateRuleService,
    loadDecisions,
    addRuleServiceConditions,
    addRuleServices,
};

export default connect(mapStateToProps, mapDispatchToProps)(RuleService);
