package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceEventMessage {

	private Long id;
	private String containerId;
	private String serviceName;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private Decision decision;
	private int count;

	public ServiceEventMessage(ServiceEvent serviceEvent) {
		this.id = serviceEvent.getId();
		this.containerId = serviceEvent.getContainerId();
		this.managerPublicIpAddress = serviceEvent.getManagerPublicIpAddress();
		this.managerPrivateIpAddress = serviceEvent.getManagerPrivateIpAddress();
		this.decision = serviceEvent.getDecision();
		this.count = serviceEvent.getCount();
	}
}
