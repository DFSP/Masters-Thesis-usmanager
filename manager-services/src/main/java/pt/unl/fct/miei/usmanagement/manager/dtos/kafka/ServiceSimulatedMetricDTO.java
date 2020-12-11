package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceSimulatedMetricDTO {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean generic;
	private boolean override;
	private boolean active;
	private Set<ServiceDTO> services;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceSimulatedMetricDTO(Long id) {
		this.id = id;
	}

	public ServiceSimulatedMetricDTO(ServiceSimulatedMetric serviceSimulatedMetric) {
		this.id = serviceSimulatedMetric.getId();
		this.name = serviceSimulatedMetric.getName();
		this.field = serviceSimulatedMetric.getField();
		this.minimumValue = serviceSimulatedMetric.getMinimumValue();
		this.maximumValue = serviceSimulatedMetric.getMaximumValue();
		this.generic = serviceSimulatedMetric.isGeneric();
		this.override = serviceSimulatedMetric.isOverride();
		this.active = serviceSimulatedMetric.isActive();
		this.services = serviceSimulatedMetric.getServices().stream().map(ServiceDTO::new).collect(Collectors.toSet());
		/*this.isNew = serviceSimulatedMetric.isNew();*/
	}

	@JsonIgnore
	public ServiceSimulatedMetric toEntity() {
		ServiceSimulatedMetric serviceSimulatedMetric = ServiceSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.generic(generic)
			.override(override)
			.active(active)
			.services(services != null ? services.stream().map(ServiceDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*serviceSimulatedMetric.setNew(isNew);*/
		return serviceSimulatedMetric;
	}

}
