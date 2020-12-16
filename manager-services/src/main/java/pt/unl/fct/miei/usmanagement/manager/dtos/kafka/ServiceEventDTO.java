package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ServiceEventDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceEventDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private DecisionDTO decision;
	private int count;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceEventDTO)) {
			return false;
		}
		ServiceEventDTO other = (ServiceEventDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceEventDTO{" +
			"id=" + id +
			", containerId='" + containerId + '\'' +
			", serviceName='" + serviceName + '\'' +
			", managerPublicIpAddress='" + managerPublicIpAddress + '\'' +
			", managerPrivateIpAddress='" + managerPrivateIpAddress + '\'' +
			", decision=" + decision +
			", count=" + count +
			'}';
	}
}
