package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.FieldDTO;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;

@Mapper(builder = @Builder(disableBuilder = true))
public interface FieldMapper {

	FieldMapper MAPPER = Mappers.getMapper(FieldMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	Field toField(FieldDTO fieldDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	FieldDTO fromField(Field field, @Context CycleAvoidingMappingContext context);

}
