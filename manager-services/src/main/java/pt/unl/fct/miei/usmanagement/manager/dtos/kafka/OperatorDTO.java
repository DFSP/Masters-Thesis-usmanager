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
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = OperatorDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OperatorDTO {

	private Long id;
	private OperatorEnum operator;
	private String symbol;
	private Set<ConditionDTO> conditions;

	public OperatorDTO(Long id) {
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
		if (!(o instanceof Operator)) {
			return false;
		}
		Operator other = (Operator) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "OperatorDTO{" +
			"id=" + id +
			", operator=" + operator +
			", symbol='" + symbol + '\'' +
			", conditions=" + (conditions == null ? "null" : conditions.stream().map(ConditionDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
