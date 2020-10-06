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
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.decision.HostDecisionResult;

@Slf4j
@Service
public class HostRulesService {

	private static final int HOST_MINIMUM_LOGS_COUNT = 1;
	private static final double PERCENTAGE = 0.01;

	private final DroolsService droolsService;
	private final HostsMonitoringService hostsMonitoringService;

	private final HostRuleRepository rules;

	private final String hostRuleTemplateFile;
	private final AtomicLong lastUpdateHostRules;

	public HostRulesService(DroolsService droolsService, @Lazy HostsMonitoringService hostsMonitoringService,
							HostRuleRepository rules, RulesProperties rulesProperties) {
		this.droolsService = droolsService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.rules = rules;
		this.hostRuleTemplateFile = rulesProperties.getHostRuleTemplateFile();
		this.lastUpdateHostRules = new AtomicLong(0);
	}

	// TODO call this when host rules and generic host rules are updated from symmetricds
	public void setLastUpdateHostRules() {
		long currentTime = System.currentTimeMillis();
		lastUpdateHostRules.getAndSet(currentTime);
	}

	public List<HostRuleEntity> getRules() {
		return rules.findAll();
	}

	public List<HostRuleEntity> getRules(String hostname) {
		return rules.findByHostname(hostname);
	}

	public HostRuleEntity getRule(Long id) {
		return rules.findById(id).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "id", id.toString()));
	}

	public HostRuleEntity getRule(String name) {
		return rules.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "name", name));
	}

	public List<HostRuleEntity> getGenericHostRules() {
		return rules.findGenericHostRules();
	}

	public HostRuleEntity getGenericHostRule(String ruleName) {
		return rules.findGenericHostRule(ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName));
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

	public CloudHostEntity getCloudHost(String ruleName, String instanceId) {
		assertRuleExists(ruleName);
		return rules.getCloudHost(ruleName, instanceId).orElseThrow(() ->
			new EntityNotFoundException(CloudHostEntity.class, "instanceId", instanceId));
	}

	public List<CloudHostEntity> getCloudHosts(String ruleName) {
		assertRuleExists(ruleName);
		return rules.getCloudHosts(ruleName);
	}

	public EdgeHostEntity getEdgeHost(String ruleName, String hostname) {
		assertRuleExists(ruleName);
		return rules.getEdgeHost(ruleName, hostname).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname));
	}

	public List<EdgeHostEntity> getEdgeHosts(String ruleName) {
		assertRuleExists(ruleName);
		return rules.getEdgeHosts(ruleName);
	}

	private void assertRuleExists(String ruleName) {
		if (!rules.hasRule(ruleName)) {
			throw new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName);
		}
	}

	public HostDecisionResult executeHostRules(String hostname, Map<String, Double> fields) {
		log.info("Running host rules at {} for fields {}", hostname, fields);
		HostEvent hostEvent = new HostEvent(hostname);
		Map<String, Double> hostEventFields = hostEvent.getFields();
		hostsMonitoringService.getHostMonitoring(hostname)
			.stream()
			.filter(loggedField ->
				loggedField.getCount() >= HOST_MINIMUM_LOGS_COUNT && fields.get(loggedField.getField()) != null)
			.forEach(loggedField -> {
				long count = loggedField.getCount();
				String field = loggedField.getField();
				//TODO conta com este newValue?
				double sumValue = loggedField.getSumValue();
				double lastValue = loggedField.getLastValue();
				double newValue = fields.get(field);
				hostEventFields.put(field + "-effective-val", newValue);
				double average = sumValue / (count * 1.0);
				hostEventFields.put(field + "-avg-val", average);
				double deviationFromAvgValue = ((newValue - average) / average) / PERCENTAGE;
				hostEventFields.put(field + "-deviation-%-on-avg-val", deviationFromAvgValue);
				double deviationFromLastValue = ((newValue - lastValue) / lastValue) / PERCENTAGE;
				hostEventFields.put(field + "-deviation-%-on-last-val", deviationFromLastValue);
			});
		return hostEventFields.isEmpty() ? new HostDecisionResult(hostname) : processHostEvent(hostname, hostEvent);
	}

	public HostDecisionResult processHostEvent(String hostname, HostEvent hostEvent) {
		if (droolsService.shouldCreateNewRuleSession(hostname, lastUpdateHostRules.get())) {
			List<Rule> rules = generateHostRules(hostname);
			Map<Long, String> drools = droolsService.executeDroolsRules(hostEvent, rules, hostRuleTemplateFile);
			droolsService.createNewHostRuleSession(hostname, drools);
		}
		return droolsService.evaluate(hostEvent);
	}

	private List<Rule> generateHostRules(String hostname) {
		List<HostRuleEntity> hostRules = getRules(hostname);
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
