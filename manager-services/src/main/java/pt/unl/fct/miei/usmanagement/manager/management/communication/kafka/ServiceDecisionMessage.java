package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import java.sql.Timestamp;
import java.util.HashSet;
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
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceDecisionMessage(ServiceDecision serviceDecision) {
		this.id = serviceDecision.getId();
		this.containerId = serviceDecision.getContainerId();
		this.serviceName = serviceDecision.getServiceName();
		this.result = serviceDecision.getResult();
		this.decision = serviceDecision.getDecision();
		this.rule = serviceDecision.getRule();
		this.timestamp = serviceDecision.getTimestamp();
		this.serviceDecisions = serviceDecision.getServiceDecisions();
		/*this.isNew = serviceDecision.isNew();*/
	}

	public ServiceDecision get() {
		ServiceDecision serviceDecision = ServiceDecision.builder()
			.id(id)
			.containerId(containerId)
			.serviceName(serviceName)
			.result(result)
			.decision(decision)
			.rule(rule)
			.timestamp(timestamp)
			.serviceDecisions(serviceDecisions != null ? serviceDecisions : new HashSet<>())
			.build();
		/*serviceDecision.setNew(isNew);*/
		return serviceDecision;
	}
}
