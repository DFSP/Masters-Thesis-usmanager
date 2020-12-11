package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostRuleConditionDTO {

	private HostRuleDTO hostRule;
	private ConditionDTO condition;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostRuleConditionDTO(HostRuleCondition hostRuleCondition) {
		this.hostRule = new HostRuleDTO(hostRuleCondition.getHostRule());
		this.condition = new ConditionDTO(hostRuleCondition.getHostCondition());
		/*this.isNew = hostRuleCondition.isNew();*/
	}

	@JsonIgnore
	public HostRuleCondition toEntity() {
		HostRuleCondition hostRuleCondition = HostRuleCondition.builder()
			.hostRule(hostRule.toEntity())
			.hostCondition(condition.toEntity())
			.build();
		/*hostRuleCondition.setNew(isNew);*/
		return hostRuleCondition;
	}
}
