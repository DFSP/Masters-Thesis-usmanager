package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class OperatorMessage {

	private Long id;
	private OperatorEnum operator;
	private String symbol;
	private Set<Condition> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public OperatorMessage(Long id) {
		this.id = id;
	}

	public OperatorMessage(Operator operator) {
		this.id = operator.getId();
		this.operator = operator.getOperator();
		this.symbol = operator.getSymbol();
		this.conditions = operator.getConditions();
		/*this.isNew = operator.isNew();*/
	}

	public Operator get() {
		Operator operator = Operator.builder()
			.id(id)
			.operator(this.operator)
			.symbol(symbol)
			.conditions(conditions != null ? conditions : new HashSet<>())
			.build();
		/*operator.setNew(isNew);*/
		return operator;
	}
}
