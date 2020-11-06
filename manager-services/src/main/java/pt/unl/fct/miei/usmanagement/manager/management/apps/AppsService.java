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
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.apps.AppRepository;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceOrder;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceType;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppsService {

	private final ServicesService servicesService;
	private final AppRulesService appRulesService;
	private final AppSimulatedMetricsService appSimulatedMetricsService;
	private final ContainersService containersService;

	private final AppRepository apps;

	public AppsService(ServicesService servicesService, AppRulesService appRulesService,
					   AppSimulatedMetricsService appSimulatedMetricsService, ContainersService containersService,
					   AppRepository apps) {
		this.servicesService = servicesService;
		this.appRulesService = appRulesService;
		this.appSimulatedMetricsService = appSimulatedMetricsService;
		this.containersService = containersService;
		this.apps = apps;
	}

	public List<AppEntity> getApps() {
		return apps.findAll();
	}

	public AppEntity getApp(Long id) {
		return apps.findById(id).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "id", id.toString()));
	}

	public AppEntity getApp(String appName) {
		return apps.findByNameIgnoreCase(appName).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "name", appName));
	}

	public AppEntity addApp(AppEntity app) {
		checkAppDoesntExist(app);
		log.info("Saving app {}", ToStringBuilder.reflectionToString(app));
		return apps.save(app);
	}

	public AppEntity updateApp(String appName, AppEntity newApp) {
		AppEntity app = getApp(appName);
		log.info("Updating app {} with {}",
			ToStringBuilder.reflectionToString(app), ToStringBuilder.reflectionToString(newApp));
		log.info("Service before copying properties: {}",
			ToStringBuilder.reflectionToString(app));
		ObjectUtils.copyValidProperties(newApp, app);
		log.info("Service after copying properties: {}",
			ToStringBuilder.reflectionToString(app));
		return apps.save(app);
	}

	public void deleteApp(String name) {
		AppEntity app = getApp(name);
		apps.delete(app);
	}

	public List<AppServiceEntity> getServices(String appName) {
		checkAppExists(appName);
		return apps.getServices(appName);
	}

	public void addService(String appName, String serviceName, int order) {
		AppEntity app = getApp(appName);
		ServiceEntity service = servicesService.getService(serviceName);
		AppServiceEntity appService = AppServiceEntity.builder()
			.app(app)
			.service(service)
			.launchOrder(order)
			.build();
		app = app.toBuilder().appService(appService).build();
		apps.save(app);
	}

	public void addServices(String appName, Map<String, Integer> services) {
		services.forEach((service, launchOrder) -> addService(appName, service, launchOrder));
	}

	public void removeService(String appName, String service) {
		removeServices(appName, List.of(service));
	}

	public void removeServices(String appName, List<String> services) {
		AppEntity app = getApp(appName);
		log.info("Removing services {}", services);
		app.getAppServices().removeIf(service -> services.contains(service.getService().getServiceName()));
		apps.save(app);
	}

	public Map<String, List<ContainerEntity>> launch(String appName, Coordinates coordinates) {
		log.info("Launching app {} at latitude {} and longitude {}", appName, coordinates.getLatitude(), coordinates.getLongitude());
		List<ServiceEntity> services = apps.getServicesOrder(appName).stream()
			.filter(serviceOrder -> serviceOrder.getService().getServiceType() != ServiceType.DATABASE)
			.map(ServiceOrder::getService)
			.collect(Collectors.toList());
		return containersService.launchApp(services, coordinates);
	}

	public List<AppRuleEntity> getRules(String appId) {
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

	public List<AppSimulatedMetricEntity> getSimulatedMetrics(String appId) {
		checkAppExists(appId);
		return apps.getSimulatedMetrics(appId);
	}

	public AppSimulatedMetricEntity getSimulatedMetric(String appId, String simulatedMetricName) {
		checkAppExists(appId);
		return apps.getSimulatedMetric(appId, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(AppSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
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

	private void checkAppExists(String appName) {
		if (!apps.hasApp(appName)) {
			throw new EntityNotFoundException(AppEntity.class, "name", appName);
		}
	}

	private void checkAppDoesntExist(AppEntity app) {
		String name = app.getName();
		if (apps.hasApp(name)) {
			throw new DataIntegrityViolationException("App '" + name + "' already exists");
		}
	}

}