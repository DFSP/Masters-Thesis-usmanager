package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ComponentTypeDTO {

	private Long id;
	private ComponentTypeEnum type;
	private Set<DecisionDTO> decisions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ComponentTypeDTO(Long id) {
		this.id = id;
	}

	public ComponentTypeDTO(ComponentType componentType) {
		this.id = componentType.getId();
		this.type = componentType.getType();
		this.decisions = componentType.getDecisions().stream().map(DecisionDTO::new).collect(Collectors.toSet());
		/*this.isNew = componentType.isNew();*/
	}

	@JsonIgnore
	public ComponentType toEntity() {
		ComponentType componentType = ComponentType.builder()
			.id(id)
			.type(type)
			.decisions(decisions != null ? decisions.stream().map(DecisionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*componentType.setNew(isNew);*/
		return componentType;
	}

}
