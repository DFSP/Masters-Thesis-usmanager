package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private DecisionDTO decision;
	private Set<ContainerDTO> containers;
	private Set<ContainerRuleConditionDTO> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ContainerRuleDTO(Long id) {
		this.id = id;
	}

	public ContainerRuleDTO(ContainerRule containerRule) {
		this.id = containerRule.getId();
		this.name = containerRule.getName();
		this.priority = containerRule.getPriority();
		this.decision = new DecisionDTO(containerRule.getDecision());
		this.containers = containerRule.getContainers().stream().map(ContainerDTO::new).collect(Collectors.toSet());
		this.conditions = containerRule.getConditions().stream().map(ContainerRuleConditionDTO::new).collect(Collectors.toSet());
		/*this.isNew = containerRule.isNew();*/
	}

	@JsonIgnore
	public ContainerRule toEntity() {
		ContainerRule containerRule = ContainerRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.decision(decision.toEntity())
			.containers(containers != null ? containers.stream().map(ContainerDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.conditions(conditions != null ? conditions.stream().map(ContainerRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*containerRule.setNew(isNew);*/
		return containerRule;
	}

}
