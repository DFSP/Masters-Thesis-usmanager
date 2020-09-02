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

package pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics.simulated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceSimulatedMetricsRepository;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;

@Slf4j
@Service
public class ServiceSimulatedMetricsService {

  private final ServiceSimulatedMetricsRepository serviceSimulatedMetrics;

  public ServiceSimulatedMetricsService(ServiceSimulatedMetricsRepository serviceSimulatedMetrics) {
    this.serviceSimulatedMetrics = serviceSimulatedMetrics;
  }

  public List<ServiceSimulatedMetricEntity> getServiceSimulatedMetrics() {
    return serviceSimulatedMetrics.findAll();
  }

  public ServiceSimulatedMetricEntity getServiceSimulatedMetric(Long id) {
    return serviceSimulatedMetrics.findById(id).orElseThrow(() ->
        new EntityNotFoundException(ServiceSimulatedMetricEntity.class, "id", id.toString()));
  }

  public ServiceSimulatedMetricEntity getServiceSimulatedMetric(String simulatedMetricName) {
    return serviceSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(ServiceSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public List<ServiceSimulatedMetricEntity> getGenericServiceSimulatedMetrics() {
    return serviceSimulatedMetrics.findGenericServiceSimulatedMetrics();
  }

  public ServiceSimulatedMetricEntity getGenericServiceSimulatedMetric(String simulatedMetricName) {
    return serviceSimulatedMetrics.findGenericServiceSimulatedMetric(simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(ServiceSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
  }

  public List<ServiceEntity> getServices(String simulatedMetricName) {
    assertServiceSimulatedMetricExists(simulatedMetricName);
    return serviceSimulatedMetrics.getServices(simulatedMetricName);
  }

  public ServiceEntity getService(String simulatedMetricName, String serviceName) {
    assertServiceSimulatedMetricExists(simulatedMetricName);
    return serviceSimulatedMetrics.getService(simulatedMetricName, serviceName).orElseThrow(() ->
        new EntityNotFoundException(ServiceEntity.class, "serviceName", serviceName));
  }
  
  private void assertServiceSimulatedMetricExists(String simulatedMetricName) {
    if (!serviceSimulatedMetrics.hasServiceSimulatedMetric(simulatedMetricName)) {
      throw new EntityNotFoundException(ServiceSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName);
    }
  }
  
  public Map<String, Double> getSimulatedFieldsValues(String serviceName) {
    List<ServiceSimulatedMetricEntity> metrics = serviceSimulatedMetrics.findByService(serviceName);
    return metrics.stream().collect(Collectors.toMap(metric -> metric.getField().getName(), this::randomizeFieldValue));
  }
  
  public Optional<Double> getSimulatedFieldValue(String serviceName, String field) {
    Optional<ServiceSimulatedMetricEntity> metric = serviceSimulatedMetrics.findByServiceAndField(serviceName, field);
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

  private Double randomizeFieldValue(ServiceSimulatedMetricEntity serviceSimulatedMetric) {
    var random = new Random();
    double minValue = serviceSimulatedMetric.getMinimumValue();
    double maxValue = serviceSimulatedMetric.getMaximumValue();
    return minValue + (maxValue - minValue) * random.nextDouble();
  }

  private Optional<Double> randomizeGenericFieldValue(String field) {
    Optional<ServiceSimulatedMetricEntity> serviceSimulatedMetric = serviceSimulatedMetrics.findGenericByField(field);
    return serviceSimulatedMetric.map(this::randomizeFieldValue);
  }

}