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

import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import React from "react";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {loadApps, loadRuleApps, removeRuleApps,} from "../../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {IApp} from "../../apps/App";
import {IRuleApp} from "./RuleApp";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    ruleApps: string[];
    apps: { [key: string]: IApp };
}

interface DispatchToProps {
    loadApps: () => void;
    loadRuleApps: (ruleName: string) => void;
    removeRuleApps: (ruleName: string, apps: string[]) => void;
}

interface AppRuleAppsListProps {
    isLoadingRuleApp: boolean;
    loadRuleAppError?: string | null;
    ruleApp: IRuleApp | Partial<IRuleApp> | null;
    unsavedApps: string[];
    onAddRuleApp: (app: string) => void;
    onRemoveRuleApps: (apps: string[]) => void;
}

type Props = StateToProps & DispatchToProps & AppRuleAppsListProps

interface State {
    entitySaved: boolean;
}

class RuleAppAppsList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadApps();
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
            emptyMessage={`Apps list is empty`}
            data={this.props.ruleApps}
            dropdown={{
                id: 'apps',
                title: 'Add app',
                empty: 'No apps to add',
                data: this.getSelectableAppNames()
            }}
            show={this.app}
            onAdd={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `rules/apps/${this.props.ruleApp?.name}/apps`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.ruleApp?.name) {
            const {name} = this.props.ruleApp;
            this.props.loadRuleApps(name);
        }
    };

    private isNew = () =>
        this.props.ruleApp?.name === undefined;

    private app = (index: number, app: string, separate: boolean, checked: boolean,
                   handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedApps.includes(app);
        return (
            <ListItem key={index} separate={separate}>
                <div className={styles.linkedItemContent}>
                    <label>
                        <input id={app}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
              <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                {app}
              </div>
            </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/apps/${app}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (app: string): void =>
        this.props.onAddRuleApp(app);

    private onRemove = (apps: string[]) =>
        this.props.onRemoveRuleApps(apps);

    private onDeleteSuccess = (apps: string[]): void => {
        if (this.props.ruleApp?.name) {
            const {name} = this.props.ruleApp;
            this.props.removeRuleApps(name, apps);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Unable to remove app`, 10000, reason, true);

    private getSelectableAppNames = () => {
        const {apps, ruleApps, unsavedApps} = this.props;
        return Object.keys(apps)
            .filter(app => !ruleApps.includes(app) && !unsavedApps.includes(app));
    };

}

function mapStateToProps(state: ReduxState, ownProps: AppRuleAppsListProps): StateToProps {
    const ruleName = ownProps.ruleApp?.name;
    const rule = ruleName && state.entities.rules.apps.data[ruleName];
    const ruleApps = rule && rule.apps;
    return {
        isLoading: state.entities.rules.apps.isLoadingApps,
        error: state.entities.rules.apps.loadAppsError,
        ruleApps: ruleApps || [],
        apps: state.entities.apps.data,
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRuleApps,
        removeRuleApps,
        loadApps,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(RuleAppAppsList);