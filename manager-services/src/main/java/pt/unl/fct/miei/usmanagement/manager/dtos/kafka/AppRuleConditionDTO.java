package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppRuleConditionDTO {

	private AppRuleDTO appRule;
	private ConditionDTO condition;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public AppRuleConditionDTO(AppRuleCondition appRuleCondition) {
		this.appRule = new AppRuleDTO(appRuleCondition.getAppRule());
		this.condition = new ConditionDTO(appRuleCondition.getAppCondition());
		/*this.isNew = appRuleCondition.isNew();*/
	}

	@JsonIgnore
	public AppRuleCondition toEntity() {
		AppRuleCondition appRuleCondition = AppRuleCondition.builder()
			.appRule(appRule.toEntity())
			.appCondition(condition.toEntity())
			.build();
		/*appRuleCondition.setNew(isNew);*/
		return appRuleCondition;
	}
}
