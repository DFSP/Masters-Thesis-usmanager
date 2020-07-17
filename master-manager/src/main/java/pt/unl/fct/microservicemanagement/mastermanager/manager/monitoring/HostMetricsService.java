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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring;

import pt.unl.fct.microservicemanagement.mastermanager.manager.fields.FieldEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.fields.FieldsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.HostProperties;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.hosts.SimulatedHostMetricsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.prometheus.PrometheusService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class HostMetricsService {

  private final PrometheusService prometheusService;
  private final SimulatedHostMetricsService simulatedHostMetricsService;
  private final FieldsService fieldsService;
  private final double maximumRamPercentage;

  public HostMetricsService(PrometheusService prometheusService,
                            SimulatedHostMetricsService simulatedHostMetricsService,
                            FieldsService fieldsService,
                            HostProperties hostProperties) {
    this.prometheusService = prometheusService;
    this.simulatedHostMetricsService = simulatedHostMetricsService;
    this.fieldsService = fieldsService;
    this.maximumRamPercentage = hostProperties.getMaximumRamPercentage();
  }

  public boolean nodeHasAvailableResources(String hostname, double avgContainerMem) {
    double totalRam = prometheusService.getTotalMemory(hostname);
    double availableRam = prometheusService.getAvailableMemory(hostname);
    //double cpuUsagePerc = prometheusService.getCpuUsagePercent(hostname);
    final var predictedRamUsage = (1.0 - ((availableRam - avgContainerMem) / totalRam)) * 100.0;
    //TODO Ignoring CPU: cpuUsagePerc < maxCpuPerc
    return predictedRamUsage < maximumRamPercentage;
  }

  public Map<String, Double> getHostStats(String hostname) {
    var fieldsValues = new HashMap<String, Double>();
    double cpuPercentage = prometheusService.getCpuUsagePercent(hostname);
    if (cpuPercentage != -1) {
      // just to make sure cpu-% is a valid field name
      FieldEntity field = fieldsService.getField("cpu-%");
      fieldsValues.put(field.getName(), cpuPercentage);
    }
    double ramPercentage = prometheusService.getMemoryUsagePercent(hostname);
    if (ramPercentage != -1) {
      // just to make sure ram-% is a valid field name
      FieldEntity field = fieldsService.getField("ram-%");
      fieldsValues.put(field.getName(), ramPercentage);
    }
    fieldsValues.putAll(simulatedHostMetricsService.getSimulatedFieldsValues(hostname));
    return fieldsValues;
  }

}
