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

package pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetrics;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
public class AppSimulatedMetricsService {

	private final AppsService appsService;
	private final KafkaService kafkaService;

	private final AppSimulatedMetrics appSimulatedMetrics;

	public AppSimulatedMetricsService(@Lazy AppsService appsService, KafkaService kafkaService,
									  AppSimulatedMetrics appSimulatedMetrics) {
		this.appsService = appsService;
		this.kafkaService = kafkaService;
		this.appSimulatedMetrics = appSimulatedMetrics;
	}

	public List<AppSimulatedMetric> getAppSimulatedMetrics() {
		return appSimulatedMetrics.findAll();
	}

	public List<AppSimulatedMetric> getAppSimulatedMetricByApp(String appName) {
		return appSimulatedMetrics.findByApp(appName);
	}

	public AppSimulatedMetric getAppSimulatedMetric(Long id) {
		return appSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(AppSimulatedMetric.class, "id", id.toString()));
	}

	public AppSimulatedMetric getAppSimulatedMetric(String simulatedMetricName) {
		return appSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(AppSimulatedMetric.class, "simulatedMetricName", simulatedMetricName));
	}

	public AppSimulatedMetric addAppSimulatedMetric(AppSimulatedMetric appSimulatedMetric) {
		checkAppSimulatedMetricDoesntExist(appSimulatedMetric);
		appSimulatedMetric = saveAppSimulatedMetric(appSimulatedMetric);
		/*AppSimulatedMetric kafkaAppSimulatedMetric = appSimulatedMetric;
		kafkaAppSimulatedMetric.setNew(true);
		kafkaService.sendAppSimulatedMetric(kafkaAppSimulatedMetric);*/
		kafkaService.sendAppSimulatedMetric(appSimulatedMetric);
		return appSimulatedMetric;
	}

	public AppSimulatedMetric updateAppSimulatedMetric(String simulatedMetricName,
													   AppSimulatedMetric newAppSimulatedMetric) {
		log.info("Updating simulated app metric {} with {}", simulatedMetricName,
			ToStringBuilder.reflectionToString(newAppSimulatedMetric));
		AppSimulatedMetric appSimulatedMetric = getAppSimulatedMetric(simulatedMetricName);
		EntityUtils.copyValidProperties(newAppSimulatedMetric, appSimulatedMetric);
		appSimulatedMetric = saveAppSimulatedMetric(appSimulatedMetric);
		kafkaService.sendAppSimulatedMetric(appSimulatedMetric);
		return appSimulatedMetric;
	}

	public AppSimulatedMetric saveAppSimulatedMetric(AppSimulatedMetric appSimulatedMetric) {
		log.info("Saving appSimulatedMetric {}", ToStringBuilder.reflectionToString(appSimulatedMetric));
		return appSimulatedMetrics.save(appSimulatedMetric);
	}

	public AppSimulatedMetric addOrUpdateSimulatedMetric(AppSimulatedMetric simulatedMetric) {
		Optional<AppSimulatedMetric> simulatedMetricOptional = appSimulatedMetrics.findById(simulatedMetric.getId());
		if (simulatedMetricOptional.isPresent()) {
			AppSimulatedMetric existingSimulatedMetric = simulatedMetricOptional.get();
			Set<App> apps = simulatedMetric.getApps();
			if (apps != null) {
				existingSimulatedMetric.getApps().retainAll(apps);
				existingSimulatedMetric.getApps().addAll(apps);
			}
			EntityUtils.copyValidProperties(simulatedMetric, existingSimulatedMetric);
			return saveAppSimulatedMetric(existingSimulatedMetric);
		}
		else {
			return saveAppSimulatedMetric(simulatedMetric);
		}
	}

	public void deleteAppSimulatedMetric(Long id) {
		log.info("Deleting simulated app metric {}", id);
		AppSimulatedMetric appSimulatedMetric = getAppSimulatedMetric(id);
		deleteAppSimulatedMetric(appSimulatedMetric);
	}

	public void deleteAppSimulatedMetric(String simulatedMetricName) {
		log.info("Deleting simulated app metric {}", simulatedMetricName);
		AppSimulatedMetric appSimulatedMetric = getAppSimulatedMetric(simulatedMetricName);
		deleteAppSimulatedMetric(appSimulatedMetric);
	}

	public void deleteAppSimulatedMetric(AppSimulatedMetric simulatedMetric) {
		simulatedMetric.removeAssociations();
		appSimulatedMetrics.delete(simulatedMetric);
	}

	public List<App> getApps(String simulatedMetricName) {
		checkAppSimulatedMetricExists(simulatedMetricName);
		return appSimulatedMetrics.getApps(simulatedMetricName);
	}

	public App getApp(String simulatedMetricName, String appName) {
		checkAppSimulatedMetricExists(simulatedMetricName);
		return appSimulatedMetrics.getApp(simulatedMetricName, appName).orElseThrow(() ->
			new EntityNotFoundException(App.class, "appName", appName));
	}

	public void addApp(String simulatedMetricName, String appName) {
		addApps(simulatedMetricName, List.of(appName));
	}

	public void addApps(String simulatedMetricName, List<String> appNames) {
		log.info("Adding apps {} to simulated metric {}", appNames, simulatedMetricName);
		AppSimulatedMetric appMetric = getAppSimulatedMetric(simulatedMetricName);
		appNames.forEach(appName -> {
			App app = appsService.getApp(appName);
			app.addAppSimulatedMetric(appMetric);
		});
		appSimulatedMetrics.save(appMetric);
	}

	public void removeApp(String simulatedMetricName, String appName) {
		removeApps(simulatedMetricName, List.of(appName));
	}

	public void removeApps(String simulatedMetricName, List<String> appNames) {
		log.info("Removing apps {} from simulated metric {}", appNames, simulatedMetricName);
		AppSimulatedMetric appMetric = getAppSimulatedMetric(simulatedMetricName);
		appNames.forEach(appName ->
			appsService.getApp(appName).removeAppSimulatedMetric(appMetric));
		appSimulatedMetrics.save(appMetric);
	}

	public Double randomizeFieldValue(AppSimulatedMetric appSimulatedMetric) {
		Random random = new Random();
		double minValue = appSimulatedMetric.getMinimumValue();
		double maxValue = appSimulatedMetric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}

	private void checkAppSimulatedMetricExists(String simulatedMetricName) {
		if (!appSimulatedMetrics.hasAppSimulatedMetric(simulatedMetricName)) {
			throw new EntityNotFoundException(AppSimulatedMetric.class, "simulatedMetricName", simulatedMetricName);
		}
	}

	private void checkAppSimulatedMetricDoesntExist(AppSimulatedMetric appSimulatedMetric) {
		String name = appSimulatedMetric.getName();
		if (appSimulatedMetrics.hasAppSimulatedMetric(name)) {
			throw new DataIntegrityViolationException("Simulated app metric '" + name + "' already exists");
		}
	}
	
}
