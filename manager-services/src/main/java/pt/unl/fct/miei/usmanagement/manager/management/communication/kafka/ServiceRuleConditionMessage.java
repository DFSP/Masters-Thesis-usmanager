package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceRuleConditionMessage {

	private ServiceRuleMessage serviceRuleMessage;
	private ConditionMessage conditionMessage;

	public ServiceRuleConditionMessage(ServiceRuleCondition serviceRuleCondition) {
		this.serviceRuleMessage = new ServiceRuleMessage(serviceRuleCondition.getServiceRule());
		this.conditionMessage = new ConditionMessage(serviceRuleCondition.getServiceCondition());
	}

	public ServiceRuleCondition get() {
		return ServiceRuleCondition.builder()
			.serviceRule(serviceRuleMessage.get())
			.serviceCondition(conditionMessage.get())
			.build();
	}
}
