package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ConditionMessage {

	private Long id;
	private String name;
	private ValueMode valueMode;
	private Field field;
	private Operator operator;
	private double value;
	private Set<HostRuleCondition> hostConditions;
	private Set<AppRuleCondition> appConditions;
	private Set<ServiceRuleCondition> serviceConditions;
	private Set<ContainerRuleCondition> containerConditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ConditionMessage(Long id) {
		this.id = id;
	}

	public ConditionMessage(Condition condition) {
		this.id = condition.getId();
		this.name = condition.getName();
		this.valueMode = condition.getValueMode();
		this.field = condition.getField();
		this.operator = condition.getOperator();
		this.value = condition.getValue();
		this.hostConditions = condition.getHostConditions();
		this.appConditions = condition.getAppConditions();
		this.serviceConditions = condition.getServiceConditions();
		this.containerConditions = condition.getContainerConditions();
		/*this.isNew = condition.isNew();*/
	}

	public Condition get() {
		Condition condition = Condition.builder()
			.id(id)
			.name(name)
			.valueMode(valueMode)
			.field(field)
			.operator(operator)
			.value(value)
			.hostConditions(hostConditions != null ? hostConditions : new HashSet<>())
			.appConditions(appConditions != null ? appConditions : new HashSet<>())
			.serviceConditions(serviceConditions != null ? serviceConditions : new HashSet<>())
			.containerConditions(containerConditions != null ? containerConditions : new HashSet<>())
			.build();
		/*condition.setNew(condition.isNew());*/
		return condition;
	}
}
