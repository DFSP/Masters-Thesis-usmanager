package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.MapMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceMapper {

	ServiceMapper MAPPER = Mappers.getMapper(ServiceMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	Service toService(ServiceDTO serviceDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceDTO fromService(Service service, @Context CycleAvoidingMappingContext context);

}
