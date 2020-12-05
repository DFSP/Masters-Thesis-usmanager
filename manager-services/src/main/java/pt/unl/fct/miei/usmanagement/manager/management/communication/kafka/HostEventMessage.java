package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostEventMessage {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private Decision decision;
	private int count;

	public HostEventMessage(HostEvent hostEvent) {
		this.id = hostEvent.getId();
		this.publicIpAddress = hostEvent.getPublicIpAddress();
		this.privateIpAddress = hostEvent.getPrivateIpAddress();
		this.managerPublicIpAddress = hostEvent.getManagerPublicIpAddress();
		this.managerPrivateIpAddress = hostEvent.getManagerPrivateIpAddress();
		this.decision = hostEvent.getDecision();
		this.count = hostEvent.getCount();
	}

	@JsonIgnore
	public HostEvent toHostEvent() {
		return HostEvent.builder()
			.id(id)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.managerPublicIpAddress(managerPublicIpAddress)
			.managerPrivateIpAddress(managerPrivateIpAddress)
			.decision(decision)
			.count(count)
			.build();
	}
}
