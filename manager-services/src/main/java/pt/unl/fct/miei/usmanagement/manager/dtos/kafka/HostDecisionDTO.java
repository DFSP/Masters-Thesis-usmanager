package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = HostDecisionDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostDecisionDTO {

	private Long id;
	private Decision decision;
	private HostRule rule;
	private String publicIpAddress;
	private String privateIpAddress;
	private Timestamp timestamp;
	private Set<HostDecisionValueDTO> hostDecisionValues;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceDecision)) {
			return false;
		}
		ServiceDecision other = (ServiceDecision) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostDecisionDTO{" +
			"id=" + id +
			", decision=" + decision +
			", rule=" + rule +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", timestamp=" + timestamp +
			", hostDecisionValues=" + (hostDecisionValues == null ? "null" : hostDecisionValues.stream()
			.map(HostDecisionValueDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
