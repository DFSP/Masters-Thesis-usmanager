package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ValueModeMessage {

	private Long id;
	private String name;
	private Set<Condition> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ValueModeMessage(Long id) {
		this.id = id;
	}

	public ValueModeMessage(ValueMode valueMode) {
		this.id = valueMode.getId();
		this.name = valueMode.getName();
		this.conditions = valueMode.getConditions();
		/*this.isNew = valueMode.isNew();*/
	}

	public ValueMode get() {
		ValueMode valueMode = ValueMode.builder()
			.id(id)
			.name(name)
			.conditions(conditions != null ? conditions : new HashSet<>())
			.build();
		/*valueMode.setNew(isNew);*/
		return valueMode;
	}
}
