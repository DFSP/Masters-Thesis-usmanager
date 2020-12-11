package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private boolean generic;
	private Decision decision;
	private Set<ServiceDTO> services;
	private Set<ServiceRuleConditionDTO> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceRuleDTO(Long id) {
		this.id = id;
	}

	public ServiceRuleDTO(ServiceRule serviceRule) {
		this.id = serviceRule.getId();
		this.name = serviceRule.getName();
		this.priority = serviceRule.getPriority();
		this.generic = serviceRule.isGeneric();
		this.decision = serviceRule.getDecision();
		this.services = serviceRule.getServices().stream().map(ServiceDTO::new).collect(Collectors.toSet());
		this.conditions = serviceRule.getConditions().stream().map(ServiceRuleConditionDTO::new).collect(Collectors.toSet());
		/*this.isNew = serviceRule.isNew();*/
	}

	@JsonIgnore
	public ServiceRule toEntity() {
		ServiceRule serviceRule = ServiceRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.generic(generic)
			.decision(decision)
			.services(services != null ? services.stream().map(ServiceDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.conditions(conditions != null ? conditions.stream().map(ServiceRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*serviceRule.setNew(serviceRule.isNew());*/
		return serviceRule;
	}
}
