package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.CloudHostDTO;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;

@Mapper(builder = @Builder(disableBuilder = true))
public interface CloudHostMapper {

	CloudHostMapper MAPPER = Mappers.getMapper(CloudHostMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	CloudHost toCloudHost(CloudHostDTO cloudHostDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	CloudHostDTO fromCloudHost(CloudHost cloudHost, @Context CycleAvoidingMappingContext context);

}
