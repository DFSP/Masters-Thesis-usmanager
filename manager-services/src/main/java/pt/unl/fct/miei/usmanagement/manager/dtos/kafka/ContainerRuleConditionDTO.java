package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerRuleConditionDTO {

	private ContainerRuleDTO containerRule;
	private ConditionDTO condition;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ContainerRuleConditionDTO(ContainerRuleCondition containerRuleCondition) {
		this.containerRule = new ContainerRuleDTO(containerRuleCondition.getContainerRule());
		this.condition = new ConditionDTO(containerRuleCondition.getContainerCondition());
		/*this.isNew = containerRuleCondition.isNew();*/
	}

	@JsonIgnore
	public ContainerRuleCondition toEntity() {
		ContainerRuleCondition containerRuleCondition = ContainerRuleCondition.builder()
			.containerRule(containerRule.toEntity())
			.containerCondition(condition.toEntity())
			.build();
		/*containerRuleCondition.setNew(isNew);*/
		return containerRuleCondition;
	}
}
