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
import {IEdgeHost} from "./EdgeHost";
import {loadEdgeHostRules, loadRulesHost, removeEdgeHostRules} from "../../../../actions";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    rules: { [key: string]: IRuleHost },
    rulesNames: string[];
}

interface DispatchToProps {
    loadRulesHost: () => void;
    loadEdgeHostRules: (hostname: string) => void;
    removeEdgeHostRules: (hostname: string, rules: string[]) => void;
}

interface HostRuleListProps {
    isLoadingEdgeHost: boolean;
    loadEdgeHostError?: string | null;
    edgeHost: IEdgeHost | Partial<IEdgeHost> | null;
    unsavedRules: string[];
    onAddHostRule: (rule: string) => void;
    onRemoveHostRules: (rule: string[]) => void;
}

type Props = StateToProps & DispatchToProps & HostRuleListProps;

type State = {
    entitySaved: boolean;
}

class EdgeHostRuleList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadRulesHost();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        const previousHostname = prevProps.edgeHost?.publicIpAddress;
        const currentHostname = this.props.edgeHost?.publicIpAddress;
        if (!previousHostname && currentHostname) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingEdgeHost || this.props.isLoading : undefined}
                               error={!isNew ? this.props.loadEdgeHostError || this.props.error : undefined}
                               emptyMessage={`Rules list is empty`}
                               data={this.props.rulesNames}
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
                                   url: `hosts/edge/${this.props.edgeHost?.publicIpAddress}/rules`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        const hostname = this.props.edgeHost?.publicIpAddress;
        if (hostname) {
            this.props.loadEdgeHostRules(hostname);
        }
    };

    private isNew = () =>
        this.props.edgeHost?.publicDnsName === undefined && this.props.edgeHost?.publicIpAddress === undefined;

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
        const hostname = this.props.edgeHost?.publicIpAddress;
        if (hostname) {
            this.props.removeEdgeHostRules(hostname, rules);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Unable to remove rule`, 10000, reason, true);

    private getSelectableRules = () => {
        const {rules, rulesNames, unsavedRules} = this.props;
        return Object.entries(rules)
            .filter(([_, rule]) => !rule.generic)
            .map(([ruleName, _]) => ruleName)
            .filter(name => !rulesNames.includes(name) && !unsavedRules.includes(name));
    };

}

function mapStateToProps(state: ReduxState, ownProps: HostRuleListProps): StateToProps {
    const hostname = ownProps.edgeHost?.publicIpAddress;
    const host = hostname && state.entities.hosts.edge.data[hostname];
    const rulesNames = host && host.hostRules;
    return {
        isLoading: state.entities.hosts.edge.isLoadingRules,
        error: state.entities.hosts.edge.loadRulesError,
        rules: Object.entries(state.entities.rules.hosts.data)
            .filter(([_, rule]) => !rule.generic)
            .map(([key, value]) => ({[key]: value}))
            .reduce((fields, field) => {
                for (let key in field) {
                    fields[key] = field[key];
                }
                return fields;
            }, {}),
        rulesNames: rulesNames || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRulesHost,
        loadEdgeHostRules,
        removeEdgeHostRules,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(EdgeHostRuleList);