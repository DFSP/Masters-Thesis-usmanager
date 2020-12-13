package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostMonitoringLogMapper {

	HostMonitoringLogMapper MAPPER = Mappers.getMapper(HostMonitoringLogMapper.class);

	@Mapping(source = "id", target = "id")
	HostMonitoringLog toHostMonitoringLog(HostMonitoringLogDTO hostMonitoringLogDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostMonitoringLogDTO fromHostMonitoringLog(HostMonitoringLog hostMonitoringLog, @Context CycleAvoidingMappingContext context);
	
}
