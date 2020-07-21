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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.services;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServiceEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServicesService;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SimulatedServiceMetricsService {

  private final ServicesService servicesService;

  private final SimulatedServiceMetricsRepository simulatedServiceMetrics;

  public SimulatedServiceMetricsService(@Lazy ServicesService servicesService,
                                        SimulatedServiceMetricsRepository simulatedServiceMetrics) {
    this.servicesService = servicesService;
    this.simulatedServiceMetrics = simulatedServiceMetrics;
  }

  public List<SimulatedServiceMetricEntity> getSimulatedServiceMetrics() {
    return simulatedServiceMetrics.findAll();
  }

  public SimulatedServiceMetricEntity getSimulatedServiceMetric(Long id) {
    return simulatedServiceMetrics.findById(id).orElseThrow(() ->
        new EntityNotFoundException(SimulatedServiceMetricEntity.class, "id", id.toString()));
  }

  public SimulatedServiceMetricEntity getSimulatedServiceMetric(String simulatedMetricName) {
    return simulatedServiceMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedServiceMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public SimulatedServiceMetricEntity addSimulatedServiceMetric(SimulatedServiceMetricEntity simulatedServiceMetric) {
    assertSimulatedServiceMetricDoesntExist(simulatedServiceMetric);
    log.debug("Saving simulated service metric {}", ToStringBuilder.reflectionToString(simulatedServiceMetric));
    return simulatedServiceMetrics.save(simulatedServiceMetric);
  }

  public SimulatedServiceMetricEntity updateSimulatedServiceMetric(
      String simulatedMetricName, SimulatedServiceMetricEntity newSimulatedServiceMetric) {
    log.debug("Updating simulated service metric {} with {}", simulatedMetricName,
        ToStringBuilder.reflectionToString(newSimulatedServiceMetric));
    SimulatedServiceMetricEntity simulatedServiceMetric = getSimulatedServiceMetric(simulatedMetricName);
    ObjectUtils.copyValidProperties(newSimulatedServiceMetric, simulatedServiceMetric);
    return simulatedServiceMetrics.save(simulatedServiceMetric);
  }

  public void deleteSimulatedServiceMetric(String simulatedMetricName) {
    log.debug("Deleting simulated service metric {}", simulatedMetricName);
    SimulatedServiceMetricEntity simulatedServiceMetric = getSimulatedServiceMetric(simulatedMetricName);
    simulatedServiceMetric.removeAssociations();
    simulatedServiceMetrics.delete(simulatedServiceMetric);
  }

  public List<SimulatedServiceMetricEntity> getGenericSimulatedServiceMetrics() {
    return simulatedServiceMetrics.findGenericSimulatedServiceMetrics();
  }

  public SimulatedServiceMetricEntity getGenericSimulatedServiceMetric(String simulatedMetricName) {
    return simulatedServiceMetrics.findGenericSimulatedServiceMetric(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedServiceMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public List<ServiceEntity> getServices(String simulatedMetricName) {
    assertSimulatedServiceMetricExists(simulatedMetricName);
    return simulatedServiceMetrics.getServices(simulatedMetricName);
  }

  public ServiceEntity getService(String simulatedMetricName, String serviceName) {
    assertSimulatedServiceMetricExists(simulatedMetricName);
    return simulatedServiceMetrics.getService(simulatedMetricName, serviceName).orElseThrow(() ->
        new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName));
  }

  public void addService(String simulatedMetricName, String serviceName) {
    addServices(simulatedMetricName, List.of(serviceName));
  }

  public void addServices(String simulatedMetricName, List<String> serviceNames) {
    log.debug("Adding services {} to simulated metric {}", serviceNames, simulatedMetricName);
    SimulatedServiceMetricEntity serviceMetric = getSimulatedServiceMetric(simulatedMetricName);
    serviceNames.forEach(serviceName -> {
      ServiceEntity service = servicesService.getService(serviceName);
      service.addSimulatedServiceMetric(serviceMetric);
    });
    simulatedServiceMetrics.save(serviceMetric);
  }

  public void removeService(String simulatedMetricName, String serviceName) {
    removeServices(simulatedMetricName, List.of(serviceName));
  }

  public void removeServices(String simulatedMetricName, List<String> serviceNames) {
    log.info("Removing services {} from simulated metric {}", serviceNames, simulatedMetricName);
    SimulatedServiceMetricEntity serviceMetric = getSimulatedServiceMetric(simulatedMetricName);
    serviceNames.forEach(serviceName ->
        servicesService.getService(serviceName).removeSimulatedServiceMetric(serviceMetric));
    simulatedServiceMetrics.save(serviceMetric);
  }

  private void assertSimulatedServiceMetricExists(String simulatedMetricName) {
    if (!simulatedServiceMetrics.hasSimulatedServiceMetric(simulatedMetricName)) {
      throw new EntityNotFoundException(SimulatedServiceMetricEntity.class, "simulatedMetricName", simulatedMetricName);
    }
  }

  private void assertSimulatedServiceMetricDoesntExist(SimulatedServiceMetricEntity simulatedServiceMetric) {
    var name = simulatedServiceMetric.getName();
    if (simulatedServiceMetrics.hasSimulatedServiceMetric(name)) {
      throw new DataIntegrityViolationException("Simulated service metric '" + name + "' already exists");
    }
  }

  public Optional<Double> getSimulatedServiceFieldValue(String serviceName, String field) {
    Optional<SimulatedServiceMetricEntity> simulatedServiceMetric = simulatedServiceMetrics.findByServiceAndField(field,
        serviceName);
    return getFieldValue(simulatedServiceMetric, field);
  }

  private Optional<Double> getFieldValue(Optional<SimulatedServiceMetricEntity> simulatedServiceMetric, String field) {
    Optional<Double> fieldValue = simulatedServiceMetric.map(this::randomizeFieldValue);
    if (fieldValue.isPresent() && simulatedServiceMetric.get().isOverride()) {
      return fieldValue;
    }
    Optional<Double> genericFieldValue = randomizeGenericFieldValue(field);
    if (genericFieldValue.isPresent()) {
      return genericFieldValue;
    }
    return fieldValue;
  }

  private Double randomizeFieldValue(SimulatedServiceMetricEntity simulatedServiceMetric) {
    var random = new Random();
    double minValue = simulatedServiceMetric.getMinimumValue();
    double maxValue = simulatedServiceMetric.getMaximumValue();
    return minValue + (maxValue - minValue) * random.nextDouble();
  }

  private Optional<Double> randomizeGenericFieldValue(String field) {
    Optional<SimulatedServiceMetricEntity> simulatedServiceMetric = simulatedServiceMetrics.findGenericByField(field);
    return simulatedServiceMetric.map(this::randomizeFieldValue);
  }

}
