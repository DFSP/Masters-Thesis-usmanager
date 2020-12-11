package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceRuleConditionDTO {

	private ServiceRuleDTO serviceRule;
	private ConditionDTO condition;

}
