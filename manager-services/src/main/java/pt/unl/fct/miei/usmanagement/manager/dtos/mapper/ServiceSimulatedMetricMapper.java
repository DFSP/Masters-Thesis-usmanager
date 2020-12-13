package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceSimulatedMetricMapper {

	ServiceSimulatedMetricMapper MAPPER = Mappers.getMapper(ServiceSimulatedMetricMapper.class);

	@Mapping(source = "id", target = "id")
	ServiceSimulatedMetric toServiceSimulatedMetric(ServiceSimulatedMetricDTO serviceSimulatedMetricDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceSimulatedMetricDTO fromServiceSimulatedMetric(ServiceSimulatedMetric serviceSimulatedMetric, @Context CycleAvoidingMappingContext context);

}
