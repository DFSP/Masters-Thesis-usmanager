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
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServiceDecisionDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDecisionDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String result;
	private DecisionDTO decision;
	private ServiceRuleDTO rule;
	private Timestamp timestamp;
	private Set<ServiceDecisionValueDTO> serviceDecisionValues;

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
		return "ServiceDecisionDTO{" +
			"id=" + id +
			", containerId='" + containerId + '\'' +
			", serviceName='" + serviceName + '\'' +
			", result='" + result + '\'' +
			", decision=" + decision +
			", rule=" + rule +
			", timestamp=" + timestamp +
			", serviceDecisionValues=" + (serviceDecisionValues == null ? "null" : serviceDecisionValues.stream()
			.map(ServiceDecisionValueDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
