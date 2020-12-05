package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerSimulatedMetricMessage {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean override;
	private boolean active;
	private Set<Container> containers;

	public ContainerSimulatedMetricMessage(ContainerSimulatedMetric containerSimulatedMetric) {
		this.id = containerSimulatedMetric.getId();
		this.name = containerSimulatedMetric.getName();
		this.field = containerSimulatedMetric.getField();
		this.minimumValue = containerSimulatedMetric.getMinimumValue();
		this.maximumValue = containerSimulatedMetric.getMaximumValue();
		this.override = containerSimulatedMetric.isOverride();
		this.active = containerSimulatedMetric.isActive();
		this.containers = containerSimulatedMetric.getContainers();
	}

	public ContainerSimulatedMetric get() {
		return ContainerSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.override(override)
			.active(active)
			.containers(containers)
			.build();
	}
}
