package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerRuleMessage {

	private Long id;
	private String name;
	private int priority;
	private Decision decision;
	private Set<Container> containers;
	private Set<ContainerRuleCondition> conditions;

	public ContainerRuleMessage(Long id) {
		this.id = id;
	}

	public ContainerRuleMessage(ContainerRule containerRule) {
		this.id = containerRule.getId();
		this.name = containerRule.getName();
		this.priority = containerRule.getPriority();
		this.decision = containerRule.getDecision();
		this.containers = containerRule.getContainers();
		this.conditions = containerRule.getConditions();
	}

	public ContainerRule get() {
		return ContainerRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.decision(decision)
			.containers(containers != null ? containers : new HashSet<>())
			.conditions(conditions != null ? conditions : new HashSet<>())
			.build();
	}

}
