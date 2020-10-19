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

import React from 'react';
import MainLayout from '../../../views/mainLayout/MainLayout';
import AddButton from "../../../components/form/AddButton";
import styles from './Rules.module.css'
import Collapsible from "../../../components/collapsible/Collapsible";
import RulesHostList from "./hosts/RulesHostList";
import RulesServiceList from "./services/RulesServiceList";
import RuleConditionsList from "./conditions/RuleConditionsList";
import RulesContainerList from "./containers/RulesContainerList";

const Rules = () =>
    <MainLayout>
        <AddButton tooltip={{text: 'Add condition or rule', position: 'left'}}
                   dropdown={{
                       id: 'addRuleOrCondition',
                       title: 'Select menu',
                       data: [
                           {text: 'Rule condition', pathname: '/rules/conditions/new_condition?new=true'},
                           {text: 'Host rule', pathname: '/rules/hosts/new_host_rule?new=true'},
                           {text: 'Service rule', pathname: '/rules/services/new_service_rule?new=true'},
                           {text: 'Container rule', pathname: '/rules/containers/new_container_rule?new=true'},
                       ],
                   }}/>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesConditionCollapsible"}
                         title={'Conditions'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RuleConditionsList/>
            </Collapsible>
        </div>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesHostCollapsible"}
                         title={'Hosts'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RulesHostList/>
            </Collapsible>
        </div>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesServiceCollapsible"}
                         title={'Services'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RulesServiceList/>
            </Collapsible>
        </div>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesContainerCollapsible"}
                         title={'Containers'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RulesContainerList/>
            </Collapsible>
        </div>
    </MainLayout>;

export default Rules;