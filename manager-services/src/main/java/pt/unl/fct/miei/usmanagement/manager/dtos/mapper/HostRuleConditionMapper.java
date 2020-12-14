package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostRuleConditionMapper {	
	
	HostRuleConditionMapper MAPPER = Mappers.getMapper(HostRuleConditionMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	HostRuleCondition toHostRuleCondition(HostRuleConditionDTO hostRuleConditionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostRuleConditionDTO fromHostRuleCondition(HostRuleCondition hostRuleCondition, @Context CycleAvoidingMappingContext context);
	
}
