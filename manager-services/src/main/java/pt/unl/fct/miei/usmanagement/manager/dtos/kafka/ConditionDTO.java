package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ConditionDTO.class)
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
		if (!(o instanceof Condition)) {
			return false;
		}
		Condition other = (Condition) o;
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
			"{rule=" + hostRuleConditionDTO.getHostRule().getId() + ", condition=" + hostRuleConditionDTO.getCondition().getId() + "}")
			.collect(Collectors.toSet())) +
			", appConditions=" + (appConditions == null ? "null" : appConditions.stream().map(AppRuleConditionDTO::getId).collect(Collectors.toSet())) +
			", serviceConditions=" + (serviceConditions == null ? "null" : serviceConditions.stream().map(serviceRuleConditionDTO ->
			"{rule=" + serviceRuleConditionDTO.getServiceRule().getId() + ", condition=" + serviceRuleConditionDTO.getCondition().getId() + "}")
			.collect(Collectors.toSet())) +
			", containerConditions=" + (containerConditions == null ? "null" : containerConditions.stream().map(containerRuleConditionDTO ->
			"{rule=" + containerRuleConditionDTO.getContainerRule().getId() + ", condition=" + containerRuleConditionDTO.getCondition().getId() + "}")
			.collect(Collectors.toSet())) +
			'}';
	}
}
