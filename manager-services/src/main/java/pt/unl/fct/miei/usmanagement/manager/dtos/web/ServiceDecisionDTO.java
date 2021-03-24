package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

import java.sql.Timestamp;

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
