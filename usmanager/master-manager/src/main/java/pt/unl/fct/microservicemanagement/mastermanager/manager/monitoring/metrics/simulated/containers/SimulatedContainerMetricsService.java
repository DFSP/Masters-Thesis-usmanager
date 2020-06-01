/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.containers;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainerEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainersService;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SimulatedContainerMetricsService {

  private final ContainersService containersService;

  private final SimulatedContainerMetricsRepository simulatedContainerMetrics;

  public SimulatedContainerMetricsService(@Lazy ContainersService containersService,
                                          SimulatedContainerMetricsRepository simulatedContainerMetrics) {
    this.containersService = containersService;
    this.simulatedContainerMetrics = simulatedContainerMetrics;
  }

  public List<SimulatedContainerMetricEntity> getSimulatedContainerMetrics() {
    return simulatedContainerMetrics.findAll();
  }

  public SimulatedContainerMetricEntity getSimulatedContainerMetric(Long id) {
    return simulatedContainerMetrics.findById(id).orElseThrow(() ->
        new EntityNotFoundException(SimulatedContainerMetricEntity.class, "id", id.toString()));
  }

  public SimulatedContainerMetricEntity getSimulatedContainerMetric(String simulatedMetricName) {
    return simulatedContainerMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedContainerMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public SimulatedContainerMetricEntity addSimulatedContainerMetric(
      SimulatedContainerMetricEntity simulatedContainerMetric) {
    assertSimulatedContainerMetricDoesntExist(simulatedContainerMetric);
    log.debug("Saving simulated container metric {}", ToStringBuilder.reflectionToString(simulatedContainerMetric));
    return simulatedContainerMetrics.save(simulatedContainerMetric);
  }

  public SimulatedContainerMetricEntity updateSimulatedContainerMetric(
      String simulatedMetricName, SimulatedContainerMetricEntity newSimulatedContainerMetric) {
    log.debug("Updating simulated container metric {} with {}", simulatedMetricName,
        ToStringBuilder.reflectionToString(newSimulatedContainerMetric));
    SimulatedContainerMetricEntity simulatedContainerMetric = getSimulatedContainerMetric(simulatedMetricName);
    ObjectUtils.copyValidProperties(newSimulatedContainerMetric, simulatedContainerMetric);
    return simulatedContainerMetrics.save(simulatedContainerMetric);
  }

  public void deleteSimulatedContainerMetric(String simulatedMetricName) {
    log.debug("Deleting simulated container metric {}", simulatedMetricName);
    SimulatedContainerMetricEntity simulatedContainerMetric = getSimulatedContainerMetric(simulatedMetricName);
    simulatedContainerMetrics.delete(simulatedContainerMetric);
  }

  public List<SimulatedContainerMetricEntity> getGenericSimulatedContainerMetrics() {
    return simulatedContainerMetrics.findGenericSimulatedContainerMetrics();
  }

  public SimulatedContainerMetricEntity getGenericSimulatedContainerMetric(String simulatedMetricName) {
    return simulatedContainerMetrics.findGenericSimulatedContainerMetric(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedContainerMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public List<ContainerEntity> getContainers(String simulatedMetricName) {
    assertSimulatedContainerMetricExists(simulatedMetricName);
    return simulatedContainerMetrics.getContainers(simulatedMetricName);
  }

  public ContainerEntity getContainer(String simulatedMetricName, String containerId) {
    assertSimulatedContainerMetricExists(simulatedMetricName);
    return simulatedContainerMetrics.getContainer(simulatedMetricName, containerId).orElseThrow(() ->
        new EntityNotFoundException(ContainerEntity.class, "containerId", containerId));
  }

  public void addContainer(String simulatedMetricName, String containerId) {
    ContainerEntity container = containersService.getContainer(containerId);
    SimulatedContainerMetricEntity simulatedContainerMetric = getSimulatedContainerMetric(simulatedMetricName);
    if (!container.getSimulatedContainerMetrics().contains(simulatedContainerMetric)) {
      log.debug("Adding container {} to simulated container metric {}", containerId, simulatedMetricName);
      simulatedContainerMetric = simulatedContainerMetric.toBuilder().container(container).build();
      simulatedContainerMetrics.save(simulatedContainerMetric);
    }
  }

  public void addContainers(String simulatedMetricName, List<String> containerIds) {
    containerIds.forEach(containerId -> addContainer(simulatedMetricName, containerId));
  }

  public void removeContainer(String simulatedMetricName, String containerId) {
    removeContainers(simulatedMetricName, List.of(containerId));
  }

  public void removeContainers(String simulatedMetricName, List<String> containerIds) {
    log.info("Removing containers {} from simulated container metric {}", containerIds, simulatedMetricName);
    SimulatedContainerMetricEntity simulatedContainerMetric = getSimulatedContainerMetric(simulatedMetricName);
    simulatedContainerMetric.getContainers()
        .removeIf(container -> containerIds.contains(container.getContainerId()));
    simulatedContainerMetrics.save(simulatedContainerMetric);
  }

  private void assertSimulatedContainerMetricExists(String name) {
    if (!simulatedContainerMetrics.hasSimulatedContainerMetric(name)) {
      throw new EntityNotFoundException(SimulatedContainerMetricEntity.class, "name", name);
    }
  }

  private void assertSimulatedContainerMetricDoesntExist(SimulatedContainerMetricEntity simulatedContainerMetric) {
    var name = simulatedContainerMetric.getName();
    if (simulatedContainerMetrics.hasSimulatedContainerMetric(name)) {
      throw new DataIntegrityViolationException("Simulated container metric '" + name + "' already exists");
    }
  }

  public Map<String, Double> getSimulatedFieldsValues(String containerId) {
    List<SimulatedContainerMetricEntity> metrics = this.simulatedContainerMetrics.findByContainer(containerId);
    return metrics.stream().collect(Collectors.toMap(metric -> metric.getField().getName(), this::randomizeFieldValue));
  }

  public Optional<Double> getSimulatedFieldValue(String containerId, String field) {
    Optional<SimulatedContainerMetricEntity> simulatedContainerMetric =
        simulatedContainerMetrics.findByContainerAndField(containerId, field);
    Optional<Double> fieldValue = simulatedContainerMetric.map(this::randomizeFieldValue);
    if (fieldValue.isPresent() && simulatedContainerMetric.get().isOverride()) {
      return fieldValue;
    }
    Optional<Double> genericFieldValue = randomizeGenericFieldValue(field);
    if (genericFieldValue.isPresent()) {
      return genericFieldValue;
    }
    return fieldValue;
  }

  private Double randomizeFieldValue(SimulatedContainerMetricEntity metric) {
    var random = new Random();
    double minValue = metric.getMinimumValue();
    double maxValue = metric.getMaximumValue();
    return minValue + (maxValue - minValue) * random.nextDouble();
  }

  private Optional<Double> randomizeGenericFieldValue(String field) {
    Optional<SimulatedContainerMetricEntity> simulatedContainerMetric =
        simulatedContainerMetrics.findGenericByField(field);
    return simulatedContainerMetric.map(this::randomizeFieldValue);
  }

}
