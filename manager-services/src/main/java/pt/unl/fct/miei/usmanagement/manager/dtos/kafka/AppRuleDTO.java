package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppRuleDTO {

	private Long id;
	private String name;
	private int priority;
	private DecisionDTO decision;
	private Set<AppDTO> apps;
	private Set<AppRuleConditionDTO> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public AppRuleDTO(Long id) {
		this.id = id;
	}

	public AppRuleDTO(AppRule rule) {
		this.id = rule.getId();
		this.name = rule.getName();
		this.priority = rule.getPriority();
		this.decision = new DecisionDTO(rule.getDecision());
		this.apps = rule.getApps().stream().map(AppDTO::new).collect(Collectors.toSet());
		this.conditions = rule.getConditions().stream().map(AppRuleConditionDTO::new).collect(Collectors.toSet());
		/*this.isNew = rule.isNew();*/
	}

	@JsonIgnore
	public AppRule toEntity() {
		AppRule appRule = AppRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.decision(decision.toEntity())
			.apps(apps != null ? apps.stream().map(AppDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.conditions(conditions != null ? conditions.stream().map(AppRuleConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*appRule.setNew(isNew);*/
		return appRule;
	}

}
