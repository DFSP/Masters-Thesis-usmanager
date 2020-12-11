package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class DecisionDTO {

	private Long id;
	private RuleDecisionEnum ruleDecision;
	private ComponentTypeDTO componentType;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public DecisionDTO(Long id) {
		this.id = id;
	}

	public DecisionDTO(Decision decision) {
		this.id = decision.getId();
		this.ruleDecision = decision.getRuleDecision();
		this.componentType = new ComponentTypeDTO(decision.getComponentType());
		/*this.isNew = decision.isNew();*/
	}

	@JsonIgnore
	public Decision toEntity() {
		Decision decision = Decision.builder()
			.id(id)
			.ruleDecision(ruleDecision)
			.componentType(componentType.toEntity())
			.build();
		/*decision.setNew(isNew);*/
		return decision;
	}

}
