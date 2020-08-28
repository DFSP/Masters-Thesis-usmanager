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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.decision.ServiceDecisionResult;

@Slf4j
@Service
public class ServiceRulesService {

  // Service minimum logs to start applying rules
  private static final int SERVICE_MINIMUM_LOGS_COUNT = 1;

  private final DroolsService droolsService;
  private final ServicesMonitoringService servicesMonitoringService;
  private final ContainerRulesService containerRulesService;

  private final ServiceRuleRepository rules;

  private final String serviceRuleTemplateFile;
  private final AtomicLong lastUpdateServiceRules;

  public ServiceRulesService(DroolsService droolsService, @Lazy ServicesMonitoringService servicesMonitoringService,
                             ContainerRulesService containerRulesService, ServiceRuleRepository rules,
                             RulesProperties rulesProperties) {
    this.droolsService = droolsService;
    this.servicesMonitoringService = servicesMonitoringService;
    this.containerRulesService = containerRulesService;
    this.rules = rules;
    this.serviceRuleTemplateFile = rulesProperties.getServiceRuleTemplateFile();
    this.lastUpdateServiceRules = new AtomicLong(0);
  }

  // TODO call this when service rules, generic service rules container rules, and generic container rules are updated
  //from symmetricds
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

  public ServiceEntity getService(String ruleName, String serviceName) {
    assertRuleExists(ruleName);
    return rules.getService(ruleName, serviceName).orElseThrow(() ->
        new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName));
  }

  public List<ServiceEntity> getServices(String ruleName) {
    assertRuleExists(ruleName);
    return rules.getServices(ruleName);
  }

  private void assertRuleExists(String ruleName) {
    if (!rules.hasRule(ruleName)) {
      throw new EntityNotFoundException(ServiceRuleEntity.class, "ruleName", ruleName);
    }
  }

  public ServiceDecisionResult executeRules(String serviceHostname, String containerId, String serviceName,
                                            Map<String, Double> fields) {
    var containerEvent = new ContainerEvent(serviceHostname, containerId, serviceName);
    Map<String, Double> containerEventFields = containerEvent.getFields();
    servicesMonitoringService.getContainerMonitoring(containerId).stream()
        .filter(loggedField ->
            loggedField.getCount() >= SERVICE_MINIMUM_LOGS_COUNT && fields.get(loggedField.getField()) != null)
        .forEach(loggedField -> {
          long count = loggedField.getCount();
          String field = loggedField.getField();
          double newValue = fields.get(field);
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
        });
    return containerEventFields.isEmpty()
        ? new ServiceDecisionResult(serviceHostname, containerId, serviceName) : processContainerEvent(containerEvent);
  }

  public ServiceDecisionResult processContainerEvent(ContainerEvent containerEvent) {
    String containerId = containerEvent.getContainerId();
    String serviceName = containerEvent.getServiceName();
    if (droolsService.shouldCreateNewRuleSession(serviceName, lastUpdateServiceRules.get())) {
      List<Rule> rules = generateServiceRules(containerId, serviceName);
      Map<Long, String> drools = droolsService.executeDroolsRules(containerEvent, rules, serviceRuleTemplateFile);
      droolsService.createNewServiceRuleSession(serviceName, drools);
    }
    return droolsService.evaluate(containerEvent);
  }

  private List<Rule> generateServiceRules(String containerId, String serviceName) {
    List<ServiceRuleEntity> genericServiceRules = getGenericServiceRules();
    List<ServiceRuleEntity> serviceRules = getServiceRules(serviceName);
    List<Rule> containerRules = containerRulesService.generateContainerRules(containerId);
    var rules = new ArrayList<Rule>(genericServiceRules.size() + serviceRules.size() + containerRules.size());
    genericServiceRules.forEach(genericServiceRule -> rules.add(generateServiceRule(genericServiceRule)));
    log.info("Generated generic service rules (count: {})", genericServiceRules.size());
    serviceRules.forEach(serviceRule -> rules.add(generateServiceRule(serviceRule)));
    log.info("Generated service rules (count: {})", serviceRules.size());
    rules.addAll(containerRules);
    return rules;
  }

  private Rule generateServiceRule(ServiceRuleEntity serviceRule) {
    Long id = serviceRule.getId();
    List<Condition> conditions = getConditions(serviceRule.getName()).stream().map(condition -> {
      String fieldName = String.format("%s-%S", condition.getField().getName(), condition.getValueMode().getName());
      double value = condition.getValue();
      Operator operator = condition.getOperator().getOperator();
      return new Condition(fieldName, value, operator);
    }).collect(Collectors.toList());
    RuleDecision decision = serviceRule.getDecision().getRuleDecision();
    int priority = serviceRule.getPriority();
    return new Rule(id, conditions, decision, priority);
  }

}
