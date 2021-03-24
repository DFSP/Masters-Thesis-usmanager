package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ServiceSimulatedMetricDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceSimulatedMetricDTO {

	private Long id;
	private String name;
	private FieldDTO field;
	private double minimumValue;
	private double maximumValue;
	private boolean generic;
	private boolean override;
	private boolean active;
	private Set<ServiceDTO> services;

	public ServiceSimulatedMetricDTO(Long id) {
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
		if (!(o instanceof ServiceSimulatedMetricDTO)) {
			return false;
		}
		ServiceSimulatedMetricDTO other = (ServiceSimulatedMetricDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceSimulatedMetricDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", field=" + field +
			", minimumValue=" + minimumValue +
			", maximumValue=" + maximumValue +
			", generic=" + generic +
			", override=" + override +
			", active=" + active +
			", services=" + (services == null ? "services" : services.stream().map(ServiceDTO::getServiceName).collect(Collectors.toSet())) +
			'}';
	}
}
