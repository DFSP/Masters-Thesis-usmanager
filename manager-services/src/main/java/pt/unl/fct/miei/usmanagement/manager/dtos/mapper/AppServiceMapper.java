package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;

@Mapper(builder = @Builder(disableBuilder = true))
public interface AppServiceMapper {

	AppServiceMapper MAPPER = Mappers.getMapper(AppServiceMapper.class);

	@Mapping(source = "id", target = "id")
	AppService toAppService(AppServiceDTO appServiceDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	AppServiceDTO fromAppService(AppService appService, @Context CycleAvoidingMappingContext context);
	
}
