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

package pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServiceRulesService {

	private final ConditionsService conditionsService;
	private final DroolsService droolsService;
	private final ServicesService servicesService;

	private final ServiceRuleRepository rules;

	private final String serviceRuleTemplateFile;
	private final AtomicLong lastUpdateServiceRules;

	public ServiceRulesService(ConditionsService conditionsService, DroolsService droolsService,
							   @Lazy ServicesService servicesService, ServiceRuleRepository rules,
							   RulesProperties rulesProperties) {
		this.conditionsService = conditionsService;
		this.droolsService = droolsService;
		this.servicesService = servicesService;
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
		checkRuleDoesntExist(rule);
		log.info("Saving rule {}", ToStringBuilder.reflectionToString(rule));
		setLastUpdateServiceRules();
		return rules.save(rule);
	}

	public ServiceRuleEntity updateRule(String ruleName, ServiceRuleEntity newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		ServiceRuleEntity rule = getRule(ruleName);
		ObjectUtils.copyValidProperties(newRule, rule);
		rule = rules.save(rule);
		setLastUpdateServiceRules();
		return rule;
	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
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
		checkRuleExists(ruleName);
		return rules.getCondition(ruleName, conditionName).orElseThrow(() ->
			new EntityNotFoundException(ConditionEntity.class, "conditionName", conditionName));
	}

	public List<ConditionEntity> getConditions(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getConditions(ruleName);
	}

	public void addCondition(String ruleName, String conditionName) {
		log.info("Adding condition {} to rule {}", conditionName, ruleName);
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
		ServiceRuleEntity rule = getRule(ruleName);
		rule.getConditions()
			.removeIf(condition -> conditionNames.contains(condition.getServiceCondition().getName()));
		rules.save(rule);
		setLastUpdateServiceRules();
	}

	public ServiceEntity getService(String ruleName, String serviceName) {
		checkRuleExists(ruleName);
		return rules.getService(ruleName, serviceName).orElseThrow(() ->
			new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName));
	}

	public List<ServiceEntity> getServices(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getServices(ruleName);
	}

	public void addService(String ruleName, String serviceName) {
		addServices(ruleName, List.of(serviceName));
	}

	public void addServices(String ruleName, List<String> serviceNames) {
		log.info("Adding services {} to rule {}", serviceNames, ruleName);
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

	private void checkRuleExists(String ruleName) {
		if (!rules.hasRule(ruleName)) {
			throw new EntityNotFoundException(ServiceRuleEntity.class, "ruleName", ruleName);
		}
	}

	private void checkRuleDoesntExist(ServiceRuleEntity serviceRule) {
		String name = serviceRule.getName();
		if (rules.hasRule(name)) {
			throw new DataIntegrityViolationException("Service rule '" + name + "' already exists");
		}
	}

	public ServiceDecisionResult processServiceEvent(String appName, HostAddress hostAddress, ContainerEvent containerEvent) {
		String serviceName = containerEvent.getServiceName();
		if (droolsService.shouldCreateNewServiceRuleSession(serviceName, lastUpdateServiceRules.get())) {
			List<Rule> rules = generateServiceRules(appName, serviceName);
			Map<Long, String> drools = droolsService.executeDroolsRules(containerEvent, rules, serviceRuleTemplateFile);
			droolsService.createNewServiceRuleSession(serviceName, drools);
		}
		return droolsService.evaluate(hostAddress, containerEvent);
	}

	private List<Rule> generateServiceRules(String appName, String serviceName) {
		List<ServiceRuleEntity> genericServiceRules = getGenericServiceRules();
		List<ServiceRuleEntity> serviceRules = getServiceRules(serviceName);
		List<Rule> rules = new ArrayList<>(genericServiceRules.size() + serviceRules.size());
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
		RuleDecision decision = serviceRule.getDecision().getRuleDecision();
		int priority = serviceRule.getPriority();
		return new Rule(id, conditions, decision, priority);
	}

}
