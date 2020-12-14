package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.DecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

@Mapper(builder = @Builder(disableBuilder = true))
public interface DecisionMapper {

	DecisionMapper MAPPER = Mappers.getMapper(DecisionMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	Decision toDecision(DecisionDTO decisionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	DecisionDTO fromDecision(Decision decision, @Context CycleAvoidingMappingContext context);

}
