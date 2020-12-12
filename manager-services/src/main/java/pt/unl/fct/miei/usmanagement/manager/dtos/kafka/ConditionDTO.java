package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ConditionDTO {

	private Long id;
	private String name;
	private ValueModeDTO valueMode;
	private FieldDTO field;
	private OperatorDTO operator;
	private double value;
	private Set<HostRuleConditionDTO> hostConditions;
	private Set<AppRuleConditionDTO> appConditions;
	private Set<ServiceRuleConditionDTO> serviceConditions;
	private Set<ContainerRuleConditionDTO> containerConditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ConditionDTO(Long id) {
		this.id = id;
	}

	public ConditionDTO(Condition condition) {
		this.id = condition.getId();
		this.name = condition.getName();
		this.valueMode = new ValueModeDTO(condition.getValueMode());
		this.field = new FieldDTO(condition.getField());
		this.operator = new OperatorDTO(condition.getOperator());
		this.value = condition.getValue();
		this.hostConditions = condition.getHostConditions().stream().map(HostRuleConditionDTO::new).collect(Collectors.toSet());
		this.appConditions = condition.getAppConditions().stream().map(AppRuleConditionDTO::new).collect(Collectors.toSet());
		this.serviceConditions = condition.getServiceConditions().stream().map(ServiceRuleConditionDTO::new).collect(Collectors.toSet());
		this.containerConditions = condition.getContainerConditions().stream().map(ContainerRuleConditionDTO::new).collect(Collectors.toSet());
		/*this.isNew = condition.isNew();*/
	}

	@JsonIgnore
	public Condition toEntity() {
		Condition condition = Condition.builder()
			.id(id)
			.name(name)
			.valueMode(valueMode.toEntity())
			.field(field.toEntity())
			.operator(operator.toEntity())
			.value(value)
			.hostConditions(hostConditions != null ? hostConditions.stream().map(HostRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.appConditions(appConditions != null ? appConditions.stream().map(AppRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.serviceConditions(serviceConditions != null ? serviceConditions.stream().map(ServiceRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.containerConditions(containerConditions != null ? containerConditions.stream().map(ContainerRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*condition.setNew(condition.isNew());*/
		return condition;
	}
}
