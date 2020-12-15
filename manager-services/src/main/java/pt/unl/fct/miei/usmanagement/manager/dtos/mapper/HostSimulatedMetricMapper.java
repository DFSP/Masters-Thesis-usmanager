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
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;

@Mapper(builder = @Builder(disableBuilder = true))
public interface HostSimulatedMetricMapper {

	HostSimulatedMetricMapper MAPPER = Mappers.getMapper(HostSimulatedMetricMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	HostSimulatedMetric toHostSimulatedMetric(HostSimulatedMetricDTO hostSimulatedMetricDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	HostSimulatedMetricDTO fromHostSimulatedMetric(HostSimulatedMetric hostSimulatedMetric, @Context CycleAvoidingMappingContext context);

}
