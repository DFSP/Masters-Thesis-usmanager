package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ValueModeDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.WorkerManagerDTO;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

@Mapper(builder = @Builder(disableBuilder = true))
public interface WorkerManagerMapper {

	WorkerManagerMapper MAPPER = Mappers.getMapper(WorkerManagerMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	WorkerManager toWorkerManager(WorkerManagerDTO workerManagerDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	WorkerManagerDTO fromWorkerManager(WorkerManager workerManager, @Context CycleAvoidingMappingContext context);

}
