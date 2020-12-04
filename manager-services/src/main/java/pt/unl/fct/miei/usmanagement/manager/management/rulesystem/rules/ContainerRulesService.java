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
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRules;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;

@Slf4j
@Service
public class ContainerRulesService {

	private final ConditionsService conditionsService;
	private final ContainersService containersService;
	private final ServiceRulesService serviceRulesService;

	private final ContainerRules rules;

	public ContainerRulesService(ConditionsService conditionsService, @Lazy ContainersService containersService,
								 ContainerRules rules, ServiceRulesService serviceRulesService) {
		this.conditionsService = conditionsService;
		this.containersService = containersService;
		this.rules = rules;
		this.serviceRulesService = serviceRulesService;
	}

	public List<ContainerRule> getRules() {
		return rules.findAll();
	}

	public ContainerRule getRule(Long id) {
		return rules.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ContainerRule.class, "id", id.toString()));
	}

	public ContainerRule getRule(String name) {
		return rules.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(ContainerRule.class, "name", name));
	}

	public ContainerRule addRule(ContainerRule rule) {
		checkRuleDoesntExist(rule);
		log.info("Saving rule {}", ToStringBuilder.reflectionToString(rule));
		serviceRulesService.setLastUpdateServiceRules();
		return rules.save(rule);
	}

	public ContainerRule updateRule(String ruleName, ContainerRule newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		ContainerRule rule = getRule(ruleName);
		ObjectUtils.copyValidProperties(newRule, rule);
		rule = rules.save(rule);
		serviceRulesService.setLastUpdateServiceRules();
		return rule;
	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
		ContainerRule rule = getRule(ruleName);
		rule.removeAssociations();
		rules.delete(rule);
		serviceRulesService.setLastUpdateServiceRules();
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
		ContainerRule rule = getRule(ruleName);
		ContainerRuleCondition containerRuleCondition =
			ContainerRuleCondition.builder().containerCondition(condition).containerRule(rule).build();
		rule = rule.toBuilder().condition(containerRuleCondition).build();
		rules.save(rule);
		serviceRulesService.setLastUpdateServiceRules();
	}

	public void addConditions(String ruleName, List<String> conditions) {
		conditions.forEach(condition -> addCondition(ruleName, condition));
	}

	public void removeCondition(String ruleName, String conditionName) {
		removeConditions(ruleName, List.of(conditionName));
	}

	public void removeConditions(String ruleName, List<String> conditionNames) {
		log.info("Removing conditions {}", conditionNames);
		ContainerRule rule = getRule(ruleName);
		rule.getConditions()
			.removeIf(condition -> conditionNames.contains(condition.getContainerCondition().getName()));
		rules.save(rule);
		serviceRulesService.setLastUpdateServiceRules();
	}

	public Container getContainer(String ruleName, String containerId) {
		checkRuleExists(ruleName);
		return rules.getContainer(ruleName, containerId).orElseThrow(() ->
			new EntityNotFoundException(Container.class, "containerId", containerId));
	}

	public List<Container> getContainers(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getContainers(ruleName);
	}

	public void addContainer(String ruleName, String containerId) {
		addContainers(ruleName, List.of(containerId));
	}

	public void addContainers(String ruleName, List<String> containerIds) {
		log.info("Adding containers {} to rule {}", containerIds, ruleName);
		ContainerRule rule = getRule(ruleName);
		containerIds.forEach(containerId -> {
			Container container = containersService.getContainer(containerId);
			container.addRule(rule);
		});
		rules.save(rule);
		serviceRulesService.setLastUpdateServiceRules();
	}

	public void removeContainer(String ruleName, String containerId) {
		removeContainers(ruleName, List.of(containerId));
	}

	public void removeContainers(String ruleName, List<String> containerIds) {
		log.info("Removing containers {} from rule {}", containerIds, ruleName);
		ContainerRule rule = getRule(ruleName);
		containerIds.forEach(containerId -> containersService.getContainer(containerId).removeRule(rule));
		rules.save(rule);
		serviceRulesService.setLastUpdateServiceRules();
	}

	private void checkRuleExists(String ruleName) {
		if (!rules.hasRule(ruleName)) {
			throw new EntityNotFoundException(ContainerRule.class, "ruleName", ruleName);
		}
	}

	private void checkRuleDoesntExist(ContainerRule containerRule) {
		String name = containerRule.getName();
		if (rules.hasRule(name)) {
			throw new DataIntegrityViolationException("Container rule '" + name + "' already exists");
		}
	}

}