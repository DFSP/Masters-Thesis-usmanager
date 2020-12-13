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
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = HostSimulatedMetricDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostSimulatedMetricDTO {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean generic;
	private boolean override;
	private boolean active;
	private Set<CloudHostDTO> cloudHosts;
	private Set<EdgeHostDTO> edgeHosts;

	public HostSimulatedMetricDTO(Long id) {
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
		if (!(o instanceof HostSimulatedMetric)) {
			return false;
		}
		HostSimulatedMetric other = (HostSimulatedMetric) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostSimulatedMetricDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", field=" + field +
			", minimumValue=" + minimumValue +
			", maximumValue=" + maximumValue +
			", generic=" + generic +
			", override=" + override +
			", active=" + active +
			", cloudHosts=" + (cloudHosts == null ? "null" : cloudHosts.stream().map(CloudHostDTO::getId).collect(Collectors.toSet())) +
			", edgeHosts=" + (edgeHosts == null ? "null" : edgeHosts.stream().map(EdgeHostDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
