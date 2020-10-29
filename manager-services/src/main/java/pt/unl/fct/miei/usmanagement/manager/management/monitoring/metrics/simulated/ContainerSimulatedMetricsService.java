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
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetricsRepository;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class ContainerSimulatedMetricsService {

	private final ContainersService containersService;

	private final ContainerSimulatedMetricsRepository containerSimulatedMetrics;

	public ContainerSimulatedMetricsService(@Lazy ContainersService containersService,
											ContainerSimulatedMetricsRepository containerSimulatedMetrics) {
		this.containersService = containersService;
		this.containerSimulatedMetrics = containerSimulatedMetrics;
	}

	public List<ContainerSimulatedMetricEntity> getContainerSimulatedMetrics() {
		return containerSimulatedMetrics.findAll();
	}

	public List<ContainerSimulatedMetricEntity> getServiceSimulatedMetricByContainer(String containerId) {
		return containerSimulatedMetrics.findByContainer(containerId);
	}

	public ContainerSimulatedMetricEntity getContainerSimulatedMetric(Long id) {
		return containerSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "id", id.toString()));
	}

	public ContainerSimulatedMetricEntity getContainerSimulatedMetric(String simulatedMetricName) {
		return containerSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName));
	}

	public ContainerSimulatedMetricEntity addContainerSimulatedMetric(ContainerSimulatedMetricEntity containerSimulatedMetric) {
		checkContainerSimulatedMetricDoesntExist(containerSimulatedMetric);
		log.info("Saving simulated container metric {}", ToStringBuilder.reflectionToString(containerSimulatedMetric));
		return containerSimulatedMetrics.save(containerSimulatedMetric);
	}

	public ContainerSimulatedMetricEntity updateContainerSimulatedMetric(String simulatedMetricName,
																		 ContainerSimulatedMetricEntity newContainerSimulatedMetric) {
		log.info("Updating simulated container metric {} with {}", simulatedMetricName,
			ToStringBuilder.reflectionToString(newContainerSimulatedMetric));
		ContainerSimulatedMetricEntity containerSimulatedMetric = getContainerSimulatedMetric(simulatedMetricName);
		ObjectUtils.copyValidProperties(newContainerSimulatedMetric, containerSimulatedMetric);
		return containerSimulatedMetrics.save(containerSimulatedMetric);
	}

	public void deleteContainerSimulatedMetric(String simulatedMetricName) {
		log.info("Deleting simulated container metric {}", simulatedMetricName);
		ContainerSimulatedMetricEntity containerSimulatedMetric = getContainerSimulatedMetric(simulatedMetricName);
		containerSimulatedMetric.removeAssociations();
		containerSimulatedMetrics.delete(containerSimulatedMetric);
	}

	public List<ContainerEntity> getContainers(String simulatedMetricName) {
		checkContainerSimulatedMetricExists(simulatedMetricName);
		return containerSimulatedMetrics.getContainers(simulatedMetricName);
	}

	public ContainerEntity getContainer(String simulatedMetricName, String containerId) {
		checkContainerSimulatedMetricExists(simulatedMetricName);
		return containerSimulatedMetrics.getContainer(simulatedMetricName, containerId).orElseThrow(() ->
			new EntityNotFoundException(ContainerEntity.class, "containerId", containerId));
	}

	public void addContainer(String simulatedMetricName, String containerId) {
		addContainers(simulatedMetricName, List.of(containerId));
	}

	public void addContainers(String simulatedMetricName, List<String> containerIds) {
		log.info("Adding containers {} to simulated metric {}", containerIds, simulatedMetricName);
		ContainerSimulatedMetricEntity containerMetric = getContainerSimulatedMetric(simulatedMetricName);
		containerIds.forEach(containerId -> {
			ContainerEntity container = containersService.getContainer(containerId);
			container.addContainerSimulatedMetric(containerMetric);
		});
		containerSimulatedMetrics.save(containerMetric);
	}

	public void removeContainer(String simulatedMetricName, String containerId) {
		removeContainers(simulatedMetricName, List.of(containerId));
	}

	public void removeContainers(String simulatedMetricName, List<String> containerIds) {
		log.info("Removing containers {} from simulated metric {}", containerIds, simulatedMetricName);
		ContainerSimulatedMetricEntity containerMetric = getContainerSimulatedMetric(simulatedMetricName);
		containerIds.forEach(containerId ->
			containersService.getContainer(containerId).removeContainerSimulatedMetric(containerMetric));
		containerSimulatedMetrics.save(containerMetric);
	}

	private void checkContainerSimulatedMetricExists(String name) {
		if (!containerSimulatedMetrics.hasContainerSimulatedMetric(name)) {
			throw new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "name", name);
		}
	}

	private void checkContainerSimulatedMetricDoesntExist(ContainerSimulatedMetricEntity containerSimulatedMetric) {
		String name = containerSimulatedMetric.getName();
		if (containerSimulatedMetrics.hasContainerSimulatedMetric(name)) {
			throw new DataIntegrityViolationException("Simulated container metric '" + name + "' already exists");
		}
	}

	public Double randomizeFieldValue(ContainerSimulatedMetricEntity metric) {
		Random random = new Random();
		double minValue = metric.getMinimumValue();
		double maxValue = metric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}

}
