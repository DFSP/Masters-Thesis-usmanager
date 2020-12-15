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
import org.hibernate.annotations.GenericGenerator;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionValue;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
		if (!(o instanceof HostDecisionValue)) {
			return false;
		}
		HostDecisionValue other = (HostDecisionValue) o;
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
