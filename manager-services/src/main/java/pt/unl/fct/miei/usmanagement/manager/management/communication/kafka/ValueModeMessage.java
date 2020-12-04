package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ValueModeMessage {

	private Long id;
	private String name;
	private Set<Condition> conditions;
	public ValueModeMessage(ValueMode valueMode) {
		this.id = valueMode.getId();
		this.name = valueMode.getName();
		this.conditions = valueMode.getConditions();
	}
}
