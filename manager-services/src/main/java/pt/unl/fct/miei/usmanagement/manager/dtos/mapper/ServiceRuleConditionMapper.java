package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceRuleConditionMapper {	
	
	ServiceRuleConditionMapper MAPPER = Mappers.getMapper(ServiceRuleConditionMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	ServiceRuleCondition toServiceRuleCondition(ServiceRuleConditionDTO serviceRuleConditionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceRuleConditionDTO fromServiceRuleCondition(ServiceRuleCondition serviceRuleCondition, @Context CycleAvoidingMappingContext context);
	
}
