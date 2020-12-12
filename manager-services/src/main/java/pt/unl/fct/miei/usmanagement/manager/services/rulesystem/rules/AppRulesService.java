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

package pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.RuleConditionsService;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRules;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleConditionKey;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AppRulesService {

	private final ConditionsService conditionsService;
	private final RuleConditionsService ruleConditionsService;
	private final AppsService appsService;
	private final ServiceRulesService serviceRulesService;
	private final KafkaService kafkaService;

	private final AppRules rules;

	public AppRulesService(ConditionsService conditionsService,
						   RuleConditionsService ruleConditionsService, @Lazy AppsService appsService, AppRules rules,
						   ServiceRulesService serviceRulesService,
						   KafkaService kafkaService) {
		this.conditionsService = conditionsService;
		this.ruleConditionsService = ruleConditionsService;
		this.appsService = appsService;
		this.rules = rules;
		this.serviceRulesService = serviceRulesService;
		this.kafkaService = kafkaService;
	}


	public List<AppRule> getRules() {
		return rules.findAll();
	}

	public AppRule getRule(Long id) {
		return rules.findById(id).orElseThrow(() ->
			new EntityNotFoundException(AppRule.class, "id", id.toString()));
	}

	public AppRule getRule(String name) {
		return rules.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(AppRule.class, "name", name));
	}

	public AppRule addRule(AppRule rule) {
		checkRuleDoesntExist(rule);
		rule = saveRule(rule);
		/*AppRule kafkaRule = rule;
		kafkaRule.setNew(true);
		kafkaService.sendAppRule(kafkaRule);*/
		kafkaService.sendAppRule(rule);
		return rule;
	}

	public AppRule updateRule(String ruleName, AppRule newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		AppRule rule = getRule(ruleName);
		EntityUtils.copyValidProperties(newRule, rule);
		rule = saveRule(rule);
		kafkaService.sendAppRule(rule);
		return rule;
	}

	public AppRule saveRule(AppRule appRule) {
		log.info("Saving appRule {}", ToStringBuilder.reflectionToString(appRule));
		AppRule rule = rules.save(appRule);
		serviceRulesService.setLastUpdateServiceRules();
		return rule;
	}

	public void deleteRule(Long id) {
		log.info("Deleting rule {}", id);
		AppRule rule = getRule(id);

	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
		AppRule rule = getRule(ruleName);
	}

	public void deleteRule(AppRule rule) {
		rule.removeAssociations();
		rules.delete(rule);
		serviceRulesService.setLastUpdateServiceRules();
	}

	public List<AppRule> getAppRules(String appName) {
		return rules.findByAppName(appName);
	}

	public Condition getCondition(String ruleName, String conditionName) {
		checkRuleExists(ruleName);
		return rules.getCondition(ruleName, conditionName).orElseThrow(() ->
			new EntityNotFoundException(Condition.class, "conditionName", conditionName));
	}

	public List<Condition> getConditions(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getConditions(ruleName);
	}

	public void addCondition(String ruleName, String conditionName) {
		log.info("Adding condition {} to rule {}", conditionName, ruleName);
		Condition condition = conditionsService.getCondition(conditionName);
		AppRule rule = getRule(ruleName);
		AppRuleCondition appRuleCondition = AppRuleCondition.builder()
			.id(new RuleConditionKey(rule.getId(), condition.getId()))
			.appRule(rule).appCondition(condition).build();
		ruleConditionsService.saveAppRuleCondition(appRuleCondition);
		rule = rule.toBuilder().condition(appRuleCondition).build();
		kafkaService.sendAppRule(rule);
	}

	public void addConditions(String ruleName, List<String> conditions) {
		conditions.forEach(condition -> addCondition(ruleName, condition));
	}

	public void removeCondition(String ruleName, String conditionName) {
		removeConditions(ruleName, List.of(conditionName));
	}

	public void removeConditions(String ruleName, List<String> conditionNames) {
		log.info("Removing conditions {}", conditionNames);
		AppRule rule = getRule(ruleName);
		rule.getConditions().removeIf(condition -> conditionNames.contains(condition.getAppCondition().getName()));
		rule = saveRule(rule);
		kafkaService.sendAppRule(rule);
	}

	public App getApp(String ruleName, String appName) {
		checkRuleExists(ruleName);
		return rules.getApp(ruleName, appName).orElseThrow(() ->
			new EntityNotFoundException(App.class, "appName", appName));
	}

	public List<App> getApps(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getApps(ruleName);
	}

	public void addApp(String ruleName, String appName) {
		addApps(ruleName, List.of(appName));
	}

	public void addApps(String ruleName, List<String> appNames) {
		log.info("Adding apps {} to rule {}", appNames, ruleName);
		AppRule rule = getRule(ruleName);
		appNames.forEach(appName -> {
			App app = appsService.getApp(appName);
			app.addRule(rule);
		});
		AppRule appRule = saveRule(rule);
		kafkaService.sendAppRule(appRule);
	}

	public void removeApp(String ruleName, String appName) {
		removeApps(ruleName, List.of(appName));
	}

	public void removeApps(String ruleName, List<String> appNames) {
		log.info("Removing apps {} from rule {}", appNames, ruleName);
		AppRule rule = getRule(ruleName);
		appNames.forEach(appName -> appsService.getApp(appName).removeRule(rule));
		AppRule appRule = saveRule(rule);
		kafkaService.sendAppRule(appRule);
	}

	private void checkRuleExists(String ruleName) {
		if (!rules.hasRule(ruleName)) {
			throw new EntityNotFoundException(AppRule.class, "ruleName", ruleName);
		}
	}

	private void checkRuleDoesntExist(AppRule appRule) {
		String name = appRule.getName();
		if (rules.hasRule(name)) {
			throw new DataIntegrityViolationException("App rule '" + name + "' already exists");
		}
	}

	public AppRule addOrUpdateRule(AppRule appRule) {
		if (appRule.getId() != null) {
			Optional<AppRule> optionalRule = rules.findById(appRule.getId());
			if (optionalRule.isPresent()) {
				AppRule rule = optionalRule.get();
				Set<AppRuleCondition> conditions = appRule.getConditions();
				if (conditions != null) {
					rule.getConditions().retainAll(appRule.getConditions());
					rule.getConditions().addAll(appRule.getConditions());
				}
				Set<App> apps = appRule.getApps();
				if (apps != null) {
					rule.getApps().retainAll(appRule.getApps());
					rule.getApps().addAll(appRule.getApps());
				}
				EntityUtils.copyValidProperties(appRule, rule);
				return saveRule(rule);
			}
		}
			return saveRule(appRule);
	}
}