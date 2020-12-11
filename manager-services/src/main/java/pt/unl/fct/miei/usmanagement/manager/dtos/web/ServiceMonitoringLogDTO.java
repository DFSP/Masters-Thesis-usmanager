package pt.unl.fct.miei.usmanagement.manager.dtos.web;

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

}
