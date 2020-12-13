package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = EdgeHostDTO.class)
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
		if (!(o instanceof EdgeHost)) {
			return false;
		}
		EdgeHost other = (EdgeHost) o;
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
