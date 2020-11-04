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
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPredictionEntity;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPredictionRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceRepository;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceType;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyEntity;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ServicesService {

	private final ServiceRepository services;
	private final ServiceEventPredictionRepository serviceEventPredictions;
	private final ServiceRulesService serviceRulesService;
	private final ServiceSimulatedMetricsService serviceSimulatedMetricsService;
	private final AppsService appsService;

	public ServicesService(ServiceRepository services, ServiceEventPredictionRepository serviceEventPredictions,
						   ServiceRulesService serviceRulesService,
						   ServiceSimulatedMetricsService serviceSimulatedMetricsService, @Lazy AppsService appsService) {
		this.services = services;
		this.serviceEventPredictions = serviceEventPredictions;
		this.serviceRulesService = serviceRulesService;
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
		this.appsService = appsService;
	}

	public List<ServiceEntity> getServices() {
		return services.findAll();
	}

	public ServiceEntity getService(Long id) {
		return services.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ServiceEntity.class, "id", id.toString()));
	}

	public ServiceEntity getService(String serviceName) {
		return services.findByServiceNameIgnoreCase(serviceName).orElseThrow(() ->
			new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName));
	}

	public List<ServiceEntity> getServicesByDockerRepository(String dockerRepository) {
		return services.findByDockerRepositoryIgnoreCase(dockerRepository);
	}

	public ServiceEntity addService(ServiceEntity service) {
		checkServiceDoesntExist(service);
		log.info("Saving service {}", ToStringBuilder.reflectionToString(service));
		return services.save(service);
	}

	public ServiceEntity updateService(String serviceName, ServiceEntity newService) {
		ServiceEntity service = getService(serviceName);
		log.info("Updating service {} with {}",
			ToStringBuilder.reflectionToString(service), ToStringBuilder.reflectionToString(newService));
		log.info("Service before copying properties: {}",
			ToStringBuilder.reflectionToString(service));
		ObjectUtils.copyValidProperties(newService, service);
		log.info("Service after copying properties: {}",
			ToStringBuilder.reflectionToString(service));
		return services.save(service);
	}

	public void deleteService(String serviceName) {
		ServiceEntity service = getService(serviceName);
		services.delete(service);
	}

	public AppEntity getApp(String serviceName, String appName) {
		checkServiceExists(serviceName);
		return services.getApp(serviceName, appName).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "appName", appName));
	}

	public List<AppEntity> getApps(String serviceName) {
		checkServiceExists(serviceName);
		return services.getApps(serviceName);
	}

	public void addApp(String serviceName, AddServiceApp addServiceApp) {
		ServiceEntity service = getService(serviceName);
		String appName = addServiceApp.getName();
		int launchOrder = addServiceApp.getLaunchOrder();
		AppEntity app = appsService.getApp(appName);
		AppServiceEntity appService = AppServiceEntity.builder()
			.app(app)
			.service(service)
			.launchOrder(launchOrder)
			.build();
		service = service.toBuilder().appService(appService).build();
		services.save(service);
	}

	public void addApps(String serviceName, List<AddServiceApp> addServiceApps) {
		addServiceApps.forEach(addServiceApp -> addApp(serviceName, addServiceApp));
	}

	public void removeApp(String serviceName, String app) {
		removeApps(serviceName, List.of(app));
	}

	public void removeApps(String serviceName, List<String> apps) {
		ServiceEntity service = getService(serviceName);
		log.info("Removing apps {}", apps);
		service.getAppServices()
			.removeIf(app -> apps.contains(app.getApp().getName()));
		services.save(service);
	}

	public List<ServiceEntity> getDependencies(String serviceName) {
		checkServiceExists(serviceName);
		return services.getDependencies(serviceName);
	}

	public List<ServiceEntity> getDependenciesByType(String serviceName, ServiceType type) {
		checkServiceExists(serviceName);
		return services.getDependenciesByType(serviceName, type);
	}

	public boolean serviceDependsOn(String serviceName, String otherServiceName) {
		checkServiceExists(serviceName);
		checkServiceExists(otherServiceName);
		return services.dependsOn(serviceName, otherServiceName);
	}

	public void addDependency(String serviceName, String dependencyName) {
		ServiceEntity service = getService(serviceName);
		ServiceEntity dependency = getService(dependencyName);
		ServiceDependencyEntity serviceDependency = ServiceDependencyEntity.builder().service(service).dependency(dependency).build();
		service = service.toBuilder().dependency(serviceDependency).build();
		services.save(service);
	}

	public void addDependencies(String serviceName, List<String> dependenciesNames) {
		dependenciesNames.forEach(dependencyName -> addDependency(serviceName, dependencyName));
	}

	public void removeDependency(String serviceName, String dependency) {
		removeDependencies(serviceName, List.of(dependency));
	}

	public void removeDependencies(String serviceName, List<String> dependencies) {
		ServiceEntity service = getService(serviceName);
		log.info("Removing dependencies {}", dependencies);
		service.getDependencies().removeIf(dependency ->
			dependencies.contains(dependency.getDependency().getServiceName()));
		services.save(service);
	}

	public List<ServiceEntity> getDependents(String serviceName) {
		checkServiceExists(serviceName);
		return services.getDependents(serviceName);
	}

	public List<ServiceEventPredictionEntity> getPredictions(String serviceName) {
		checkServiceExists(serviceName);
		return services.getPredictions(serviceName);
	}

	public ServiceEventPredictionEntity addPrediction(String serviceName, ServiceEventPredictionEntity prediction) {
		ServiceEntity service = getService(serviceName);
		ServiceEventPredictionEntity servicePrediction = prediction.toBuilder()
			.service(service).lastUpdate(Timestamp.from(Instant.now())).build();
		service = service.toBuilder().eventPrediction(servicePrediction).build();
		services.save(service);
		return getEventPrediction(serviceName, prediction.getName());
	}

	public List<ServiceEventPredictionEntity> addPredictions(String serviceName,
															 List<ServiceEventPredictionEntity> predictions) {
		List<ServiceEventPredictionEntity> predictionsEntities = new ArrayList<>(predictions.size());
		predictions.forEach(prediction -> predictionsEntities.add(addPrediction(serviceName, prediction)));
		return predictionsEntities;
	}

	public void removePrediction(String serviceName, String predictionName) {
		removePredictions(serviceName, List.of(predictionName));
	}

	public void removePredictions(String serviceName, List<String> predictionsName) {
		ServiceEntity service = getService(serviceName);
		service.getEventPredictions()
			.removeIf(prediction -> predictionsName.contains(prediction.getName()));
		services.save(service);
	}

	public ServiceEventPredictionEntity getEventPrediction(String serviceName,
														   String predictionsName) {
		checkServiceExists(serviceName);
		return services.getPrediction(serviceName, predictionsName).orElseThrow(() ->
			new EntityNotFoundException(
				ServiceEventPredictionEntity.class, "predictionsName", predictionsName)
		);
	}

	public List<ServiceRuleEntity> getRules(String serviceName) {
		checkServiceExists(serviceName);
		return services.getRules(serviceName);
	}

	public ServiceRuleEntity getRule(String serviceName, String ruleName) {
		checkServiceExists(serviceName);
		return services.getRule(serviceName, ruleName).orElseThrow(() ->
			new EntityNotFoundException(ServiceRuleEntity.class, "ruleName", ruleName)
		);
	}

	public void addRule(String serviceName, String ruleName) {
		checkServiceExists(serviceName);
		serviceRulesService.addService(ruleName, serviceName);
	}

	public void addRules(String serviceName, List<String> ruleNames) {
		checkServiceExists(serviceName);
		ruleNames.forEach(rule -> serviceRulesService.addService(rule, serviceName));
	}

	public void removeRule(String serviceName, String ruleName) {
		checkServiceExists(serviceName);
		serviceRulesService.removeService(ruleName, serviceName);
	}

	public void removeRules(String serviceName, List<String> ruleNames) {
		checkServiceExists(serviceName);
		ruleNames.forEach(rule -> serviceRulesService.removeService(rule, serviceName));
	}

	public List<ServiceSimulatedMetricEntity> getSimulatedMetrics(String serviceName) {
		checkServiceExists(serviceName);
		return services.getSimulatedMetrics(serviceName);
	}

	public ServiceSimulatedMetricEntity getSimulatedMetric(String serviceName, String simulatedMetricName) {
		checkServiceExists(serviceName);
		return services.getSimulatedMetric(serviceName, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ServiceSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String serviceName, String simulatedMetricName) {
		checkServiceExists(serviceName);
		serviceSimulatedMetricsService.addService(simulatedMetricName, serviceName);
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

	private void checkServiceExists(Long serviceId) {
		if (!services.hasService(serviceId)) {
			throw new EntityNotFoundException(ServiceEntity.class, "id", serviceId.toString());
		}
	}

	private void checkServiceExists(String serviceName) {
		if (!services.hasService(serviceName)) {
			throw new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName);
		}
	}

	private void checkServiceDoesntExist(ServiceEntity service) {
		String name = service.getServiceName();
		if (services.hasService(name)) {
			throw new DataIntegrityViolationException("Service '" + name + "' already exists");
		}
	}

	public boolean hasService(String name) {
		return services.hasService(name);
	}
}
