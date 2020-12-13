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
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServiceDecisionValueDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDecisionValueDTO {

	private Long id;
	private ServiceDecisionDTO serviceDecision;
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
		if (!(o instanceof ServiceDecisionValue)) {
			return false;
		}
		ServiceDecisionValue other = (ServiceDecisionValue) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceDecisionValueDTO{" +
			"id=" + id +
			", serviceDecision=" + serviceDecision +
			", field=" + field +
			", value=" + value +
			'}';
	}
}
