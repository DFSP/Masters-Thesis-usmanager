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

import Card from "../../../../components/cards/Card";
import CardItem from "../../../../components/list/CardItem";
import React from "react";
import {deleteCondition} from "../../../../actions";
import {IRuleCondition} from "./RuleCondition";
import BaseComponent from "../../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../../components/contextmenu/LinkedContextMenuItem";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface ConditionCardProps {
    condition: IRuleCondition;
}

interface DispatchToProps {
    deleteCondition: (condition: IRuleCondition) => void;
}

type Props = DispatchToProps & ConditionCardProps;

class RuleConditionCard extends BaseComponent<Props, State> {

    private mounted = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            loading: false
        }
    }

    public componentDidMount(): void {
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    private onDeleteSuccess = (condition: IRuleCondition): void => {
        super.toast(`<span class="green-text">Condition <b class="white-text">${condition.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteCondition(condition);
    }

    private onDeleteFailure = (reason: string, condition: IRuleCondition): void => {
        super.toast(`Unable to delete <a href=/rules/conditions/${condition.name}><b>${condition.name}</b></a> condition`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {condition} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Modify services'}
                pathname={`/simulated-metrics/services/${condition.name}#services`}
                state={condition}/>,
        ];
    }

    public render() {
        const {condition} = this.props;
        const {loading} = this.state;
        const CardRuleCondition = Card<IRuleCondition>();
        return <CardRuleCondition id={`condition-${condition.id}`}
                                  title={condition.name.toString()}
                                   link={{to: {pathname: `/rules/conditions/${condition.name}`, state: condition}}}
                                   height={'150px'}
                                   margin={'10px 0'}
                                   hoverable
                                   delete={{
                                       url: `simulated-metrics/services/${condition.name}`,
                                       successCallback: this.onDeleteSuccess,
                                       failureCallback: this.onDeleteFailure,
                                   }}
                                   loading={loading}
                                   contextMenuItems={this.contextMenu()}>
            <CardItem key={'valueMode'}
                      label={'Value mode'}
                      value={condition.valueMode.name}/>
            <CardItem key={'field'}
                      label={'Field'}
                      value={condition.field.name}/>
            <CardItem key={'operator'}
                      label={'Operator'}
                      value={condition.operator.symbol}/>
            <CardItem key={'value'}
                      label={'Value'}
                      value={condition.value.toString()}/>
        </CardRuleCondition>
    }
}

const mapDispatchToProps: DispatchToProps = {
    deleteCondition,
};

export default connect(null, mapDispatchToProps)(RuleConditionCard);
