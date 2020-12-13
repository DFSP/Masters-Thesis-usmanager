package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ValueModeDTO;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ValueModeMapper {

	ValueModeMapper MAPPER = Mappers.getMapper(ValueModeMapper.class);

	@Mapping(source = "id", target = "id")
	ValueMode toValueMode(ValueModeDTO valueModeDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ValueModeDTO fromValueMode(ValueMode valueMode, @Context CycleAvoidingMappingContext context);

}
