package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceEventDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private DecisionDTO decision;
	private int count;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceEventDTO(ServiceEvent serviceEvent) {
		this.id = serviceEvent.getId();
		this.containerId = serviceEvent.getContainerId();
		this.managerPublicIpAddress = serviceEvent.getManagerPublicIpAddress();
		this.managerPrivateIpAddress = serviceEvent.getManagerPrivateIpAddress();
		this.decision = new DecisionDTO(serviceEvent.getDecision());
		this.count = serviceEvent.getCount();
		/*this.isNew = serviceEvent.isNew();*/
	}

	@JsonIgnore
	public ServiceEvent toEntity() {
		ServiceEvent serviceEvent = ServiceEvent.builder()
			.id(id)
			.serviceName(serviceName)
			.containerId(containerId)
			.managerPublicIpAddress(managerPublicIpAddress)
			.managerPrivateIpAddress(managerPrivateIpAddress)
			.decision(decision.toEntity())
			.count(count)
			.build();
		/*serviceEvent.setNew(isNew);*/
		return serviceEvent;
	}
}
