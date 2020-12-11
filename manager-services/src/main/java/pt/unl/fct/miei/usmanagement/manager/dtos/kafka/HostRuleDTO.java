package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private DecisionDTO decision;
	private boolean generic;
	private Set<CloudHostDTO> cloudHosts;
	private Set<EdgeHostDTO> edgeHosts;
	private Set<HostRuleConditionDTO> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostRuleDTO(Long id) {
		this.id = id;
	}

	public HostRuleDTO(HostRule hostRule) {
		this.id = hostRule.getId();
		this.name = hostRule.getName();
		this.priority = hostRule.getPriority();
		this.decision = new DecisionDTO(hostRule.getDecision());
		this.generic = hostRule.isGeneric();
		this.cloudHosts = hostRule.getCloudHosts().stream().map(CloudHostDTO::new).collect(Collectors.toSet());
		this.edgeHosts = hostRule.getEdgeHosts().stream().map(EdgeHostDTO::new).collect(Collectors.toSet());
		this.conditions = hostRule.getConditions().stream().map(HostRuleConditionDTO::new).collect(Collectors.toSet());
		/*this.isNew = hostRule.isNew();*/
	}

	@JsonIgnore
	public HostRule toEntity() {
		HostRule hostRule = HostRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.decision(decision.toEntity())
			.generic(generic)
			.cloudHosts(cloudHosts != null ? cloudHosts.stream().map(CloudHostDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.edgeHosts(edgeHosts != null ? edgeHosts.stream().map(EdgeHostDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.conditions(conditions != null ? conditions.stream().map(HostRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*hostRule.setNew(isNew);*/
		return hostRule;
	}

}
