/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package pt.unl.fct.microservicemanagement.mastermanager.manager.services;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.apps.AppEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.apps.AppServiceEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.apps.AppsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.services.SimulatedServiceMetricEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.services.SimulatedServiceMetricsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.prediction.ServiceEventPredictionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.prediction.ServiceEventPredictionRepository;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.services.ServiceRuleEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.services.ServiceRulesService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.dependencies.ServiceDependencyEntity;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServicesService {

  private final ServiceRepository services;
  private final ServiceEventPredictionRepository serviceEventPredictions;
  private final ServiceRulesService serviceRulesService;
  private final SimulatedServiceMetricsService simulatedServiceMetricsService;
  private final AppsService appsService;

  public ServicesService(ServiceRepository services, ServiceEventPredictionRepository serviceEventPredictions,
                         ServiceRulesService serviceRulesService,
                         SimulatedServiceMetricsService simulatedServiceMetricsService, @Lazy AppsService appsService) {
    this.services = services;
    this.serviceEventPredictions = serviceEventPredictions;
    this.serviceRulesService = serviceRulesService;
    this.simulatedServiceMetricsService = simulatedServiceMetricsService;
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
    assertServiceDoesntExist(service);
    log.debug("Saving service {}", ToStringBuilder.reflectionToString(service));
    return services.save(service);
  }

  public ServiceEntity updateService(String serviceName, ServiceEntity newService) {
    var service = getService(serviceName);
    log.debug("Updating service {} with {}",
        ToStringBuilder.reflectionToString(service), ToStringBuilder.reflectionToString(newService));
    log.debug("Service before copying properties: {}",
        ToStringBuilder.reflectionToString(service));
    ObjectUtils.copyValidProperties(newService, service);
    log.debug("Service after copying properties: {}",
        ToStringBuilder.reflectionToString(service));
    return services.save(service);
  }

  public void deleteService(String serviceName) {
    var service = getService(serviceName);
    services.delete(service);
  }

  public AppEntity getApp(String serviceName, String appName) {
    assertServiceExists(serviceName);
    return services.getApp(serviceName, appName).orElseThrow(() ->
        new EntityNotFoundException(AppEntity.class, "appName", appName));
  }

  public List<AppEntity> getApps(String serviceName) {
    assertServiceExists(serviceName);
    return services.getApps(serviceName);
  }

  public void addApp(String serviceName, AddServiceApp addServiceApp) {
    var service = getService(serviceName);
    var appName = addServiceApp.getName();
    var launchOrder = addServiceApp.getLaunchOrder();
    var app = appsService.getApp(appName);
    var appService = AppServiceEntity.builder()
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
    var service = getService(serviceName);
    log.info("Removing apps {}", apps);
    service.getAppServices()
        .removeIf(app -> apps.contains(app.getApp().getName()));
    services.save(service);
  }

  public List<ServiceEntity> getDependencies(String serviceName) {
    assertServiceExists(serviceName);
    return services.getDependencies(serviceName);
  }

  public List<ServiceEntity> getDependenciesByType(String serviceName, ServiceType type) {
    assertServiceExists(serviceName);
    return services.getDependenciesByType(serviceName, type);
  }

  public boolean serviceDependsOn(String serviceName, String otherServiceName) {
    assertServiceExists(serviceName);
    assertServiceExists(otherServiceName);
    return services.dependsOn(serviceName, otherServiceName);
  }

  public void addDependency(String serviceName, String dependencyName) {
    ServiceEntity service = getService(serviceName);
    ServiceEntity dependency = getService(dependencyName);
    var serviceDependency = ServiceDependencyEntity.builder().service(service).dependency(dependency).build();
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
    var service = getService(serviceName);
    log.info("Removing dependencies {}", dependencies);
    service.getDependencies().removeIf(dependency ->
        dependencies.contains(dependency.getDependency().getServiceName()));
    services.save(service);
  }

  public List<ServiceEntity> getDependents(String serviceName) {
    assertServiceExists(serviceName);
    return services.getDependents(serviceName);
  }

  public List<ServiceEventPredictionEntity> getPredictions(String serviceName) {
    assertServiceExists(serviceName);
    return services.getPredictions(serviceName);
  }

  public ServiceEventPredictionEntity addPrediction(String serviceName, ServiceEventPredictionEntity prediction) {
    var service = getService(serviceName);
    var servicePrediction = prediction.toBuilder().service(service).lastUpdate(Timestamp.from(Instant.now())).build();
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
    var service = getService(serviceName);
    service.getEventPredictions()
        .removeIf(prediction -> predictionsName.contains(prediction.getName()));
    services.save(service);
  }

  public ServiceEventPredictionEntity getEventPrediction(String serviceName,
                                                         String predictionsName) {
    assertServiceExists(serviceName);
    return services.getPrediction(serviceName, predictionsName).orElseThrow(() ->
        new EntityNotFoundException(
            ServiceEventPredictionEntity.class, "predictionsName", predictionsName)
    );
  }

  public List<ServiceRuleEntity> getRules(String serviceName) {
    assertServiceExists(serviceName);
    return services.getRules(serviceName);
  }

  public ServiceRuleEntity getRule(String serviceName, String ruleName) {
    assertServiceExists(serviceName);
    return services.getRule(serviceName, ruleName).orElseThrow(() ->
        new EntityNotFoundException(ServiceRuleEntity.class, "ruleName", ruleName)
    );
  }

  public void addRule(String serviceName, String ruleName) {
    assertServiceExists(serviceName);
    serviceRulesService.addService(ruleName, serviceName);
  }

  public void addRules(String serviceName, List<String> ruleNames) {
    assertServiceExists(serviceName);
    ruleNames.forEach(rule -> serviceRulesService.addService(rule, serviceName));
  }

  public void removeRule(String serviceName, String ruleName) {
    assertServiceExists(serviceName);
    serviceRulesService.removeService(ruleName, serviceName);
  }

  public void removeRules(String serviceName, List<String> ruleNames) {
    assertServiceExists(serviceName);
    ruleNames.forEach(rule -> serviceRulesService.removeService(rule, serviceName));
  }

  public List<SimulatedServiceMetricEntity> getSimulatedMetrics(String serviceName) {
    assertServiceExists(serviceName);
    return services.getSimulatedMetrics(serviceName);
  }

  public SimulatedServiceMetricEntity getSimulatedMetric(String serviceName, String simulatedMetricName) {
    assertServiceExists(serviceName);
    return services.getSimulatedMetric(serviceName, simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedServiceMetricEntity.class, "simulatedMetricName", simulatedMetricName)
    );
  }

  public void addSimulatedMetric(String serviceName, String simulatedMetricName) {
    assertServiceExists(serviceName);
    simulatedServiceMetricsService.addService(simulatedMetricName, serviceName);
  }

  public void addSimulatedMetrics(String serviceName, List<String> simulatedMetricNames) {
    assertServiceExists(serviceName);
    simulatedMetricNames.forEach(simulatedMetric ->
        simulatedServiceMetricsService.addService(simulatedMetric, serviceName));
  }

  public void removeSimulatedMetric(String serviceName, String simulatedMetricName) {
    assertServiceExists(serviceName);
    simulatedServiceMetricsService.removeService(simulatedMetricName, serviceName);
  }

  public void removeSimulatedMetrics(String serviceName, List<String> simulatedMetricNames) {
    assertServiceExists(serviceName);
    simulatedMetricNames.forEach(simulatedMetric ->
        simulatedServiceMetricsService.removeService(simulatedMetric, serviceName));
  }

  public int getMinReplicasByServiceName(String serviceName) {
    Integer customMinReplicas = serviceEventPredictions.getMinReplicasByServiceName(serviceName, LocalDate.now());
    if (customMinReplicas != null) {
      log.debug("Found event prediction with {} replicas", customMinReplicas);
      return customMinReplicas;
    }
    return services.getMinReplicas(serviceName);
  }

  public int getMaxReplicasByServiceName(String serviceName) {
    return services.getMaxReplicas(serviceName);
  }

  private void assertServiceExists(Long serviceId) {
    if (!services.hasService(serviceId)) {
      throw new EntityNotFoundException(ServiceEntity.class, "id", serviceId.toString());
    }
  }

  private void assertServiceExists(String serviceName) {
    if (!services.hasService(serviceName)) {
      throw new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName);
    }
  }

  private void assertServiceDoesntExist(ServiceEntity service) {
    var name = service.getServiceName();
    if (services.hasService(name)) {
      throw new DataIntegrityViolationException("Service '" + name + "' already exists");
    }
  }

  public boolean hasService(String name) {
    return services.hasService(name);
  }
}
