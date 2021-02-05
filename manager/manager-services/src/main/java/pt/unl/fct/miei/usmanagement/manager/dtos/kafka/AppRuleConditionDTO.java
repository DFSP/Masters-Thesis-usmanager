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
public class AppRuleConditionDTO {

	private RuleConditionKey id;
	private AppRuleDTO rule;
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
		if (!(o instanceof AppRuleConditionDTO)) {
			return false;
		}
		AppRuleConditionDTO other = (AppRuleConditionDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "AppRuleConditionDTO{" +
			"id=" + id +
			", appRule=" + null +
			", condition=" + condition +
			'}';
	}
}
