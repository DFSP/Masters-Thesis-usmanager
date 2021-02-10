package pt.unl.fct.miei.usmanagement.manager.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyKey;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ServiceDependencyDTO.class)
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
		if (!(o instanceof ServiceDependency)) {
			return false;
		}
		ServiceDependency other = (ServiceDependency) o;
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
