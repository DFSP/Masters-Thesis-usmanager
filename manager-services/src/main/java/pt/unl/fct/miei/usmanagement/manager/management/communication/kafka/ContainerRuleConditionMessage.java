package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerRuleConditionMessage {

	private ContainerRuleMessage containerRuleMessage;
	private ConditionMessage conditionMessage;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ContainerRuleConditionMessage(ContainerRuleCondition containerRuleCondition) {
		this.containerRuleMessage = new ContainerRuleMessage(containerRuleCondition.getContainerRule());
		this.conditionMessage = new ConditionMessage(containerRuleCondition.getContainerCondition());
		/*this.isNew = containerRuleCondition.isNew();*/
	}

	public ContainerRuleCondition get() {
		ContainerRuleCondition containerRuleCondition = ContainerRuleCondition.builder()
			.containerRule(containerRuleMessage.get())
			.containerCondition(conditionMessage.get())
			.build();
		/*containerRuleCondition.setNew(isNew);*/
		return containerRuleCondition;
	}
}
