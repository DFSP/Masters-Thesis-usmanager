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

import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import React from "react";
import {Link} from "react-router-dom";
import List from "../../../components/list/List";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {loadRulesHost} from "../../../actions";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    genericHostRules: string[];
}

interface DispatchToProps {
    loadRulesHost: () => void;
}

type Props = StateToProps & DispatchToProps;

class GenericHostRuleList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadRulesHost();
    }

    public render() {
        const RulesList = List<string>();
        return (
            <RulesList isLoading={this.props.isLoading}
                       error={this.props.error}
                       emptyMessage={`Não existem regras que se apliquem a todos os hosts`}
                       list={this.props.genericHostRules}
                       show={this.rule}/>
        );
    }

    private rule = (rule: string, index: number): JSX.Element =>
        <ListItem key={index} separate={index !== this.props.genericHostRules.length - 1}>
            <Link to={`/regras/hosts/${rule}`}
                  className={`${styles.link}`}>
                <div className={`${styles.linkedItemContent}`}>
                    <span>{rule}</span>
                    <i className={`${styles.linkIcon} material-icons right`}>link</i>
                </div>
            </Link>
        </ListItem>;

}

function mapStateToProps(state: ReduxState): StateToProps {
    return {
        isLoading: state.entities.rules.hosts.isLoadingRules,
        error: state.entities.rules.hosts.loadRulesError,
        genericHostRules: Object.entries(state.entities.rules.hosts.data)
            .filter(([_, rule]) => rule.generic)
            .map(([ruleName, _]) => ruleName)
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRulesHost,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(GenericHostRuleList);