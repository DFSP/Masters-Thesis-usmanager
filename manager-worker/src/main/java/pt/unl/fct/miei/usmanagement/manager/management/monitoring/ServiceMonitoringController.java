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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceFieldAverage;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoring;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;

import java.util.List;

@RestController
@RequestMapping("/monitoring/services")
public class ServiceMonitoringController {

	private final ServicesMonitoringService servicesMonitoringService;

	public ServiceMonitoringController(ServicesMonitoringService servicesMonitoringService) {
		this.servicesMonitoringService = servicesMonitoringService;
	}

	@GetMapping
	public List<ServiceMonitoring> getServicesMonitoring() {
		return servicesMonitoringService.getServicesMonitoring();
	}

	@GetMapping("/{serviceName}")
	public List<ServiceMonitoring> getServiceMonitoring(@PathVariable String serviceName) {
		return servicesMonitoringService.getServiceMonitoring(serviceName);
	}

	@GetMapping("/{serviceName}/avg")
	public List<ServiceFieldAverage> getServiceFieldsAvg(@PathVariable String serviceName) {
		return servicesMonitoringService.getServiceFieldsAvg(serviceName);
	}

	@GetMapping("/{serviceName}/fields/{field}/avg")
	public ServiceFieldAverage getServiceFieldAverage(@PathVariable String serviceName, @PathVariable String field) {
		return servicesMonitoringService.getServiceFieldAverage(serviceName, field);
	}

	@GetMapping("/logs")
	public List<ServiceMonitoringLog> getServiceMonitoringLogs() {
		return servicesMonitoringService.getServiceMonitoringLogs();
	}

	@GetMapping("/logs/{serviceName}")
	public List<ServiceMonitoringLog> getServiceMonitoringLogsByServiceName(@PathVariable String serviceName) {
		return servicesMonitoringService.getServiceMonitoringLogsByServiceName(serviceName);
	}

	@GetMapping("/logs/containers/{containerId}")
	public List<ServiceMonitoringLog> getServiceMonitoringLogsByContainerId(@PathVariable String containerId) {
		return servicesMonitoringService.getServiceMonitoringLogsByContainerId(containerId);
	}

}
