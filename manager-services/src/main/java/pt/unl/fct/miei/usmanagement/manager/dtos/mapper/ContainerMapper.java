package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ContainerMapper {

	ContainerMapper MAPPER = Mappers.getMapper(ContainerMapper.class);

	@Mapping(source = "id", target = "id")
	Container toContainer(ContainerDTO containerDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ContainerDTO fromContainer(Container container, @Context CycleAvoidingMappingContext context);

}
