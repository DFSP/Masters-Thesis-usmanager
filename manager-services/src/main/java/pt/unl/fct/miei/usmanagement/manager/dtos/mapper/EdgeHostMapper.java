package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.EdgeHostDTO;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;

@Mapper(builder = @Builder(disableBuilder = true))
public interface EdgeHostMapper {

	EdgeHostMapper MAPPER = Mappers.getMapper(EdgeHostMapper.class);

	@Mapping(source = "id", target = "id")
	EdgeHost toEdgeHost(EdgeHostDTO edgeHostDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	EdgeHostDTO fromEdgeHost(EdgeHost edgeHost, @Context CycleAvoidingMappingContext context);

}
