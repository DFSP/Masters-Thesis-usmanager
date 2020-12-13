package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostEventDTO;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostEventMapper {

	HostEventMapper MAPPER = Mappers.getMapper(HostEventMapper.class);

	@Mapping(source = "id", target = "id")
	HostEvent toHostEvent(HostEventDTO hostEventDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostEventDTO fromHostEvent(HostEvent hostEvent, @Context CycleAvoidingMappingContext context);
	
}
