package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppDTO;

@Mapper(builder = @Builder(disableBuilder = true))
public interface AppMapper {

	AppMapper MAPPER = Mappers.getMapper(AppMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	App toApp(AppDTO appDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	AppDTO fromApp(App app, @Context CycleAvoidingMappingContext context);

}
