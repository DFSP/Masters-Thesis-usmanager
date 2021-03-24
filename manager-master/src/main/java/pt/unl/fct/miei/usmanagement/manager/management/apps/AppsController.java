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

package pt.unl.fct.miei.usmanagement.manager.management.apps;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.config.ManagerServicesConfiguration;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;
import pt.unl.fct.miei.usmanagement.manager.Mode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apps")
public class AppsController {

	private final AppsService appsService;
	private final WorkerManagersService workerManagersService;
	private final ManagerServicesConfiguration managerServicesConfiguration;

	public AppsController(AppsService appsService, WorkerManagersService workerManagersService,
						  ManagerServicesConfiguration managerServicesConfiguration) {
		this.appsService = appsService;
		this.workerManagersService = workerManagersService;
		this.managerServicesConfiguration = managerServicesConfiguration;
	}

	@GetMapping
	public List<App> getApps() {
		return appsService.getApps();
	}

	@GetMapping("/{appName}")
	public App getApp(@PathVariable String appName) {
		return appsService.getApp(appName);
	}

	@PostMapping
	public App addApp(@RequestBody App app) {
		Validation.validatePostRequest(app.getId());
		return appsService.addApp(app);
	}

	@PutMapping("/{appName}")
	public App updateApp(@PathVariable String appName, @RequestBody App app) {
		Validation.validatePutRequest(app.getId());
		return appsService.updateApp(appName, app);
	}

	@DeleteMapping("/{appName}")
	public void deleteApp(@PathVariable String appName) {
		appsService.deleteApp(appName);
	}

	@GetMapping("/{appName}/services")
	public List<AppService> getAppServices(@PathVariable String appName) {
		return appsService.getServices(appName);
	}

	@PostMapping("/{appName}/services")
	public void addAppServices(@PathVariable String appName, @RequestBody AddAppService[] services) {
		Map<String, Integer> serviceOrders = Arrays.stream(services).collect(
			Collectors.toMap(addAppService -> addAppService.getService().getServiceName(), AddAppService::getLaunchOrder));
		appsService.addServices(appName, serviceOrders);
	}

	@DeleteMapping("/{appName}/services")
	public void removeAppServices(@PathVariable String appName, @RequestBody String[] services) {
		appsService.removeServices(appName, Arrays.asList(services));
	}

	@DeleteMapping("/{appName}/services/{serviceName}")
	public void removeAppService(@PathVariable String appName, @PathVariable String serviceName) {
		appsService.removeService(appName, serviceName);
	}

	@PostMapping("/{appName}/launch")
	public Map<String, List<Container>> launch(@PathVariable String appName, @RequestBody Coordinates coordinates) {
<<<<<<< HEAD:usmanager/manager/manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/management/apps/AppsController.java
		return managerServicesConfiguration.getMode() == Mode.LOCAL
			? appsService.launch(appName, coordinates)
			: workerManagersService.launchApp(appName, coordinates);
=======
		//return appsService.launch(appName, coordinates);
		return workerManagersService.launchApp(appName, coordinates);
>>>>>>> 02b9fb7e15ad1655a7a18f29507ba6f0ca23dee7:manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/management/apps/AppsController.java
	}

	@GetMapping("/{appId}/rules")
	public List<AppRule> addAppRule(@PathVariable String appId) {
		return appsService.getRules(appId);
	}

	@PostMapping("/{appId}/rules")
	public void addAppRules(@PathVariable String appId, @RequestBody String[] rules) {
		appsService.addRules(appId, Arrays.asList(rules));
	}

	@DeleteMapping("/{appId}/rules")
	public void removeAppRules(@PathVariable String appId, @RequestBody String[] rules) {
		appsService.removeRules(appId, Arrays.asList(rules));
	}

	@DeleteMapping("/{appId}/rules/{ruleName}")
	public void removeAppRule(@PathVariable String appId, @PathVariable String ruleName) {
		appsService.removeRule(appId, ruleName);
	}

	@GetMapping("/{appId}/simulated-metrics")
	public List<AppSimulatedMetric> getAppSimulatedMetrics(@PathVariable String appId) {
		return appsService.getSimulatedMetrics(appId);
	}

	@PostMapping("/{appId}/simulated-metrics")
	public void addAppSimulatedMetrics(@PathVariable String appId, @RequestBody String[] simulatedMetrics) {
		appsService.addSimulatedMetrics(appId, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{appId}/simulated-metrics")
	public void removeAppSimulatedMetrics(@PathVariable String appId, @RequestBody String[] simulatedMetrics) {
		appsService.removeSimulatedMetrics(appId, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{appId}/simulated-metrics/{simulatedMetricName}")
	public void removeAppSimulatedMetric(@PathVariable String appId, @PathVariable String simulatedMetricName) {
		appsService.removeSimulatedMetric(appId, simulatedMetricName);
	}

}
