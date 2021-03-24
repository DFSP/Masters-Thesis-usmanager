package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = HostMonitoringLogDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostMonitoringLogDTO {

	/*private Long id;*/
	private String publicIpAddress;
	private String privateIpAddress;
	private String field;
	private double value;
	private LocalDateTime timestamp;

	@Override
	public int hashCode() {
		return Objects.hash(publicIpAddress, privateIpAddress, field, timestamp);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HostMonitoringLogDTO)) {
			return false;
		}
		HostMonitoringLogDTO other = (HostMonitoringLogDTO) o;
		return publicIpAddress != null && publicIpAddress.equals(other.getPublicIpAddress()) &&
			privateIpAddress != null && publicIpAddress.equals(other.getPrivateIpAddress()) &&
			field != null && field.equalsIgnoreCase(other.getField()) &&
			timestamp != null && timestamp.equals(other.getTimestamp());
	}

	@Override
	public String toString() {
		return "HostMonitoringLogDTO{" +
			/*"id=" + id +
			", */"publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", field='" + field + '\'' +
			", value=" + value +
			", timestamp=" + timestamp +
			'}';
	}
}
