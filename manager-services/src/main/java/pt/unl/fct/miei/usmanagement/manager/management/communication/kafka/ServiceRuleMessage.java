package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceRuleMessage {

	private Long id;
	private String name;
	private int priority;
	private boolean generic;
	private Set<Service> services;
	private Decision decision;
	private Set<ServiceRuleCondition> conditions;

	public ServiceRuleMessage(ServiceRule serviceRule) {
		this.id = serviceRule.getId();
		this.name = serviceRule.getName();
		this.priority = serviceRule.getPriority();
		this.generic = serviceRule.isGeneric();
		this.services = serviceRule.getServices();
		this.decision = serviceRule.getDecision();
		this.conditions = serviceRule.getConditions();
	}
}
