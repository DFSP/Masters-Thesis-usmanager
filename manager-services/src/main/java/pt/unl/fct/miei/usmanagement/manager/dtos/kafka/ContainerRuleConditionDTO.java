package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleConditionKey;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContainerRuleConditionDTO {

	private RuleConditionKey id;
	private ContainerRuleDTO rule;
	private ConditionDTO condition;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ContainerRuleConditionDTO)) {
			return false;
		}
		ContainerRuleConditionDTO other = (ContainerRuleConditionDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ContainerRuleConditionDTO{" +
			"id=" + id +
			", containerRule=" + rule +
			", condition=" + condition +
			'}';
	}
}
