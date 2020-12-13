package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ContainerRuleDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContainerRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private DecisionDTO decision;
	private Set<ContainerDTO> containers;
	private Set<ContainerRuleConditionDTO> conditions;

	public ContainerRuleDTO(Long id) {
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
		if (!(o instanceof ContainerRule)) {
			return false;
		}
		ContainerRule other = (ContainerRule) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ContainerRuleDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", priority=" + priority +
			", decision=" + decision +
			", containers=" + containers +
			", conditions=" + conditions +
			'}';
	}
}
