package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

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

	public ContainerRuleConditionMessage(ContainerRuleCondition containerRuleCondition) {
		this.containerRuleMessage = new ContainerRuleMessage(containerRuleCondition.getContainerRule());
		this.conditionMessage = new ConditionMessage(containerRuleCondition.getContainerCondition());
	}

	public ContainerRuleCondition get() {
		return ContainerRuleCondition.builder()
			.containerRule(containerRuleMessage.get())
			.containerCondition(conditionMessage.get())
			.build();
	}
}
