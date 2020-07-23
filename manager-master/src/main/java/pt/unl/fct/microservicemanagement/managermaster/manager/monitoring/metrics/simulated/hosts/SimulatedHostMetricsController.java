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

package pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.metrics.simulated.hosts;

import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.microservicemanagement.managermaster.util.Validation;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulated-metrics/hosts")
public class SimulatedHostMetricsController {

  private final SimulatedHostMetricsService simulatedHostMetricsService;

  public SimulatedHostMetricsController(SimulatedHostMetricsService simulatedHostMetricsService) {
    this.simulatedHostMetricsService = simulatedHostMetricsService;
  }

  @GetMapping
  public List<SimulatedHostMetricEntity> getSimulatedHostMetrics() {
    return simulatedHostMetricsService.getSimulatedHostMetrics();
  }

  @GetMapping("/{simulatedMetricName}")
  public SimulatedHostMetricEntity getSimulatedHostMetric(@PathVariable String simulatedMetricName) {
    return simulatedHostMetricsService.getSimulatedHostMetric(simulatedMetricName);
  }

  @PostMapping
  public SimulatedHostMetricEntity addSimulatedHostMetric(@RequestBody SimulatedHostMetricEntity simulatedMetric) {
    Validation.validatePostRequest(simulatedMetric.getId());
    return simulatedHostMetricsService.addSimulatedHostMetric(simulatedMetric);
  }

  @PutMapping("/{simulatedMetricName}")
  public SimulatedHostMetricEntity updateSimulatedHostMetric(@PathVariable String simulatedMetricName,
                                                             @RequestBody SimulatedHostMetricEntity simulatedMetric) {
    Validation.validatePutRequest(simulatedMetric.getId());
    return simulatedHostMetricsService.updateSimulatedHostMetric(simulatedMetricName, simulatedMetric);
  }

  @DeleteMapping("/{simulatedMetricName}")
  public void deleteSimulatedHostMetrics(@PathVariable String simulatedMetricName) {
    simulatedHostMetricsService.deleteSimulatedHostMetric(simulatedMetricName);
  }

  @GetMapping("/{simulatedMetricName}/cloud-hosts")
  public List<CloudHostEntity> getSimulatedHostMetricCloudHosts(@PathVariable String simulatedMetricName) {
    return simulatedHostMetricsService.getCloudHosts(simulatedMetricName);
  }

  @PostMapping("/{simulatedMetricName}/cloud-hosts")
  public void addSimulatedHostMetricCloudHosts(@PathVariable String simulatedMetricName,
                                               @RequestBody List<String> cloudHosts) {
    simulatedHostMetricsService.addCloudHosts(simulatedMetricName, cloudHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/cloud-hosts")
  public void removeSimulatedHostMetricCloudHosts(@PathVariable String simulatedMetricName,
                                                  @RequestBody List<String> cloudHosts) {
    simulatedHostMetricsService.removeCloudHosts(simulatedMetricName, cloudHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/cloud-hosts/{instanceId}")
  public void removeSimulatedHostMetricCloudHost(@PathVariable String simulatedMetricName,
                                                 @PathVariable String instanceId) {
    simulatedHostMetricsService.removeCloudHost(simulatedMetricName, instanceId);
  }

  @GetMapping("/{simulatedMetricName}/edge-hosts")
  public List<EdgeHostEntity> getSimulatedHostMetricEdgeHosts(@PathVariable String simulatedMetricName) {
    return simulatedHostMetricsService.getEdgeHosts(simulatedMetricName);
  }

  @PostMapping("/{simulatedMetricName}/edge-hosts")
  public void addSimulatedHostMetricEdgeHosts(@PathVariable String simulatedMetricName,
                                              @RequestBody List<String> edgeHosts) {
    simulatedHostMetricsService.addEdgeHosts(simulatedMetricName, edgeHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/edge-hosts")
  public void removeSimulatedHostMetricEdgeHosts(@PathVariable String simulatedMetricName,
                                                 @RequestBody List<String> edgeHosts) {
    simulatedHostMetricsService.removeEdgeHosts(simulatedMetricName, edgeHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/edge-hosts/{hostname}")
  public void removeSimulatedHostMetricEdgeHost(@PathVariable String simulatedMetricName,
                                                @PathVariable String hostname) {
    simulatedHostMetricsService.removeEdgeHost(simulatedMetricName, hostname);
  }

}
