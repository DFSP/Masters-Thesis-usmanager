package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceDecisionDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String result;
	private Decision decision;
	private ServiceRuleDTO rule;
	private Timestamp timestamp;

}
