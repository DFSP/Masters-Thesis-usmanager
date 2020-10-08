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

package pt.unl.fct.miei.usmanagement.manager.master.management.apps;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;
import pt.unl.fct.miei.usmanagement.manager.service.management.apps.AppsService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apps")
public final class AppsController {

	private final AppsService appsService;

	public AppsController(AppsService appsService) {
		this.appsService = appsService;
	}

	@GetMapping
	public List<AppEntity> getApps() {
		return appsService.getApps();
	}

	@GetMapping("/{appName}")
	public AppEntity getApp(@PathVariable String appName) {
		return appsService.getApp(appName);
	}

	@PostMapping
	public AppEntity addApp(@RequestBody AppEntity app) {
		Validation.validatePostRequest(app.getId());
		return appsService.addApp(app);
	}

	@PutMapping("/{appName}")
	public AppEntity updateApp(@PathVariable String appName, @RequestBody AppEntity app) {
		Validation.validatePutRequest(app.getId());
		return appsService.updateApp(appName, app);
	}

	@DeleteMapping("/{appName}")
	public void deleteApp(@PathVariable String appName) {
		appsService.deleteApp(appName);
	}

	@GetMapping("/{appName}/services")
	public List<AppServiceEntity> getAppServices(@PathVariable String appName) {
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

	//TODO fix client
	@PostMapping("/{appName}/launch")
	public Map<String, List<ContainerEntity>> launch(@PathVariable String appName, @RequestBody Coordinates coordinates) {
		return appsService.launch(appName, coordinates);
	}

}
