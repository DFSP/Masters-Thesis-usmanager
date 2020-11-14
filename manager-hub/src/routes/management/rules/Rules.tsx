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
import RulesAppList from "./apps/RulesAppList";

const Rules = () =>
    <MainLayout>
        <AddButton button={{text: 'Nova condição ou regra'}}
                   dropdown={{
                       id: 'addRuleOrCondition',
                       title: 'Regra aplicada a...',
                       data: [
                           {text: 'hosts', pathname: '/regras/hosts/nova regra?new#rule'},
                           {text: 'aplicações', pathname: '/regras/aplicações/nova regra?new#rule'},
                           {text: 'serviços', pathname: '/regras/serviços/nova regra?new#rule'},
                           {text: 'contentores', pathname: '/regras/contentores/nova regra?new#rule'},
                           {text: 'ou adicionar condição', pathname: '/regras/condições/nova condição?new#rule'},
                       ],
                   }}/>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesConditionCollapsible"}
                         title={'Condições'}
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
            <Collapsible id={"rulesAppCollapsible"}
                         title={'Aplicações'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RulesAppList/>
            </Collapsible>
        </div>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesServiceCollapsible"}
                         title={'Serviços'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RulesServiceList/>
            </Collapsible>
        </div>
        <div className={`${styles.collapsibleContainer}`}>
            <Collapsible id={"rulesContainerCollapsible"}
                         title={'Contentores'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <RulesContainerList/>
            </Collapsible>
        </div>
    </MainLayout>;

export default Rules;