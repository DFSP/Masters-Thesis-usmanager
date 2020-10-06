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

package pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics.simulated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricsRepository;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;

@Slf4j
@Service
public class HostSimulatedMetricsService {

	private final HostSimulatedMetricsRepository hostSimulatedMetrics;

	public HostSimulatedMetricsService(HostSimulatedMetricsRepository hostSimulatedMetrics) {
		this.hostSimulatedMetrics = hostSimulatedMetrics;
	}

	public List<HostSimulatedMetricEntity> getHostSimulatedMetrics() {
		return hostSimulatedMetrics.findAll();
	}

	public HostSimulatedMetricEntity getHostSimulatedMetric(Long id) {
		return hostSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetricEntity.class, "id", id.toString()));
	}

	public HostSimulatedMetricEntity getHostSimulatedMetric(String simulatedMetricName) {
		return hostSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
	}

	public List<HostSimulatedMetricEntity> getGenericHostSimulatedMetrics() {
		return hostSimulatedMetrics.findGenericHostSimulatedMetrics();
	}

	public HostSimulatedMetricEntity getGenericHostSimulatedMetric(String simulatedMetricName) {
		return hostSimulatedMetrics.findGenericHostSimulatedMetric(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
	}

	public List<CloudHostEntity> getCloudHosts(String simulatedMetricName) {
		assertHostSimulatedMetricExists(simulatedMetricName);
		return hostSimulatedMetrics.getCloudHosts(simulatedMetricName);
	}

	public CloudHostEntity getCloudHost(String simulatedMetricName, String instanceId) {
		assertHostSimulatedMetricExists(simulatedMetricName);
		return hostSimulatedMetrics.getCloudHost(simulatedMetricName, instanceId).orElseThrow(() ->
			new EntityNotFoundException(CloudHostEntity.class, "instanceId", instanceId));
	}

	public List<EdgeHostEntity> getEdgeHosts(String simulatedMetricName) {
		assertHostSimulatedMetricExists(simulatedMetricName);
		return hostSimulatedMetrics.getEdgeHosts(simulatedMetricName);
	}

	public EdgeHostEntity getEdgeHost(String simulatedMetricName, String hostname) {
		assertHostSimulatedMetricExists(simulatedMetricName);
		return hostSimulatedMetrics.getEdgeHost(simulatedMetricName, hostname).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname));
	}

	private void assertHostSimulatedMetricExists(String simulatedMetricName) {
		if (!hostSimulatedMetrics.hasHostSimulatedMetric(simulatedMetricName)) {
			throw new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName);
		}
	}

	public Map<String, Double> getSimulatedFieldsValues(String hostname) {
		List<HostSimulatedMetricEntity> metrics = hostSimulatedMetrics.findByHost(hostname);
		return metrics.stream().collect(Collectors.toMap(metric -> metric.getField().getName(), this::randomizeFieldValue));
	}

	public Optional<Double> getSimulatedFieldValue(String hostname, String field) {
		Optional<HostSimulatedMetricEntity> metric = hostSimulatedMetrics.findByHostAndField(hostname, field);
		Optional<Double> fieldValue = metric.map(this::randomizeFieldValue);
		if (fieldValue.isPresent() && metric.get().isOverride()) {
			return fieldValue;
		}
		Optional<Double> genericFieldValue = randomizeGenericFieldValue(field);
		if (genericFieldValue.isPresent()) {
			return genericFieldValue;
		}
		return fieldValue;
	}

	private Double randomizeFieldValue(HostSimulatedMetricEntity metric) {
		Random random = new Random();
		double minValue = metric.getMinimumValue();
		double maxValue = metric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}

	private Optional<Double> randomizeGenericFieldValue(String field) {
		Optional<HostSimulatedMetricEntity> metric = hostSimulatedMetrics.findGenericByField(field);
		return metric.map(this::randomizeFieldValue);
	}

}
