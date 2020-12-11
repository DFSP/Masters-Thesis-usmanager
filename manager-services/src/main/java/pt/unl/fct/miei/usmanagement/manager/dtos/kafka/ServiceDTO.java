package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceDTO {

	private Long id;
	private String serviceName;
	private String dockerRepository;
	private Integer defaultExternalPort;
	private Integer defaultInternalPort;
	private String defaultDb;
	private String launchCommand;
	private Integer minimumReplicas;
	private Integer maximumReplicas;
	private String outputLabel;
	private ServiceTypeEnum serviceType;
	private Set<String> environment;
	private Set<String> volumes;
	private Double expectedMemoryConsumption;
	private Set<AppServiceDTO> appServices;
	private Set<ServiceDependencyDTO> dependencies;
	private Set<ServiceDependencyDTO> dependents;
	private Set<ServiceEventPredictionDTO> eventPredictions;
	private Set<ServiceRuleDTO> serviceRules;
	private Set<ServiceSimulatedMetricDTO> simulatedServiceMetrics;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceDTO(Long id) {
		this.id = id;
	}

	public ServiceDTO(Service service) {
		this.id = service.getId();
		this.serviceName = service.getServiceName();
		this.dockerRepository = service.getDockerRepository();
		this.defaultExternalPort = service.getDefaultExternalPort();
		this.defaultInternalPort = service.getDefaultInternalPort();
		this.defaultDb = service.getDefaultDb();
		this.launchCommand = service.getLaunchCommand();
		this.minimumReplicas = service.getMinimumReplicas();
		this.maximumReplicas = service.getMaximumReplicas();
		this.outputLabel = service.getOutputLabel();
		this.serviceType = service.getServiceType();
		this.environment = service.getEnvironment();
		this.volumes = service.getVolumes();
		this.expectedMemoryConsumption = service.getExpectedMemoryConsumption();
		this.appServices = service.getAppServices().stream().map(AppServiceDTO::new).collect(Collectors.toSet());
		this.dependencies = service.getDependencies().stream().map(ServiceDependencyDTO::new).collect(Collectors.toSet());
		this.dependents = service.getDependents().stream().map(ServiceDependencyDTO::new).collect(Collectors.toSet());
		this.eventPredictions = service.getEventPredictions().stream().map(ServiceEventPredictionDTO::new).collect(Collectors.toSet());
		this.serviceRules = service.getServiceRules().stream().map(ServiceRuleDTO::new).collect(Collectors.toSet());
		this.simulatedServiceMetrics = service.getSimulatedServiceMetrics().stream().map(ServiceSimulatedMetricDTO::new).collect(Collectors.toSet());
		/*this.isNew = service.isNew();*/
	}

	@JsonIgnore
	public Service toEntity() {
		Service service = Service.builder()
			.id(id)
			.serviceName(serviceName)
			.dockerRepository(dockerRepository)
			.defaultExternalPort(defaultExternalPort)
			.defaultInternalPort(defaultInternalPort)
			.defaultDb(defaultDb)
			.launchCommand(launchCommand)
			.minimumReplicas(minimumReplicas)
			.maximumReplicas(maximumReplicas)
			.outputLabel(outputLabel)
			.serviceType(serviceType)
			.environment(environment)
			.volumes(volumes)
			.expectedMemoryConsumption(expectedMemoryConsumption)
			.appServices(appServices != null ? appServices.stream().map(AppServiceDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.dependencies(dependencies != null ? dependencies.stream().map(ServiceDependencyDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.dependents(dependents != null ? dependents.stream().map(ServiceDependencyDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.eventPredictions(eventPredictions != null ? eventPredictions.stream().map(ServiceEventPredictionDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.serviceRules(serviceRules != null ? serviceRules.stream().map(ServiceRuleDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.simulatedServiceMetrics(simulatedServiceMetrics != null ? simulatedServiceMetrics.stream().map(ServiceSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*service.setNew(isNew);*/
		return service;
	}
}
