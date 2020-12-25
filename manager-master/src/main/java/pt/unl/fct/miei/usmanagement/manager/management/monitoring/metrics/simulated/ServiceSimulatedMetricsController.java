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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/simulated-metrics/services")
public class ServiceSimulatedMetricsController {

	private final ServiceSimulatedMetricsService serviceSimulatedMetricsService;

	public ServiceSimulatedMetricsController(ServiceSimulatedMetricsService serviceSimulatedMetricsService) {
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
	}

	@GetMapping
	public List<ServiceSimulatedMetric> getServiceSimulatedMetrics() {
		return serviceSimulatedMetricsService.getServiceSimulatedMetrics();
	}

	@GetMapping("/{simulatedMetricName}")
	public ServiceSimulatedMetric getServiceSimulatedMetric(@PathVariable String simulatedMetricName) {
		return serviceSimulatedMetricsService.getServiceSimulatedMetric(simulatedMetricName);
	}

	@PostMapping
	public ServiceSimulatedMetric addServiceSimulatedMetric(@RequestBody ServiceSimulatedMetric serviceSimulatedMetric) {
		Validation.validatePostRequest(serviceSimulatedMetric.getId());
		return serviceSimulatedMetricsService.addServiceSimulatedMetric(serviceSimulatedMetric);
	}

	@PutMapping("/{simulatedMetricName}")
	public ServiceSimulatedMetric updateServiceSimulatedMetric(@PathVariable String simulatedMetricName,
															   @RequestBody ServiceSimulatedMetric simulatedMetric) {
		Validation.validatePutRequest(simulatedMetric.getId());
		return serviceSimulatedMetricsService.updateServiceSimulatedMetric(simulatedMetricName, simulatedMetric);
	}

	@DeleteMapping("/{simulatedMetricName}")
	public void deleteServiceSimulatedMetric(@PathVariable String simulatedMetricName) {
		serviceSimulatedMetricsService.deleteServiceSimulatedMetric(simulatedMetricName);
	}

	@GetMapping("/{simulatedMetricName}/services")
	public List<Service> getServiceSimulatedMetricServices(@PathVariable String simulatedMetricName) {
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
