package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ValueModeDTO {

	private Long id;
	private String name;
	private Set<ConditionDTO> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ValueModeDTO(Long id) {
		this.id = id;
	}

	public ValueModeDTO(ValueMode valueMode) {
		this.id = valueMode.getId();
		this.name = valueMode.getName();
		this.conditions = valueMode.getConditions().stream().map(ConditionDTO::new).collect(Collectors.toSet());;
		/*this.isNew = valueMode.isNew();*/
	}

	@JsonIgnore
	public ValueMode toEntity() {
		ValueMode valueMode = ValueMode.builder()
			.id(id)
			.name(name)
			.conditions(conditions != null ? conditions.stream().map(ConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*valueMode.setNew(isNew);*/
		return valueMode;
	}
}
