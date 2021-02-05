package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostDecisionMapper {

	HostDecisionMapper MAPPER = Mappers.getMapper(HostDecisionMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	HostDecision toHostDecision(HostDecisionDTO hostDecisionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostDecisionDTO fromHostDecision(HostDecision hostDecision, @Context CycleAvoidingMappingContext context);

}
