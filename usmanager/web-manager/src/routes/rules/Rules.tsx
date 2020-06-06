import React from 'react';
import MainLayout from '../../views/mainLayout/MainLayout';
import AddButton from "../../components/form/AddButton";
import styles from './Rules.module.css'
import Collapsible from "../../components/collapsible/Collapsible";
import RulesHostList from "./hosts/RulesHostList";
import RulesServiceList from "./services/RulesServiceList";
import RuleConditionsList from "./conditions/RuleConditionsList";
import RulesContainerList from "./containers/RulesContainerList";

const Rules = () =>
  <MainLayout>
    <AddButton tooltip={{text: 'Add condition or rule', position: 'left'}}
               dropdown={{
                 id: 'addRuleOrCondition',
                 title: 'Select option',
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