package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = DecisionDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DecisionDTO {

	private Long id;
	private RuleDecisionEnum ruleDecision;
	private ComponentTypeDTO componentType;

	public DecisionDTO(Long id) {
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
		if (!(o instanceof Decision)) {
			return false;
		}
		Decision other = (Decision) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "DecisionDTO{" +
			"id=" + id +
			", ruleDecision=" + ruleDecision +
			", componentType=" + componentType +
			'}';
	}
}
