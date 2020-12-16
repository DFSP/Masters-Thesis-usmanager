package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = HostDecisionDTO.class)
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
		if (!(o instanceof HostDecisionDTO)) {
			return false;
		}
		HostDecisionDTO other = (HostDecisionDTO) o;
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
