package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppServiceDTO {

	private AppServiceKey id;
	private int launchOrder;
	private AppDTO app;
	private ServiceDTO service;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AppServiceDTO)) {
			return false;
		}
		AppServiceDTO other = (AppServiceDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "AppServiceDTO{" +
			"id=" + id +
			", launchOrder=" + launchOrder +
			", app=" + (app == null ? "null" : app.getName()) +
			", service=" + (service == null ? "null" : service.getServiceName()) +
			'}';
	}
}
