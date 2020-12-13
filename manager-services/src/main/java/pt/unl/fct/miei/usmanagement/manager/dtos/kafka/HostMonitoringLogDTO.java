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
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = HostMonitoringLogDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostMonitoringLogDTO {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
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
		if (!(o instanceof HostMonitoringLog)) {
			return false;
		}
		HostMonitoringLog other = (HostMonitoringLog) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostMonitoringLogDTO{" +
			"id=" + id +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", field='" + field + '\'' +
			", value=" + value +
			", timestamp=" + timestamp +
			'}';
	}
}
