package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyKey;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDependencyDTO {

	private ServiceDependencyKey id;
	private ServiceDTO service;
	private ServiceDTO dependency;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceDependencyDTO)) {
			return false;
		}
		ServiceDependencyDTO other = (ServiceDependencyDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceDependencyDTO{" +
			"id=" + id +
			", service=" + (service == null ? "null" : service.getServiceName()) +
			", dependency=" + (dependency == null ? "null" : dependency.getServiceName()) +
			'}';
	}
}
