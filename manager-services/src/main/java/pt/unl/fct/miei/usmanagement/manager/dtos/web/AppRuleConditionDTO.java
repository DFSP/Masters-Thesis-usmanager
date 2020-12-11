package pt.unl.fct.miei.usmanagement.manager.dtos.web;

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

}
