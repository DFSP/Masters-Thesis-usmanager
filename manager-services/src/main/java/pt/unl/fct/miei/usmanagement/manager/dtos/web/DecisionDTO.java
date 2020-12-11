package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class DecisionDTO {

	private Long id;
	private RuleDecisionEnum ruleDecision;
	private ComponentTypeDTO componentType;

}
