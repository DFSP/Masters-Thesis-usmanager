package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class HostEventDTO {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private DecisionDTO decision;
	private int count;

}
