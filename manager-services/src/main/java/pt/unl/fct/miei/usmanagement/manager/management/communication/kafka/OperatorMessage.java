package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
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
}
