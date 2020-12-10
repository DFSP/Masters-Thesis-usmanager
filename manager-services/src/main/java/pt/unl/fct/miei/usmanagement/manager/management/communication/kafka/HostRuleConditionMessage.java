package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostRuleConditionMessage {

	private HostRuleMessage hostRuleMessage;
	private ConditionMessage conditionMessage;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostRuleConditionMessage(HostRuleCondition hostRuleCondition) {
		this.hostRuleMessage = new HostRuleMessage(hostRuleCondition.getHostRule());
		this.conditionMessage = new ConditionMessage(hostRuleCondition.getHostCondition());
		/*this.isNew = hostRuleCondition.isNew();*/
	}

	public HostRuleCondition get() {
		HostRuleCondition hostRuleCondition = HostRuleCondition.builder()
			.hostRule(hostRuleMessage.get())
			.hostCondition(conditionMessage.get())
			.build();
		/*hostRuleCondition.setNew(isNew);*/
		return hostRuleCondition;
	}
}
