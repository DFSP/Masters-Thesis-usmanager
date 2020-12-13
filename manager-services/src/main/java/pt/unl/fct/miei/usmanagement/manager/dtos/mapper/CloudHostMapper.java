package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.CloudHostDTO;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;

@Mapper(builder = @Builder(disableBuilder = true))
public interface CloudHostMapper {

	CloudHostMapper MAPPER = Mappers.getMapper(CloudHostMapper.class);

	@Mapping(source = "id", target = "id")
	CloudHost toCloudHost(CloudHostDTO cloudHostDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	CloudHostDTO fromCloudHost(CloudHost cloudHost, @Context CycleAvoidingMappingContext context);
	
}
