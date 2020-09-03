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

package pt.unl.fct.miei.usmanagement.manager.worker.management.services;

import java.time.LocalDate;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.prediction.ServiceEventPredictionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.prediction.ServiceEventPredictionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceRepository;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceType;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;

@Slf4j
@Service
public class ServicesService {

  private final ServiceRepository services;
  private final ServiceEventPredictionRepository serviceEventPredictions;

  public ServicesService(ServiceRepository services, ServiceEventPredictionRepository serviceEventPredictions) {
    this.services = services;
    this.serviceEventPredictions = serviceEventPredictions;
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

  public AppEntity getApp(String serviceName, String appName) {
    assertServiceExists(serviceName);
    return services.getApp(serviceName, appName).orElseThrow(() ->
        new EntityNotFoundException(AppEntity.class, "appName", appName));
  }

  public List<AppEntity> getApps(String serviceName) {
    assertServiceExists(serviceName);
    return services.getApps(serviceName);
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

  public List<ServiceEntity> getDependents(String serviceName) {
    assertServiceExists(serviceName);
    return services.getDependents(serviceName);
  }

  public List<ServiceEventPredictionEntity> getPredictions(String serviceName) {
    assertServiceExists(serviceName);
    return services.getPredictions(serviceName);
  }

  public ServiceEventPredictionEntity getEventPrediction(String serviceName, String predictionsName) {
    assertServiceExists(serviceName);
    return services.getPrediction(serviceName, predictionsName).orElseThrow(() ->
        new EntityNotFoundException(ServiceEventPredictionEntity.class, "predictionsName", predictionsName));
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

  public List<ServiceSimulatedMetricEntity> getSimulatedMetrics(String serviceName) {
    assertServiceExists(serviceName);
    return services.getSimulatedMetrics(serviceName);
  }

  public ServiceSimulatedMetricEntity getSimulatedMetric(String serviceName, String simulatedMetricName) {
    assertServiceExists(serviceName);
    return services.getSimulatedMetric(serviceName, simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(ServiceSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
    );
  }

  public int getMinReplicasByServiceName(String serviceName) {
    Integer customMinReplicas = serviceEventPredictions.getMinReplicasByServiceName(serviceName, LocalDate.now());
    if (customMinReplicas != null) {
      log.info("Found event prediction with {} replicas", customMinReplicas);
      return customMinReplicas;
    }
    return services.getMinReplicas(serviceName);
  }

  public int getMaxReplicasByServiceName(String serviceName) {
    return services.getMaxReplicas(serviceName);
  }

  private void assertServiceExists(String serviceName) {
    if (!services.hasService(serviceName)) {
      throw new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName);
    }
  }

}
