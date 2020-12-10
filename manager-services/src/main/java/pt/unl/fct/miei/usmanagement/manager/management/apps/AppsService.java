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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;
import pt.unl.fct.miei.usmanagement.manager.apps.Apps;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceOrder;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.stereotype.Service
public class AppsService {

	private final ServicesService servicesService;
	private final AppRulesService appRulesService;
	private final AppSimulatedMetricsService appSimulatedMetricsService;
	private final ContainersService containersService;
	private final KafkaService kafkaService;

	private final Apps apps;

	public AppsService(ServicesService servicesService, AppRulesService appRulesService,
					   AppSimulatedMetricsService appSimulatedMetricsService, ContainersService containersService,
					   KafkaService kafkaService, Apps apps) {
		this.servicesService = servicesService;
		this.appRulesService = appRulesService;
		this.appSimulatedMetricsService = appSimulatedMetricsService;
		this.containersService = containersService;
		this.kafkaService = kafkaService;
		this.apps = apps;
	}

	public List<App> getApps() {
		return apps.findAll();
	}

	public App saveApp(App app) {
		log.info("Saving app {}", app.getName());
		return apps.save(app);
	}

	public App getApp(Long id) {
		return apps.findById(id).orElseThrow(() ->
			new EntityNotFoundException(App.class, "id", id.toString()));
	}

	public App getApp(String appName) {
		return apps.findByNameIgnoreCase(appName).orElseThrow(() ->
			new EntityNotFoundException(App.class, "name", appName));
	}

	public App getAppAndEntities(String appName) {
		return apps.findByNameWithEntities(appName).orElseThrow(() ->
			new EntityNotFoundException(App.class, "name", appName));
	}

	public App addApp(App app) {
		checkAppDoesntExist(app);
		app = saveApp(app);
		/*App kafkaApp = app;
		kafkaApp.setNew(true);
		kafkaService.sendApp(kafkaApp);*/
		kafkaService.sendApp(app);
		return app;
	}

	public App updateApp(String appName, App newApp) {
		App app = getAppAndEntities(appName);
		log.info("Updating app {} with {}", ToStringBuilder.reflectionToString(app), ToStringBuilder.reflectionToString(newApp));
		EntityUtils.copyValidProperties(newApp, app);
		app = saveApp(app);
		kafkaService.sendApp(app);
		return app;
	}

	public void deleteApp(Long id) {
		log.info("Deleting app {}", id);
		apps.deleteById(id);
	}

	public void deleteApp(String name) {
		App app = getApp(name);
		apps.delete(app);
		kafkaService.sendDeleteApp(app);
	}

	public List<AppService> getServices(String appName) {
		checkAppExists(appName);
		return apps.getServices(appName);
	}

	public void addService(String appName, AppService appService) {
		App app = getApp(appName);
		addService(app, appService);
	}

	public App addService(App app, AppService appService) {
		app.getAppServices().remove(appService);
		app = app.toBuilder().appService(appService).build();
		return apps.save(app);
	}

	public void addService(String appName, String serviceName, int order) {
		App app = getAppAndEntities(appName);
		Service service = servicesService.getService(serviceName);
		AppService appService = AppService.builder().id(new AppServiceKey(app.getId(), service.getId())).app(app).service(service).launchOrder(order).build();
		app = addService(app, appService);
		kafkaService.sendApp(app);
	}

	public void addServices(String appName, Map<String, Integer> services) {
		services.forEach((service, launchOrder) -> addService(appName, service, launchOrder));
	}

	public void addServices(App app, Set<AppService> appServices) {
		appServices.forEach(appService -> addService(app, appService));
	}

	public void removeService(String appName, String service) {
		removeServices(appName, List.of(service));
	}

	public void removeServices(String appName, List<String> services) {
		App app = getAppAndEntities(appName);
		log.info("Removing services {}", services);
		app.getAppServices().removeIf(appService -> services.contains(servicesService.getService(appService.getId().getServiceId()).getServiceName()));
		app = apps.save(app);
		kafkaService.sendApp(app);
	}

	public Map<String, List<Container>> launch(String appName, Coordinates coordinates) {
		log.info("Launching app {} at latitude {} and longitude {}", appName, coordinates.getLatitude(), coordinates.getLongitude());
		List<Service> services = apps.getServicesOrder(appName).stream()
			.filter(serviceOrder -> serviceOrder.getService().getServiceType() != ServiceTypeEnum.DATABASE)
			.map(ServiceOrder::getService)
			.collect(Collectors.toList());
		return containersService.launchApp(services, coordinates);
	}

	public List<AppRule> getRules(String appId) {
		checkAppExists(appId);
		return apps.getRules(appId);
	}

	public void addRule(String appId, String ruleName) {
		checkAppExists(appId);
		appRulesService.addApp(ruleName, appId);
	}

	public void addRules(String appId, List<String> ruleNames) {
		checkAppExists(appId);
		ruleNames.forEach(rule -> appRulesService.addApp(rule, appId));
	}

	public void removeRule(String appId, String ruleName) {
		checkAppExists(appId);
		appRulesService.removeApp(ruleName, appId);
	}

	public void removeRules(String appId, List<String> ruleNames) {
		checkAppExists(appId);
		ruleNames.forEach(rule -> appRulesService.removeApp(rule, appId));
	}

	public List<AppSimulatedMetric> getSimulatedMetrics(String appId) {
		checkAppExists(appId);
		return apps.getSimulatedMetrics(appId);
	}

	public AppSimulatedMetric getSimulatedMetric(String appId, String simulatedMetricName) {
		checkAppExists(appId);
		return apps.getSimulatedMetric(appId, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(AppSimulatedMetric.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String appId, String simulatedMetricName) {
		checkAppExists(appId);
		appSimulatedMetricsService.addApp(simulatedMetricName, appId);
	}

	public void addSimulatedMetrics(String appId, List<String> simulatedMetricNames) {
		checkAppExists(appId);
		simulatedMetricNames.forEach(simulatedMetric ->
			appSimulatedMetricsService.addApp(simulatedMetric, appId));
	}

	public void removeSimulatedMetric(String appId, String simulatedMetricName) {
		checkAppExists(appId);
		appSimulatedMetricsService.removeApp(simulatedMetricName, appId);
	}

	public void removeSimulatedMetrics(String appId, List<String> simulatedMetricNames) {
		checkAppExists(appId);
		simulatedMetricNames.forEach(simulatedMetric ->
			appSimulatedMetricsService.removeApp(simulatedMetric, appId));
	}

	public boolean hasApp(String name) {
		return apps.hasApp(name);
	}

	private void checkAppExists(String appName) {
		if (!apps.hasApp(appName)) {
			throw new EntityNotFoundException(App.class, "name", appName);
		}
	}

	private void checkAppDoesntExist(App app) {
		String name = app.getName();
		if (apps.hasApp(name)) {
			throw new DataIntegrityViolationException("App '" + name + "' already exists");
		}
	}

}
