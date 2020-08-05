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

package pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.services;

import pt.unl.fct.miei.usmanagement.manager.master.management.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;

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
@RequestMapping("/simulated-metrics/services")
public class SimulatedServiceMetricsController {

  private final SimulatedServiceMetricsService simulatedServiceMetricsService;

  public SimulatedServiceMetricsController(SimulatedServiceMetricsService simulatedServiceMetricsService) {
    this.simulatedServiceMetricsService = simulatedServiceMetricsService;
  }

  @GetMapping
  public List<SimulatedServiceMetricEntity> getSimulatedServiceMetrics() {
    return simulatedServiceMetricsService.getSimulatedServiceMetrics();
  }

  @GetMapping("/{simulatedMetricName}")
  public SimulatedServiceMetricEntity getSimulatedServiceMetric(@PathVariable String simulatedMetricName) {
    return simulatedServiceMetricsService.getSimulatedServiceMetric(simulatedMetricName);
  }

  @PostMapping
  public SimulatedServiceMetricEntity addSimulatedServiceMetric(
      @RequestBody SimulatedServiceMetricEntity simulatedServiceMetric) {
    Validation.validatePostRequest(simulatedServiceMetric.getId());
    return simulatedServiceMetricsService.addSimulatedServiceMetric(simulatedServiceMetric);
  }

  @PutMapping("/{simulatedMetricName}")
  public SimulatedServiceMetricEntity updateSimulatedServiceMetric(
      @PathVariable String simulatedMetricName,
      @RequestBody SimulatedServiceMetricEntity simulatedMetric) {
    Validation.validatePutRequest(simulatedMetric.getId());
    return simulatedServiceMetricsService.updateSimulatedServiceMetric(simulatedMetricName, simulatedMetric);
  }

  @DeleteMapping("/{simulatedMetricName}")
  public void deleteSimulatedServiceMetric(@PathVariable String simulatedMetricName) {
    simulatedServiceMetricsService.deleteSimulatedServiceMetric(simulatedMetricName);
  }


  @GetMapping("/{simulatedMetricName}/services")
  public List<ServiceEntity> getSimulatedServiceMetricServices(@PathVariable String simulatedMetricName) {
    return simulatedServiceMetricsService.getServices(simulatedMetricName);
  }

  @PostMapping("/{simulatedMetricName}/services")
  public void addSimulatedServiceMetricServices(@PathVariable String simulatedMetricName,
                                                @RequestBody List<String> services) {
    simulatedServiceMetricsService.addServices(simulatedMetricName, services);
  }

  @DeleteMapping("/{simulatedMetricName}/services")
  public void removeSimulatedServiceMetricServices(@PathVariable String simulatedMetricName,
                                                   @RequestBody List<String> services) {
    simulatedServiceMetricsService.removeServices(simulatedMetricName, services);
  }

  @DeleteMapping("/{simulatedMetricName}/services/{serviceName}")
  public void removeSimulatedServiceMetricService(@PathVariable String simulatedMetricName,
                                                  @PathVariable String serviceName) {
    simulatedServiceMetricsService.removeService(simulatedMetricName, serviceName);
  }

}
