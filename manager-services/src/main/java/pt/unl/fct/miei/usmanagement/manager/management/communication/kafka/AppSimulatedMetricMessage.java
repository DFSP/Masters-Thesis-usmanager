package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppSimulatedMetricMessage {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean override;
	private boolean active;
	private Set<App> apps;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public AppSimulatedMetricMessage(Long id) {
		this.id = id;
	}

	public AppSimulatedMetricMessage(AppSimulatedMetric appSimulatedMetric) {
		this.id = appSimulatedMetric.getId();
		this.name = appSimulatedMetric.getName();
		this.field = appSimulatedMetric.getField();
		this.minimumValue = appSimulatedMetric.getMinimumValue();
		this.maximumValue = appSimulatedMetric.getMaximumValue();
		this.override = appSimulatedMetric.isOverride();
		this.active = appSimulatedMetric.isActive();
		this.apps = appSimulatedMetric.getApps();
		/*this.isNew = appSimulatedMetric.isNew();*/
	}

	public AppSimulatedMetric get() {
		AppSimulatedMetric appSimulatedMetric = AppSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.override(override)
			.active(active)
			.apps(apps != null ? apps : new HashSet<>())
			.build();
		/*appSimulatedMetric.setNew(isNew);*/
		return appSimulatedMetric;
	}
}
