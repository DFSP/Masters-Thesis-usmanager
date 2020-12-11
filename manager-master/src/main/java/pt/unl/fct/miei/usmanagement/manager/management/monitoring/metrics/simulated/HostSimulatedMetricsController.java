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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/simulated-metrics/hosts")
public class HostSimulatedMetricsController {

	private final HostSimulatedMetricsService hostSimulatedMetricsService;

	public HostSimulatedMetricsController(HostSimulatedMetricsService hostSimulatedMetricsService) {
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
	}

	@GetMapping
	public List<HostSimulatedMetric> getHostSimulatedMetrics() {
		return hostSimulatedMetricsService.getHostSimulatedMetrics();
	}

	@GetMapping("/{simulatedMetricName}")
	public HostSimulatedMetric getHostSimulatedMetric(@PathVariable String simulatedMetricName) {
		return hostSimulatedMetricsService.getHostSimulatedMetric(simulatedMetricName);
	}

	@PostMapping
	public HostSimulatedMetric addHostSimulatedMetric(@RequestBody HostSimulatedMetric simulatedMetric) {
		Validation.validatePostRequest(simulatedMetric.getId());
		return hostSimulatedMetricsService.addHostSimulatedMetric(simulatedMetric);
	}

	@PutMapping("/{simulatedMetricName}")
	public HostSimulatedMetric updateHostSimulatedMetric(@PathVariable String simulatedMetricName,
														 @RequestBody HostSimulatedMetric simulatedMetric) {
		Validation.validatePutRequest(simulatedMetric.getId());
		return hostSimulatedMetricsService.updateHostSimulatedMetric(simulatedMetricName, simulatedMetric);
	}

	@DeleteMapping("/{simulatedMetricName}")
	public void deleteHostSimulatedMetrics(@PathVariable String simulatedMetricName) {
		hostSimulatedMetricsService.deleteHostSimulatedMetric(simulatedMetricName);
	}

	@GetMapping("/{simulatedMetricName}/cloud-hosts")
	public List<CloudHost> getHostSimulatedMetricCloudHosts(@PathVariable String simulatedMetricName) {
		return hostSimulatedMetricsService.getCloudHosts(simulatedMetricName);
	}

	@PostMapping("/{simulatedMetricName}/cloud-hosts")
	public void addHostSimulatedMetricCloudHosts(@PathVariable String simulatedMetricName,
												 @RequestBody List<String> cloudHosts) {
		hostSimulatedMetricsService.addCloudHosts(simulatedMetricName, cloudHosts);
	}

	@DeleteMapping("/{simulatedMetricName}/cloud-hosts")
	public void removeHostSimulatedMetricCloudHosts(@PathVariable String simulatedMetricName,
													@RequestBody List<String> cloudHosts) {
		hostSimulatedMetricsService.removeCloudHosts(simulatedMetricName, cloudHosts);
	}

	@DeleteMapping("/{simulatedMetricName}/cloud-hosts/{instanceId}")
	public void removeHostSimulatedMetricCloudHost(@PathVariable String simulatedMetricName,
												   @PathVariable String instanceId) {
		hostSimulatedMetricsService.removeCloudHost(simulatedMetricName, instanceId);
	}

	@GetMapping("/{simulatedMetricName}/edge-hosts")
	public List<EdgeHost> getHostSimulatedMetricEdgeHosts(@PathVariable String simulatedMetricName) {
		return hostSimulatedMetricsService.getEdgeHosts(simulatedMetricName);
	}

	@PostMapping("/{simulatedMetricName}/edge-hosts")
	public void addHostSimulatedMetricEdgeHosts(@PathVariable String simulatedMetricName,
												@RequestBody List<String> edgeHosts) {
		// TODO parameter List of HostAddress instead
		List<HostAddress> hostAddresses = edgeHosts.stream().map(edgeHost -> {
			String[] addresses = edgeHost.split("-");
			return new HostAddress(addresses[0], addresses[1]);
		}).collect(Collectors.toList());
		hostSimulatedMetricsService.addEdgeHosts(simulatedMetricName, hostAddresses);
	}

	@DeleteMapping("/{simulatedMetricName}/edge-hosts")
	public void removeHostSimulatedMetricEdgeHosts(@PathVariable String simulatedMetricName,
												   @RequestBody List<String> edgeHosts) {
		// TODO parameter List of HostAddress instead
		List<HostAddress> hostAddresses = edgeHosts.stream().map(edgeHost -> {
			String[] addresses = edgeHost.split("-");
			return new HostAddress(addresses[0], addresses[1]);
		}).collect(Collectors.toList());
		hostSimulatedMetricsService.removeEdgeHosts(simulatedMetricName, hostAddresses);
	}

	@DeleteMapping("/{simulatedMetricName}/edge-hosts/{edgeHost}")
	public void removeHostSimulatedMetricEdgeHost(@PathVariable String simulatedMetricName,
												  @PathVariable String edgeHost) {
		// TODO 2 path variables publicIpAddress and privateIpAddress
		String publicIpAddress = edgeHost.split("-")[0];
		String privateIpAddress = edgeHost.split("-")[1];
		hostSimulatedMetricsService.removeEdgeHost(simulatedMetricName, new HostAddress(publicIpAddress, privateIpAddress));
	}

}
