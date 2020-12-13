package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;

@Mapper(builder = @Builder(disableBuilder = true))
public interface AppRuleConditionMapper {	
	
	AppRuleConditionMapper MAPPER = Mappers.getMapper(AppRuleConditionMapper.class);

	@Mapping(source = "id", target = "id")
	AppRuleCondition toAppRuleCondition(AppRuleConditionDTO appRuleConditionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	AppRuleConditionDTO fromAppRuleCondition(AppRuleCondition appRuleCondition, @Context CycleAvoidingMappingContext context);
	
}
