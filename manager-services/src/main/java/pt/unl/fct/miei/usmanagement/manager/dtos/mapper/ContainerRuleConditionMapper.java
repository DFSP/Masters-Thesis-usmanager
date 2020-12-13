package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ContainerRuleConditionMapper {	
	
	ContainerRuleConditionMapper MAPPER = Mappers.getMapper(ContainerRuleConditionMapper.class);

	@Mapping(source = "id", target = "id")
	ContainerRuleCondition toContainerRuleCondition(ContainerRuleConditionDTO containerRuleConditionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ContainerRuleConditionDTO fromContainerRuleCondition(ContainerRuleCondition containerRuleCondition, @Context CycleAvoidingMappingContext context);
	
}
