package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceRuleMapper {

	ServiceRuleMapper MAPPER = Mappers.getMapper(ServiceRuleMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	ServiceRule toServiceRule(ServiceRuleDTO serviceRuleDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceRuleDTO fromServiceRule(ServiceRule serviceRule, @Context CycleAvoidingMappingContext context);

}
