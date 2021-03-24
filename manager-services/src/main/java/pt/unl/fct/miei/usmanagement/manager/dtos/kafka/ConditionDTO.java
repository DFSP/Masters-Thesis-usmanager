package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ConditionDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConditionDTO {

	private Long id;
	private String name;
	private ValueModeDTO valueMode;
	private FieldDTO field;
	private OperatorDTO operator;
	private double value;
	private Set<HostRuleConditionDTO> hostConditions;
	private Set<AppRuleConditionDTO> appConditions;
	private Set<ServiceRuleConditionDTO> serviceConditions;
	private Set<ContainerRuleConditionDTO> containerConditions;

	public ConditionDTO(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ConditionDTO)) {
			return false;
		}
		ConditionDTO other = (ConditionDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ConditionDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", valueMode=" + valueMode +
			", field=" + field +
			", operator=" + operator +
			", value=" + value +
			", hostConditions=" + (hostConditions == null ? "null" : hostConditions.stream().map(hostRuleConditionDTO ->
			hostRuleConditionDTO == null ? "null" :
				"{rule=" + (hostRuleConditionDTO.getRule() == null ? "null" : hostRuleConditionDTO.getRule().getId()) +
					", condition=" + (hostRuleConditionDTO.getCondition() == null ? "null" : hostRuleConditionDTO.getCondition().getId()) + "}")
			.collect(Collectors.toSet())) +
			", appConditions=" + (appConditions == null ? "null" : appConditions.stream().map(appRuleConditionDTO ->
			appRuleConditionDTO == null ? "null" :
				"{rule=" + (appRuleConditionDTO.getRule() == null ? "null" : appRuleConditionDTO.getRule().getId()) +
					", condition=" + (appRuleConditionDTO.getCondition() == null ? "null" : appRuleConditionDTO.getCondition().getId()) + "}")
			.collect(Collectors.toSet())) +
			", serviceConditions=" + (serviceConditions == null ? "null" : serviceConditions.stream().map(serviceRuleConditionDTO ->
			serviceRuleConditionDTO == null ? "null" :
				"{rule=" + (serviceRuleConditionDTO.getRule() == null ? "null" : serviceRuleConditionDTO.getRule().getId()) +
					", condition=" + (serviceRuleConditionDTO.getCondition() == null ? "null" : serviceRuleConditionDTO.getCondition().getId()) + "}")
			.collect(Collectors.toSet())) +
			", containerConditions=" + (containerConditions == null ? "null" : containerConditions.stream().map(containerRuleConditionDTO ->
			containerRuleConditionDTO == null ? "null" :
				"{rule=" + (containerRuleConditionDTO.getRule() == null ? "null" : containerRuleConditionDTO.getRule().getId()) +
					", condition=" + (containerRuleConditionDTO.getCondition() == null ? "null" : containerRuleConditionDTO.getCondition().getId()) + "}")
			.collect(Collectors.toSet())) +
			'}';
	}
}
