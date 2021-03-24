package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = OperatorDTO.class)
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
		if (!(o instanceof OperatorDTO)) {
			return false;
		}
		OperatorDTO other = (OperatorDTO) o;
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
