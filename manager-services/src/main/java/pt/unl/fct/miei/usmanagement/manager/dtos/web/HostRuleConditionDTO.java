package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostRuleConditionDTO {

	private HostRuleDTO hostRule;
	private ConditionDTO condition;

}
