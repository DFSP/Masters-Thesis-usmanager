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

package pt.unl.fct.miei.usmanagement.manager.master.management.services;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.prediction.ServiceEventPredictionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;
import pt.unl.fct.miei.usmanagement.manager.services.management.services.AddServiceApp;
import pt.unl.fct.miei.usmanagement.manager.services.management.services.ServicesService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/services")
public class ServicesController {

	private final ServicesService servicesService;

	public ServicesController(ServicesService servicesService) {
		this.servicesService = servicesService;
	}

	@GetMapping
	public List<ServiceEntity> getServices() {
		return servicesService.getServices();
	}

	@GetMapping("/{serviceName}")
	public ServiceEntity getService(@PathVariable String serviceName) {
		return servicesService.getService(serviceName);
	}

	@PostMapping
	public ServiceEntity addService(@RequestBody ServiceEntity service) {
		Validation.validatePostRequest(service.getId());
		return servicesService.addService(service);
	}

	@PutMapping("/{serviceName}")
	public ServiceEntity updateService(@PathVariable String serviceName, @RequestBody ServiceEntity service) {
		Validation.validatePutRequest(service.getId());
		return servicesService.updateService(serviceName, service);
	}

	@DeleteMapping("/{serviceName}")
	public void deleteService(@PathVariable String serviceName) {
		servicesService.deleteService(serviceName);
	}

	@GetMapping("/{serviceName}/apps")
	public List<AppEntity> getServiceApps(@PathVariable String serviceName) {
		return servicesService.getApps(serviceName);
	}

	@PostMapping("/{serviceName}/apps")
	public void addServiceApps(@PathVariable String serviceName, @RequestBody AddServiceApp[] addServiceApps) {
		servicesService.addApps(serviceName, Arrays.asList(addServiceApps));
	}

	@DeleteMapping("/{serviceName}/apps")
	public void removeServiceApps(@PathVariable String serviceName, @RequestBody String[] apps) {
		servicesService.removeApps(serviceName, Arrays.asList(apps));
	}

	@DeleteMapping("/{serviceName}/apps/{appName}")
	public void removeServiceApp(@PathVariable String serviceName, @PathVariable String appName) {
		servicesService.removeApp(serviceName, appName);
	}

	@GetMapping("/{serviceName}/dependencies")
	public List<ServiceEntity> getServiceDependencies(@PathVariable String serviceName) {
		return servicesService.getDependencies(serviceName);
	}

	@PostMapping("/{serviceName}/dependencies")
	public void addServiceDependencies(@PathVariable String serviceName, @RequestBody String[] dependencies) {
		servicesService.addDependencies(serviceName, Arrays.asList(dependencies));
	}

	@DeleteMapping("/{serviceName}/dependencies")
	public void removeServiceDependencies(@PathVariable String serviceName, @RequestBody String[] dependencies) {
		servicesService.removeDependencies(serviceName, Arrays.asList(dependencies));
	}

	@DeleteMapping("/{serviceName}/dependencies/{dependencyName}")
	public void removeServiceDependency(@PathVariable String serviceName, @PathVariable String dependencyName) {
		servicesService.removeDependency(serviceName, dependencyName);
	}

	@GetMapping("/{serviceName}/dependents")
	public List<ServiceEntity> getServiceDependents(@PathVariable String serviceName) {
		return servicesService.getDependents(serviceName);
	}

	@GetMapping("/{serviceName}/predictions")
	public List<ServiceEventPredictionEntity> getServicePredictions(@PathVariable String serviceName) {
		return servicesService.getPredictions(serviceName);
	}

	@PostMapping("/{serviceName}/predictions")
	public List<ServiceEventPredictionEntity> addServicePredictions(@PathVariable String serviceName,
																	@RequestBody ServiceEventPredictionEntity[] predictions) {
		return servicesService.addPredictions(serviceName, Arrays.asList(predictions));
	}

	@DeleteMapping("/{serviceName}/predictions")
	public void removeServicePredictions(@PathVariable String serviceName, @RequestBody String[] predictions) {
		servicesService.removePredictions(serviceName, Arrays.asList(predictions));
	}

	@DeleteMapping("/{serviceName}/predictions/{predictionName}")
	public void removeServicePrediction(@PathVariable String serviceName, @PathVariable String predictionName) {
		servicesService.removePrediction(serviceName, predictionName);
	}

	@GetMapping("/{serviceName}/rules")
	public List<ServiceRuleEntity> getServiceRules(@PathVariable String serviceName) {
		return servicesService.getRules(serviceName);
	}

	@PostMapping("/{serviceName}/rules")
	public void addServiceRules(@PathVariable String serviceName, @RequestBody String[] rules) {
		servicesService.addRules(serviceName, Arrays.asList(rules));
	}

	@DeleteMapping("/{serviceName}/rules")
	public void removeServiceRules(@PathVariable String serviceName, @RequestBody String[] rules) {
		servicesService.removeRules(serviceName, Arrays.asList(rules));
	}

	@DeleteMapping("/{serviceName}/rules/{ruleName}")
	public void removeServiceRule(@PathVariable String serviceName, @PathVariable String ruleName) {
		servicesService.removeRule(serviceName, ruleName);
	}

	@GetMapping("/{serviceName}/simulated-metrics")
	public List<ServiceSimulatedMetricEntity> getServiceSimulatedMetrics(@PathVariable String serviceName) {
		return servicesService.getSimulatedMetrics(serviceName);
	}

	@PostMapping("/{serviceName}/simulated-metrics")
	public void addServiceSimulatedMetrics(@PathVariable String serviceName, @RequestBody String[] simulatedMetrics) {
		servicesService.addSimulatedMetrics(serviceName, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{serviceName}/simulated-metrics")
	public void removeServiceSimulatedMetrics(@PathVariable String serviceName, @RequestBody String[] simulatedMetrics) {
		servicesService.removeSimulatedMetrics(serviceName, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{serviceName}/simulated-metrics/{simulatedMetricName}")
	public void removeServiceSimulatedMetric(@PathVariable String serviceName, @PathVariable String simulatedMetricName) {
		servicesService.removeSimulatedMetric(serviceName, simulatedMetricName);
	}

}
