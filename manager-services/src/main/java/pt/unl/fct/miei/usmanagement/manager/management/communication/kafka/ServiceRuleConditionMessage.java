package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceRuleConditionMessage(ServiceRuleCondition serviceRuleCondition) {
		this.serviceRuleMessage = new ServiceRuleMessage(serviceRuleCondition.getServiceRule());
		this.conditionMessage = new ConditionMessage(serviceRuleCondition.getServiceCondition());
		/*this.isNew = serviceRuleCondition.isNew();*/
	}

	public ServiceRuleCondition get() {
		ServiceRuleCondition serviceRuleCondition = ServiceRuleCondition.builder()
			.serviceRule(serviceRuleMessage.get())
			.serviceCondition(conditionMessage.get())
			.build();
		/*serviceRuleCondition.setNew(isNew);*/
		return serviceRuleCondition;
	}
}
