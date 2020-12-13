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
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = FieldDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FieldDTO {

	private Long id;
	private String name;
	private PrometheusQueryEnum prometheusQuery;
	private Set<Condition> conditions;
	private Set<HostSimulatedMetricDTO> simulatedHostMetrics;
	private Set<ServiceSimulatedMetricDTO> simulatedServiceMetrics;

	public FieldDTO(Long id) {
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
		if (!(o instanceof Field)) {
			return false;
		}
		Field other = (Field) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "FieldDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", prometheusQuery=" + prometheusQuery +
			", conditions=" + (conditions == null ? "null" : conditions.stream().map(Condition::getId).collect(Collectors.toSet())) +
			", simulatedHostMetrics=" + (simulatedHostMetrics == null ? "null" : simulatedHostMetrics.stream()
			.map(HostSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			", simulatedServiceMetrics=" + (simulatedServiceMetrics == null ? "null" : simulatedServiceMetrics.stream()
			.map(ServiceSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
