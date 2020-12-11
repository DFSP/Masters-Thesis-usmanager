package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ServiceMonitoringLogDTO {

	private Long id;
	private String containerId;
	private String serviceName;
	private String field;
	private double value;
	private LocalDateTime timestamp;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceMonitoringLogDTO(ServiceMonitoringLog serviceMonitoringLog) {
		this.id = serviceMonitoringLog.getId();
		this.containerId = serviceMonitoringLog.getContainerId();
		this.serviceName = serviceMonitoringLog.getServiceName();
		this.field = serviceMonitoringLog.getField();
		this.value = serviceMonitoringLog.getValue();
		this.timestamp = serviceMonitoringLog.getTimestamp();
		/*this.isNew = serviceMonitoringLog.isNew();*/
	}

	@JsonIgnore
	public ServiceMonitoringLog toEntity() {
		ServiceMonitoringLog serviceMonitoringLog = ServiceMonitoringLog.builder()
			.id(id)
			.containerId(containerId)
			.serviceName(serviceName)
			.field(field)
			.value(value)
			.timestamp(timestamp)
			.build();
		/*serviceMonitoringLog.setNew(isNew);*/
		return serviceMonitoringLog;
	}
}
