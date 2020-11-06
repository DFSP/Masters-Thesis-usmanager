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
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.util.Validation;

import java.util.List;

@RestController
@RequestMapping("/simulated-metrics/containers")
public class ContainerSimulatedMetricsController {

	private final ContainerSimulatedMetricsService containerSimulatedMetricsService;

	public ContainerSimulatedMetricsController(ContainerSimulatedMetricsService containerSimulatedMetricsService) {
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
	}

	@GetMapping
	public List<ContainerSimulatedMetric> getContainerSimulatedMetrics() {
		return containerSimulatedMetricsService.getContainerSimulatedMetrics();
	}

	@GetMapping("/{simulatedMetricName}")
	public ContainerSimulatedMetric getContainerSimulatedMetric(@PathVariable String simulatedMetricName) {
		return containerSimulatedMetricsService.getContainerSimulatedMetric(simulatedMetricName);
	}

	@PostMapping
	public ContainerSimulatedMetric addContainerSimulatedMetric(@RequestBody ContainerSimulatedMetric containerSimulatedMetric) {
		Validation.validatePostRequest(containerSimulatedMetric.getId());
		return containerSimulatedMetricsService.addContainerSimulatedMetric(containerSimulatedMetric);
	}

	@PutMapping("/{simulatedMetricName}")
	public ContainerSimulatedMetric updateContainerSimulatedMetric(@PathVariable String simulatedMetricName,
																   @RequestBody ContainerSimulatedMetric simulatedMetric) {
		Validation.validatePutRequest(simulatedMetric.getId());
		return containerSimulatedMetricsService.updateContainerSimulatedMetric(simulatedMetricName, simulatedMetric);
	}

	@DeleteMapping("/{simulatedMetricName}")
	public void deleteContainerSimulatedMetric(@PathVariable String simulatedMetricName) {
		containerSimulatedMetricsService.deleteContainerSimulatedMetric(simulatedMetricName);
	}

	@GetMapping("/{simulatedMetricName}/containers")
	public List<Container> getContainerSimulatedMetricContainers(@PathVariable String simulatedMetricName) {
		return containerSimulatedMetricsService.getContainers(simulatedMetricName);
	}

	@PostMapping("/{simulatedMetricName}/containers")
	public void addContainerSimulatedMetricContainers(@PathVariable String simulatedMetricName, @RequestBody List<String> containers) {
		containerSimulatedMetricsService.addContainers(simulatedMetricName, containers);
	}

	@DeleteMapping("/{simulatedMetricName}/containers")
	public void removeContainerSimulatedMetricContainers(@PathVariable String simulatedMetricName, @RequestBody List<String> containers) {
		containerSimulatedMetricsService.removeContainers(simulatedMetricName, containers);
	}

	@DeleteMapping("/{simulatedMetricName}/containers/{containerId}")
	public void removeContainerSimulatedMetricContainer(@PathVariable String simulatedMetricName, @PathVariable String containerId) {
		containerSimulatedMetricsService.removeContainer(simulatedMetricName, containerId);
	}

}
