package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

	public AppSimulatedMetricMessage(AppSimulatedMetric appSimulatedMetric) {
		this.id = appSimulatedMetric.getId();
		this.name = appSimulatedMetric.getName();
		this.field = appSimulatedMetric.getField();
		this.minimumValue = appSimulatedMetric.getMinimumValue();
		this.maximumValue = appSimulatedMetric.getMaximumValue();
		this.override = appSimulatedMetric.isOverride();
		this.active = appSimulatedMetric.isActive();
		this.apps = appSimulatedMetric.getApps();
	}
}
