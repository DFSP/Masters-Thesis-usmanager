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

package pt.unl.fct.microservicemanagement.manager.master.manager.rulesystem.rules.containers;

import pt.unl.fct.microservicemanagement.manager.master.manager.containers.ContainersService;
import pt.unl.fct.microservicemanagement.manager.master.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.microservicemanagement.manager.master.manager.rulesystem.condition.ConditionsService;
import pt.unl.fct.microservicemanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.manager.master.manager.containers.ContainerEntity;
import pt.unl.fct.microservicemanagement.manager.master.manager.rulesystem.rules.DroolsService;
import pt.unl.fct.microservicemanagement.manager.master.manager.rulesystem.rules.RulesProperties;
import pt.unl.fct.microservicemanagement.manager.master.util.ObjectUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContainerRulesService {

  private final ConditionsService conditionsService;
  private final ContainersService containersService;
  private final DroolsService droolsService;

  private final ContainerRuleRepository rules;

  //private final String containerRuleTemplateFile;
  //private final AtomicLong lastUpdateContainerRules;

  public ContainerRulesService(ConditionsService conditionsService, @Lazy ContainersService containersService,
                               DroolsService droolsService, ContainerRuleRepository rules,
                               RulesProperties rulesProperties) {
    this.conditionsService = conditionsService;
    this.containersService = containersService;
    this.droolsService = droolsService;
    this.rules = rules;
    //this.containerRuleTemplateFile = rulesProperties.getContainerRuleTemplateFile();
    //this.lastUpdateContainerRules = new AtomicLong(0);
  }

  /*public void setLastUpdateContainerRules() {
    long currentTime = System.currentTimeMillis();
    lastUpdateContainerRules.getAndSet(currentTime);
  }*/

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

  public ContainerRuleEntity addRule(ContainerRuleEntity rule) {
    assertRuleDoesntExist(rule);
    log.debug("Saving rule {}", ToStringBuilder.reflectionToString(rule));
    //setLastUpdateContainerRules();
    return rules.save(rule);
  }

  public ContainerRuleEntity updateRule(String ruleName, ContainerRuleEntity newRule) {
    log.debug("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
    ContainerRuleEntity rule = getRule(ruleName);
    ObjectUtils.copyValidProperties(newRule, rule);
    rule = rules.save(rule);
    //setLastUpdateContainerRules();
    return rule;
  }

  public void deleteRule(String ruleName) {
    log.debug("Deleting rule {}", ruleName);
    ContainerRuleEntity rule = getRule(ruleName);
    rule.removeAssociations();
    rules.delete(rule);
    //setLastUpdateContainerRules();
  }

  public List<ContainerRuleEntity> getGenericContainerRules() {
    return rules.findGenericContainerRules();
  }

  public ContainerRuleEntity getGenericContainerRule(String ruleName) {
    return rules.findGenericContainerRule(ruleName).orElseThrow(() ->
        new EntityNotFoundException(ContainerRuleEntity.class, "ruleName", ruleName));
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

  public void addCondition(String ruleName, String conditionName) {
    log.debug("Adding condition {} to rule {}", conditionName, ruleName);
    ConditionEntity condition = conditionsService.getCondition(conditionName);
    ContainerRuleEntity rule = getRule(ruleName);
    ContainerRuleConditionEntity containerRuleCondition =
        ContainerRuleConditionEntity.builder().containerCondition(condition).containerRule(rule).build();
    rule = rule.toBuilder().condition(containerRuleCondition).build();
    rules.save(rule);
    //setLastUpdateContainerRules();
  }

  public void addConditions(String ruleName, List<String> conditions) {
    conditions.forEach(condition -> addCondition(ruleName, condition));
  }

  public void removeCondition(String ruleName, String conditionName) {
    removeConditions(ruleName, List.of(conditionName));
  }

  public void removeConditions(String ruleName, List<String> conditionNames) {
    log.info("Removing conditions {}", conditionNames);
    var rule = getRule(ruleName);
    rule.getConditions()
        .removeIf(condition -> conditionNames.contains(condition.getContainerCondition().getName()));
    rules.save(rule);
    //setLastUpdateContainerRules();
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

  public void addContainer(String ruleName, String containerId) {
    addContainers(ruleName, List.of(containerId));
  }

  public void addContainers(String ruleName, List<String> containerIds) {
    log.debug("Adding containers {} to rule {}", containerIds, ruleName);
    ContainerRuleEntity rule = getRule(ruleName);
    containerIds.forEach(containerId -> {
      ContainerEntity container = containersService.getContainer(containerId);
      container.addRule(rule);
    });
    rules.save(rule);
    //setLastUpdateContainerRules();
  }

  public void removeContainer(String ruleName, String containerId) {
    removeContainers(ruleName, List.of(containerId));
  }

  public void removeContainers(String ruleName, List<String> containerIds) {
    log.info("Removing containers {} from rule {}", containerIds, ruleName);
    ContainerRuleEntity rule = getRule(ruleName);
    containerIds.forEach(containerId -> containersService.getContainer(containerId).removeRule(rule));
    rules.save(rule);
    //setLastUpdateContainerRules();
  }

  private void assertRuleExists(String ruleName) {
    if (!rules.hasRule(ruleName)) {
      throw new EntityNotFoundException(ContainerRuleEntity.class, "ruleName", ruleName);
    }
  }

  private void assertRuleDoesntExist(ContainerRuleEntity containerRule) {
    var name = containerRule.getName();
    if (rules.hasRule(name)) {
      throw new DataIntegrityViolationException("Container rule '" + name + "' already exists");
    }
  }

  /* public HostDecisionResult processHostEvent(String hostname, HostEvent hostEvent) {
    if (droolsService.shouldCreateNewRuleSession(hostname, lastUpdateContainerRules.get())) {
      List<Rule> rules = generateContainerRules(hostname);
      Map<Long, String> drools = droolsService.executeDroolsRules(hostEvent, rules, containerRuleTemplateFile);
      droolsService.createNewContainerRuleSession(hostname, drools);
    }
    return droolsService.evaluate(hostEvent);
  }

  private List<Rule> generateContainerRules(String hostname) {
    //FIXME what about containers?
    List<ContainerRuleEntity> containerRules = getRules(hostname);
    var rules = new ArrayList<Rule>(containerRules.size());
    log.info("Generating Container rules... (count: {})", rules.size());
    containerRules.forEach(containerRule -> rules.add(generateRule(containerRule)));
    return rules;
  }

  private Rule generateRule(ContainerRuleEntity containerRule) {
    Long id = containerRule.getId();
    List<Condition> conditions = getConditions(containerRule.getName()).stream().map(condition -> {
      String fieldName = String.format("%s-%S", condition.getField().getName(),
          condition.getValueMode().getName());
      double value = condition.getValue();
      Operator operator = Operator.fromValue(condition.getOperator().getName());
      return new Condition(fieldName, value, operator);
    }).collect(Collectors.toList());
    RuleDecision decision = RuleDecision.fromValue(containerRule.getDecision().getName());
    int priority = containerRule.getPriority();
    return new Rule(id, conditions, decision, priority);
  }*/

}
