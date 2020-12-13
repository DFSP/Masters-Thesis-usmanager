package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceDecisionMapper {

	ServiceDecisionMapper MAPPER = Mappers.getMapper(ServiceDecisionMapper.class);

	@Mapping(source = "id", target = "id")
	ServiceDecision toServiceDecision(ServiceDecisionDTO serviceDecisionDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceDecisionDTO fromServiceDecision(ServiceDecision serviceDecision, @Context CycleAvoidingMappingContext context);
	
}
