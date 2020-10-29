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
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppRulesService {

	private final ConditionsService conditionsService;
	private final DroolsService droolsService;
	private final AppsService appsService;

	private final AppRuleRepository rules;

	private final String appRuleTemplateFile;
	private final AtomicLong lastUpdateAppRules;

	public AppRulesService(ConditionsService conditionsService, DroolsService droolsService,
						   @Lazy AppsService appsService, AppRuleRepository rules,
						   RulesProperties rulesProperties) {
		this.conditionsService = conditionsService;
		this.droolsService = droolsService;
		this.appsService = appsService;
		this.rules = rules;
		this.appRuleTemplateFile = rulesProperties.getAppRuleTemplateFile();
		this.lastUpdateAppRules = new AtomicLong(0);
	}

	public void setLastUpdateAppRules() {
		long currentTime = System.currentTimeMillis();
		lastUpdateAppRules.getAndSet(currentTime);
	}

	public List<AppRuleEntity> getRules() {
		return rules.findAll();
	}

	public AppRuleEntity getRule(Long id) {
		return rules.findById(id).orElseThrow(() ->
			new EntityNotFoundException(AppRuleEntity.class, "id", id.toString()));
	}

	public AppRuleEntity getRule(String name) {
		return rules.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(AppRuleEntity.class, "name", name));
	}

	public AppRuleEntity addRule(AppRuleEntity rule) {
		checkRuleDoesntExist(rule);
		log.info("Saving rule {}", ToStringBuilder.reflectionToString(rule));
		setLastUpdateAppRules();
		return rules.save(rule);
	}

	public AppRuleEntity updateRule(String ruleName, AppRuleEntity newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		AppRuleEntity rule = getRule(ruleName);
		ObjectUtils.copyValidProperties(newRule, rule);
		rule = rules.save(rule);
		setLastUpdateAppRules();
		return rule;
	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
		AppRuleEntity rule = getRule(ruleName);
		rule.removeAssociations();
		rules.delete(rule);
		setLastUpdateAppRules();
	}

	public List<AppRuleEntity> getAppRules(String appName) {
		return rules.findByAppName(appName);
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
		AppRuleEntity rule = getRule(ruleName);
		AppRuleConditionEntity appRuleCondition =
			AppRuleConditionEntity.builder().appCondition(condition).appRule(rule).build();
		rule = rule.toBuilder().condition(appRuleCondition).build();
		rules.save(rule);
		setLastUpdateAppRules();
	}

	public void addConditions(String ruleName, List<String> conditions) {
		conditions.forEach(condition -> addCondition(ruleName, condition));
	}

	public void removeCondition(String ruleName, String conditionName) {
		removeConditions(ruleName, List.of(conditionName));
	}

	public void removeConditions(String ruleName, List<String> conditionNames) {
		log.info("Removing conditions {}", conditionNames);
		AppRuleEntity rule = getRule(ruleName);
		rule.getConditions()
			.removeIf(condition -> conditionNames.contains(condition.getAppCondition().getName()));
		rules.save(rule);
		setLastUpdateAppRules();
	}

	public AppEntity getApp(String ruleName, String appName) {
		checkRuleExists(ruleName);
		return rules.getApp(ruleName, appName).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "appName", appName));
	}

	public List<AppEntity> getApps(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getApps(ruleName);
	}

	public void addApp(String ruleName, String appName) {
		addApps(ruleName, List.of(appName));
	}

	public void addApps(String ruleName, List<String> appNames) {
		log.info("Adding apps {} to rule {}", appNames, ruleName);
		AppRuleEntity rule = getRule(ruleName);
		appNames.forEach(appName -> {
			AppEntity app = appsService.getApp(appName);
			app.addRule(rule);
		});
		rules.save(rule);
		setLastUpdateAppRules();
	}

	public void removeApp(String ruleName, String appName) {
		removeApps(ruleName, List.of(appName));
	}

	public void removeApps(String ruleName, List<String> appNames) {
		log.info("Removing apps {} from rule {}", appNames, ruleName);
		AppRuleEntity rule = getRule(ruleName);
		appNames.forEach(appName -> appsService.getApp(appName).removeRule(rule));
		rules.save(rule);
		setLastUpdateAppRules();
	}

	private void checkRuleExists(String ruleName) {
		if (!rules.hasRule(ruleName)) {
			throw new EntityNotFoundException(AppRuleEntity.class, "ruleName", ruleName);
		}
	}

	private void checkRuleDoesntExist(AppRuleEntity appRule) {
		String name = appRule.getName();
		if (rules.hasRule(name)) {
			throw new DataIntegrityViolationException("App rule '" + name + "' already exists");
		}
	}

	public AppDecisionResult processAppEvent(HostAddress hostAddress, ContainerEvent containerEvent) {
		String appName = containerEvent.getAppName();
		if (droolsService.shouldCreateNewAppRuleSession(appName, lastUpdateAppRules.get())) {
			List<Rule> rules = generateAppRules(appName);
			Map<Long, String> drools = droolsService.executeDroolsRules(containerEvent, rules, appRuleTemplateFile);
			droolsService.createNewAppRuleSession(appName, drools);
		}
		return droolsService.evaluate(hostAddress, containerEvent);
	}

	private List<Rule> generateAppRules(String appName) {
		List<AppRuleEntity> appRules = getAppRules(appName);
		log.info("Generating app rules... (count: {})", appRules.size());
		return appRules.stream().map(this::generateAppRule).collect(Collectors.toList());
	}

	private Rule generateAppRule(AppRuleEntity appRule) {
		Long id = appRule.getId();
		List<Condition> conditions = getConditions(appRule.getName()).stream().map(condition -> {
			String fieldName = String.format("%s-%S", condition.getField().getName(),
				condition.getValueMode().getName());
			double value = condition.getValue();
			Operator operator = condition.getOperator().getOperator();
			return new Condition(fieldName, value, operator);
		}).collect(Collectors.toList());
		RuleDecision decision = appRule.getDecision().getRuleDecision();
		int priority = appRule.getPriority();
		return new Rule(id, conditions, decision, priority);
	}

}
