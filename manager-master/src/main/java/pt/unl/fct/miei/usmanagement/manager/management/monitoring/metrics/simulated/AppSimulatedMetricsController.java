/* * MIT License
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
 * */

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.List;

@RestController
@RequestMapping("/simulated-metrics/apps")
public class AppSimulatedMetricsController {

	private final AppSimulatedMetricsService appSimulatedMetricsService;

	public AppSimulatedMetricsController(AppSimulatedMetricsService appSimulatedMetricsService) {
		this.appSimulatedMetricsService = appSimulatedMetricsService;
	}

	@GetMapping
	public List<AppSimulatedMetric> getAppSimulatedMetrics() {
		return appSimulatedMetricsService.getAppSimulatedMetrics();
	}

	@GetMapping("/{simulatedMetricName}")
	public AppSimulatedMetric getAppSimulatedMetric(@PathVariable String simulatedMetricName) {
		return appSimulatedMetricsService.getAppSimulatedMetric(simulatedMetricName);
	}

	@PostMapping
	public AppSimulatedMetric addAppSimulatedMetric(@RequestBody AppSimulatedMetric appSimulatedMetric) {
		Validation.validatePostRequest(appSimulatedMetric.getId());
		return appSimulatedMetricsService.addAppSimulatedMetric(appSimulatedMetric);
	}

	@PutMapping("/{simulatedMetricName}")
	public AppSimulatedMetric updateAppSimulatedMetric(@PathVariable String simulatedMetricName,
													   @RequestBody AppSimulatedMetric simulatedMetric) {
		Validation.validatePutRequest(simulatedMetric.getId());
		return appSimulatedMetricsService.updateAppSimulatedMetric(simulatedMetricName, simulatedMetric);
	}

	@DeleteMapping("/{simulatedMetricName}")
	public void deleteAppSimulatedMetric(@PathVariable String simulatedMetricName) {
		appSimulatedMetricsService.deleteAppSimulatedMetric(simulatedMetricName);
	}

	@GetMapping("/{simulatedMetricName}/apps")
	public List<App> getAppSimulatedMetricApps(@PathVariable String simulatedMetricName) {
		return appSimulatedMetricsService.getApps(simulatedMetricName);
	}

	@PostMapping("/{simulatedMetricName}/apps")
	public void addAppSimulatedMetricApps(@PathVariable String simulatedMetricName, @RequestBody List<String> apps) {
		appSimulatedMetricsService.addApps(simulatedMetricName, apps);
	}

	@DeleteMapping("/{simulatedMetricName}/apps")
	public void removeAppSimulatedMetricApps(@PathVariable String simulatedMetricName, @RequestBody List<String> apps) {
		appSimulatedMetricsService.removeApps(simulatedMetricName, apps);
	}

	@DeleteMapping("/{simulatedMetricName}/apps/{appId}")
	public void removeAppSimulatedMetricApp(@PathVariable String simulatedMetricName, @PathVariable String appId) {
		appSimulatedMetricsService.removeApp(simulatedMetricName, appId);
	}

}
