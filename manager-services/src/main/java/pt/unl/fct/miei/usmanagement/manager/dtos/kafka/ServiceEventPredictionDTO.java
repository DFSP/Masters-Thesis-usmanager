package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceEventPredictionDTO {

	private Long id;
	private String name;
	private String description;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate startDate;
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate endDate;
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;
	private int minimumReplicas;
	private ServiceDTO service;
	private Timestamp lastUpdate;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceEventPredictionDTO(ServiceEventPrediction serviceEventPrediction) {
		this.id = serviceEventPrediction.getId();
		this.name = serviceEventPrediction.getName();
		this.description = serviceEventPrediction.getDescription();
		this.startDate = serviceEventPrediction.getStartDate();
		this.startTime = serviceEventPrediction.getStartTime();
		this.endDate = serviceEventPrediction.getEndDate();
		this.endTime = serviceEventPrediction.getEndTime();
		this.minimumReplicas = serviceEventPrediction.getMinimumReplicas();
		this.service = new ServiceDTO(serviceEventPrediction.getService());
		this.lastUpdate = serviceEventPrediction.getLastUpdate();
		/*this.isNew = serviceEventPrediction.isNew();*/
	}

	public ServiceEventPrediction toEntity() {
		ServiceEventPrediction serviceEventPrediction = ServiceEventPrediction.builder()
			.id(id)
			.name(name)
			.description(description)
			.startDate(startDate)
			.startTime(startTime)
			.endDate(endDate)
			.endTime(endTime)
			.minimumReplicas(minimumReplicas)
			.service(service.toEntity())
			.lastUpdate(lastUpdate)
			.build();
		/*serviceEventPrediction.setNew(isNew);*/
		return serviceEventPrediction;
	}

}
