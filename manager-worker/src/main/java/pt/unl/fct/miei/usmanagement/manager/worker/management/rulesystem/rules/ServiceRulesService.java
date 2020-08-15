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
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.worker.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.master.util.ObjectUtils;

@Slf4j
@Service
public class ServiceRulesService {

  // Container minimum logs to start applying rules
  private static final int CONTAINER_MINIMUM_LOGS_COUNT = 1;

  private final ConditionsService conditionsService;
  private final DroolsService droolsService;
  private final ServicesService servicesService;
  private final ServicesMonitoringService servicesMonitoringService;

  private final ServiceRuleRepository rules;

  private final String serviceRuleTemplateFile;
  private final AtomicLong lastUpdateServiceRules;

  public ServiceRulesService(ConditionsService conditionsService, DroolsService droolsService,
                             @Lazy ServicesService servicesService, ServicesMonitoringService servicesMonitoringService,
                             ServiceRuleRepository rules, RulesProperties rulesProperties) {
    this.conditionsService = conditionsService;
    this.droolsService = droolsService;
    this.servicesService = servicesService;
    this.servicesMonitoringService = servicesMonitoringService;
    this.rules = rules;
    this.serviceRuleTemplateFile = rulesProperties.getServiceRuleTemplateFile();
    this.lastUpdateServiceRules = new AtomicLong(0);
  }

  public void setLastUpdateServiceRules() {
    long currentTime = System.currentTimeMillis();
    lastUpdateServiceRules.getAndSet(currentTime);
  }

  public List<ServiceRuleEntity> getRules() {
    return rules.findAll();
  }

  public ServiceRuleEntity getRule(Long id) {
    return rules.findById(id).orElseThrow(() ->
        new EntityNotFoundException(ServiceRuleEntity.class, "id", id.toString()));
  }

  public ServiceRuleEntity getRule(String name) {
    return rules.findByNameIgnoreCase(name).orElseThrow(() ->
        new EntityNotFoundException(ServiceRuleEntity.class, "name", name));
  }

  public ServiceRuleEntity addRule(ServiceRuleEntity rule) {
    assertRuleDoesntExist(rule);
    log.debug("Saving rule {}", ToStringBuilder.reflectionToString(rule));
    setLastUpdateServiceRules();
    return rules.save(rule);
  }

  public ServiceRuleEntity updateRule(String ruleName, ServiceRuleEntity newRule) {
    log.debug("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
    ServiceRuleEntity rule = getRule(ruleName);
    ObjectUtils.copyValidProperties(newRule, rule);
    rule = rules.save(rule);
    setLastUpdateServiceRules();
    return rule;
  }

  public void deleteRule(String ruleName) {
    log.debug("Deleting rule {}", ruleName);
    ServiceRuleEntity rule = getRule(ruleName);
    rule.removeAssociations();
    rules.delete(rule);
    setLastUpdateServiceRules();
  }

  public List<ServiceRuleEntity> getServiceRules(String serviceName) {
    return rules.findByServiceName(serviceName);
  }

  public List<ServiceRuleEntity> getGenericServiceRules() {
    return rules.findGenericServiceRules();
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
    ServiceRuleEntity rule = getRule(ruleName);
    ServiceRuleConditionEntity serviceRuleCondition =
        ServiceRuleConditionEntity.builder().serviceCondition(condition).serviceRule(rule).build();
    rule = rule.toBuilder().condition(serviceRuleCondition).build();
    rules.save(rule);
    setLastUpdateServiceRules();
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
        .removeIf(condition -> conditionNames.contains(condition.getServiceCondition().getName()));
    rules.save(rule);
    setLastUpdateServiceRules();
  }

  public ServiceEntity getService(String ruleName, String serviceName) {
    assertRuleExists(ruleName);
    return rules.getService(ruleName, serviceName).orElseThrow(() ->
        new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName));
  }

  public List<ServiceEntity> getServices(String ruleName) {
    assertRuleExists(ruleName);
    return rules.getServices(ruleName);
  }

  public void addService(String ruleName, String serviceName) {
    addServices(ruleName, List.of(serviceName));
  }

  public void addServices(String ruleName, List<String> serviceNames) {
    log.debug("Adding services {} to rule {}", serviceNames, ruleName);
    ServiceRuleEntity rule = getRule(ruleName);
    serviceNames.forEach(serviceName -> {
      ServiceEntity service = servicesService.getService(serviceName);
      service.addRule(rule);
    });
    rules.save(rule);
    setLastUpdateServiceRules();
  }

  public void removeService(String ruleName, String serviceName) {
    removeServices(ruleName, List.of(serviceName));
  }

  public void removeServices(String ruleName, List<String> serviceNames) {
    log.info("Removing services {} from rule {}", serviceNames, ruleName);
    ServiceRuleEntity rule = getRule(ruleName);
    serviceNames.forEach(serviceName -> servicesService.getService(serviceName).removeRule(rule));
    rules.save(rule);
    setLastUpdateServiceRules();
  }

  private void assertRuleExists(String ruleName) {
    if (!rules.hasRule(ruleName)) {
      throw new EntityNotFoundException(ServiceRuleEntity.class, "ruleName", ruleName);
    }
  }

  private void assertRuleDoesntExist(ServiceRuleEntity serviceRule) {
    var name = serviceRule.getName();
    if (rules.hasRule(name)) {
      throw new DataIntegrityViolationException("Service rule '" + name + "' already exists");
    }
  }

  public ServiceDecisionResult runRules(String serviceHostname, String containerId,
                                        String serviceName, Map<String, Double> newFields) {
    List<ServiceMonitoringEntity> loggedFields = servicesMonitoringService.getContainerMonitoring(containerId);
    var containerEvent = new ContainerEvent(containerId, serviceName);
    Map<String, Double> containerEventFields = containerEvent.getFields();
    for (var loggedField : loggedFields) {
      long count = loggedField.getCount();
      if (count < CONTAINER_MINIMUM_LOGS_COUNT) {
        continue;
      }
      String field = loggedField.getField();
      Double newValue = newFields.get(field);
      if (newValue == null) {
        continue;
      }
      containerEventFields.put(field + "-effective-val", newValue);
      //TODO conta com este newValue?
      double sumValue = loggedField.getSumValue();
      double average = sumValue / (count * 1.0);
      containerEventFields.put(field + "-avg-val", average);
      double deviationFromAvgValue = ((newValue - average) / average) * 100;
      containerEventFields.put(field + "-deviation-%-on-avg-val", deviationFromAvgValue);
      double lastValue = loggedField.getLastValue();
      double deviationFromLastValue = ((newValue - lastValue) / lastValue) * 100;
      containerEventFields.put(field + "-deviation-%-on-last-val", deviationFromLastValue);
    }
    return containerEventFields.isEmpty()
        ? new ServiceDecisionResult(serviceHostname, containerId, serviceName)
        : processServiceEvent(serviceHostname, containerEvent);
  }

  public ServiceDecisionResult processServiceEvent(String serviceHostname, ContainerEvent containerEvent) {
    //FIXME: appName and serviceHostname
    String serviceName = containerEvent.getServiceName();
    if (droolsService.shouldCreateNewRuleSession(serviceName, lastUpdateServiceRules.get())) {
      List<Rule> rules = generateServiceRules(serviceName);
      Map<Long, String> drools = droolsService.executeDroolsRules(containerEvent, rules, serviceRuleTemplateFile);
      droolsService.createNewServiceRuleSession(serviceName, drools);
    }
    return droolsService.evaluate(serviceHostname, containerEvent);
  }

  private List<Rule> generateServiceRules(String serviceName) {
    List<ServiceRuleEntity> genericServiceRules = getGenericServiceRules();
    List<ServiceRuleEntity> serviceRules = getServiceRules(serviceName);
    var rules = new ArrayList<Rule>(genericServiceRules.size() + serviceRules.size());
    log.info("Generating service rules... (count: {})", rules.size());
    genericServiceRules.forEach(genericServiceRule -> rules.add(generateServiceRule(genericServiceRule)));
    serviceRules.forEach(serviceRule -> rules.add(generateServiceRule(serviceRule)));
    return rules;
  }

  private Rule generateServiceRule(ServiceRuleEntity serviceRule) {
    Long id = serviceRule.getId();
    List<Condition> conditions = getConditions(serviceRule.getName()).stream().map(condition -> {
      String fieldName = String.format("%s-%S", condition.getField().getName(),
          condition.getValueMode().getName());
      double value = condition.getValue();
      Operator operator = condition.getOperator().getOperator();
      return new Condition(fieldName, value, operator);
    }).collect(Collectors.toList());
    RuleDecision decision = serviceRule.getDecision().getValue();
    int priority = serviceRule.getPriority();
    return new Rule(id, conditions, decision, priority);
  }

}
