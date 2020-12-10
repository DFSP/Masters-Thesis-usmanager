package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ComponentTypeMessage {

	private Long id;
	private ComponentTypeEnum type;
	private Set<Decision> decisions;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ComponentTypeMessage(Long id) {
		this.id = id;
	}

	public ComponentTypeMessage(ComponentType componentType) {
		this.id = componentType.getId();
		this.type = componentType.getType();
		this.decisions = componentType.getDecisions();
		/*this.isNew = componentType.isNew();*/
	}

	public ComponentType get() {
		ComponentType componentType = ComponentType.builder()
			.id(id)
			.type(type)
			.decisions(decisions != null ? decisions : new HashSet<>())
			.build();
		/*componentType.setNew(isNew);*/
		return componentType;
	}

}
