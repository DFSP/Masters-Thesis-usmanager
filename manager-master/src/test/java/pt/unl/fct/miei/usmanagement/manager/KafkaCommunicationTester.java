package pt.unl.fct.miei.usmanagement.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMapper;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
public class KafkaCommunicationTester {

	private final CycleAvoidingMappingContext cycleAvoidingMappingContext = new CycleAvoidingMappingContext();
	private JacksonTester<ServiceDTO> serviceDTOJson;

	@Before
	public void setup() {
		ObjectMapper objectMapper = new ObjectMapper();
		JacksonTester.initFields(this, objectMapper);
	}

	@Test
	public void testMapDtoToEntity() {
		AppDTO appDTO = new AppDTO(1L);
		ServiceDTO serviceDto = new ServiceDTO(2L);
		AppServiceDTO appServiceDTO = new AppServiceDTO(new AppServiceKey(serviceDto.getId(), appDTO.getId()), 0, appDTO, serviceDto);
		serviceDto.setAppServices(Set.of(appServiceDTO));

		Service service = ServiceMapper.MAPPER.toService(serviceDto, cycleAvoidingMappingContext);
		assertThat(service).isNotNull();
		assertThat(service.getId()).isEqualTo(2L);

		Set<AppService> appServices = service.getAppServices();
		assertThat(appServices).hasSize(1);
		assertThat(appServices).extracting("service").containsExactly(service);
	}

	@Test
	public void testMapEntityToDto() {
		ServiceDTO serviceDTO = getServiceDTO();
		System.out.println(serviceDTO);
		assertThat(serviceDTO).isNotNull();
		assertThat(serviceDTO.getId()).isNotNull();

		Set<AppServiceDTO> appServicesDTO = serviceDTO.getAppServices();
		System.out.println(appServicesDTO);
		assertThat(appServicesDTO).hasSize(1);
		assertThat(appServicesDTO).extracting("service").contains(serviceDTO);
	}

	@Test
	public void testWriteReadJson() throws IOException {
		ServiceDTO serviceDTO = getServiceDTO();
		System.out.println(serviceDTO);
		JsonContent<ServiceDTO> jsonContent = serviceDTOJson.write(serviceDTO);
		System.out.println(jsonContent);
		String json = "{\"id\":2,\"serviceName\":\"service\",\"dockerRepository\":null,\"defaultExternalPort\":null,\"defaultInternalPort\":null,\"defaultDb\":null,\"launchCommand\":null,\"minimumReplicas\":null,\"maximumReplicas\":0,\"outputLabel\":null,\"serviceType\":null,\"environment\":null,\"volumes\":null,\"expectedMemoryConsumption\":null,\"appServices\":[{\"id\":{\"appId\":1,\"serviceId\":2},\"launchOrder\":0}]}";
		assertThat(jsonContent).isEqualToJson(json);
		File serviceDto = new File("src/test/resources/serviceDto.json");
		ServiceDTO deserializedServiceDTO = serviceDTOJson.readObject(serviceDto);
		System.out.println(serviceDTO);
		assertThat(deserializedServiceDTO.getId()).isEqualTo(serviceDTO.getId());
	}

	private ServiceDTO getServiceDTO() {
		AppService appService = AppService.builder().build();
		App app = App.builder().id(1L).name("app").appService(appService).build();
		Service service = Service.builder().id(2L).serviceName("service").build();
		appService.setId(new AppServiceKey(app.getId(), service.getId()));
		appService.setApp(app);
		appService.setService(service);
		service.setAppServices(Set.of(appService));
		app.setAppServices(Set.of(appService));

		return ServiceMapper.MAPPER.fromService(service, new CycleAvoidingMappingContext());
	}

}
