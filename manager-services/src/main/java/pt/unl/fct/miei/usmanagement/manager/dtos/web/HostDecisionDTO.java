package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

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
