package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionValue;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceDecisionValueDTO {

	private Long id;
	private ServiceDecisionDTO serviceDecision;
	private FieldDTO field;
	private double value;

	public ServiceDecisionValueDTO(ServiceDecisionValue serviceDecisionValue) {
		this.id = serviceDecisionValue.getId();
		this.serviceDecision = new ServiceDecisionDTO(serviceDecisionValue.getServiceDecision());
		this.field = new FieldDTO(serviceDecisionValue.getField());
		this.value = serviceDecisionValue.getValue();
		/*this.isNew = serviceDecisionValue.isNew();*/
	}

	@JsonIgnore
	public ServiceDecisionValue toEntity() {
		ServiceDecisionValue serviceDecisionValue = ServiceDecisionValue.builder()
			.id(id)
			.serviceDecision(serviceDecision.toEntity())
			.field(field.toEntity())
			.value(value)
			.build();
		/*serviceDecisionValue.setNew(isNew);*/
		return serviceDecisionValue;
	}
}
