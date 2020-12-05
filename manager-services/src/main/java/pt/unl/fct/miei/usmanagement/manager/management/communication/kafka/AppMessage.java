package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppMessage {

	private Long id;
	private String name;
	private String description;
	private Set<AppService> appServices;
	private Set<AppRule> appRules;
	private Set<AppSimulatedMetric> simulatedAppMetrics;

	public AppMessage(App app) {
		this.id = app.getId();
		this.name = app.getName();
		this.description = app.getDescription();
		this.appServices = app.getAppServices();
		this.appRules = app.getAppRules();
		this.simulatedAppMetrics = app.getSimulatedAppMetrics();
	}

	public App get() {
		return App.builder()
			.id(id)
			.name(name)
			.description(description)
			.appServices(appServices)
			.appRules(appRules)
			.simulatedAppMetrics(simulatedAppMetrics)
			.build();
	}

}
