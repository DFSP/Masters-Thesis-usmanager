package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ServiceDecisionDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String result;
	private Decision decision;
	private ServiceRule rule;
	private Timestamp timestamp;
	private Set<ServiceDecisionValueDTO> serviceDecisionValues;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceDecisionDTO(ServiceDecision serviceDecision) {
		this.id = serviceDecision.getId();
		this.containerId = serviceDecision.getContainerId();
		this.serviceName = serviceDecision.getServiceName();
		this.result = serviceDecision.getResult();
		this.decision = serviceDecision.getDecision();
		this.rule = serviceDecision.getRule();
		this.timestamp = serviceDecision.getTimestamp();
		this.serviceDecisionValues = serviceDecision.getServiceDecisionValues().stream().map(ServiceDecisionValueDTO::new).collect(Collectors.toSet());
		/*this.isNew = serviceDecision.isNew();*/
	}

	@JsonIgnore
	public ServiceDecision toEntity() {
		ServiceDecision serviceDecision = ServiceDecision.builder()
			.id(id)
			.containerId(containerId)
			.serviceName(serviceName)
			.result(result)
			.decision(decision)
			.rule(rule)
			.timestamp(timestamp)
			.serviceDecisionValues(serviceDecisionValues != null ? serviceDecisionValues.stream().map(ServiceDecisionValueDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*serviceDecision.setNew(isNew);*/
		return serviceDecision;
	}
}
