package pt.unl.fct.miei.usmanagement.manager.dtos.web;

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
public class HostMonitoringLogDTO {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
	private String field;
	private double value;
	private LocalDateTime timestamp;

}
