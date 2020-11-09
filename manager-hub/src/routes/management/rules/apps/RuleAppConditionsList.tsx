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

import {IRuleApp} from "./RuleApp";
import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import React from "react";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {loadConditions, loadRuleAppConditions, removeRuleAppConditions} from "../../../../actions";
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
    loadRuleAppConditions: (ruleName: string) => void;
    removeRuleAppConditions: (ruleName: string, conditions: string[]) => void;
}

interface AppRuleConditionListProps {
    isLoadingRuleApp: boolean;
    loadRuleAppError?: string | null;
    ruleApp: IRuleApp | Partial<IRuleApp> | null;
    unsavedConditions: string[];
    onAddRuleCondition: (condition: string) => void;
    onRemoveRuleConditions: (condition: string[]) => void;
}

type Props = StateToProps & DispatchToProps & AppRuleConditionListProps

interface State {
    entitySaved: boolean;
}

class RuleAppConditionsList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadConditions();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.ruleApp?.name && this.props.ruleApp?.name) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList
            isLoading={!isNew ? this.props.isLoadingRuleApp || this.props.isLoading : undefined}
            error={!isNew ? this.props.loadRuleAppError || this.props.error : undefined}
            emptyMessage={`Não existem condições para mostrar`}
            data={this.props.ruleConditions}
            dropdown={{
                id: 'conditions',
                title: 'Selecionar a condição',
                empty: 'Não existem condição disponíveis',
                data: this.getSelectableConditionNames()
            }}
            show={this.condition}
            onAdd={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `rules/apps/${this.props.ruleApp?.name}/conditions`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.ruleApp?.name) {
            const {name} = this.props.ruleApp;
            this.props.loadRuleAppConditions(name);
        }
    };

    private isNew = () =>
        this.props.ruleApp?.name === undefined;

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
                    <Link to={`/regras/condições/${condition}`}
                          className={`${styles.link}`}>
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
        if (this.props.ruleApp?.name) {
            const {name} = this.props.ruleApp;
            this.props.removeRuleAppConditions(name, conditions);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remover a condição`, 10000, reason, true);

    private getSelectableConditionNames = () => {
        const {conditions, ruleConditions, unsavedConditions} = this.props;
        return Object.keys(conditions)
            .filter(condition => !ruleConditions.includes(condition) && !unsavedConditions.includes(condition));
    };

}

function mapStateToProps(state: ReduxState, ownProps: AppRuleConditionListProps): StateToProps {
    const ruleName = ownProps.ruleApp?.name;
    const rule = ruleName && state.entities.rules.apps.data[ruleName];
    const ruleConditions = rule && rule.conditions;
    return {
        isLoading: state.entities.rules.apps.isLoadingConditions,
        error: state.entities.rules.apps.loadConditionsError,
        ruleConditions: ruleConditions || [],
        conditions: state.entities.rules.conditions.data,
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRuleAppConditions,
        removeRuleAppConditions,
        loadConditions,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(RuleAppConditionsList);