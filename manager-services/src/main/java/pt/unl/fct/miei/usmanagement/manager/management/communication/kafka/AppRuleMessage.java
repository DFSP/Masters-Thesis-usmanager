package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppRuleMessage {

	private Long id;
	private String name;
	private int priority;
	private Decision decision;
	private Set<App> apps;
	private Set<AppRuleCondition> conditions;

	public AppRuleMessage(Long id) {
		this.id = id;
	}

	public AppRuleMessage(AppRule rule) {
		this.id = rule.getId();
		this.name = rule.getName();
		this.priority = rule.getPriority();
		this.decision = rule.getDecision();
		this.apps = rule.getApps();
		this.conditions = rule.getConditions();
	}

	public AppRule get() {
		return AppRule.builder()
			.id(id)
			.name(name)
			.priority(priority)
			.decision(decision)
			.apps(apps != null ? apps : new HashSet<>())
			.conditions(conditions != null ? conditions : new HashSet<>())
			.build();
	}

}
