package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
}
