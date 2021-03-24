package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class OperatorDTO {

	private Long id;
	private OperatorEnum operator;
	private String symbol;

}
