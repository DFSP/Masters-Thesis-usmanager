package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionValue;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class HostDecisionValueDTO {

	private Long id;
	private HostDecisionDTO hostDecision;
	private FieldDTO field;
	private double value;

	public HostDecisionValueDTO(HostDecisionValue hostDecisionValue) {
		this.id = hostDecisionValue.getId();
		this.hostDecision = new HostDecisionDTO(hostDecisionValue.getHostDecision());
		this.field = new FieldDTO(hostDecisionValue.getField());
		this.value = hostDecisionValue.getValue();
		/*this.isNew = hostDecisionValue.isNew();*/
	}

	@JsonIgnore
	public HostDecisionValue toEntity() {
		HostDecisionValue hostDecisionValue = HostDecisionValue.builder()
			.id(id)
			.hostDecision(hostDecision.toEntity())
			.field(field.toEntity())
			.value(value)
			.build();
		/*hostDecisionValue.setNew(isNew);*/
		return hostDecisionValue;
	}
}
