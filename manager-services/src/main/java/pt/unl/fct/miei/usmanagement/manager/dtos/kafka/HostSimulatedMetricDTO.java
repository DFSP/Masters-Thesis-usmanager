package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
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
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostSimulatedMetricDTO(Long id) {
		this.id = id;
	}

	public HostSimulatedMetricDTO(HostSimulatedMetric hostSimulatedMetric) {
		this.id = hostSimulatedMetric.getId();
		this.name = hostSimulatedMetric.getName();
		this.field = hostSimulatedMetric.getField();
		this.minimumValue = hostSimulatedMetric.getMinimumValue();
		this.maximumValue = hostSimulatedMetric.getMaximumValue();
		this.generic = hostSimulatedMetric.isGeneric();
		this.override = hostSimulatedMetric.isOverride();
		this.active = hostSimulatedMetric.isActive();
		this.cloudHosts = hostSimulatedMetric.getCloudHosts().stream().map(CloudHostDTO::new).collect(Collectors.toSet());
		this.edgeHosts = hostSimulatedMetric.getEdgeHosts().stream().map(EdgeHostDTO::new).collect(Collectors.toSet());
		/*this.isNew = hostSimulatedMetric.isNew();*/
	}

	@JsonIgnore
	public HostSimulatedMetric toEntity() {
		HostSimulatedMetric hostSimulatedMetric = HostSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.generic(generic)
			.override(override)
			.active(active)
			.cloudHosts(cloudHosts != null ? cloudHosts.stream().map(CloudHostDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.edgeHosts(edgeHosts != null ? edgeHosts.stream().map(EdgeHostDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*hostSimulatedMetric.setNew(isNew);*/
		return hostSimulatedMetric;
	}
}
