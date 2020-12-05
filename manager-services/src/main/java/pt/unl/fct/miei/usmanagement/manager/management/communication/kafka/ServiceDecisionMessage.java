package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import java.sql.Timestamp;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceDecisionMessage {

	private Long id;
	private String containerId;
	private String serviceName;
	private String result;
	private Decision decision;
	private ServiceRule rule;
	private Timestamp timestamp;
	private Set<ServiceDecisionValue> serviceDecisions;

	public ServiceDecisionMessage(ServiceDecision serviceDecision) {
		this.id = serviceDecision.getId();
		this.containerId = serviceDecision.getContainerId();
		this.serviceName = serviceDecision.getServiceName();
		this.result = serviceDecision.getResult();
		this.decision = serviceDecision.getDecision();
		this.rule = serviceDecision.getRule();
		this.timestamp = serviceDecision.getTimestamp();
		this.serviceDecisions = serviceDecision.getServiceDecisions();
	}

	public ServiceDecision toServiceDecision() {
		return ServiceDecision.builder()
			.id(id)
			.containerId(containerId)
			.serviceName(serviceName)
			.result(result)
			.decision(decision)
			.rule(rule)
			.timestamp(timestamp)
			.serviceDecisions(serviceDecisions)
			.build();
	}
}
