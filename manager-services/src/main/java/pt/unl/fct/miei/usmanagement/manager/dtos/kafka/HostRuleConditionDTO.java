package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleConditionKey;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostRuleConditionDTO {

	private RuleConditionKey id;
	private HostRuleDTO hostRule;
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
		if (!(o instanceof HostRuleCondition)) {
			return false;
		}
		HostRuleCondition other = (HostRuleCondition) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostRuleConditionDTO{" +
			"id=" + id +
			", hostRule=" + hostRule +
			", condition=" + condition +
			'}';
	}
}
