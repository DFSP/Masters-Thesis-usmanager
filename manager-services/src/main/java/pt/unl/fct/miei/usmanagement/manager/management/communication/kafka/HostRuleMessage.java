package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostRuleMessage {

	private Long id;
	private String name;
	private int priority;
	private Decision decision;
	private boolean generic;
	private Set<CloudHost> cloudHosts;
	private Set<EdgeHost> edgeHosts;
	private Set<HostRuleCondition> conditions;

	public HostRuleMessage(HostRule hostRule) {
		this.id = hostRule.getId();
		this.name = hostRule.getName();
		this.priority = hostRule.getPriority();
		this.decision = hostRule.getDecision();
		this.generic = hostRule.isGeneric();
		this.cloudHosts = hostRule.getCloudHosts();
		this.edgeHosts = hostRule.getEdgeHosts();
		this.conditions = hostRule.getConditions();
	}

}
