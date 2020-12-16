package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = EdgeHostDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EdgeHostDTO {

	private Long id;
	private String username;
	private String publicIpAddress;
	private String privateIpAddress;
	private String publicDnsName;
	private RegionEnum region;
	private Coordinates coordinates;
	private WorkerManagerDTO managedByWorker;
	private Set<HostRuleDTO> hostRules;
	private Set<HostSimulatedMetricDTO> simulatedHostMetrics;

	public EdgeHostDTO(Long id) {
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
		if (!(o instanceof EdgeHostDTO)) {
			return false;
		}
		EdgeHostDTO other = (EdgeHostDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "EdgeHostDTO{" +
			"id=" + id +
			", username='" + username + '\'' +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", publicDnsName='" + publicDnsName + '\'' +
			", region=" + region +
			", coordinates=" + coordinates +
			", managedByWorker=" + managedByWorker +
			", hostRules=" + (hostRules == null ? "null" : hostRules.stream().map(HostRuleDTO::getId).collect(Collectors.toSet())) +
			", simulatedHostMetrics=" + (simulatedHostMetrics == null ? "null" : simulatedHostMetrics.stream()
			.map(HostSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
