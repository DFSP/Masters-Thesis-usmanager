package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ContainerDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContainerDTO {

	private String id;
	private ContainerTypeEnum type;
	private long created;
	private String name;
	private String image;
	private String command;
	private String network;
	private String publicIpAddress;
	private String privateIpAddress;
	private Set<String> mounts;
	private Set<ContainerPortMapping> ports;
	private Map<String, String> labels;
	private RegionEnum region;
	private String managerId;
	private Coordinates coordinates;
	private Set<ContainerRuleDTO> containerRules;
	private Set<ContainerSimulatedMetricDTO> simulatedContainerMetrics;

	public ContainerDTO(String id) {
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
		if (!(o instanceof Container)) {
			return false;
		}
		Container other = (Container) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ContainerDTO{" +
			"id='" + id + '\'' +
			", type=" + type +
			", created=" + created +
			", name='" + name + '\'' +
			", image='" + image + '\'' +
			", command='" + command + '\'' +
			", network='" + network + '\'' +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", mounts=" + mounts +
			", ports=" + ports +
			", labels=" + labels +
			", region=" + region +
			", managerId='" + managerId + '\'' +
			", coordinates=" + coordinates +
			", containerRules=" + (containerRules == null ? "null" : containerRules.stream().map(ContainerRuleDTO::getId).collect(Collectors.toSet())) +
			", simulatedContainerMetrics=" + (simulatedContainerMetrics == null ? "null" : simulatedContainerMetrics.stream()
			.map(ContainerSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
