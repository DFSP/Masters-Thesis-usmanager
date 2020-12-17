package pt.unl.fct.miei.usmanagement.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyKey;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDependencyDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMapper;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SuppressWarnings("unused")
public class KafkaCommunicationTester {

	private final CycleAvoidingMappingContext cycleAvoidingMappingContext = new CycleAvoidingMappingContext();
	private JacksonTester<ServiceDTO> serviceDTOJson;
	private JacksonTester<Set<ServiceDependencyDTO>> serviceDependenciesDTOJson;

	@Before
	public void setup() {
		ObjectMapper objectMapper = new ObjectMapper();
		JacksonTester.initFields(this, objectMapper);
	}

	@Test
	public void testServiceDTO() throws IOException {
		File serviceDTOJsonFile = new File("src/test/resources/media-text.json");
		ServiceDTO serviceDTO = serviceDTOJson.readObject(serviceDTOJsonFile);
		Service service = ServiceMapper.MAPPER.toService(serviceDTO, cycleAvoidingMappingContext);
		log.info("{}", service.toString());
	}

	@Test
	public void testServiceDependency() throws IOException {
		File serviceDTOJsonFile = new File("src/test/resources/media-movie-id.json");
		ServiceDTO serviceDTO = serviceDTOJson.readObject(serviceDTOJsonFile);
		File dependenciesJsonFile = new File("src/test/resources/media-movie-id-dependencies.json");
		Set<ServiceDependencyDTO> dependenciesDTO = serviceDependenciesDTOJson.readObject(dependenciesJsonFile);
		serviceDTO.setDependencies(dependenciesDTO);
		Service service = ServiceMapper.MAPPER.toService(serviceDTO, cycleAvoidingMappingContext);
		assertThat(service).isNotNull();

		assertThat(service.getDependencies()).hasSize(4);
		serviceDTO = ServiceMapper.MAPPER.fromService(service, cycleAvoidingMappingContext);
		assertThat(serviceDTO).isNotNull();
		assertThat(serviceDTO.getDependencies()).hasSize(4);
		log.info("{}", serviceDTO.toString());
	}

	@Test
	public void testMapDtoToEntity() {
		ServiceDTO serviceDto = new ServiceDTO(1L);
		ServiceDTO dependencyDto = new ServiceDTO(2L);
		ServiceDependencyKey key = new ServiceDependencyKey(serviceDto.getId(), dependencyDto.getId());
		ServiceDependencyDTO serviceDependencyDto = new ServiceDependencyDTO(key, serviceDto, dependencyDto);
		serviceDto.setDependencies(Set.of(serviceDependencyDto));
		Service service = ServiceMapper.MAPPER.toService(serviceDto, cycleAvoidingMappingContext);
		assertThat(service).isNotNull();
		assertThat(service.getId()).isEqualTo(1L);
		Set<ServiceDependency> serviceDependencies = service.getDependencies();
		assertThat(serviceDependencies).hasSize(1);
		assertThat(serviceDependencies).extracting("service").containsExactly(service);
	}

	@Test
	public void testMapEntityToDto() {
		ServiceDTO serviceDTO = getServiceDTO();
		System.out.println(serviceDTO);
		assertThat(serviceDTO).isNotNull();
		assertThat(serviceDTO.getId()).isNotNull();

		Set<ServiceDependencyDTO> serviceDependenciesDTO = serviceDTO.getDependencies();
		System.out.println(serviceDependenciesDTO);
		assertThat(serviceDependenciesDTO).hasSize(1);
		assertThat(serviceDependenciesDTO).extracting("service").contains(serviceDTO);
	}

	@Test
	public void testWriteReadJson() throws IOException {
		ServiceDTO serviceDTO = getServiceDTO();
		System.out.println(serviceDTO);
		JsonContent<ServiceDTO> jsonContent = serviceDTOJson.write(serviceDTO);
		System.out.println(jsonContent);
		String json = "{\"id\":1,\"serviceName\":\"service\",\"dockerRepository\":null,\"defaultExternalPort\":null,\"defaultInternalPort\":null,\"defaultDb\":null,\"launchCommand\":null,\"minimumReplicas\":null,\"maximumReplicas\":0,\"outputLabel\":null,\"serviceType\":null,\"environment\":null,\"volumes\":null,\"expectedMemoryConsumption\":null,\"dependencies\":[{\"id\":{\"serviceId\":1,\"dependencyId\":2},\"service\":1,\"dependency\":{\"id\":2,\"serviceName\":\"dependency\",\"dockerRepository\":null,\"defaultExternalPort\":null,\"defaultInternalPort\":null,\"defaultDb\":null,\"launchCommand\":null,\"minimumReplicas\":null,\"maximumReplicas\":0,\"outputLabel\":null,\"serviceType\":null,\"environment\":null,\"volumes\":null,\"expectedMemoryConsumption\":null,\"dependencies\":[],\"dependents\":[],\"eventPredictions\":[]}}],\"dependents\":[],\"eventPredictions\":[]}";
		assertThat(jsonContent).isEqualToJson(json);
		File serviceDto = new File("src/test/resources/serviceDto.json");
		ServiceDTO deserializedServiceDTO = serviceDTOJson.readObject(serviceDto);
		System.out.println(serviceDTO);
		assertThat(deserializedServiceDTO.getId()).isEqualTo(serviceDTO.getId());
	}

	private ServiceDTO getServiceDTO() {
		Service service = Service.builder().id(1L).serviceName("service").build();
		Service dependency = Service.builder().id(2L).serviceName("dependency").build();
		ServiceDependency serviceDependency = ServiceDependency.builder()
			.id(new ServiceDependencyKey(service.getId(), dependency.getId()))
			.service(service)
			.dependency(dependency)
			.build();
		service.setDependencies(Set.of(serviceDependency));

		return ServiceMapper.MAPPER.fromService(service, new CycleAvoidingMappingContext());
	}

}
