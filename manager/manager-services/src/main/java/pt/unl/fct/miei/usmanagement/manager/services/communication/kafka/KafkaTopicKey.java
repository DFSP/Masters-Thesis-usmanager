package pt.unl.fct.miei.usmanagement.manager.services.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaTopicKey {

	private String managerId;
	private String operation;

	public KafkaTopicKey(String managerId) {
		this.managerId = managerId;
		this.operation = null;
	}
}
