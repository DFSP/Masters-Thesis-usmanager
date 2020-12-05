package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

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

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceMessage {

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
	private Set<AppService> appServices;
	private Set<ServiceDependency> dependencies;
	private Set<ServiceDependency> dependents;
	private Set<ServiceEventPrediction> eventPredictions;
	private Set<ServiceRule> serviceRules;
	private Set<ServiceSimulatedMetric> simulatedServiceMetrics;

	public ServiceMessage(Service service) {
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
		this.dependencies = service.getDependencies();
		this.dependents = service.getDependents();
		this.eventPredictions = service.getEventPredictions();
		this.serviceRules = service.getServiceRules();
		this.simulatedServiceMetrics = service.getSimulatedServiceMetrics();
	}

	public Service get() {
		return Service.builder()
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
			.dependencies(dependencies)
			.dependents(dependents)
			.eventPredictions(eventPredictions)
			.serviceRules(serviceRules)
			.simulatedServiceMetrics(simulatedServiceMetrics)
			.build();
	}
}
