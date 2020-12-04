package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppMessage {

	private Long id;
	private String name;
	private String description;
	private Set<Long> appServices;
	private Set<Long> appRules;
	private Set<Long> simulatedAppMetrics;

	public AppMessage(App app) {
		this.id = app.getId();
		this.name = app.getName();
		this.description = app.getDescription();
		Set<AppService> appServices = app.getAppServices();
		if (appServices != null) {
			this.appServices = new HashSet<>(appServices.size());
			appServices.forEach(appService -> this.appServices.add(appService.getId()));
		}
		Set<AppRule> appRules = app.getAppRules();
		if (appRules != null) {
			this.appRules = new HashSet<>(appRules.size());
			appRules.forEach(appRule -> this.appRules.add(appRule.getId()));
		}
		Set<AppSimulatedMetric> simulatedAppMetrics = app.getSimulatedAppMetrics();
		if (simulatedAppMetrics != null) {
			this.simulatedAppMetrics = new HashSet<>(simulatedAppMetrics.size());
			simulatedAppMetrics.forEach(simulatedAppMetric -> this.simulatedAppMetrics.add(simulatedAppMetric.getId()));
		}
	}

}
