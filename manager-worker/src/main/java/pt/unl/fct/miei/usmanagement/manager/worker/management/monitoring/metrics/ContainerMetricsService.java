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

package pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics;

import java.util.HashMap;
import java.util.Map;

import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.CpuStats;
import com.spotify.docker.client.messages.MemoryStats;
import com.spotify.docker.client.messages.NetworkStats;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics.simulated.ContainerSimulatedMetricsService;

@Service
public class ContainerMetricsService {

  private final ContainersService containersService;
  private final ContainerSimulatedMetricsService containerSimulatedMetricsService;
  private final ServicesMonitoringService servicesMonitoringService;

  public ContainerMetricsService(ContainersService containersService,
                                 ContainerSimulatedMetricsService containerSimulatedMetricsService,
                                 @Lazy ServicesMonitoringService servicesMonitoringService) {
    this.containersService = containersService;
    this.containerSimulatedMetricsService = containerSimulatedMetricsService;
    this.servicesMonitoringService = servicesMonitoringService;
  }

  public Map<String, Double> getContainerStats(ContainerEntity container, double secondsInterval) {
    String containerId = container.getContainerId();
    ContainerStats containerStats = containersService.getContainerStats(container);
    CpuStats cpuStats = containerStats.cpuStats();
    CpuStats preCpuStats = containerStats.precpuStats();
    double cpu = cpuStats.cpuUsage().totalUsage().doubleValue();
    double cpuPercent = getContainerCpuPercent(preCpuStats, cpuStats);
    MemoryStats memoryStats = containerStats.memoryStats();
    double ram = memoryStats.usage().doubleValue();
    double ramPercent = getContainerRamPercent(memoryStats);
    double rxBytes = 0;
    double txBytes = 0;
    for (NetworkStats stats : containerStats.networks().values()) {
      rxBytes += stats.rxBytes().doubleValue();
      txBytes += stats.txBytes().doubleValue();
    }
    // Metrics from docker
    final var fields = new HashMap<>(Map.of(
        "cpu", cpu,
        "ram", ram,
        "cpu-%", cpuPercent,
        "ram-%", ramPercent,
        "rx-bytes", rxBytes,
        "tx-bytes", txBytes));
    // Simulated metrics
    if (container.getLabels().containsKey(ContainerConstants.Label.SERVICE_NAME)) {
      Map<String, Double> simulatedFields = containerSimulatedMetricsService.getSimulatedFieldsValues(containerId);
      fields.putAll(simulatedFields);
    }
    // Calculated metrics
    Map.of("rx-bytes", rxBytes, "tx-bytes", txBytes).forEach((field, value) -> {
      ServiceMonitoringEntity monitoring = servicesMonitoringService.getContainerMonitoring(containerId, field);
      double lastValue = monitoring == null ? 0 : monitoring.getLastValue();
      double bytesPerSec = Math.max(0, (value - lastValue) / secondsInterval);
      fields.put(field + "-per-sec", bytesPerSec);
    });
    return fields;
  }

  private double getContainerCpuPercent(CpuStats preCpuStats, CpuStats cpuStats) {
    double systemDelta = cpuStats.systemCpuUsage().doubleValue() - preCpuStats.systemCpuUsage().doubleValue();
    double cpuDelta = cpuStats.cpuUsage().totalUsage().doubleValue() - preCpuStats.cpuUsage().totalUsage().doubleValue();
    double cpuPercent = 0.0;
    if (systemDelta > 0.0 && cpuDelta > 0.0) {
      double onlineCpus = cpuStats.cpuUsage().percpuUsage().stream().filter(cpuUsage -> cpuUsage >= 1).count();
      cpuPercent = (cpuDelta / systemDelta) * onlineCpus * 100.0;
    }
    return cpuPercent;
  }

  private double getContainerRamPercent(MemoryStats memStats) {
    return memStats.limit() < 1 ? 0.0 : (memStats.usage().doubleValue() / memStats.limit().doubleValue()) * 100.0;
  }

}
