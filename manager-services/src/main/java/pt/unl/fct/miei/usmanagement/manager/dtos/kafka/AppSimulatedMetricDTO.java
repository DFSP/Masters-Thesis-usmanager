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

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = AppSimulatedMetricDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppSimulatedMetricDTO {

	private Long id;
	private String name;
	private FieldDTO field;
	private double minimumValue;
	private double maximumValue;
	private boolean override;
	private boolean active;
	private Set<AppDTO> apps;

	public AppSimulatedMetricDTO(Long id) {
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
		if (!(o instanceof AppSimulatedMetricDTO)) {
			return false;
		}
		AppSimulatedMetricDTO other = (AppSimulatedMetricDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "AppSimulatedMetricDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", field=" + field +
			", minimumValue=" + minimumValue +
			", maximumValue=" + maximumValue +
			", override=" + override +
			", active=" + active +
			", apps=" + (apps == null ? "null" : apps.stream().map(AppDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
