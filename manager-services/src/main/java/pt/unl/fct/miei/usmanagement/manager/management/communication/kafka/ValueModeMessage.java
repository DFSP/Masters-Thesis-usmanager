package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ValueModeMessage {

	private String name;
	private Set<Condition> conditions;

	public ValueModeMessage(ValueMode valueMode) {
		this.name = valueMode.getName();
		this.conditions = valueMode.getConditions();
	}

	public ValueMode get() {
		return ValueMode.builder()
			.name(name)
			.conditions(conditions)
			.build();
	}
}
