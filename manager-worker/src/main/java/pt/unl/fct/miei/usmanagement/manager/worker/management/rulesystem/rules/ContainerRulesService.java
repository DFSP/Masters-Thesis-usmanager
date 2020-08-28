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

package pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ContainerRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ContainerRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.condition.Condition;

@Slf4j
@Service
public class ContainerRulesService {

  private final ContainerRuleRepository rules;

  public ContainerRulesService(ContainerRuleRepository rules) {
    this.rules = rules;
  }

  public List<ContainerRuleEntity> getRules() {
    return rules.findAll();
  }

  public ContainerRuleEntity getRule(Long id) {
    return rules.findById(id).orElseThrow(() ->
        new EntityNotFoundException(ContainerRuleEntity.class, "id", id.toString()));
  }

  public ContainerRuleEntity getRule(String name) {
    return rules.findByNameIgnoreCase(name).orElseThrow(() ->
        new EntityNotFoundException(ContainerRuleEntity.class, "name", name));
  }

  public List<ContainerRuleEntity> getContainerRules(String containerId) {
    return rules.findByContainerId(containerId);
  }
  
  public List<ContainerRuleEntity> getGenericContainerRules() {
    return rules.findGenericContainerRules();
  }

  public ConditionEntity getCondition(String ruleName, String conditionName) {
    assertRuleExists(ruleName);
    return rules.getCondition(ruleName, conditionName).orElseThrow(() ->
        new EntityNotFoundException(ConditionEntity.class, "conditionName", conditionName));
  }

  public List<ConditionEntity> getConditions(String ruleName) {
    assertRuleExists(ruleName);
    return rules.getConditions(ruleName);
  }
  
  public ContainerEntity getContainer(String ruleName, String containerId) {
    assertRuleExists(ruleName);
    return rules.getContainer(ruleName, containerId).orElseThrow(() ->
        new EntityNotFoundException(ContainerEntity.class, "containerId", containerId));
  }

  public List<ContainerEntity> getContainers(String ruleName) {
    assertRuleExists(ruleName);
    return rules.getContainers(ruleName);
  }

 
  private void assertRuleExists(String ruleName) {
    if (!rules.hasRule(ruleName)) {
      throw new EntityNotFoundException(ContainerRuleEntity.class, "ruleName", ruleName);
    }
  }

  public List<Rule> generateContainerRules(String containerId) {
    List<ContainerRuleEntity> genericContainerRules = getGenericContainerRules();
    List<ContainerRuleEntity> containerRules = getContainerRules(containerId);
    var rules = new ArrayList<Rule>(genericContainerRules.size() + containerRules.size());
    genericContainerRules.forEach(genericContainerRule -> rules.add(generateContainerRule(genericContainerRule)));
    log.info("Generated generic container rules (count: {})", genericContainerRules.size());
    containerRules.forEach(containerRule -> rules.add(generateContainerRule(containerRule)));
    log.info("Generated container rules (count: {})", containerRules.size());
    return rules;
  }

  private Rule generateContainerRule(ContainerRuleEntity containerRule) {
    Long id = containerRule.getId();
    List<Condition> conditions = getConditions(containerRule.getName()).stream().map(condition -> {
      String fieldName = String.format("%s-%S", condition.getField().getName(), condition.getValueMode().getName());
      double value = condition.getValue();
      Operator operator = condition.getOperator().getOperator();
      return new Condition(fieldName, value, operator);
    }).collect(Collectors.toList());
    RuleDecision decision = containerRule.getDecision().getRuleDecision();
    int priority = containerRule.getPriority();
    return new Rule(id, conditions, decision, priority);
  }

}
