package pt.unl.fct.miei.usmanagement.manager;

import org.junit.Test;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMapper;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityDtoMappingTester {

	private final CycleAvoidingMappingContext cycleAvoidingMappingContext = new CycleAvoidingMappingContext();

	@Test
	public void testMapDtoToEntity() {
		AppDTO appDTO = new AppDTO(1L);
		ServiceDTO serviceDto = new ServiceDTO(2L);
		AppServiceDTO appServiceDTO = new AppServiceDTO(new AppServiceKey(serviceDto.getId(), appDTO.getId()), 0, appDTO, serviceDto);
		serviceDto.setAppServices(Set.of(appServiceDTO));

		/*App app = ServiceMapper.MAPPER.toApp(appDTO, cycleAvoidingMappingContext);
		assertThat(app).isNotNull();
		assertThat(app.getId()).isEqualTo(1L);*/

		Service service = ServiceMapper.MAPPER.toService(serviceDto, cycleAvoidingMappingContext);
		assertThat(service).isNotNull();
		assertThat(service.getId()).isEqualTo(2L);

		Set<AppService> appServices = service.getAppServices();
		assertThat(appServices).hasSize(1);
		assertThat(appServices).extracting("service").containsExactly(service);
	}

	@Test
	public void testMapEntityToDto() {
		AppService appService = AppService.builder().build();
		App app = App.builder().id(1L).name("app").appService(appService).build();
		Service service = Service.builder().id(2L).serviceName("service").appService(appService).build();
		appService.setId(new AppServiceKey(app.getId(), service.getId()));
		appService.setApp(app);
		appService.setService(service);

		/*AppDTO appDTO = AppMapper.MAPPER.fromApp(app, new CycleAvoidingMappingContext());
		assertThat(appDTO).isNotNull();
		assertThat(appDTO.getId()).isNotNull();*/

		ServiceDTO serviceDTO = ServiceMapper.MAPPER.fromService(service, new CycleAvoidingMappingContext());
		System.out.println(serviceDTO);
		assertThat(serviceDTO).isNotNull();
		assertThat(serviceDTO.getId()).isNotNull();

		Set<AppServiceDTO> appServicesDTO = serviceDTO.getAppServices();
		System.out.println(appServicesDTO);
		assertThat(appServicesDTO).hasSize(1);
		assertThat(appServicesDTO).extracting("service").contains(service);
	}
}
