package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class OperatorDTO {

	private Long id;
	private OperatorEnum operator;
	private String symbol;
	private Set<ConditionDTO> conditions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public OperatorDTO(Long id) {
		this.id = id;
	}

	public OperatorDTO(Operator operator) {
		this.id = operator.getId();
		this.operator = operator.getOperator();
		this.symbol = operator.getSymbol();
		this.conditions = operator.getConditions().stream().map(ConditionDTO::new).collect(Collectors.toSet());
		/*this.isNew = operator.isNew();*/
	}

	@JsonIgnore
	public Operator toEntity() {
		Operator operator = Operator.builder()
			.id(id)
			.operator(this.operator)
			.symbol(symbol)
			.conditions(conditions != null ? conditions.stream().map(ConditionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*operator.setNew(isNew);*/
		return operator;
	}
}
