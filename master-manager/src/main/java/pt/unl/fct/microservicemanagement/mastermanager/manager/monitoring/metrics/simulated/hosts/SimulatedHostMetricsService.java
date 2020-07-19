/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.hosts;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.cloud.CloudHostsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.edge.EdgeHostsService;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SimulatedHostMetricsService {

  private final CloudHostsService cloudHostsService;
  private final EdgeHostsService edgeHostsService;

  private final SimulatedHostMetricsRepository simulatedHostMetrics;

  public SimulatedHostMetricsService(CloudHostsService cloudHostsService, EdgeHostsService edgeHostsService,
                                     SimulatedHostMetricsRepository simulatedHostMetrics) {
    this.cloudHostsService = cloudHostsService;
    this.edgeHostsService = edgeHostsService;
    this.simulatedHostMetrics = simulatedHostMetrics;
  }

  public List<SimulatedHostMetricEntity> getSimulatedHostMetrics() {
    return simulatedHostMetrics.findAll();
  }

  public SimulatedHostMetricEntity getSimulatedHostMetric(Long id) {
    return simulatedHostMetrics.findById(id).orElseThrow(() ->
        new EntityNotFoundException(SimulatedHostMetricEntity.class, "id", id.toString()));
  }

  public SimulatedHostMetricEntity getSimulatedHostMetric(String simulatedMetricName) {
    return simulatedHostMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedHostMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public SimulatedHostMetricEntity addSimulatedHostMetric(SimulatedHostMetricEntity simulatedHostMetric) {
    assertSimulatedHostMetricDoesntExist(simulatedHostMetric);
    log.debug("Saving simulated host metric {}", ToStringBuilder.reflectionToString(simulatedHostMetric));
    return simulatedHostMetrics.save(simulatedHostMetric);
  }

  public SimulatedHostMetricEntity updateSimulatedHostMetric(String simulatedMetricName,
                                                             SimulatedHostMetricEntity newSimulatedHostMetric) {
    log.debug("Updating simulated host metric {} with {}", simulatedMetricName,
        ToStringBuilder.reflectionToString(newSimulatedHostMetric));
    SimulatedHostMetricEntity simulatedHostMetric = getSimulatedHostMetric(simulatedMetricName);
    ObjectUtils.copyValidProperties(newSimulatedHostMetric, simulatedHostMetric);
    return simulatedHostMetrics.save(simulatedHostMetric);
  }

  public void deleteSimulatedHostMetric(String simulatedMetricName) {
    log.debug("Deleting simulated host metric {}", simulatedMetricName);
    SimulatedHostMetricEntity simulatedHostMetric = getSimulatedHostMetric(simulatedMetricName);
    simulatedHostMetric.removeAssociations();
    simulatedHostMetrics.delete(simulatedHostMetric);
  }

  public List<SimulatedHostMetricEntity> getGenericSimulatedHostMetrics() {
    return simulatedHostMetrics.findGenericSimulatedHostMetrics();
  }

  public SimulatedHostMetricEntity getGenericSimulatedHostMetric(String simulatedMetricName) {
    return simulatedHostMetrics.findGenericSimulatedHostMetric(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(SimulatedHostMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public List<CloudHostEntity> getCloudHosts(String simulatedMetricName) {
    assertSimulatedHostMetricExists(simulatedMetricName);
    return simulatedHostMetrics.getCloudHosts(simulatedMetricName);
  }

  public CloudHostEntity getCloudHost(String simulatedMetricName, String instanceId) {
    assertSimulatedHostMetricExists(simulatedMetricName);
    return simulatedHostMetrics.getCloudHost(simulatedMetricName, instanceId).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "instanceId", instanceId));
  }

  public void addCloudHost(String simulatedMetricName, String instanceId) {
    addCloudHosts(simulatedMetricName, List.of(instanceId));
  }

  public void addCloudHosts(String simulatedMetricName, List<String> instanceIds) {
    log.debug("Adding cloud hosts {} to simulated metric {}", instanceIds, simulatedMetricName);
    SimulatedHostMetricEntity hostMetric = getSimulatedHostMetric(simulatedMetricName);
    instanceIds.forEach(instanceId -> {
      CloudHostEntity cloudHost = cloudHostsService.getCloudHost(instanceId);
      cloudHost.addSimulatedHostMetric(hostMetric);
    });
    simulatedHostMetrics.save(hostMetric);
  }

  public void removeCloudHost(String simulatedMetricName, String instanceId) {
    removeCloudHosts(simulatedMetricName, List.of(instanceId));
  }

  public void removeCloudHosts(String simulatedMetricName, List<String> instanceIds) {
    log.info("Removing cloud hosts {} from simulated metric {}", instanceIds, simulatedMetricName);
    SimulatedHostMetricEntity hostMetric = getSimulatedHostMetric(simulatedMetricName);
    instanceIds.forEach(instanceId -> cloudHostsService.getCloudHost(instanceId).removeSimulatedHostMetric(hostMetric));
    simulatedHostMetrics.save(hostMetric);
  }

  public List<EdgeHostEntity> getEdgeHosts(String simulatedMetricName) {
    assertSimulatedHostMetricExists(simulatedMetricName);
    return simulatedHostMetrics.getEdgeHosts(simulatedMetricName);
  }

  public EdgeHostEntity getEdgeHost(String simulatedMetricName, String hostname) {
    assertSimulatedHostMetricExists(simulatedMetricName);
    return simulatedHostMetrics.getEdgeHost(simulatedMetricName, hostname).orElseThrow(() ->
        new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname));
  }

  public void addEdgeHost(String simulatedMetricName, String hostname) {
    addEdgeHosts(simulatedMetricName, List.of(hostname));
  }

  public void addEdgeHosts(String simulatedMetricName, List<String> hostnames) {
    log.debug("Adding edge hosts {} to simulated metric {}", hostnames, simulatedMetricName);
    SimulatedHostMetricEntity hostMetric = getSimulatedHostMetric(simulatedMetricName);
    hostnames.forEach(hostname -> {
      EdgeHostEntity edgeHost = edgeHostsService.getEdgeHost(hostname);
      edgeHost.addSimulatedHostMetric(hostMetric);
    });
    simulatedHostMetrics.save(hostMetric);
  }

  public void removeEdgeHost(String simulatedMetricName, String instanceId) {
    removeEdgeHosts(simulatedMetricName, List.of(instanceId));
  }

  public void removeEdgeHosts(String simulatedMetricName, List<String> instanceIds) {
    log.info("Removing edge hosts {} from simulated metric {}", instanceIds, simulatedMetricName);
    SimulatedHostMetricEntity hostMetric = getSimulatedHostMetric(simulatedMetricName);
    instanceIds.forEach(instanceId -> edgeHostsService.getEdgeHost(instanceId).removeSimulatedHostMetric(hostMetric));
    simulatedHostMetrics.save(hostMetric);
  }

  private void assertSimulatedHostMetricExists(String simulatedMetricName) {
    if (!simulatedHostMetrics.hasSimulatedHostMetric(simulatedMetricName)) {
      throw new EntityNotFoundException(SimulatedHostMetricEntity.class, "simulatedMetricName", simulatedMetricName);
    }
  }

  private void assertSimulatedHostMetricDoesntExist(SimulatedHostMetricEntity simulatedHostMetric) {
    var name = simulatedHostMetric.getName();
    if (simulatedHostMetrics.hasSimulatedHostMetric(name)) {
      throw new DataIntegrityViolationException("Simulated host metric '" + name + "' already exists");
    }
  }

  public Map<String, Double> getSimulatedFieldsValues(String hostname) {
    List<SimulatedHostMetricEntity> metrics = simulatedHostMetrics.findByHost(hostname);
    return metrics.stream().collect(Collectors.toMap(metric -> metric.getField().getName(), this::randomizeFieldValue));
  }

  public Optional<Double> getSimulatedFieldValue(String hostname, String field) {
    Optional<SimulatedHostMetricEntity> metric = simulatedHostMetrics.findByHostAndField(hostname, field);
    Optional<Double> fieldValue = metric.map(this::randomizeFieldValue);
    if (fieldValue.isPresent() && metric.get().isOverride()) {
      return fieldValue;
    }
    Optional<Double> genericFieldValue = randomizeGenericFieldValue(field);
    if (genericFieldValue.isPresent()) {
      return genericFieldValue;
    }
    return fieldValue;
  }

  private Double randomizeFieldValue(SimulatedHostMetricEntity metric) {
    var random = new Random();
    double minValue = metric.getMinimumValue();
    double maxValue = metric.getMaximumValue();
    return minValue + (maxValue - minValue) * random.nextDouble();
  }

  private Optional<Double> randomizeGenericFieldValue(String field) {
    Optional<SimulatedHostMetricEntity> metric = simulatedHostMetrics.findGenericByField(field);
    return metric.map(this::randomizeFieldValue);
  }

}
