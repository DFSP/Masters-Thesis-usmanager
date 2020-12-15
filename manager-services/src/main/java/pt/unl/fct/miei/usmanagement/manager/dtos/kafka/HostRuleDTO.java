package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = HostRuleDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private DecisionDTO decision;
	private boolean generic;
	private Set<CloudHostDTO> cloudHosts;
	private Set<EdgeHostDTO> edgeHosts;
	private Set<HostRuleConditionDTO> conditions;

	public HostRuleDTO(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HostRule)) {
			return false;
		}
		HostRule other = (HostRule) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostRuleDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", priority=" + priority +
			", decision=" + decision +
			", generic=" + generic +
			", cloudHosts=" + (cloudHosts == null ? "null" : cloudHosts.stream().map(CloudHostDTO::getId).collect(Collectors.toSet())) +
			", edgeHosts=" + (edgeHosts == null ? "null" : edgeHosts.stream().map(EdgeHostDTO::getId).collect(Collectors.toSet())) +
			", conditions=" + (conditions == null ? "null" : conditions.stream().map(HostRuleConditionDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
