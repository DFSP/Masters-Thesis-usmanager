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

package pt.unl.fct.miei.usmanagement.manager.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.dtos.ServiceDependencyDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mappers.ServiceDependencyMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mappers.ServiceMapper;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.services.AddServiceApp;
import pt.unl.fct.miei.usmanagement.manager.services.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/services")
public class ServicesController {

	private final ServicesService servicesService;
	private final CycleAvoidingMappingContext context;

	public ServicesController(ServicesService servicesService, CycleAvoidingMappingContext context) {
		this.servicesService = servicesService;
		this.context = context;
	}

	@GetMapping
	public List<ServiceDTO> getServices() {
		return ServiceMapper.MAPPER.fromServices(servicesService.getServices(), context);
	}

	@GetMapping("/{serviceName}")
	public ServiceDTO getService(@PathVariable String serviceName) {
		return ServiceMapper.MAPPER.fromService(servicesService.getService(serviceName), context);
	}

	@PostMapping
	public ServiceDTO addService(@RequestBody ServiceDTO serviceDTO) {
		Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
		Validation.validatePostRequest(service.getId());
		return ServiceMapper.MAPPER.fromService(servicesService.addService(service), context);
	}

	@PutMapping("/{serviceName}")
	public ServiceDTO updateService(@PathVariable String serviceName, @RequestBody ServiceDTO serviceDTO) {
		Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
		Validation.validatePutRequest(service.getId());
		return ServiceMapper.MAPPER.fromService(servicesService.updateService(serviceName, service), context);
	}

	@DeleteMapping("/{serviceName}")
	public void deleteService(@PathVariable String serviceName) {
		servicesService.deleteService(serviceName);
	}

	@GetMapping("/{serviceName}/apps")
	public List<App> getServiceApps(@PathVariable String serviceName) {
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

	@GetMapping("/{serviceName}/dependencies/services")
	public List<ServiceDTO> getDependenciesServices(@PathVariable String serviceName) {
		return ServiceMapper.MAPPER.fromServices(servicesService.getDependenciesServices(serviceName), context);
	}

	@GetMapping("/{serviceName}/dependencies")
	public List<ServiceDependencyDTO> getDependencies(@PathVariable String serviceName) {
		return ServiceDependencyMapper.MAPPER.fromServiceDependencies(servicesService.getDependencies(serviceName), context);
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
	public List<Service> getServiceDependents(@PathVariable String serviceName) {
		return servicesService.getDependents(serviceName);
	}

	@GetMapping("/{serviceName}/predictions")
	public List<ServiceEventPrediction> getServicePredictions(@PathVariable String serviceName) {
		return servicesService.getPredictions(serviceName);
	}

	@PostMapping("/{serviceName}/predictions")
	public List<ServiceEventPrediction> addServicePredictions(@PathVariable String serviceName,
															  @RequestBody ServiceEventPrediction[] predictions) {
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
	public List<ServiceRule> getServiceRules(@PathVariable String serviceName) {
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
	public List<ServiceSimulatedMetric> getServiceSimulatedMetrics(@PathVariable String serviceName) {
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
