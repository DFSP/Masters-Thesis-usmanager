package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceRuleConditionDTO {

	private ServiceRuleDTO serviceRule;
	private ConditionDTO condition;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceRuleConditionDTO(ServiceRuleCondition serviceRuleCondition) {
		this.serviceRule = new ServiceRuleDTO(serviceRuleCondition.getServiceRule());
		this.condition = new ConditionDTO(serviceRuleCondition.getServiceCondition());
		/*this.isNew = serviceRuleCondition.isNew();*/
	}

	@JsonIgnore
	public ServiceRuleCondition toEntity() {
		ServiceRuleCondition serviceRuleCondition = ServiceRuleCondition.builder()
			.serviceRule(serviceRule.toEntity())
			.serviceCondition(condition.toEntity())
			.build();
		/*serviceRuleCondition.setNew(isNew);*/
		return serviceRuleCondition;
	}
}
