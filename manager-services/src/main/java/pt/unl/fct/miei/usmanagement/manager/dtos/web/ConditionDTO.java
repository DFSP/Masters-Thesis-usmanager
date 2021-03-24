package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ConditionDTO {

	private Long id;
	private String name;
	private ValueModeDTO valueMode;
	private FieldDTO field;
	private OperatorDTO operator;
	private double value;

}
