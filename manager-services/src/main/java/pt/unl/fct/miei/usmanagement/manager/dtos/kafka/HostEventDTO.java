package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class HostEventDTO {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private DecisionDTO decision;
	private int count;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostEventDTO(HostEvent hostEvent) {
		this.id = hostEvent.getId();
		this.publicIpAddress = hostEvent.getPublicIpAddress();
		this.privateIpAddress = hostEvent.getPrivateIpAddress();
		this.managerPublicIpAddress = hostEvent.getManagerPublicIpAddress();
		this.managerPrivateIpAddress = hostEvent.getManagerPrivateIpAddress();
		this.decision = new DecisionDTO(hostEvent.getDecision());
		this.count = hostEvent.getCount();
		/*this.isNew = hostEvent.isNew();*/
	}

	@JsonIgnore
	public HostEvent toEntity() {
		HostEvent hostEvent = HostEvent.builder()
			.id(id)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.managerPublicIpAddress(managerPublicIpAddress)
			.managerPrivateIpAddress(managerPrivateIpAddress)
			.decision(decision.toEntity())
			.count(count)
			.build();
		/*hostEvent.setNew(isNew);*/
		return hostEvent;
	}
}
