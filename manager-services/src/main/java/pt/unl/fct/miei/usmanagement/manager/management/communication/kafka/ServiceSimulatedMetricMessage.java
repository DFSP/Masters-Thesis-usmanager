package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceSimulatedMetricMessage {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean generic;
	private boolean override;
	private boolean active;
	private Set<Service> services;

	public ServiceSimulatedMetricMessage(ServiceSimulatedMetric serviceSimulatedMetric) {
		this.id = serviceSimulatedMetric.getId();
		this.name = serviceSimulatedMetric.getName();
		this.field = serviceSimulatedMetric.getField();
		this.minimumValue = serviceSimulatedMetric.getMinimumValue();
		this.maximumValue = serviceSimulatedMetric.getMaximumValue();
		this.generic = serviceSimulatedMetric.isGeneric();
		this.override = serviceSimulatedMetric.isOverride();
		this.active = serviceSimulatedMetric.isActive();
		this.services = serviceSimulatedMetric.getServices();
	}

	public ServiceSimulatedMetric get() {
		return ServiceSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.generic(generic)
			.override(override)
			.active(active)
			.services(services)
			.build();
	}

}
