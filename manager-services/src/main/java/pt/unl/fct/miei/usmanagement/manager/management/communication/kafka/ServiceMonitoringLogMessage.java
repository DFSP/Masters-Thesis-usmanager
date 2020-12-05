package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceMonitoringLogMessage {

	private Long id;
	private String containerId;
	private String serviceName;
	private String field;
	private double value;
	private LocalDateTime timestamp;

	public ServiceMonitoringLogMessage(ServiceMonitoringLog serviceMonitoringLog) {
		this.id = serviceMonitoringLog.getId();
		this.containerId = serviceMonitoringLog.getContainerId();
		this.serviceName = serviceMonitoringLog.getServiceName();
		this.field = serviceMonitoringLog.getField();
		this.value = serviceMonitoringLog.getValue();
		this.timestamp = serviceMonitoringLog.getTimestamp();
	}

	public ServiceMonitoringLog toServiceMonitoringLog() {
		return ServiceMonitoringLog.builder()
			.id(id)
			.containerId(containerId)
			.serviceName(serviceName)
			.field(field)
			.value(value)
			.timestamp(timestamp)
			.build();
	}
}
