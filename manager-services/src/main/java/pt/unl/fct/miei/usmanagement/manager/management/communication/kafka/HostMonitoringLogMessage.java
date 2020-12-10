package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostMonitoringLogMessage {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
	private String field;
	private double value;
	private LocalDateTime timestamp;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostMonitoringLogMessage(HostMonitoringLog hostMonitoringLog) {
		this.id = hostMonitoringLog.getId();
		this.publicIpAddress = hostMonitoringLog.getPublicIpAddress();
		this.privateIpAddress = hostMonitoringLog.getPrivateIpAddress();
		this.field = hostMonitoringLog.getField();
		this.value = hostMonitoringLog.getValue();
		this.timestamp = hostMonitoringLog.getTimestamp();
		/*this.isNew = hostMonitoringLog.isNew();*/
	}

	public HostMonitoringLog get() {
		HostMonitoringLog hostMonitoringLog = HostMonitoringLog.builder()
			.id(id)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.field(field)
			.value(value)
			.timestamp(timestamp)
			.build();
		/*hostMonitoringLog.setNew(isNew);*/
		return hostMonitoringLog;
	}
}
