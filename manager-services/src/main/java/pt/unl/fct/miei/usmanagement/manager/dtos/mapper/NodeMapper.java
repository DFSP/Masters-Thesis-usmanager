package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.NodeDTO;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;

@Mapper(builder = @Builder(disableBuilder = true))
public interface NodeMapper {

	NodeMapper MAPPER = Mappers.getMapper(NodeMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	Node toNode(NodeDTO nodeDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	NodeDTO fromNode(Node node, @Context CycleAvoidingMappingContext context);

}
