package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

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
		if (!(o instanceof AppService)) {
			return false;
		}
		AppService other = (AppService) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "AppServiceDTO{" +
			"id=" + id +
			", launchOrder=" + launchOrder +
			", app=" + (app == null ? "null" : app.getId()) +
			", service=" + (service == null ? "null" : service.getId()) +
			'}';
	}
}
