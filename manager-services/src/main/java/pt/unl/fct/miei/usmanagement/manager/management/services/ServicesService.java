/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.management.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencies;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPredictions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.services.Services;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@org.springframework.stereotype.Service
public class ServicesService {

	private final Services services;
	private final ServiceEventPredictions serviceEventPredictions;
	private final ServiceRulesService serviceRulesService;
	private final ServiceSimulatedMetricsService serviceSimulatedMetricsService;
	private final AppsService appsService;
	private final KafkaService kafkaService;

	public ServicesService(Services services, ServiceEventPredictions serviceEventPredictions,
						   ServiceRulesService serviceRulesService,
						   ServiceSimulatedMetricsService serviceSimulatedMetricsService, @Lazy AppsService appsService,
						   KafkaService kafkaService) {
		this.services = services;
		this.serviceEventPredictions = serviceEventPredictions;
		this.serviceRulesService = serviceRulesService;
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
		this.appsService = appsService;
		this.kafkaService = kafkaService;
	}

	public List<Service> getServices() {
		return services.findAll();
	}

	public Service getService(Long id) {
		return services.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Service.class, "id", id.toString()));
	}

	public Service getService(String serviceName) {
		return services.findByServiceNameIgnoreCase(serviceName).orElseThrow(() ->
			new EntityNotFoundException(Service.class, "serviceName", serviceName));
	}

	public Service getServiceAndEntities(String serviceName) {
		return services.findByServiceNameIgnoreCaseWithEntities(serviceName).orElseThrow(() ->
			new EntityNotFoundException(Service.class, "serviceName", serviceName));
	}

	public List<Service> getServicesByDockerRepository(String dockerRepository) {
		return services.findByDockerRepositoryIgnoreCase(dockerRepository);
	}

	public Service addService(Service service) {
		checkServiceDoesntExist(service);
		service = saveService(service);
		/*Service kafkaService = service;
		kafkaService.setNew(true);
		this.kafkaService.sendService(kafkaService);*/
		this.kafkaService.sendService(service);
		return service;
	}

	public Service updateService(String serviceName, Service newService) {
		Service service = getService(serviceName);
		log.info("Updating service {} with {}", ToStringBuilder.reflectionToString(service), ToStringBuilder.reflectionToString(newService));
		EntityUtils.copyValidProperties(newService, service);
		service = saveService(service);
		kafkaService.sendService(service);
		return service;
	}

	public Service addOrUpdateService(Service service) {
		Optional<Service> serviceOptional = services.findById(service.getId());
		if (serviceOptional.isPresent()) {
			Service existingService = serviceOptional.get();
			Set<ServiceRule> rules = service.getServiceRules();
			if (rules != null) {
				existingService.getServiceRules().retainAll(rules);
				existingService.getServiceRules().addAll(rules);
			}
			Set<ServiceSimulatedMetric> simulatedMetrics = service.getSimulatedServiceMetrics();
			if (simulatedMetrics != null) {
				existingService.getSimulatedServiceMetrics().retainAll(simulatedMetrics);
				existingService.getSimulatedServiceMetrics().addAll(simulatedMetrics);
			}
			Set<ServiceEventPrediction> serviceEventPredictions = service.getEventPredictions();
			if (serviceEventPredictions != null) {
				existingService.getEventPredictions().retainAll(serviceEventPredictions);
				existingService.getEventPredictions().addAll(serviceEventPredictions);
			}
			Set<AppService> appServices = service.getAppServices();
			if (appServices != null) {
				existingService.getAppServices().retainAll(appServices);
				existingService.getAppServices().addAll(appServices);
			}
			Set<ServiceDependency> dependencies = service.getDependencies();
			if (dependencies != null) {
				existingService.getDependencies().retainAll(dependencies);
				existingService.getDependencies().addAll(dependencies);
			}
			Set<ServiceDependency> dependents = service.getDependents();
			if (dependents != null) {
				existingService.getDependents().retainAll(dependents);
				existingService.getDependents().addAll(dependents);
			}
			EntityUtils.copyValidProperties(service, existingService);
			return saveService(existingService);
		}
		else {
			return saveService(service);
		}
	}

	public Service saveService(Service service) {
		log.info("Saving service {}", ToStringBuilder.reflectionToString(service));
		return services.save(service);
	}

	public void deleteService(Long id) {
		services.deleteById(id);
	}

	public void deleteService(String serviceName) {
		Service service = getService(serviceName);
		services.delete(service);
		kafkaService.sendDeleteService(service);
	}

	public App getApp(String serviceName, String appName) {
		checkServiceExists(serviceName);
		return services.getApp(serviceName, appName).orElseThrow(() ->
			new EntityNotFoundException(App.class, "appName", appName));
	}

	public List<App> getApps(String serviceName) {
		checkServiceExists(serviceName);
		return services.getApps(serviceName);
	}

	public void addApp(String serviceName, AddServiceApp addServiceApp) {
		Service service = getServiceAndEntities(serviceName);
		String appName = addServiceApp.getName();
		int launchOrder = addServiceApp.getLaunchOrder();
		App app = appsService.getApp(appName);
		AppService appService = AppService.builder().id(new AppServiceKey(app.getId(), service.getId())).app(app).service(service).launchOrder(launchOrder).build();
		service.getAppServices().add(appService);
		service = saveService(service);
		kafkaService.sendService(service);
	}

	public void addApps(String serviceName, List<AddServiceApp> addServiceApps) {
		addServiceApps.forEach(addServiceApp -> addApp(serviceName, addServiceApp));
	}

	public void removeApp(String serviceName, String app) {
		removeApps(serviceName, List.of(app));
	}

	public void removeApps(String serviceName, List<String> apps) {
		Service service = getService(serviceName);
		log.info("Removing apps {}", apps);
		service.getAppServices().removeIf(appService -> apps.contains(appsService.getApp(appService.getId().getAppId()).getName()));
		service = saveService(service);
		kafkaService.sendService(service);
	}

	public List<Service> getDependencies(String serviceName) {
		checkServiceExists(serviceName);
		return services.getDependencies(serviceName);
	}

	public List<Service> getDependenciesByType(String serviceName, ServiceTypeEnum type) {
		checkServiceExists(serviceName);
		return services.getDependenciesByType(serviceName, type);
	}

	public boolean serviceDependsOn(String serviceName, String otherServiceName) {
		checkServiceExists(serviceName);
		checkServiceExists(otherServiceName);
		return services.dependsOn(serviceName, otherServiceName);
	}

	public void addDependency(String serviceName, String dependencyName) {
		Service service = getService(serviceName);
		Service dependency = getService(dependencyName);
		ServiceDependency serviceDependency = ServiceDependency.builder().service(service).dependency(dependency).build();
		service = service.toBuilder().dependency(serviceDependency).build();
		service = saveService(service);
		kafkaService.sendService(service);
	}

	public void addDependencies(String serviceName, List<String> dependenciesNames) {
		dependenciesNames.forEach(dependencyName -> addDependency(serviceName, dependencyName));
	}

	public void removeDependency(String serviceName, String dependency) {
		removeDependencies(serviceName, List.of(dependency));
	}

	public void removeDependencies(String serviceName, List<String> dependencies) {
		Service service = getService(serviceName);
		log.info("Removing dependencies {}", dependencies);
		service.getDependencies().removeIf(dependency -> dependencies.contains(dependency.getDependency().getServiceName()));
		service = saveService(service);
		kafkaService.sendService(service);
	}

	public List<Service> getDependents(String serviceName) {
		checkServiceExists(serviceName);
		return services.getDependents(serviceName);
	}

	public List<ServiceEventPrediction> getPredictions(String serviceName) {
		checkServiceExists(serviceName);
		return services.getPredictions(serviceName);
	}

	public ServiceEventPrediction addPrediction(String serviceName, ServiceEventPrediction prediction) {
		Service service = getService(serviceName);
		ServiceEventPrediction servicePrediction = prediction.toBuilder()
			.service(service).lastUpdate(Timestamp.from(Instant.now())).build();
		service = service.toBuilder().eventPrediction(servicePrediction).build();
		service = saveService(service);
		kafkaService.sendService(service);
		return getEventPrediction(serviceName, prediction.getName());
	}

	public List<ServiceEventPrediction> addPredictions(String serviceName,
													   List<ServiceEventPrediction> predictions) {
		List<ServiceEventPrediction> predictionsEntities = new ArrayList<>(predictions.size());
		predictions.forEach(prediction -> predictionsEntities.add(addPrediction(serviceName, prediction)));
		return predictionsEntities;
	}

	public void removePrediction(String serviceName, String predictionName) {
		removePredictions(serviceName, List.of(predictionName));
	}

	public void removePredictions(String serviceName, List<String> predictionsName) {
		Service service = getService(serviceName);
		service.getEventPredictions()
			.removeIf(prediction -> predictionsName.contains(prediction.getName()));
		service = saveService(service);
		kafkaService.sendService(service);
	}

	public ServiceEventPrediction getEventPrediction(String serviceName,
													 String predictionsName) {
		checkServiceExists(serviceName);
		return services.getPrediction(serviceName, predictionsName).orElseThrow(() ->
			new EntityNotFoundException(
				ServiceEventPrediction.class, "predictionsName", predictionsName)
		);
	}

	public List<ServiceRule> getRules(String serviceName) {
		checkServiceExists(serviceName);
		return services.getRules(serviceName);
	}

	public ServiceRule getRule(String serviceName, String ruleName) {
		checkServiceExists(serviceName);
		return services.getRule(serviceName, ruleName).orElseThrow(() ->
			new EntityNotFoundException(ServiceRule.class, "ruleName", ruleName)
		);
	}

	public void addRule(String serviceName, String ruleName) {
		addRules(serviceName, List.of(ruleName));
	}

	public void addRules(String serviceName, List<String> ruleNames) {
		checkServiceExists(serviceName);
		ruleNames.forEach(rule -> serviceRulesService.addService(rule, serviceName));
	}

	public void removeRule(String serviceName, String ruleName) {
		removeRules(serviceName, List.of(ruleName));
	}

	public void removeRules(String serviceName, List<String> ruleNames) {
		checkServiceExists(serviceName);
		ruleNames.forEach(rule -> serviceRulesService.removeService(rule, serviceName));
	}

	public List<ServiceSimulatedMetric> getSimulatedMetrics(String serviceName) {
		checkServiceExists(serviceName);
		return services.getSimulatedMetrics(serviceName);
	}

	public ServiceSimulatedMetric getSimulatedMetric(String serviceName, String simulatedMetricName) {
		checkServiceExists(serviceName);
		return services.getSimulatedMetric(serviceName, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ServiceSimulatedMetric.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String serviceName, String simulatedMetricName) {
		addSimulatedMetrics(serviceName, List.of(simulatedMetricName));
	}

	public void addSimulatedMetrics(String serviceName, List<String> simulatedMetricNames) {
		checkServiceExists(serviceName);
		simulatedMetricNames.forEach(simulatedMetric ->
			serviceSimulatedMetricsService.addService(simulatedMetric, serviceName));
	}

	public void removeSimulatedMetric(String serviceName, String simulatedMetricName) {
		checkServiceExists(serviceName);
		serviceSimulatedMetricsService.removeService(simulatedMetricName, serviceName);
	}

	public void removeSimulatedMetrics(String serviceName, List<String> simulatedMetricNames) {
		checkServiceExists(serviceName);
		simulatedMetricNames.forEach(simulatedMetric ->
			serviceSimulatedMetricsService.removeService(simulatedMetric, serviceName));
	}

	public int getMinimumReplicasByServiceName(String serviceName) {
		Integer customMinimumReplicas = serviceEventPredictions.getMinimumReplicasByServiceName(serviceName, LocalDate.now());
		if (customMinimumReplicas != null) {
			log.info("Found event prediction with {} replicas", customMinimumReplicas);
			return customMinimumReplicas;
		}
		return services.getMinimumReplicas(serviceName);
	}

	public int getMaximumReplicasByServiceName(String serviceName) {
		return services.getMaximumReplicas(serviceName);
	}

	public double getExpectedMemoryConsumption(String serviceName) {
		Double memoryConsumption = getService(serviceName).getExpectedMemoryConsumption();
		return memoryConsumption == null ? 0 : memoryConsumption;
	}

	private void checkServiceExists(Long serviceId) {
		if (!services.hasService(serviceId)) {
			throw new EntityNotFoundException(Service.class, "id", serviceId.toString());
		}
	}

	private void checkServiceExists(String serviceName) {
		if (!services.hasService(serviceName)) {
			throw new EntityNotFoundException(Service.class, "serviceName", serviceName);
		}
	}

	private void checkServiceDoesntExist(Service service) {
		String name = service.getServiceName();
		if (services.hasService(name)) {
			throw new DataIntegrityViolationException("Service '" + name + "' already exists");
		}
	}

	public boolean hasService(String name) {
		return services.hasService(name);
	}

}
