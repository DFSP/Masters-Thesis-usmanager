package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class DecisionMessage {

	private Long id;
	private RuleDecisionEnum ruleDecision;
	private ComponentType componentType;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public DecisionMessage(Long id) {
		this.id = id;
	}

	public DecisionMessage(Decision decision) {
		this.id = decision.getId();
		this.ruleDecision = decision.getRuleDecision();
		this.componentType = decision.getComponentType();
		/*this.isNew = decision.isNew();*/
	}

	public Decision get() {
		Decision decision = Decision.builder()
			.id(id)
			.ruleDecision(ruleDecision)
			.componentType(componentType)
			.build();
		/*decision.setNew(isNew);*/
		return decision;
	}

}
