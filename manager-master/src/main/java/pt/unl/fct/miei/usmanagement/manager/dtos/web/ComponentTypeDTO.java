package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ComponentTypeDTO {

	private Long id;
	private ComponentTypeEnum type;

}
