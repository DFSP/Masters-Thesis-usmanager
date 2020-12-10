package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.HashSet;
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
	private Decision decision;
	private Set<Service> services;
	private Set<ServiceRuleCondition> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceRuleMessage(Long id) {
		this.id = id;
	}

	public ServiceRuleMessage(ServiceRule serviceRule) {
		this.id = serviceRule.getId();
		this.name = serviceRule.getName();
		this.priority = serviceRule.getPriority();
		this.generic = serviceRule.isGeneric();
		this.decision = serviceRule.getDecision();
		this.services = serviceRule.getServices();
		this.conditions = serviceRule.getConditions();
		/*this.isNew = serviceRule.isNew();*/
	}

	public ServiceRule get() {
		ServiceRule serviceRule = ServiceRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.generic(generic)
			.decision(decision)
			.services(services != null ? services : new HashSet<>())
			.conditions(conditions != null ? conditions : new HashSet<>())
			.build();
		/*serviceRule.setNew(serviceRule.isNew());*/
		return serviceRule;
	}
}
