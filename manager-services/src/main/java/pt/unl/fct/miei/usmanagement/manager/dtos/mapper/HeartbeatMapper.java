package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HeartbeatDTO;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeat;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HeartbeatMapper {

	HeartbeatMapper MAPPER = Mappers.getMapper(HeartbeatMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	Heartbeat toHeartbeat(HeartbeatDTO heartbeatDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HeartbeatDTO fromHeartbeat(Heartbeat heartbeat, @Context CycleAvoidingMappingContext context);

}
