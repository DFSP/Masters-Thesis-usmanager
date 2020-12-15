package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = AppRuleDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private DecisionDTO decision;
	private Set<AppDTO> apps;
	private Set<AppRuleConditionDTO> conditions;

	public AppRuleDTO(Long id) {
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
		if (!(o instanceof AppRule)) {
			return false;
		}
		AppRule other = (AppRule) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "AppRuleDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", priority=" + priority +
			", decision=" + decision +
			", apps=" + (apps == null ? "null" : apps.stream().map(AppDTO::getId).collect(Collectors.toSet())) +
			", conditions=" + (conditions == null ? "null" : conditions.stream().map(appRuleConditionDTO ->
			"{rule=" + appRuleConditionDTO.getRule().toString() + ", condition=" + appRuleConditionDTO.getCondition().toString() + "}")
			.collect(Collectors.toSet())) +
			'}';
	}
}
