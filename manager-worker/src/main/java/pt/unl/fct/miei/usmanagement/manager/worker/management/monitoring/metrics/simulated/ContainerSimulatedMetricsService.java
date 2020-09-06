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
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ContainerSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ContainerSimulatedMetricsRepository;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;

@Slf4j
@Service
public class ContainerSimulatedMetricsService {

	private final ContainerSimulatedMetricsRepository containerSimulatedMetrics;

	public ContainerSimulatedMetricsService(ContainerSimulatedMetricsRepository containerSimulatedMetrics) {
		this.containerSimulatedMetrics = containerSimulatedMetrics;
	}

	public List<ContainerSimulatedMetricEntity> getContainerSimulatedMetrics() {
		return containerSimulatedMetrics.findAll();
	}

	public ContainerSimulatedMetricEntity getContainerSimulatedMetric(Long id) {
		return containerSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "id", id.toString()));
	}

	public ContainerSimulatedMetricEntity getContainerSimulatedMetric(String simulatedMetricName) {
		return containerSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
	}

	public List<ContainerSimulatedMetricEntity> getGenericContainerSimulatedMetrics() {
		return containerSimulatedMetrics.findGenericContainerSimulatedMetrics();
	}

	public ContainerSimulatedMetricEntity getGenericContainerSimulatedMetric(String simulatedMetricName) {
		return containerSimulatedMetrics.findGenericContainerSimulatedMetric(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
	}

	public List<ContainerEntity> getContainers(String simulatedMetricName) {
		assertContainerSimulatedMetricExists(simulatedMetricName);
		return containerSimulatedMetrics.getContainers(simulatedMetricName);
	}

	public ContainerEntity getContainer(String simulatedMetricName, String containerId) {
		assertContainerSimulatedMetricExists(simulatedMetricName);
		return containerSimulatedMetrics.getContainer(simulatedMetricName, containerId).orElseThrow(() ->
			new EntityNotFoundException(ContainerEntity.class, "containerId", containerId));
	}

	private void assertContainerSimulatedMetricExists(String name) {
		if (!containerSimulatedMetrics.hasContainerSimulatedMetric(name)) {
			throw new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "name", name);
		}
	}

	public Map<String, Double> getSimulatedFieldsValues(String containerId) {
		List<ContainerSimulatedMetricEntity> metrics = containerSimulatedMetrics.findByContainer(containerId);
		return metrics.stream().collect(Collectors.toMap(metric -> metric.getField().getName(), this::randomizeFieldValue));
	}

	public Optional<Double> getSimulatedFieldValue(String containerId, String field) {
		Optional<ContainerSimulatedMetricEntity> containerSimulatedMetric =
			containerSimulatedMetrics.findByContainerAndField(containerId, field);
		Optional<Double> fieldValue = containerSimulatedMetric.map(this::randomizeFieldValue);
		if (fieldValue.isPresent() && containerSimulatedMetric.get().isOverride()) {
			return fieldValue;
		}
		Optional<Double> genericFieldValue = randomizeGenericFieldValue(field);
		if (genericFieldValue.isPresent()) {
			return genericFieldValue;
		}
		return fieldValue;
	}

	private Double randomizeFieldValue(ContainerSimulatedMetricEntity metric) {
		var random = new Random();
		double minValue = metric.getMinimumValue();
		double maxValue = metric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}

	private Optional<Double> randomizeGenericFieldValue(String field) {
		Optional<ContainerSimulatedMetricEntity> containerSimulatedMetric =
			containerSimulatedMetrics.findGenericByField(field);
		return containerSimulatedMetric.map(this::randomizeFieldValue);
	}

}
