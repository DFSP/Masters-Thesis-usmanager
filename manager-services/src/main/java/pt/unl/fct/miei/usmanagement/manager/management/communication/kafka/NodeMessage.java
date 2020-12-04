package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class NodeMessage {

	private String id;
	private String publicIpAddress;
	private NodeAvailability availability;
	private NodeRole role;
	private long version;
	private String state;
	private ManagerStatus managerStatus;
	private String managerId;
	private Map<String, String> labels;

	public NodeMessage(Node node) {
		this.id = node.getId();
		this.publicIpAddress = node.getPublicIpAddress();
		this.availability = node.getAvailability();
		this.role = node.getRole();
		this.version = node.getVersion();
		this.state = node.getState();
		this.managerStatus = node.getManagerStatus();
		this.managerId = node.getManagerId();
		this.labels = node.getLabels();
	}

}
