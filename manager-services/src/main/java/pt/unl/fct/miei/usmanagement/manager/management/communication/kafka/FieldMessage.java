package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class FieldMessage {

	private Long id;
	private String name;
	private PrometheusQueryEnum prometheusQuery;
	private Set<Condition> conditions;
	private Set<HostSimulatedMetric> simulatedHostMetrics;
	private Set<ServiceSimulatedMetric> simulatedServiceMetrics;

	public FieldMessage(Long id) {
		this.id = id;
	}

	public FieldMessage(Field field) {
		this.id = id;
		this.name = field.getName();
		this.prometheusQuery = field.getPrometheusQuery();
		this.conditions = field.getConditions();
		this.simulatedHostMetrics = field.getSimulatedHostMetrics();
		this.simulatedServiceMetrics = field.getSimulatedServiceMetrics();
	}

	public Field get() {
		return Field.builder()
			.id(id)
			.name(name)
			.prometheusQuery(prometheusQuery)
			.conditions(conditions)
			.simulatedHostMetrics(simulatedHostMetrics != null ? simulatedHostMetrics : new HashSet<>())
			.simulatedServiceMetrics(simulatedServiceMetrics != null ? simulatedServiceMetrics : new HashSet<>())
			.build();
	}
}
