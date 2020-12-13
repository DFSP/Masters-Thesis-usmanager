package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.OperatorDTO;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;

@Mapper(builder = @Builder(disableBuilder = true))
public interface OperatorMapper {

	OperatorMapper MAPPER = Mappers.getMapper(OperatorMapper.class);

	@Mapping(source = "id", target = "id")
	Operator toOperator(OperatorDTO operatorDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	OperatorDTO fromOperator(Operator operator, @Context CycleAvoidingMappingContext context);

}
