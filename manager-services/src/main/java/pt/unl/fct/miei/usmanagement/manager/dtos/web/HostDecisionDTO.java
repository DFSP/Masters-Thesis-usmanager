package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostDecisionDTO {

	private Long id;
	private DecisionDTO decision;
	private HostRuleDTO rule;
	private String publicIpAddress;
	private String privateIpAddress;
	private Timestamp timestamp;

}
