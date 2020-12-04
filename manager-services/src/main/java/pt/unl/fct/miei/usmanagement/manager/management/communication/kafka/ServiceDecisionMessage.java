package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
	private Set<ServiceDecisionValue> serviceDecisionValues;

	public ServiceDecisionMessage(ServiceDecision serviceDecision) {
		this.id = serviceDecision.getId();
		this.containerId = serviceDecision.getContainerId();
		this.serviceName = serviceDecision.getServiceName();
		this.result = serviceDecision.getResult();
		this.decision = serviceDecision.getDecision();
		this.rule = serviceDecision.getRule();
		this.timestamp = serviceDecision.getTimestamp();
		this.serviceDecisionValues = serviceDecision.getServiceDecisionValues();
	}

}
