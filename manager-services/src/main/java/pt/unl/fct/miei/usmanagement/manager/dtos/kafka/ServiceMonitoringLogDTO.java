package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ServiceMonitoringLogDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceMonitoringLogDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String field;
	private double value;
	private LocalDateTime timestamp;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceMonitoringLog)) {
			return false;
		}
		ServiceMonitoringLog other = (ServiceMonitoringLog) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceMonitoringLogDTO{" +
			"id=" + id +
			", containerId='" + containerId + '\'' +
			", serviceName='" + serviceName + '\'' +
			", field='" + field + '\'' +
			", value=" + value +
			", timestamp=" + timestamp +
			'}';
	}
}
