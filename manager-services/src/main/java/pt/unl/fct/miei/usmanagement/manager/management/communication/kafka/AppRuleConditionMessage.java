package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppRuleConditionMessage {

	private AppRuleMessage appRuleMessage;
	private ConditionMessage conditionMessage;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public AppRuleConditionMessage(AppRuleCondition appRuleCondition) {
		this.appRuleMessage = new AppRuleMessage(appRuleCondition.getAppRule());
		this.conditionMessage = new ConditionMessage(appRuleCondition.getAppCondition());
		/*this.isNew = appRuleCondition.isNew();*/
	}

	public AppRuleCondition get() {
		AppRuleCondition appRuleCondition = AppRuleCondition.builder()
			.appRule(appRuleMessage.get())
			.appCondition(conditionMessage.get())
			.build();
		/*appRuleCondition.setNew(isNew);*/
		return appRuleCondition;
	}
}
