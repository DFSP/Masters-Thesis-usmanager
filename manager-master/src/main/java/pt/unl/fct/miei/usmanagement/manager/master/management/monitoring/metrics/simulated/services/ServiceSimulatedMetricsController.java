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

import org.springframework.web.bind.annotation.*;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.metrics.simulated.services.ServiceSimulatedMetricsService;

import java.util.List;

@RestController
@RequestMapping("/simulated-metrics/services")
public class ServiceSimulatedMetricsController {

	private final ServiceSimulatedMetricsService serviceSimulatedMetricsService;

	public ServiceSimulatedMetricsController(ServiceSimulatedMetricsService serviceSimulatedMetricsService) {
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
	}

	@GetMapping
	public List<ServiceSimulatedMetricEntity> getServiceSimulatedMetrics() {
		return serviceSimulatedMetricsService.getServiceSimulatedMetrics();
	}

	@GetMapping("/{simulatedMetricName}")
	public ServiceSimulatedMetricEntity getServiceSimulatedMetric(@PathVariable String simulatedMetricName) {
		return serviceSimulatedMetricsService.getServiceSimulatedMetric(simulatedMetricName);
	}

	@PostMapping
	public ServiceSimulatedMetricEntity addServiceSimulatedMetric(
		@RequestBody ServiceSimulatedMetricEntity serviceSimulatedMetric) {
		Validation.validatePostRequest(serviceSimulatedMetric.getId());
		return serviceSimulatedMetricsService.addServiceSimulatedMetric(serviceSimulatedMetric);
	}

	@PutMapping("/{simulatedMetricName}")
	public ServiceSimulatedMetricEntity updateServiceSimulatedMetric(
		@PathVariable String simulatedMetricName,
		@RequestBody ServiceSimulatedMetricEntity simulatedMetric) {
		Validation.validatePutRequest(simulatedMetric.getId());
		return serviceSimulatedMetricsService.updateServiceSimulatedMetric(simulatedMetricName, simulatedMetric);
	}

	@DeleteMapping("/{simulatedMetricName}")
	public void deleteServiceSimulatedMetric(@PathVariable String simulatedMetricName) {
		serviceSimulatedMetricsService.deleteServiceSimulatedMetric(simulatedMetricName);
	}


	@GetMapping("/{simulatedMetricName}/services")
	public List<ServiceEntity> getServiceSimulatedMetricServices(@PathVariable String simulatedMetricName) {
		return serviceSimulatedMetricsService.getServices(simulatedMetricName);
	}

	@PostMapping("/{simulatedMetricName}/services")
	public void addServiceSimulatedMetricServices(@PathVariable String simulatedMetricName,
												  @RequestBody List<String> services) {
		serviceSimulatedMetricsService.addServices(simulatedMetricName, services);
	}

	@DeleteMapping("/{simulatedMetricName}/services")
	public void removeServiceSimulatedMetricServices(@PathVariable String simulatedMetricName,
													 @RequestBody List<String> services) {
		serviceSimulatedMetricsService.removeServices(simulatedMetricName, services);
	}

	@DeleteMapping("/{simulatedMetricName}/services/{serviceName}")
	public void removeServiceSimulatedMetricService(@PathVariable String simulatedMetricName,
													@PathVariable String serviceName) {
		serviceSimulatedMetricsService.removeService(simulatedMetricName, serviceName);
	}

}
