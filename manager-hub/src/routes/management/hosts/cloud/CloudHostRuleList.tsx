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
import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {IRuleHost} from "../../rules/hosts/RuleHost";
import {Link} from "react-router-dom";
import {ICloudHost} from "./CloudHost";
import {loadCloudHostRules, loadRulesHost, removeCloudHostRules} from "../../../../actions";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    rules: { [key: string]: IRuleHost },
    rulesName: string[];
}

interface DispatchToProps {
    loadRulesHost: () => void;
    loadCloudHostRules: (hostname: string) => void;
    removeCloudHostRules: (hostname: string, rules: string[]) => void;
}

interface HostRuleListProps {
    isLoadingCloudHost: boolean;
    loadCloudHostError?: string | null;
    cloudHost: ICloudHost | Partial<ICloudHost> | null;
    unsavedRules: string[];
    onAddHostRule: (rule: string) => void;
    onRemoveHostRules: (rule: string[]) => void;
}

type Props = StateToProps & DispatchToProps & HostRuleListProps;

interface State {
    entitySaved: boolean;
}

class CloudHostRuleList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadRulesHost();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.cloudHost?.instanceId && this.props.cloudHost?.instanceId) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingCloudHost || this.props.isLoading : undefined}
                               error={!isNew ? this.props.loadCloudHostError || this.props.error : undefined}
                               emptyMessage={`Rules list is empty`}
                               data={this.props.rulesName}
                               dropdown={{
                                   id: 'rules',
                                   title: 'Add host rule',
                                   empty: 'No rules to add',
                                   data: this.getSelectableRules()
                               }}
                               show={this.rule}
                               onAdd={this.onAdd}
                               onRemove={this.onRemove}
                               onDelete={{
                                   url: `hosts/cloud/${this.props.cloudHost?.instanceId}/rules`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        const instanceId = this.props.cloudHost?.instanceId;
        if (instanceId) {
            this.props.loadCloudHostRules(instanceId);
        }
    };

    private isNew = () =>
        this.props.cloudHost?.instanceId === undefined;

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
                    <Link to={`/rules/hosts/${rule}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (rule: string): void =>
        this.props.onAddHostRule(rule);

    private onRemove = (rules: string[]) =>
        this.props.onRemoveHostRules(rules);

    private onDeleteSuccess = (rules: string[]): void => {
        const instanceId = this.props.cloudHost?.instanceId;
        if (instanceId) {
            this.props.removeCloudHostRules(instanceId, rules);
        }
    };

    private onDeleteFailure = (reason: string, rules?: string[]): void =>
        super.toast(`Unable to remove ${rules?.length === 1 ? rules[0] : 'rules'} from <b>${this.props.cloudHost?.instanceId}</b> cloud instance`, 10000, reason, true);

    private getSelectableRules = () => {
        const {rules, rulesName, unsavedRules} = this.props;
        return Object.entries(rules)
            .filter(([_, rule]) => !rule.generic)
            .map(([ruleName, _]) => ruleName)
            .filter(name => !rulesName.includes(name) && !unsavedRules.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: HostRuleListProps): StateToProps {
    const instanceId = ownProps.cloudHost?.instanceId;
    const host = instanceId && state.entities.hosts.cloud.data[instanceId];
    const rulesName = host && host.hostRules;
    return {
        isLoading: state.entities.hosts.cloud.isLoadingRules,
        error: state.entities.hosts.cloud.loadRulesError,
        rules: state.entities.rules.hosts.data,
        rulesName: rulesName || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRulesHost,
        loadCloudHostRules,
        removeCloudHostRules,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CloudHostRuleList);