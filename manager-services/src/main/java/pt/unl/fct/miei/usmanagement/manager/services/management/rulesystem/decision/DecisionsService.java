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

package pt.unl.fct.miei.usmanagement.manager.services.management.rulesystem.decision;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionValueEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionValueRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionValueEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionValueRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.management.rulesystem.rules.ServiceRulesService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DecisionsService {

	private final ServiceRulesService serviceRulesService;
	private final HostRulesService hostRulesService;
	private final DecisionRepository decisions;
	private final ServiceDecisionRepository serviceDecisions;
	private final HostDecisionRepository hostDecisions;
	private final ServiceDecisionValueRepository serviceDecisionValues;
	private final HostDecisionValueRepository hostDecisionValues;
	private final FieldsService fieldsService;

	public DecisionsService(ServiceRulesService serviceRulesService, HostRulesService hostRulesService,
							DecisionRepository decisions, ServiceDecisionRepository serviceDecisions,
							HostDecisionRepository hostDecisions,
							ServiceDecisionValueRepository serviceDecisionValues,
							HostDecisionValueRepository hostDecisionValues,
							FieldsService fieldsService) {
		this.serviceRulesService = serviceRulesService;
		this.hostRulesService = hostRulesService;
		this.decisions = decisions;
		this.serviceDecisions = serviceDecisions;
		this.hostDecisions = hostDecisions;
		this.serviceDecisionValues = serviceDecisionValues;
		this.hostDecisionValues = hostDecisionValues;
		this.fieldsService = fieldsService;
	}

	public List<DecisionEntity> getDecisions() {
		return decisions.findAll();
	}

	public DecisionEntity getDecision(String decisionName) {
		RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
		return decisions.findByRuleDecision(decision).orElseThrow(() ->
			new EntityNotFoundException(DecisionEntity.class, "decision", decisionName));
	}

	public DecisionEntity getDecision(Long id) {
		return decisions.findById(id).orElseThrow(() ->
			new EntityNotFoundException(DecisionEntity.class, "id", id.toString()));
	}

	public DecisionEntity addDecision(DecisionEntity decisionEntity) {
		return decisions.save(decisionEntity);
	}

	public List<DecisionEntity> getServicesPossibleDecisions() {
		return decisions.findByComponentTypeType(ComponentType.SERVICE);
	}

	public List<DecisionEntity> getHostsPossibleDecisions() {
		return decisions.findByComponentTypeType(ComponentType.HOST);
	}

	public DecisionEntity getServicePossibleDecision(String decisionName) {
		RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
		return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentType.SERVICE).orElseThrow(() ->
			new EntityNotFoundException(DecisionEntity.class, "decisionName", decisionName));
	}

	public DecisionEntity getContainerPossibleDecision(String decisionName) {
		RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
		return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentType.CONTAINER).orElseThrow(() ->
			new EntityNotFoundException(DecisionEntity.class, "decisionName", decisionName));
	}

	public DecisionEntity getHostPossibleDecision(String decisionName) {
		RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
		return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentType.HOST).orElseThrow(() ->
			new EntityNotFoundException(DecisionEntity.class, "decisionName", decisionName));
	}

	public ServiceDecisionEntity addServiceDecision(String containerId, String serviceName, String decisionName,
													long ruleId, String result) {
		ServiceRuleEntity rule = serviceRulesService.getRule(ruleId);
		DecisionEntity decision = getServicePossibleDecision(decisionName);
		Timestamp timestamp = Timestamp.from(Instant.now());
		ServiceDecisionEntity serviceDecision = ServiceDecisionEntity.builder()
			.containerId(containerId)
			.serviceName(serviceName)
			.result(result)
			.rule(rule)
			.decision(decision)
			.timestamp(timestamp).build();
		return serviceDecisions.save(serviceDecision);
	}

	public HostDecisionEntity addHostDecision(HostDetails hostDetails, String decisionName, long ruleId) {
		HostRuleEntity rule = hostRulesService.getRule(ruleId);
		DecisionEntity decision = getHostPossibleDecision(decisionName);
		HostAddress hostAddress = hostDetails.getAddress();
		HostDecisionEntity hostDecision = HostDecisionEntity.builder().hostAddress(hostAddress).rule(rule).decision(decision).build();
		return hostDecisions.save(hostDecision);
	}

	public void addServiceDecisionValueFromFields(ServiceDecisionEntity serviceDecision,
												  Map<String, Double> fields) {
		serviceDecisionValues.saveAll(
			fields.entrySet().stream()
				.filter(field -> field.getKey().contains("effective-val"))
				.map(field ->
					ServiceDecisionValueEntity.builder()
						.serviceDecision(serviceDecision)
						.field(fieldsService.getField(field.getKey().split("-effective-val")[0]))
						.value(field.getValue())
						.build())
				.collect(Collectors.toList())
		);
	}

	public void addHostDecisionValueFromFields(HostDecisionEntity hostDecision, Map<String, Double> fields) {
		hostDecisionValues.saveAll(
			fields.entrySet().stream()
				.filter(field -> field.getKey().contains("effective-val"))
				.map(field ->
					HostDecisionValueEntity.builder()
						.hostDecision(hostDecision)
						.field(fieldsService.getField(field.getKey().split("-effective-val")[0]))
						.value(field.getValue())
						.build())
				.collect(Collectors.toList())
		);
	}

	public List<ServiceDecisionEntity> getServiceDecisions(String serviceName) {
		return serviceDecisions.findByServiceName(serviceName);
	}

	public List<ServiceDecisionEntity> getContainerDecisions(String containerId) {
		return serviceDecisions.findByContainerId(containerId);
	}

	public List<HostDecisionEntity> getHostDecisions(HostAddress hostAddress) {
		return hostDecisions.findByHostAddress(hostAddress);
	}

}
