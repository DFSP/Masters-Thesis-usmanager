package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ServiceMonitoringLogDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceMonitoringLogDTO {

	/*private Long id;*/
	private String containerId;
	private String serviceName;
	private String field;
	private double value;
	private LocalDateTime timestamp;

	@Override
	public int hashCode() {
		return Objects.hash(containerId, field, timestamp);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceMonitoringLogDTO)) {
			return false;
		}
		ServiceMonitoringLogDTO other = (ServiceMonitoringLogDTO) o;
		return containerId != null && containerId.equals(other.getContainerId()) &&
			field != null && field.equalsIgnoreCase(other.getField()) &&
			timestamp != null && timestamp.equals(other.getTimestamp());
	}

	@Override
	public String toString() {
		return "ServiceMonitoringLogDTO{" +
			/*"id=" + id +
			", */"containerId='" + containerId + '\'' +
			", serviceName='" + serviceName + '\'' +
			", field='" + field + '\'' +
			", value=" + value +
			", timestamp=" + timestamp +
			'}';
	}
}
