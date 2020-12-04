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

import java.util.HashSet;
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
	private Set<Long> appServices;
	private Set<Long> dependencies;
	private Set<Long> dependents;
	private Set<Long> eventPredictions;
	private Set<Long> serviceRules;
	private Set<Long> simulatedServiceMetrics;

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
		Set<AppService> appServices = service.getAppServices();
		if (appServices != null) {
			this.appServices = new HashSet<>(appServices.size());
			appServices.forEach(appService -> this.appServices.add(appService.getId()));
		}
		Set<ServiceDependency> dependencies = service.getDependencies();
		if (dependencies != null) {
			this.dependencies = new HashSet<>(dependencies.size());
			dependencies.forEach(dependency -> this.dependencies.add(dependency.getDependency().getId()));
		}
		Set<ServiceDependency> dependents = service.getDependents();
		if (dependents != null) {
			this.dependents = new HashSet<>(dependents.size());
			dependents.forEach(dependent -> this.dependents.add(dependent.getDependency().getId()));
		}
		Set<ServiceEventPrediction> eventPredictions = service.getEventPredictions();
		if (eventPredictions != null) {
			this.eventPredictions = new HashSet<>(eventPredictions.size());
			eventPredictions.forEach(eventPrediction -> this.eventPredictions.add(eventPrediction.getId()));
		}
		Set<ServiceRule> serviceRules = service.getServiceRules();
		if (serviceRules != null) {
			this.serviceRules = new HashSet<>(serviceRules.size());
			serviceRules.forEach(serviceRule -> this.serviceRules.add(serviceRule.getId()));
		}
		Set<ServiceSimulatedMetric> simulatedServiceMetrics = service.getSimulatedServiceMetrics();
		if (simulatedServiceMetrics != null) {
			this.simulatedServiceMetrics = new HashSet<>(simulatedServiceMetrics.size());
			simulatedServiceMetrics.forEach(simulatedServiceMetric -> this.simulatedServiceMetrics.add(simulatedServiceMetric.getId()));
		}
	}
}
