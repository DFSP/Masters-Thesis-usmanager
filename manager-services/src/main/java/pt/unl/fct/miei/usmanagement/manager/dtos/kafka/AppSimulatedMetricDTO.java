package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppSimulatedMetricDTO {

	private Long id;
	private String name;
	private Field field;
	private double minimumValue;
	private double maximumValue;
	private boolean override;
	private boolean active;
	private Set<AppDTO> apps;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public AppSimulatedMetricDTO(Long id) {
		this.id = id;
	}

	public AppSimulatedMetricDTO(AppSimulatedMetric appSimulatedMetric) {
		this.id = appSimulatedMetric.getId();
		this.name = appSimulatedMetric.getName();
		this.field = appSimulatedMetric.getField();
		this.minimumValue = appSimulatedMetric.getMinimumValue();
		this.maximumValue = appSimulatedMetric.getMaximumValue();
		this.override = appSimulatedMetric.isOverride();
		this.active = appSimulatedMetric.isActive();
		this.apps = appSimulatedMetric.getApps().stream().map(AppDTO::new).collect(Collectors.toSet());
		/*this.isNew = appSimulatedMetric.isNew();*/
	}

	@JsonIgnore
	public AppSimulatedMetric toEntity() {
		AppSimulatedMetric appSimulatedMetric = AppSimulatedMetric.builder()
			.id(id)
			.name(name)
			.field(field)
			.minimumValue(minimumValue)
			.maximumValue(maximumValue)
			.override(override)
			.active(active)
			.apps(apps != null ? apps.stream().map(AppDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*appSimulatedMetric.setNew(isNew);*/
		return appSimulatedMetric;
	}
}
