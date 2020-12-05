package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

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

	public OperatorMessage(Operator operator) {
		this.id = operator.getId();
		this.operator = operator.getOperator();
		this.symbol = operator.getSymbol();
		this.conditions = operator.getConditions();
	}

	public Operator get() {
		return Operator.builder()
			.id(id)
			.operator(operator)
			.symbol(symbol)
			.conditions(conditions)
			.build();
	}
}
