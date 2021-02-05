package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = FieldDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FieldDTO {

	private Long id;
	private String name;
	private PrometheusQueryEnum prometheusQuery;
	private Set<ConditionDTO> conditions;
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
		if (!(o instanceof FieldDTO)) {
			return false;
		}
		FieldDTO other = (FieldDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "FieldDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", prometheusQuery=" + prometheusQuery +
			", conditions=" + (conditions == null ? "null" : conditions.stream()
			.map(ConditionDTO::getId).collect(Collectors.toSet())) +
			", simulatedHostMetrics=" + (simulatedHostMetrics == null ? "null" : simulatedHostMetrics.stream()
			.map(HostSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			", simulatedServiceMetrics=" + (simulatedServiceMetrics == null ? "null" : simulatedServiceMetrics.stream()
			.map(ServiceSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
