package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerRuleConditionDTO {

	private ContainerRuleDTO containerRule;
	private ConditionDTO condition;

}
