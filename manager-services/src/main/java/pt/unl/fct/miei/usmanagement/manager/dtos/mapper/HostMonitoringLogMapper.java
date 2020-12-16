package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostMonitoringLogMapper {

	HostMonitoringLogMapper MAPPER = Mappers.getMapper(HostMonitoringLogMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	HostMonitoringLog toHostMonitoringLog(HostMonitoringLogDTO hostMonitoringLogDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostMonitoringLogDTO fromHostMonitoringLog(HostMonitoringLog hostMonitoringLog, @Context CycleAvoidingMappingContext context);

}
