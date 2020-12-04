package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
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
	private Set<ServiceDecisionValue> componentDecisionValueLogs;

	public FieldMessage(Field field) {
		this.id = field.getId();
		this.name = field.getName();
		this.prometheusQuery = field.getPrometheusQuery();
		this.conditions = field.getConditions();
		this.simulatedHostMetrics = field.getSimulatedHostMetrics();
		this.simulatedServiceMetrics = field.getSimulatedServiceMetrics();
		this.componentDecisionValueLogs = field.getComponentDecisionValueLogs();
	}
}
