package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class NodeDTO {

	private String id;
	private String publicIpAddress;
	private NodeAvailability availability;
	private NodeRole role;
	private long version;
	private String state;
	private ManagerStatus managerStatus;
	private String managerId;
	private Map<String, String> labels;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public NodeDTO(String id) {
		this.id = id;
	}

	public NodeDTO(Node node) {
		this.id = node.getId();
		this.publicIpAddress = node.getPublicIpAddress();
		this.availability = node.getAvailability();
		this.role = node.getRole();
		this.version = node.getVersion();
		this.state = node.getState();
		this.managerStatus = node.getManagerStatus();
		this.managerId = node.getManagerId();
		this.labels = node.getLabels();
		/*this.isNew = node.isNew();*/
	}

	@JsonIgnore
	public Node toEntity() {
		Node node = Node.builder()
			.id(id)
			.publicIpAddress(publicIpAddress)
			.availability(availability)
			.role(role)
			.version(version)
			.state(state)
			.managerStatus(managerStatus)
			.managerId(managerId)
			.labels(labels)
			.build();
		/*node.setNew(isNew);*/
		return node;
	}

}
