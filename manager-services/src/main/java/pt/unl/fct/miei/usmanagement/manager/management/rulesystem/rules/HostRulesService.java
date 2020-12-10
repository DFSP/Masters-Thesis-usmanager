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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.RuleConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRules;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleConditionKey;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostRulesService {

	private final ConditionsService conditionsService;
	private final RuleConditionsService ruleConditionsService;
	private final CloudHostsService cloudHostsService;
	private final EdgeHostsService edgeHostsService;
	private final DroolsService droolsService;
	private final KafkaService kafkaService;

	private final HostRules rules;

	private final String hostRuleTemplateFile;
	private final AtomicLong lastUpdateHostRules;

	public HostRulesService(ConditionsService conditionsService, RuleConditionsService ruleConditionsService, CloudHostsService cloudHostsService,
							EdgeHostsService edgeHostsService, DroolsService droolsService, KafkaService kafkaService,
							HostRules rules, RulesProperties rulesProperties) {
		this.conditionsService = conditionsService;
		this.ruleConditionsService = ruleConditionsService;
		this.cloudHostsService = cloudHostsService;
		this.edgeHostsService = edgeHostsService;
		this.droolsService = droolsService;
		this.kafkaService = kafkaService;
		this.rules = rules;
		this.hostRuleTemplateFile = rulesProperties.getHostRuleTemplateFile();
		this.lastUpdateHostRules = new AtomicLong(0);
	}

	public void setLastUpdateHostRules() {
		long currentTime = System.currentTimeMillis();
		lastUpdateHostRules.getAndSet(currentTime);
	}

	public List<HostRule> getRules() {
		return rules.findAll();
	}

	public List<HostRule> getRules(HostAddress hostAddress) {
		String publicIpAddress = hostAddress.getPublicIpAddress();
		String privateIpAddress = hostAddress.getPrivateIpAddress();
		return rules.findByHostAddress(publicIpAddress, privateIpAddress);
	}

	public HostRule getRule(Long id) {
		return rules.findById(id).orElseThrow(() ->
			new EntityNotFoundException(HostRule.class, "id", id.toString()));
	}

	public HostRule getRule(String name) {
		return rules.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(HostRule.class, "name", name));
	}

	public HostRule getRuleAndEntities(String name) {
		return rules.findByNameWithEntities(name).orElseThrow(() ->
			new EntityNotFoundException(HostRule.class, "name", name));
	}

	public HostRule addRule(HostRule rule) {
		checkRuleDoesntExist(rule);
		rule = saveRule(rule);
		/*HostRule kafkaRule = rule;
		kafkaRule.setNew(true);
		kafkaService.sendHostRule(kafkaRule);*/
		kafkaService.sendHostRule(rule);
		return rule;
	}

	public HostRule updateRule(String ruleName, HostRule newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		HostRule rule = getRule(ruleName);
		EntityUtils.copyValidProperties(newRule, rule);
		rule = saveRule(rule);
		kafkaService.sendHostRule(rule);
		return rule;
	}

	public HostRule addOrUpdateRule(HostRule hostRule) {
		Optional<HostRule> optionalRule = rules.findById(hostRule.getId());
		if (optionalRule.isPresent()) {
			HostRule rule = optionalRule.get();
			Set<HostRuleCondition> conditions = hostRule.getConditions();
			if (conditions != null) {
				rule.getConditions().retainAll(hostRule.getConditions());
				rule.getConditions().addAll(hostRule.getConditions());
			}
			Set<EdgeHost> edgeHosts = hostRule.getEdgeHosts();
			if (edgeHosts != null) {
				rule.getEdgeHosts().retainAll(hostRule.getEdgeHosts());
				rule.getEdgeHosts().addAll(hostRule.getEdgeHosts());
			}
			Set<CloudHost> cloudHosts = hostRule.getCloudHosts();
			if (cloudHosts != null) {
				rule.getCloudHosts().retainAll(hostRule.getCloudHosts());
				rule.getCloudHosts().addAll(hostRule.getCloudHosts());
			}
			EntityUtils.copyValidProperties(hostRule, rule);
			return saveRule(rule);
		}
		else {
			return saveRule(hostRule);
		}
	}

	public HostRule saveRule(HostRule hostRule) {
		log.info("Saving hostRule {}", ToStringBuilder.reflectionToString(hostRule));
		hostRule = rules.save(hostRule);
		setLastUpdateHostRules();
		return hostRule;
	}

	public void deleteRule(Long id) {
		log.info("Deleting rule {}", id);
		HostRule rule = getRule(id);
		deleteRule(rule);
	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
		HostRule rule = getRule(ruleName);
		deleteRule(rule);
	}

	public void deleteRule(HostRule rule) {
		rule.removeAssociations();
		rules.delete(rule);
		setLastUpdateHostRules();
	}

	public List<HostRule> getGenericHostRules() {
		return rules.findGenericHostRules();
	}

	public HostRule getGenericHostRule(String ruleName) {
		return rules.findGenericHostRule(ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRule.class, "ruleName", ruleName));
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
		HostRule rule = getRuleAndEntities(ruleName);
		HostRuleCondition hostRuleCondition = HostRuleCondition.builder()
			.id(new RuleConditionKey(rule.getId(), condition.getId()))
			.hostRule(rule).hostCondition(condition).build();
		ruleConditionsService.saveHostRuleCondition(hostRuleCondition);
		rule = rule.toBuilder().condition(hostRuleCondition).build();
		kafkaService.sendHostRule(rule);
	}

	public void addConditions(String ruleName, List<String> conditions) {
		conditions.forEach(condition -> addCondition(ruleName, condition));
	}

	public void removeCondition(String ruleName, String conditionName) {
		removeConditions(ruleName, List.of(conditionName));
	}

	public void removeConditions(String ruleName, List<String> conditionNames) {
		log.info("Removing conditions {}", conditionNames);
		HostRule rule = getRuleAndEntities(ruleName);
		rule.getConditions().removeIf(condition -> conditionNames.contains(condition.getHostCondition().getName()));
		rule = saveRule(rule);
		kafkaService.sendHostRule(rule);
	}

	public CloudHost getCloudHost(String ruleName, String instanceId) {
		checkRuleExists(ruleName);
		return rules.getCloudHost(ruleName, instanceId).orElseThrow(() ->
			new EntityNotFoundException(CloudHost.class, "instanceId", instanceId));
	}

	public List<CloudHost> getCloudHosts(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getCloudHosts(ruleName);
	}

	public void addCloudHost(String ruleName, String instanceId) {
		addCloudHosts(ruleName, List.of(instanceId));
	}

	public void addCloudHosts(String ruleName, List<String> instanceIds) {
		log.info("Adding cloud hosts {} to rule {}", instanceIds, ruleName);
		HostRule rule = getRuleAndEntities(ruleName);
		instanceIds.forEach(instanceId -> {
			CloudHost cloudHost = cloudHostsService.getCloudHostByIdOrIp(instanceId);
			cloudHost.addRule(rule);
		});
		HostRule hostRule = saveRule(rule);
		kafkaService.sendHostRule(hostRule);
	}

	public void removeCloudHost(String ruleName, String instanceId) {
		removeCloudHosts(ruleName, List.of(instanceId));
	}

	public void removeCloudHosts(String ruleName, List<String> instanceIds) {
		log.info("Removing cloud hosts {} from rule {}", instanceIds, ruleName);
		HostRule rule = getRuleAndEntities(ruleName);
		instanceIds.forEach(instanceId -> cloudHostsService.getCloudHostByIdOrIp(instanceId).removeRule(rule));
		HostRule hostRule = saveRule(rule);
		kafkaService.sendHostRule(hostRule);
	}

	public EdgeHost getEdgeHost(String ruleName, String hostname) {
		checkRuleExists(ruleName);
		return rules.getEdgeHost(ruleName, hostname).orElseThrow(() ->
			new EntityNotFoundException(EdgeHost.class, "hostname", hostname));
	}

	public List<EdgeHost> getEdgeHosts(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getEdgeHosts(ruleName);
	}

	public void addEdgeHost(String ruleName, HostAddress hostAddress) {
		addEdgeHosts(ruleName, List.of(hostAddress));
	}

	public void addEdgeHosts(String ruleName, List<HostAddress> hostAddresses) {
		log.info("Adding edge hosts {} to rule {}", hostAddresses, ruleName);
		HostRule rule = getRuleAndEntities(ruleName);
		hostAddresses.forEach(hostAddress -> {
			EdgeHost edgeHost = edgeHostsService.getEdgeHostByAddress(hostAddress);
			edgeHost.addRule(rule);
		});
		HostRule hostRule = saveRule(rule);
		kafkaService.sendHostRule(hostRule);
	}

	public void removeEdgeHost(String ruleName, HostAddress hostAddress) {
		removeEdgeHosts(ruleName, List.of(hostAddress));
	}

	public void removeEdgeHosts(String ruleName, List<HostAddress> hostAddresses) {
		log.info("Removing edge hosts {} from rule {}", hostAddresses, ruleName);
		HostRule rule = getRuleAndEntities(ruleName);
		hostAddresses.forEach(hostAddress -> edgeHostsService.getEdgeHostByAddress(hostAddress).removeRule(rule));
		HostRule hostRule = saveRule(rule);
		kafkaService.sendHostRule(hostRule);
	}

	private void checkRuleExists(String ruleName) {
		if (!hasRule(ruleName)) {
			throw new EntityNotFoundException(HostRule.class, "ruleName", ruleName);
		}
	}

	private void checkRuleDoesntExist(HostRule hostRule) {
		String name = hostRule.getName();
		if (hasRule(name)) {
			throw new DataIntegrityViolationException("Host rule '" + name + "' already exists");
		}
	}

	public boolean hasRule(String name) {
		return rules.hasRule(name);
	}

	public HostDecisionResult processHostEvent(HostEvent hostEvent) {
		HostAddress hostAddress = hostEvent.getHostAddress();
		if (droolsService.shouldCreateNewHostRuleSession(hostAddress, lastUpdateHostRules.get())) {
			List<Rule> rules = generateHostRules(hostAddress);
			Map<Long, String> drools = droolsService.executeDroolsRules(hostEvent, rules, hostRuleTemplateFile);
			droolsService.createNewHostRuleSession(hostAddress, drools);
		}
		return droolsService.evaluate(hostEvent);
	}

	private List<Rule> generateHostRules(HostAddress hostAddress) {
		List<HostRule> genericHostRules = getGenericHostRules();
		List<HostRule> hostRules = getRules(hostAddress);
		int count = genericHostRules.size() + hostRules.size();
		List<Rule> rules = new ArrayList<>(count);
		log.info("Generating host rules... (count: {})", count);
		hostRules.forEach(hostRule -> rules.add(generateRule(hostRule)));
		genericHostRules.forEach(hostRule -> rules.add(generateRule(hostRule)));
		return rules;
	}

	private Rule generateRule(HostRule hostRule) {
		Long id = hostRule.getId();
		List<pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.Condition> conditions =
			getConditions(hostRule.getName()).stream().map(condition -> {
				String fieldName = String.format("%s-%s", condition.getField().getName(), condition.getValueMode().getName().toLowerCase());
				double value = condition.getValue();
				OperatorEnum operator = condition.getOperator().getOperator();
				return new pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.Condition(fieldName, value, operator);
			}).collect(Collectors.toList());
		RuleDecisionEnum decision = hostRule.getDecision().getRuleDecision();
		int priority = hostRule.getPriority();
		return new Rule(id, conditions, decision, priority);
	}

}