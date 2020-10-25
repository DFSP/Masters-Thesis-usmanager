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
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostRulesService {

	private final ConditionsService conditionsService;
	private final CloudHostsService cloudHostsService;
	private final EdgeHostsService edgeHostsService;
	private final DroolsService droolsService;

	private final HostRuleRepository rules;

	private final String hostRuleTemplateFile;
	private final AtomicLong lastUpdateHostRules;

	public HostRulesService(ConditionsService conditionsService, CloudHostsService cloudHostsService,
							EdgeHostsService edgeHostsService, DroolsService droolsService, HostRuleRepository rules,
							RulesProperties rulesProperties) {
		this.conditionsService = conditionsService;
		this.cloudHostsService = cloudHostsService;
		this.edgeHostsService = edgeHostsService;
		this.droolsService = droolsService;
		this.rules = rules;
		this.hostRuleTemplateFile = rulesProperties.getHostRuleTemplateFile();
		this.lastUpdateHostRules = new AtomicLong(0);
	}

	public void setLastUpdateHostRules() {
		long currentTime = System.currentTimeMillis();
		lastUpdateHostRules.getAndSet(currentTime);
	}

	public List<HostRuleEntity> getRules() {
		return rules.findAll();
	}

	public List<HostRuleEntity> getRules(HostAddress hostAddress) {
		String publicIpAddress = hostAddress.getPublicIpAddress();
		String privateIpAddress = hostAddress.getPrivateIpAddress();
		return rules.findByHostAddress(publicIpAddress, privateIpAddress);
	}

	public HostRuleEntity getRule(Long id) {
		return rules.findById(id).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "id", id.toString()));
	}

	public HostRuleEntity getRule(String name) {
		return rules.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "name", name));
	}

	public HostRuleEntity addRule(HostRuleEntity rule) {
		checkRuleDoesntExist(rule);
		log.info("Saving rule {}", ToStringBuilder.reflectionToString(rule));
		setLastUpdateHostRules();
		return rules.save(rule);
	}

	public HostRuleEntity updateRule(String ruleName, HostRuleEntity newRule) {
		log.info("Updating rule {} with {}", ruleName, ToStringBuilder.reflectionToString(newRule));
		HostRuleEntity rule = getRule(ruleName);
		ObjectUtils.copyValidProperties(newRule, rule);
		rule = rules.save(rule);
		setLastUpdateHostRules();
		return rule;
	}

	public void deleteRule(String ruleName) {
		log.info("Deleting rule {}", ruleName);
		HostRuleEntity rule = getRule(ruleName);
		rule.removeAssociations();
		rules.delete(rule);
		setLastUpdateHostRules();
	}

	public List<HostRuleEntity> getGenericHostRules() {
		return rules.findGenericHostRules();
	}

	public HostRuleEntity getGenericHostRule(String ruleName) {
		return rules.findGenericHostRule(ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName));
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
		HostRuleEntity rule = getRule(ruleName);
		HostRuleConditionEntity hostRuleCondition =
			HostRuleConditionEntity.builder().hostCondition(condition).hostRule(rule).build();
		rule = rule.toBuilder().condition(hostRuleCondition).build();
		rules.save(rule);
		setLastUpdateHostRules();
	}

	public void addConditions(String ruleName, List<String> conditions) {
		conditions.forEach(condition -> addCondition(ruleName, condition));
	}

	public void removeCondition(String ruleName, String conditionName) {
		removeConditions(ruleName, List.of(conditionName));
	}

	public void removeConditions(String ruleName, List<String> conditionNames) {
		log.info("Removing conditions {}", conditionNames);
		HostRuleEntity rule = getRule(ruleName);
		rule.getConditions()
			.removeIf(condition -> conditionNames.contains(condition.getHostCondition().getName()));
		rules.save(rule);
		setLastUpdateHostRules();
	}

	public CloudHostEntity getCloudHost(String ruleName, String instanceId) {
		checkRuleExists(ruleName);
		return rules.getCloudHost(ruleName, instanceId).orElseThrow(() ->
			new EntityNotFoundException(CloudHostEntity.class, "instanceId", instanceId));
	}

	public List<CloudHostEntity> getCloudHosts(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getCloudHosts(ruleName);
	}

	public void addCloudHost(String ruleName, String instanceId) {
		addCloudHosts(ruleName, List.of(instanceId));
	}

	public void addCloudHosts(String ruleName, List<String> instanceIds) {
		log.info("Adding cloud hosts {} to rule {}", instanceIds, ruleName);
		HostRuleEntity rule = getRule(ruleName);
		instanceIds.forEach(instanceId -> {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIdOrIp(instanceId);
			cloudHost.addRule(rule);
		});
		rules.save(rule);
		setLastUpdateHostRules();
	}

	public void removeCloudHost(String ruleName, String instanceId) {
		removeCloudHosts(ruleName, List.of(instanceId));
	}

	public void removeCloudHosts(String ruleName, List<String> instanceIds) {
		log.info("Removing cloud hosts {} from rule {}", instanceIds, ruleName);
		HostRuleEntity rule = getRule(ruleName);
		instanceIds.forEach(instanceId -> cloudHostsService.getCloudHostByIdOrIp(instanceId).removeRule(rule));
		rules.save(rule);
		setLastUpdateHostRules();
	}

	public EdgeHostEntity getEdgeHost(String ruleName, String hostname) {
		checkRuleExists(ruleName);
		return rules.getEdgeHost(ruleName, hostname).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname));
	}

	public List<EdgeHostEntity> getEdgeHosts(String ruleName) {
		checkRuleExists(ruleName);
		return rules.getEdgeHosts(ruleName);
	}

	public void addEdgeHost(String ruleName, String hostname) {
		addEdgeHosts(ruleName, List.of(hostname));
	}

	public void addEdgeHosts(String ruleName, List<String> hostnames) {
		log.info("Adding edge hosts {} to rule {}", hostnames, ruleName);
		HostRuleEntity rule = getRule(ruleName);
		hostnames.forEach(hostname -> {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(hostname);
			edgeHost.addRule(rule);
		});
		rules.save(rule);
		setLastUpdateHostRules();
	}

	public void removeEdgeHost(String ruleName, String hostname) {
		removeEdgeHosts(ruleName, List.of(hostname));
	}

	public void removeEdgeHosts(String ruleName, List<String> hostnames) {
		log.info("Removing edge hosts {} from rule {}", hostnames, ruleName);
		HostRuleEntity rule = getRule(ruleName);
		hostnames.forEach(hostname -> edgeHostsService.getEdgeHostByDnsOrIp(hostname).removeRule(rule));
		rules.save(rule);
		setLastUpdateHostRules();
	}

	private void checkRuleExists(String ruleName) {
		if (!rules.hasRule(ruleName)) {
			throw new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName);
		}
	}

	private void checkRuleDoesntExist(HostRuleEntity hostRule) {
		String name = hostRule.getName();
		if (rules.hasRule(name)) {
			throw new DataIntegrityViolationException("Host rule '" + name + "' already exists");
		}
	}

	public HostDecisionResult processHostEvent(HostAddress hostAddress, HostEvent hostEvent) {
		if (droolsService.shouldCreateNewHostRuleSession(hostAddress, lastUpdateHostRules.get())) {
			List<Rule> rules = generateHostRules(hostAddress);
			Map<Long, String> drools = droolsService.executeDroolsRules(hostEvent, rules, hostRuleTemplateFile);
			droolsService.createNewHostRuleSession(hostAddress, drools);
		}
		return droolsService.evaluate(hostEvent);
	}

	private List<Rule> generateHostRules(HostAddress hostAddress) {
		//FIXME what about cloud hosts?
		List<HostRuleEntity> hostRules = getRules(hostAddress);
		List<Rule> rules = new ArrayList<>(hostRules.size());
		log.info("Generating host rules... (count: {})", rules.size());
		hostRules.forEach(hostRule -> rules.add(generateRule(hostRule)));
		return rules;
	}

	private Rule generateRule(HostRuleEntity hostRule) {
		Long id = hostRule.getId();
		List<Condition> conditions = getConditions(hostRule.getName()).stream().map(condition -> {
			String fieldName = String.format("%s-%S", condition.getField().getName(), condition.getValueMode().getName());
			double value = condition.getValue();
			Operator operator = condition.getOperator().getOperator();
			return new Condition(fieldName, value, operator);
		}).collect(Collectors.toList());
		RuleDecision decision = hostRule.getDecision().getRuleDecision();
		int priority = hostRule.getPriority();
		return new Rule(id, conditions, decision, priority);
	}

}
