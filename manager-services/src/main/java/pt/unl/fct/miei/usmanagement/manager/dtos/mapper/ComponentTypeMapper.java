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
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ComponentTypeDTO;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ComponentTypeMapper {

	ComponentTypeMapper MAPPER = Mappers.getMapper(ComponentTypeMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	ComponentType toComponentType(ComponentTypeDTO serviceDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ComponentTypeDTO fromComponentType(ComponentType service, @Context CycleAvoidingMappingContext context);
}
