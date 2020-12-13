package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ContainerSimulatedMetricMapper {

	ContainerSimulatedMetricMapper MAPPER = Mappers.getMapper(ContainerSimulatedMetricMapper.class);

	@Mapping(source = "id", target = "id")
	ContainerSimulatedMetric toContainerSimulatedMetric(ContainerSimulatedMetricDTO containerSimulatedMetricDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ContainerSimulatedMetricDTO fromContainerSimulatedMetric(ContainerSimulatedMetric containerSimulatedMetric, @Context CycleAvoidingMappingContext context);

}
