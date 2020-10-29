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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetricsRepository;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AppSimulatedMetricsService {

	private final AppsService appsService;

	private final AppSimulatedMetricsRepository appSimulatedMetrics;

	public AppSimulatedMetricsService(@Lazy AppsService appsService, AppSimulatedMetricsRepository appSimulatedMetrics) {
		this.appsService = appsService;
		this.appSimulatedMetrics = appSimulatedMetrics;
	}

	public List<AppSimulatedMetricEntity> getAppSimulatedMetrics() {
		return appSimulatedMetrics.findAll();
	}

	public List<AppSimulatedMetricEntity> getAppSimulatedMetricByApp(String appName) {
		return appSimulatedMetrics.findByApp(appName);
	}

	public AppSimulatedMetricEntity getAppSimulatedMetric(Long id) {
		return appSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(AppSimulatedMetricEntity.class, "id", id.toString()));
	}

	public AppSimulatedMetricEntity getAppSimulatedMetric(String simulatedMetricName) {
		return appSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(AppSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
	}

	public AppSimulatedMetricEntity addAppSimulatedMetric(AppSimulatedMetricEntity appSimulatedMetric) {
		checkAppSimulatedMetricDoesntExist(appSimulatedMetric);
		log.info("Saving simulated app metric {}", ToStringBuilder.reflectionToString(appSimulatedMetric));
		return appSimulatedMetrics.save(appSimulatedMetric);
	}

	public AppSimulatedMetricEntity updateAppSimulatedMetric(String simulatedMetricName,
																	 AppSimulatedMetricEntity newAppSimulatedMetric) {
		log.info("Updating simulated app metric {} with {}", simulatedMetricName,
			ToStringBuilder.reflectionToString(newAppSimulatedMetric));
		AppSimulatedMetricEntity appSimulatedMetric = getAppSimulatedMetric(simulatedMetricName);
		ObjectUtils.copyValidProperties(newAppSimulatedMetric, appSimulatedMetric);
		return appSimulatedMetrics.save(appSimulatedMetric);
	}

	public void deleteAppSimulatedMetric(String simulatedMetricName) {
		log.info("Deleting simulated app metric {}", simulatedMetricName);
		AppSimulatedMetricEntity appSimulatedMetric = getAppSimulatedMetric(simulatedMetricName);
		appSimulatedMetric.removeAssociations();
		appSimulatedMetrics.delete(appSimulatedMetric);
	}
	
	public List<AppEntity> getApps(String simulatedMetricName) {
		checkAppSimulatedMetricExists(simulatedMetricName);
		return appSimulatedMetrics.getApps(simulatedMetricName);
	}

	public AppEntity getApp(String simulatedMetricName, String appName) {
		checkAppSimulatedMetricExists(simulatedMetricName);
		return appSimulatedMetrics.getApp(simulatedMetricName, appName).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "appName", appName));
	}

	public void addApp(String simulatedMetricName, String appName) {
		addApps(simulatedMetricName, List.of(appName));
	}

	public void addApps(String simulatedMetricName, List<String> appNames) {
		log.info("Adding apps {} to simulated metric {}", appNames, simulatedMetricName);
		AppSimulatedMetricEntity appMetric = getAppSimulatedMetric(simulatedMetricName);
		appNames.forEach(appName -> {
			AppEntity app = appsService.getApp(appName);
			app.addAppSimulatedMetric(appMetric);
		});
		appSimulatedMetrics.save(appMetric);
	}

	public void removeApp(String simulatedMetricName, String appName) {
		removeApps(simulatedMetricName, List.of(appName));
	}

	public void removeApps(String simulatedMetricName, List<String> appNames) {
		log.info("Removing apps {} from simulated metric {}", appNames, simulatedMetricName);
		AppSimulatedMetricEntity appMetric = getAppSimulatedMetric(simulatedMetricName);
		appNames.forEach(appName ->
			appsService.getApp(appName).removeAppSimulatedMetric(appMetric));
		appSimulatedMetrics.save(appMetric);
	}

	public Double randomizeFieldValue(AppSimulatedMetricEntity appSimulatedMetric) {
		Random random = new Random();
		double minValue = appSimulatedMetric.getMinimumValue();
		double maxValue = appSimulatedMetric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}
	
	private void checkAppSimulatedMetricExists(String simulatedMetricName) {
		if (!appSimulatedMetrics.hasAppSimulatedMetric(simulatedMetricName)) {
			throw new EntityNotFoundException(AppSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName);
		}
	}

	private void checkAppSimulatedMetricDoesntExist(AppSimulatedMetricEntity appSimulatedMetric) {
		String name = appSimulatedMetric.getName();
		if (appSimulatedMetrics.hasAppSimulatedMetric(name)) {
			throw new DataIntegrityViolationException("Simulated app metric '" + name + "' already exists");
		}
	}
	
}
