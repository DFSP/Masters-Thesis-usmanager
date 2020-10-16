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
import React from "react";
import styles from "../../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import List from "../../../components/list/List";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import ListItem from "../../../components/list/ListItem";
import {loadRulesService} from "../../../actions";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    genericServiceRules: string[];
}

interface DispatchToProps {
    loadRulesService: () => void;
}

type Props = StateToProps & DispatchToProps;

class GenericServiceRuleList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadRulesService();
    }

    public render() {
        const RulesList = List<string>();
        return (
            <RulesList isLoading={this.props.isLoading}
                       error={this.props.error}
                       emptyMessage={`Generic service rules list is empty`}
                       list={this.props.genericServiceRules}
                       show={this.rule}/>
        );
    }

    private rule = (rule: string, index: number): JSX.Element =>
        <ListItem key={index} separate={index !== this.props.genericServiceRules.length - 1}>
            <div className={`${styles.linkedItemContent}`}>
                <span>{rule}</span>
            </div>
            <Link to={`/rules/services/${rule}`}
                  className={`${styles.link} waves-effect`}>
                <i className={`${styles.linkIcon} material-icons right`}>link</i>
            </Link>
        </ListItem>;

}

function mapStateToProps(state: ReduxState): StateToProps {
    return {
        isLoading: state.entities.rules.services.isLoadingRules,
        error: state.entities.rules.services.loadRulesError,
        genericServiceRules: Object.entries(state.entities.rules.services.data)
            .filter(([_, rule]) => rule.generic)
            .map(([ruleName, _]) => ruleName)
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRulesService,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(GenericServiceRuleList);