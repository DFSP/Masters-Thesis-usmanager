package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class NodeDTO {

	private String id;
	private String publicIpAddress;
	private NodeAvailability availability;
	private NodeRole role;
	private long version;
	private String state;
	private ManagerStatus managerStatus;
	private String managerId;


}
