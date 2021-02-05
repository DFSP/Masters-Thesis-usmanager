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

import React from "react";
import {IService} from "./Service";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadRulesService, loadServiceRules, removeServiceRules} from "../../../actions";
import {connect} from "react-redux";
import {IRuleService} from "../rules/services/RuleService";
import {Link} from "react-router-dom";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    rules: { [key: string]: IRuleService },
    rulesName: string[];
}

interface DispatchToProps {
    loadRulesService: (name?: string) => any;
    loadServiceRules: (serviceName: string) => void;
    removeServiceRules: (serviceName: string, rules: string[]) => void;
}

interface ServiceRuleListProps {
    isLoadingService: boolean;
    loadServiceError?: string | null;
    service: IService | Partial<IService> | null;
    unsavedRules: string[];
    onAddServiceRule: (rule: string) => void;
    onRemoveServiceRules: (rule: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceRuleListProps;

interface State {
    entitySaved: boolean;
}

class ServiceRuleList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadRulesService();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.service?.serviceName && this.props.service?.serviceName) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingService || this.props.isLoading : undefined}
                               error={this.props.loadServiceError || (isNew ? undefined : this.props.error)}
                               emptyMessage={`Sem regras associadas`}
                               data={this.props.rulesName}
                               dropdown={{
                                   id: 'rules',
                                   title: 'Selecionar a regra',
                                   empty: 'Não existem regras disponíveis',
                                   data: this.getSelectableRules()
                               }}
                               show={this.rule}
                               onAdd={this.onAdd}
                               onRemove={this.onRemove}
                               onDelete={{
                                   url: `services/${this.props.service?.serviceName}/rules`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.service?.serviceName) {
            const {serviceName} = this.props.service;
            this.props.loadServiceRules(serviceName);
        }
    };

    private isNew = () =>
        this.props.service?.serviceName === undefined;

    private rule = (index: number, rule: string, separate: boolean, checked: boolean,
                    handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedRules.includes(rule);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${styles.linkedItemContent}`}>
                    <label>
                        <input id={rule}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                                {rule}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/regras/serviços/${rule}`}
                          className={`${styles.link}`}>
                        <i className={`link-icon material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (rule: string): void =>
        this.props.onAddServiceRule(rule);

    private onRemove = (rules: string[]) =>
        this.props.onRemoveServiceRules(rules);

    private onDeleteSuccess = (rules: string[]): void => {
        if (this.props.service?.serviceName) {
            const {serviceName} = this.props.service;
            this.props.removeServiceRules(serviceName, rules);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remover a regra`, 10000, reason, true);

    private getSelectableRules = () => {
        const {rules, rulesName, unsavedRules} = this.props;
        return Object.entries(rules)
            .filter(([_, rule]) => !rule.generic)
            .map(([ruleName, _]) => ruleName)
            .filter(name => !rulesName.includes(name) && !unsavedRules.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: ServiceRuleListProps): StateToProps {
    const serviceName = ownProps.service?.serviceName;
    const service = serviceName && state.entities.services.data[serviceName];
    const rulesName = service && service.serviceRules;
    return {
        isLoading: state.entities.services.isLoadingRules,
        error: state.entities.services.loadRulesError,
        rules: Object.entries(state.entities.rules.services.data)
            .filter(([_, rule]) => !rule.generic)
            .map(([key, value]) => ({[key]: value}))
            .reduce((fields, field) => {
                for (let key in field) {
                    fields[key] = field[key];
                }
                return fields;
            }, {}),
        rulesName: rulesName || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRulesService,
        loadServiceRules,
        removeServiceRules,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ServiceRuleList);