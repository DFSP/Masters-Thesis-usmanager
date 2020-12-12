package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ContainerSimulatedMetricDTO {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean override;
	private boolean active;
	private Set<ContainerDTO> containers;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ContainerSimulatedMetricDTO(Long id) {
		this.id = id;
	}

	public ContainerSimulatedMetricDTO(ContainerSimulatedMetric containerSimulatedMetric) {
		this.id = containerSimulatedMetric.getId();
		this.name = containerSimulatedMetric.getName();
		this.field = containerSimulatedMetric.getField();
		this.minimumValue = containerSimulatedMetric.getMinimumValue();
		this.maximumValue = containerSimulatedMetric.getMaximumValue();
		this.override = containerSimulatedMetric.isOverride();
		this.active = containerSimulatedMetric.isActive();
		this.containers = containerSimulatedMetric.getContainers().stream().map(ContainerDTO::new).collect(Collectors.toSet());
		/*this.isNew = containerSimulatedMetric.isNew();*/
	}

	@JsonIgnore
	public ContainerSimulatedMetric toEntity() {
		ContainerSimulatedMetric containerSimulatedMetric = ContainerSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.override(override)
			.active(active)
			.containers(containers != null ? containers.stream().map(ContainerDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*containerSimulatedMetric.setNew(isNew);*/
		return containerSimulatedMetric;
	}
}
