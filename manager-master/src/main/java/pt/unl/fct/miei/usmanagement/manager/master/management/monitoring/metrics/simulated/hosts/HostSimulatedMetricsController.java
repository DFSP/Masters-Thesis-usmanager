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

package pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.hosts;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;

@RestController
@RequestMapping("/simulated-metrics/hosts")
public class HostSimulatedMetricsController {

  private final HostSimulatedMetricsService hostSimulatedMetricsService;

  public HostSimulatedMetricsController(HostSimulatedMetricsService hostSimulatedMetricsService) {
    this.hostSimulatedMetricsService = hostSimulatedMetricsService;
  }

  @GetMapping
  public List<HostSimulatedMetricEntity> getHostSimulatedMetrics() {
    return hostSimulatedMetricsService.getHostSimulatedMetrics();
  }

  @GetMapping("/{simulatedMetricName}")
  public HostSimulatedMetricEntity getHostSimulatedMetric(@PathVariable String simulatedMetricName) {
    return hostSimulatedMetricsService.getHostSimulatedMetric(simulatedMetricName);
  }

  @PostMapping
  public HostSimulatedMetricEntity addHostSimulatedMetric(@RequestBody HostSimulatedMetricEntity simulatedMetric) {
    Validation.validatePostRequest(simulatedMetric.getId());
    return hostSimulatedMetricsService.addHostSimulatedMetric(simulatedMetric);
  }

  @PutMapping("/{simulatedMetricName}")
  public HostSimulatedMetricEntity updateHostSimulatedMetric(@PathVariable String simulatedMetricName,
                                                             @RequestBody HostSimulatedMetricEntity simulatedMetric) {
    Validation.validatePutRequest(simulatedMetric.getId());
    return hostSimulatedMetricsService.updateHostSimulatedMetric(simulatedMetricName, simulatedMetric);
  }

  @DeleteMapping("/{simulatedMetricName}")
  public void deleteHostSimulatedMetrics(@PathVariable String simulatedMetricName) {
    hostSimulatedMetricsService.deleteHostSimulatedMetric(simulatedMetricName);
  }

  @GetMapping("/{simulatedMetricName}/cloud-hosts")
  public List<CloudHostEntity> getHostSimulatedMetricCloudHosts(@PathVariable String simulatedMetricName) {
    return hostSimulatedMetricsService.getCloudHosts(simulatedMetricName);
  }

  @PostMapping("/{simulatedMetricName}/cloud-hosts")
  public void addHostSimulatedMetricCloudHosts(@PathVariable String simulatedMetricName,
                                               @RequestBody List<String> cloudHosts) {
    hostSimulatedMetricsService.addCloudHosts(simulatedMetricName, cloudHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/cloud-hosts")
  public void removeHostSimulatedMetricCloudHosts(@PathVariable String simulatedMetricName,
                                                  @RequestBody List<String> cloudHosts) {
    hostSimulatedMetricsService.removeCloudHosts(simulatedMetricName, cloudHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/cloud-hosts/{instanceId}")
  public void removeHostSimulatedMetricCloudHost(@PathVariable String simulatedMetricName,
                                                 @PathVariable String instanceId) {
    hostSimulatedMetricsService.removeCloudHost(simulatedMetricName, instanceId);
  }

  @GetMapping("/{simulatedMetricName}/edge-hosts")
  public List<EdgeHostEntity> getHostSimulatedMetricEdgeHosts(@PathVariable String simulatedMetricName) {
    return hostSimulatedMetricsService.getEdgeHosts(simulatedMetricName);
  }

  @PostMapping("/{simulatedMetricName}/edge-hosts")
  public void addHostSimulatedMetricEdgeHosts(@PathVariable String simulatedMetricName,
                                              @RequestBody List<String> edgeHosts) {
    hostSimulatedMetricsService.addEdgeHosts(simulatedMetricName, edgeHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/edge-hosts")
  public void removeHostSimulatedMetricEdgeHosts(@PathVariable String simulatedMetricName,
                                                 @RequestBody List<String> edgeHosts) {
    hostSimulatedMetricsService.removeEdgeHosts(simulatedMetricName, edgeHosts);
  }

  @DeleteMapping("/{simulatedMetricName}/edge-hosts/{hostname}")
  public void removeHostSimulatedMetricEdgeHost(@PathVariable String simulatedMetricName,
                                                @PathVariable String hostname) {
    hostSimulatedMetricsService.removeEdgeHost(simulatedMetricName, hostname);
  }

}
