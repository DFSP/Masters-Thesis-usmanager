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

import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainerEntity;
import pt.unl.fct.microservicemanagement.mastermanager.util.Validation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulated-metrics/containers")
public class SimulatedContainerMetricsController {

  private final SimulatedContainerMetricsService simulatedContainerMetricsService;

  public SimulatedContainerMetricsController(SimulatedContainerMetricsService simulatedContainerMetricsService) {
    this.simulatedContainerMetricsService = simulatedContainerMetricsService;
  }

  @GetMapping
  public List<SimulatedContainerMetricEntity> getSimulatedContainerMetrics() {
    return simulatedContainerMetricsService.getSimulatedContainerMetrics();
  }

  @GetMapping("/{simulatedMetricName}")
  public SimulatedContainerMetricEntity getSimulatedContainerMetric(@PathVariable String simulatedMetricName) {
    return simulatedContainerMetricsService.getSimulatedContainerMetric(simulatedMetricName);
  }

  @PostMapping
  public SimulatedContainerMetricEntity addSimulatedContainerMetric(
      @RequestBody SimulatedContainerMetricEntity simulatedContainerMetric) {
    Validation.validatePostRequest(simulatedContainerMetric.getId());
    return simulatedContainerMetricsService.addSimulatedContainerMetric(simulatedContainerMetric);
  }

  @PutMapping("/{simulatedMetricName}")
  public SimulatedContainerMetricEntity updateSimulatedContainerMetric(
      @PathVariable String simulatedMetricName,
      @RequestBody SimulatedContainerMetricEntity simulatedMetric) {
    Validation.validatePutRequest(simulatedMetric.getId());
    return simulatedContainerMetricsService.updateSimulatedContainerMetric(simulatedMetricName, simulatedMetric);
  }

  @DeleteMapping("/{simulatedMetricName}")
  public void deleteSimulatedContainerMetric(@PathVariable String simulatedMetricName) {
    simulatedContainerMetricsService.deleteSimulatedContainerMetric(simulatedMetricName);
  }

  @GetMapping("/{simulatedMetricName}/containers")
  public List<ContainerEntity> getSimulatedContainerMetricContainers(@PathVariable String simulatedMetricName) {
    return simulatedContainerMetricsService.getContainers(simulatedMetricName);
  }

  @PostMapping("/{simulatedMetricName}/containers")
  public void addSimulatedContainerMetricContainers(@PathVariable String simulatedMetricName,
                                                    @RequestBody List<String> containers) {
    simulatedContainerMetricsService.addContainers(simulatedMetricName, containers);
  }

  @DeleteMapping("/{simulatedMetricName}/containers")
  public void removeSimulatedContainerMetricContainers(@PathVariable String simulatedMetricName,
                                                       @RequestBody List<String> containers) {
    simulatedContainerMetricsService.removeContainers(simulatedMetricName, containers);
  }

  @DeleteMapping("/{simulatedMetricName}/containers/{containerId}")
  public void removeSimulatedContainerMetricContainer(@PathVariable String simulatedMetricName,
                                                      @PathVariable String containerId) {
    simulatedContainerMetricsService.removeContainer(simulatedMetricName, containerId);
  }

}
