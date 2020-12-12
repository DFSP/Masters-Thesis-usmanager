package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FieldDTO {

	private Long id;
	private String name;
	private PrometheusQueryEnum prometheusQuery;
	private Set<Condition> conditions;
	private Set<HostSimulatedMetricDTO> simulatedHostMetrics;
	private Set<ServiceSimulatedMetricDTO> simulatedServiceMetrics;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public FieldDTO(Long id) {
		this.id = id;
	}

	public FieldDTO(Field field) {
		this.id = field.getId();
		this.name = field.getName();
		this.prometheusQuery = field.getPrometheusQuery();
		this.conditions = field.getConditions();
		this.simulatedHostMetrics = field.getSimulatedHostMetrics().stream().map(HostSimulatedMetricDTO::new).collect(Collectors.toSet());
		this.simulatedServiceMetrics = field.getSimulatedServiceMetrics().stream().map(ServiceSimulatedMetricDTO::new).collect(Collectors.toSet());
		/*this.isNew = field.isNew();*/
	}

	@JsonIgnore
	public Field toEntity() {
		Field field = Field.builder()
			.id(id)
			.name(name)
			.prometheusQuery(prometheusQuery)
			.conditions(conditions)
			.simulatedHostMetrics(simulatedHostMetrics != null ? simulatedHostMetrics.stream().map(HostSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.simulatedServiceMetrics(simulatedServiceMetrics != null ? simulatedServiceMetrics.stream().map(ServiceSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*field.setNew(isNew);*/
		return field;
	}
}
