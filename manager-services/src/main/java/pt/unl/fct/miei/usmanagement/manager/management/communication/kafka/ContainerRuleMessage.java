package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
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

	public ContainerRuleMessage(ContainerRule containerRule) {
		this.id = containerRule.getId();
		this.name = containerRule.getName();
		this.priority = containerRule.getPriority();
		this.decision = containerRule.getDecision();
		this.containers = containerRule.getContainers();
		this.conditions = containerRule.getConditions();
	}

}
