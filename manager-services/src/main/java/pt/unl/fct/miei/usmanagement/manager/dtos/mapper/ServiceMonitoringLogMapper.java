package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceMonitoringLogMapper {

	ServiceMonitoringLogMapper MAPPER = Mappers.getMapper(ServiceMonitoringLogMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	ServiceMonitoringLog toServiceMonitoringLog(ServiceMonitoringLogDTO serviceMonitoringLogDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceMonitoringLogDTO fromServiceMonitoringLog(ServiceMonitoringLog serviceMonitoringLog, @Context CycleAvoidingMappingContext context);
	
}
