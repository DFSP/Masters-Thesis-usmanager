package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;

@Mapper(builder = @Builder(disableBuilder = true))
public interface AppRuleMapper {

	AppRuleMapper MAPPER = Mappers.getMapper(AppRuleMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	AppRule toAppRule(AppRuleDTO appRuleDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	AppRuleDTO fromAppRule(AppRule appRule, @Context CycleAvoidingMappingContext context);

}
