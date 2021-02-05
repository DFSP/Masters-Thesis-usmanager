package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.Map;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = NodeDTO.class)
@AllArgsConstructor
@NoArgsConstructor
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

	public NodeDTO(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NodeDTO)) {
			return false;
		}
		NodeDTO other = (NodeDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "NodeDTO{" +
			"id='" + id + '\'' +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", availability=" + availability +
			", role=" + role +
			", version=" + version +
			", state='" + state + '\'' +
			", managerStatus=" + managerStatus +
			", labels=" + labels +
			'}';
	}

	@JsonIgnore
	public RegionEnum getRegion() {
		String region = labels.get(NodeConstants.Label.REGION);
		return region == null ? null : RegionEnum.getRegion(region);
	}

}
