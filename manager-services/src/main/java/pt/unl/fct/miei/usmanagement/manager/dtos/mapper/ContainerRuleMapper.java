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
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ContainerRuleMapper {

	ContainerRuleMapper MAPPER = Mappers.getMapper(ContainerRuleMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	ContainerRule toContainerRule(ContainerRuleDTO containerRuleDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ContainerRuleDTO fromContainerRule(ContainerRule containerRule, @Context CycleAvoidingMappingContext context);

}
