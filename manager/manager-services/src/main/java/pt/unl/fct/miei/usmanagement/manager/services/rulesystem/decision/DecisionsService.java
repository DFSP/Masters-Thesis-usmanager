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

package pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decisions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionValues;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValues;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DecisionsService {

	private final ServiceRulesService serviceRulesService;
	private final HostRulesService hostRulesService;
	private final Decisions decisions;
	private final ServiceDecisions serviceDecisions;
	private final HostDecisions hostDecisions;
	private final ServiceDecisionValues serviceDecisionValues;
	private final HostDecisionValues hostDecisionValues;
	private final FieldsService fieldsService;
	private final KafkaService kafkaService;

	public DecisionsService(ServiceRulesService serviceRulesService, HostRulesService hostRulesService,
							Decisions decisions, ServiceDecisions serviceDecisions,
							HostDecisions hostDecisions,
							ServiceDecisionValues serviceDecisionValues,
							HostDecisionValues hostDecisionValues,
							FieldsService fieldsService, KafkaService kafkaService) {
		this.serviceRulesService = serviceRulesService;
		this.hostRulesService = hostRulesService;
		this.decisions = decisions;
		this.serviceDecisions = serviceDecisions;
		this.hostDecisions = hostDecisions;
		this.serviceDecisionValues = serviceDecisionValues;
		this.hostDecisionValues = hostDecisionValues;
		this.fieldsService = fieldsService;
		this.kafkaService = kafkaService;
	}

	@Transactional(readOnly = true)
	public List<pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision> getDecisions() {
		return decisions.findAll();
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getDecision(String decisionName) {
		RuleDecisionEnum decision = RuleDecisionEnum.valueOf(decisionName.toUpperCase());
		return getDecision(decision);
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getDecision(RuleDecisionEnum decision) {
		return decisions.findByRuleDecision(decision).orElseThrow(() ->
			new EntityNotFoundException(pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.class, "decision", decision.name()));
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getDecision(Long id) {
		return decisions.findById(id).orElseThrow(() ->
			new EntityNotFoundException(pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.class, "id", id.toString()));
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision addDecision(pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision decision) {
		checkDecisionDoesntExist(decision);
		decision = saveDecision(decision);
		kafkaService.sendDecision(decision);
		return decision;
	}

	public Decision addIfNotPresent(Decision decision) {
		Optional<Decision> decisionOptional = decisions.findById(decision.getId());
		return decisionOptional.orElseGet(() -> {
			decision.clearAssociations();
			return saveDecision(decision);
		});
	}

	public Decision addOrUpdateDecision(Decision decision) {
		if (decision.getId() != null) {
			Optional<Decision> decisionOptional = decisions.findById(decision.getId());
			if (decisionOptional.isPresent()) {
				Decision existingDecision = decisionOptional.get();
				Set<ServiceEvent> serviceEvents = decision.getServiceEvents();
				if (serviceEvents != null) {
					Set<pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent> currentServiceEvents = existingDecision.getServiceEvents();
					if (currentServiceEvents == null) {
						existingDecision.setServiceEvents(new HashSet<>(serviceEvents));
					}
					else {
						currentServiceEvents.retainAll(serviceEvents);
						currentServiceEvents.addAll(serviceEvents);
					}
				}
				Set<HostEvent> hostEvents = decision.getHostEvents();
				if (hostEvents != null) {
					Set<HostEvent> currentHostEvents = existingDecision.getHostEvents();
					if (currentHostEvents == null) {
						existingDecision.setHostEvents(new HashSet<>(hostEvents));
					}
					else {
						currentHostEvents.retainAll(hostEvents);
						currentHostEvents.addAll(hostEvents);
					}
				}
				EntityUtils.copyValidProperties(decision, existingDecision);
				return saveDecision(existingDecision);
			}
		}
		return saveDecision(decision);
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision saveDecision(Decision decision) {
		log.info("Saving decision {}", ToStringBuilder.reflectionToString(decision));
		return decisions.save(decision);
	}

	public void deleteDecision(Long id) {
		decisions.deleteById(id);
	}

	public List<pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision> getServicesPossibleDecisions() {
		return decisions.findByComponentTypeType(ComponentTypeEnum.SERVICE);
	}

	public List<pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision> getHostsPossibleDecisions() {
		return decisions.findByComponentTypeType(ComponentTypeEnum.HOST);
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getServicePossibleDecision(String decisionName) {
		RuleDecisionEnum decision = RuleDecisionEnum.valueOf(decisionName.toUpperCase());
		return getServicePossibleDecision(decision);
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getServicePossibleDecision(RuleDecisionEnum decision) {
		return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentTypeEnum.SERVICE).orElseThrow(() ->
			new EntityNotFoundException(pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.class, "decisionName", decision.name()));
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getContainerPossibleDecision(String decisionName) {
		RuleDecisionEnum decision = RuleDecisionEnum.valueOf(decisionName.toUpperCase());
		return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentTypeEnum.CONTAINER).orElseThrow(() ->
			new EntityNotFoundException(pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.class, "decisionName", decisionName));
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getHostPossibleDecision(String decisionName) {
		RuleDecisionEnum decision = RuleDecisionEnum.valueOf(decisionName.toUpperCase());
		return getHostPossibleDecision(decision);
	}

	public pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision getHostPossibleDecision(RuleDecisionEnum decision) {
		return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentTypeEnum.HOST).orElseThrow(() ->
			new EntityNotFoundException(pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.class, "decisionName", decision.name()));
	}

	public ServiceDecision addServiceDecision(String containerId, String serviceName, String decisionName,
											  long ruleId, String result) {
		ServiceRule rule = serviceRulesService.getRule(ruleId);
		Decision decision = getServicePossibleDecision(decisionName);
		Timestamp timestamp = Timestamp.from(Instant.now());
		ServiceDecision serviceDecision = ServiceDecision.builder()
			.containerId(containerId)
			.serviceName(serviceName)
			.result(result)
			.ruleName(rule.getName())
			.decision(decision)
			.timestamp(timestamp).build();
		return saveServiceDecision(serviceDecision);
	}

	public ServiceDecision saveServiceDecision(ServiceDecision serviceDecision) {
		log.info("Saving service decision: {}", ToStringBuilder.reflectionToString(serviceDecision));
		return serviceDecisions.save(serviceDecision);
	}

	public HostDecision addHostDecision(HostAddress hostAddress, String decisionName, long ruleId) {
		HostRule rule = hostRulesService.getRule(ruleId);
		pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision decision = getHostPossibleDecision(decisionName);
		HostDecision hostDecision = HostDecision.builder().publicIpAddress(hostAddress.getPublicIpAddress())
			.privateIpAddress(hostAddress.getPrivateIpAddress()).ruleName(rule.getName()).decision(decision).build();
		return saveHostDecision(hostDecision);
	}

	public HostDecision saveHostDecision(HostDecision hostDecision) {
		log.info("Saving host decision: {}", ToStringBuilder.reflectionToString(hostDecision));
		return hostDecisions.save(hostDecision);
	}

	public void addServiceDecisionValueFromFields(ServiceDecision serviceDecision,
												  Map<String, Double> fields) {
		serviceDecisionValues.saveAll(
			fields.entrySet().stream()
				.filter(field -> field.getKey().contains("effective-val"))
				.map(field ->
					ServiceDecisionValue.builder()
						.serviceDecision(serviceDecision)
						.field(fieldsService.getField(field.getKey().split("-effective-val")[0]))
						.value(field.getValue())
						.build())
				.collect(Collectors.toList())
		);
	}

	public void addHostDecisionValueFromFields(HostDecision hostDecision, Map<String, Double> fields) {
		hostDecisionValues.saveAll(
			fields.entrySet().stream()
				.filter(field -> field.getKey().contains("effective-val"))
				.map(field ->
					HostDecisionValue.builder()
						.hostDecision(hostDecision)
						.field(fieldsService.getField(field.getKey().split("-effective-val")[0]))
						.value(field.getValue())
						.build())
				.collect(Collectors.toList())
		);
	}

	public List<ServiceDecision> getServiceDecisions() {
		return serviceDecisions.findAll();
	}

	public List<ServiceDecision> getServiceDecisions(String serviceName) {
		return serviceDecisions.findByServiceName(serviceName);
	}

	public List<ServiceDecision> getContainerDecisions(String containerId) {
		return serviceDecisions.findByContainerIdStartingWith(containerId);
	}

	public List<HostDecision> getHostDecisions() {
		return hostDecisions.findAll();
	}

	public List<HostDecision> getHostDecisions(HostAddress hostAddress) {
		return hostDecisions.findByPublicIpAddressAndPrivateIpAddress(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress());
	}

	private void checkDecisionDoesntExist(Decision decision) {
		Long id = decision.getId();
		if (decisions.hasDecision(id)) {
			throw new DataIntegrityViolationException("Decision '" + id + "' already exists");
		}
	}

}
