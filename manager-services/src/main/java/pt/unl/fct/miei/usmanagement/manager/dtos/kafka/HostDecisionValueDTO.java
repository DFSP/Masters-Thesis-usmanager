package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = HostDecisionValueDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostDecisionValueDTO {

	private Long id;
	private HostDecisionDTO hostDecision;
	private FieldDTO field;
	private double value;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HostDecisionValueDTO)) {
			return false;
		}
		HostDecisionValueDTO other = (HostDecisionValueDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostDecisionValueDTO{" +
			"id=" + id +
			", hostDecision=" + hostDecision +
			", field=" + field +
			", value=" + value +
			'}';
	}
}
