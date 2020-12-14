package pt.unl.fct.miei.usmanagement.manager.dtos.mappers;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.dtos.ServiceDependencyDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ServiceDependencyMapper {

	ServiceDependencyMapper MAPPER = Mappers.getMapper(ServiceDependencyMapper.class);

	default Set<String> mapStringToSet(String str) {
		return str == null ? new HashSet<>() : Arrays.stream(str.split(" ")).collect(Collectors.toSet());
	}

	default String mapSetToString(Set<String> set) {
		return set == null ? null : String.join(" ", set);
	}

	@Mapping(source = "id", target = "id")
	ServiceDependency toServiceDependency(ServiceDependencyDTO serviceDependencyDTO, @Context CycleAvoidingMappingContext context);

	@Mapping(source = "id", target = "id")
	List<ServiceDependency> toServiceDependencies(List<ServiceDependencyDTO> serviceDependencyDTOs, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ServiceDependencyDTO fromServiceDependency(ServiceDependency serviceDependency, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	List<ServiceDependencyDTO> fromServiceDependencies(List<ServiceDependency> serviceDependencies, @Context CycleAvoidingMappingContext context);
}
