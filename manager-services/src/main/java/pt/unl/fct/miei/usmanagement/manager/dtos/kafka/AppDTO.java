package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.App;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppDTO {

	private Long id;
	private String name;
	private String description;
	private Set<AppServiceDTO> appServices;
	private Set<AppRuleDTO> appRules;
	private Set<AppSimulatedMetricDTO> simulatedAppMetrics;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public AppDTO(Long id) {
		this.id = id;
	}

	public AppDTO(App app) {
		this.id = app.getId();
		this.name = app.getName();
		this.description = app.getDescription();
		//this.appServices = app.getAppServices().stream().map(AppServiceDTO::new).collect(Collectors.toSet());
		this.appRules = app.getAppRules().stream().map(AppRuleDTO::new).collect(Collectors.toSet());
		this.simulatedAppMetrics = app.getSimulatedAppMetrics().stream().map(AppSimulatedMetricDTO::new).collect(Collectors.toSet());
		/*this.isNew = app.isNew();*/
	}

	@JsonIgnore
	public App toEntity() {
		App app = App.builder()
			.id(id)
			.name(name)
			.description(description)
			//.appServices(appServices != null ? appServices.stream().map(AppServiceDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.appRules(appRules != null ? appRules.stream().map(AppRuleDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.simulatedAppMetrics(simulatedAppMetrics != null ? simulatedAppMetrics .stream().map(AppSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*app.setNew(isNew);*/
		return app;
	}

	@Override
	public String toString() {
		return "AppDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", appServices=" + (appServices != null ? appServices.stream().map(AppServiceDTO::getId).collect(Collectors.toSet()) : "null") +
			", appRules=" + (appRules != null ? appRules.stream().map(AppRuleDTO::getId).collect(Collectors.toSet()) : "null") +
			", simulatedAppMetrics=" + (simulatedAppMetrics != null ? simulatedAppMetrics .stream().map(AppSimulatedMetricDTO::getId).collect(Collectors.toSet()) : "null") +
			'}';
	}
}
