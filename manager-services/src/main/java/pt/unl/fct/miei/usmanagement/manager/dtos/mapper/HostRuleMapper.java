package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostRuleMapper {

	HostRuleMapper MAPPER = Mappers.getMapper(HostRuleMapper.class);

	@Mapping(source = "id", target = "id")
	HostRule toHostRule(HostRuleDTO hostRuleDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostRuleDTO fromHostRule(HostRule hostRule, @Context CycleAvoidingMappingContext context);

}
