package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceEventDTO;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceEventMapper {

	ServiceEventMapper MAPPER = Mappers.getMapper(ServiceEventMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	ServiceEvent toServiceEvent(ServiceEventDTO serviceEventDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceEventDTO fromServiceEvent(ServiceEvent serviceEvent, @Context CycleAvoidingMappingContext context);

}
