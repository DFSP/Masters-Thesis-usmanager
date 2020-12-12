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
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.RuleConditionsService;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRules;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleConditionKey;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ContainerRulesService {

	private final ConditionsService conditionsService;
	private final RuleConditionsService ruleConditionsService;
	private final ContainersService containersService;
	private final ServiceRulesService serviceRulesService;
	private final KafkaService kafkaService;

	private final ContainerRules rules;

	public ContainerRulesService(ConditionsService conditionsService, RuleConditionsService ruleConditionsService, @Lazy ContainersService containersService,
								 ContainerRules rules, ServiceRulesService serviceRulesService, KafkaService kafkaService) {
		this.conditionsService = conditionsService;
		this.ruleConditionsService = ruleConditionsService;
		this.containersService = containersService;
		this.rules = rules;
		this.serviceRulesService = serviceRulesService;
		this.kafkaService = kafkaService;
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
		rule = saveRule(rule);
		/*ContainerRule kafkaRule = rule;
		kafkaRule.setNew(true);
		kafkaService.sendContainerRule(kafkaRule);*/
		kafkaService.sendContainerRule(rule);
		return rule;
	}

	public ContainerRule updateRule(String ruleName, ContainerRule newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		ContainerRule rule = getRule(ruleName);
		EntityUtils.copyValidProperties(newRule, rule);
		rule = saveRule(rule);
		kafkaService.sendContainerRule(rule);
		return rule;
	}

	public ContainerRule saveRule(ContainerRule containerRule) {
		log.info("Saving containerRule {}", ToStringBuilder.reflectionToString(containerRule));
		containerRule = rules.save(containerRule);
		serviceRulesService.setLastUpdateServiceRules();
		return containerRule;
	}

	public void deleteRule(Long id) {
		log.info("Deleting rule {}", id);
		ContainerRule rule = getRule(id);
		deleteRule(rule);
	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
		ContainerRule rule = getRule(ruleName);
		deleteRule(rule);
	}

	public void deleteRule(ContainerRule rule) {
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
		ContainerRuleCondition containerRuleCondition = ContainerRuleCondition.builder()
			.id(new RuleConditionKey(rule.getId(), condition.getId()))
			.containerRule(rule).containerCondition(condition).build();
		ruleConditionsService.saveContainerRuleCondition(containerRuleCondition);
		rule = rule.toBuilder().condition(containerRuleCondition).build();
		kafkaService.sendContainerRule(rule);
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
		rule.getConditions().removeIf(condition -> conditionNames.contains(condition.getContainerCondition().getName()));
		rule = saveRule(rule);
		kafkaService.sendContainerRule(rule);
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
		ContainerRule containerRule = saveRule(rule);
		kafkaService.sendContainerRule(containerRule);
	}

	public void removeContainer(String ruleName, String containerId) {
		removeContainers(ruleName, List.of(containerId));
	}

	public void removeContainers(String ruleName, List<String> containerIds) {
		log.info("Removing containers {} from rule {}", containerIds, ruleName);
		ContainerRule rule = getRule(ruleName);
		containerIds.forEach(containerId -> containersService.getContainer(containerId).removeRule(rule));
		ContainerRule containerRule = saveRule(rule);
		kafkaService.sendContainerRule(containerRule);
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

	public ContainerRule addOrUpdateRule(ContainerRule containerRule) {
		if (containerRule.getId() != null) {
			Optional<ContainerRule> optionalRule = rules.findById(containerRule.getId());
			if (optionalRule.isPresent()) {
				ContainerRule rule = optionalRule.get();
				Set<ContainerRuleCondition> conditions = containerRule.getConditions();
				if (conditions != null) {
					rule.getConditions().retainAll(containerRule.getConditions());
					rule.getConditions().addAll(containerRule.getConditions());
				}
				Set<Container> containers = containerRule.getContainers();
				if (containers != null) {
					rule.getContainers().retainAll(containerRule.getContainers());
					rule.getContainers().addAll(containerRule.getContainers());
				}
				EntityUtils.copyValidProperties(containerRule, rule);
				return saveRule(rule);
			}
		}
			return saveRule(containerRule);
	}
	
}