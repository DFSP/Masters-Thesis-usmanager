package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ValueModeDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ValueModeDTO {

	private Long id;
	private String name;
	private Set<ConditionDTO> conditions;

	public ValueModeDTO(Long id) {
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
		if (!(o instanceof ValueMode)) {
			return false;
		}
		ValueMode other = (ValueMode) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ValueModeDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", conditions=" + (conditions == null ? "null" : conditions.stream().map(ConditionDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
