package pt.unl.fct.miei.usmanagement.manager.dtos.mappers;


import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceMapper {

	ServiceMapper MAPPER = Mappers.getMapper(ServiceMapper.class);

	default Set<String> mapStringToSet(String str) {
		return str == null || str.isEmpty() ? new HashSet<>() : Arrays.stream(str.split(" ")).collect(Collectors.toSet());
	}

	default String mapSetToString(Set<String> set) {
		return set == null || set.isEmpty() ? null : String.join(" ", set);
	}

	@Mapping(source = "id", target = "id")
	Service toService(ServiceDTO serviceDTO, @Context CycleAvoidingMappingContext context);

	@Mapping(source = "id", target = "id")
	List<Service> toServices(List<ServiceDTO> serviceDTOs, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceDTO fromService(Service service, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	List<ServiceDTO> fromServices(List<Service> services, @Context CycleAvoidingMappingContext context);
}
