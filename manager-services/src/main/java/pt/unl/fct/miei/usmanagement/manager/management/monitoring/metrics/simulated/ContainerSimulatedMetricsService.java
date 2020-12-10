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
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetrics;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class ContainerSimulatedMetricsService {

	private final ContainersService containersService;
	private final KafkaService kafkaService;

	private final ContainerSimulatedMetrics containerSimulatedMetrics;

	public ContainerSimulatedMetricsService(@Lazy ContainersService containersService,
											KafkaService kafkaService, ContainerSimulatedMetrics containerSimulatedMetrics) {
		this.containersService = containersService;
		this.kafkaService = kafkaService;
		this.containerSimulatedMetrics = containerSimulatedMetrics;
	}

	public List<ContainerSimulatedMetric> getContainerSimulatedMetrics() {
		return containerSimulatedMetrics.findAll();
	}

	public List<ContainerSimulatedMetric> getServiceSimulatedMetricByContainer(String containerId) {
		return containerSimulatedMetrics.findByContainer(containerId);
	}

	public ContainerSimulatedMetric getContainerSimulatedMetric(Long id) {
		return containerSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetric.class, "id", id.toString()));
	}

	public ContainerSimulatedMetric getContainerSimulatedMetric(String simulatedMetricName) {
		return containerSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetric.class, "simulatedMetricName", simulatedMetricName));
	}

	public ContainerSimulatedMetric addContainerSimulatedMetric(ContainerSimulatedMetric containerSimulatedMetric) {
		checkContainerSimulatedMetricDoesntExist(containerSimulatedMetric);
		containerSimulatedMetric = saveContainerSimulatedMetric(containerSimulatedMetric);
		/*ContainerSimulatedMetric kafkaContainerSimulatedMetric = containerSimulatedMetric;
		kafkaContainerSimulatedMetric.setNew(true);
		kafkaService.sendContainerSimulatedMetric(kafkaContainerSimulatedMetric);*/
		kafkaService.sendContainerSimulatedMetric(containerSimulatedMetric);
		return containerSimulatedMetric;
	}

	public ContainerSimulatedMetric updateContainerSimulatedMetric(String simulatedMetricName,
																   ContainerSimulatedMetric newContainerSimulatedMetric) {
		log.info("Updating simulated container metric {} with {}", simulatedMetricName,
			ToStringBuilder.reflectionToString(newContainerSimulatedMetric));
		ContainerSimulatedMetric containerSimulatedMetric = getContainerSimulatedMetric(simulatedMetricName);
		EntityUtils.copyValidProperties(newContainerSimulatedMetric, containerSimulatedMetric);
		containerSimulatedMetric = saveContainerSimulatedMetric(containerSimulatedMetric);
		kafkaService.sendContainerSimulatedMetric(containerSimulatedMetric);
		return containerSimulatedMetric;
	}

	public ContainerSimulatedMetric saveContainerSimulatedMetric(ContainerSimulatedMetric containerSimulatedMetric) {
		log.info("Saving containerSimulatedMetric {}", ToStringBuilder.reflectionToString(containerSimulatedMetric));
		return containerSimulatedMetrics.save(containerSimulatedMetric);
	}

	public void deleteContainerSimulatedMetric(Long id) {
		log.info("Deleting simulated container metric {}", id);
		ContainerSimulatedMetric containerSimulatedMetric = getContainerSimulatedMetric(id);
		deleteContainerSimulatedMetric(containerSimulatedMetric);
	}

	public void deleteContainerSimulatedMetric(String simulatedMetricName) {
		log.info("Deleting simulated container metric {}", simulatedMetricName);
		ContainerSimulatedMetric containerSimulatedMetric = getContainerSimulatedMetric(simulatedMetricName);
		deleteContainerSimulatedMetric(containerSimulatedMetric);
	}

	public void deleteContainerSimulatedMetric(ContainerSimulatedMetric simulatedMetric) {
		simulatedMetric.removeAssociations();
		containerSimulatedMetrics.delete(simulatedMetric);
	}

	public List<Container> getContainers(String simulatedMetricName) {
		checkContainerSimulatedMetricExists(simulatedMetricName);
		return containerSimulatedMetrics.getContainers(simulatedMetricName);
	}

	public Container getContainer(String simulatedMetricName, String containerId) {
		checkContainerSimulatedMetricExists(simulatedMetricName);
		return containerSimulatedMetrics.getContainer(simulatedMetricName, containerId).orElseThrow(() ->
			new EntityNotFoundException(Container.class, "containerId", containerId));
	}

	public void addContainer(String simulatedMetricName, String containerId) {
		addContainers(simulatedMetricName, List.of(containerId));
	}

	public void addContainers(String simulatedMetricName, List<String> containerIds) {
		log.info("Adding containers {} to simulated metric {}", containerIds, simulatedMetricName);
		ContainerSimulatedMetric containerMetric = getContainerSimulatedMetric(simulatedMetricName);
		containerIds.forEach(containerId -> {
			Container container = containersService.getContainer(containerId);
			container.addContainerSimulatedMetric(containerMetric);
		});
		ContainerSimulatedMetric containerSimulatedMetric = saveContainerSimulatedMetric(containerMetric);
		kafkaService.sendContainerSimulatedMetric(containerSimulatedMetric);
	}

	public void removeContainer(String simulatedMetricName, String containerId) {
		removeContainers(simulatedMetricName, List.of(containerId));
	}

	public void removeContainers(String simulatedMetricName, List<String> containerIds) {
		log.info("Removing containers {} from simulated metric {}", containerIds, simulatedMetricName);
		ContainerSimulatedMetric containerMetric = getContainerSimulatedMetric(simulatedMetricName);
		containerIds.forEach(containerId ->
			containersService.getContainer(containerId).removeContainerSimulatedMetric(containerMetric));
		ContainerSimulatedMetric containerSimulatedMetric = saveContainerSimulatedMetric(containerMetric);
		kafkaService.sendContainerSimulatedMetric(containerSimulatedMetric);
	}

	private void checkContainerSimulatedMetricExists(String name) {
		if (!containerSimulatedMetrics.hasContainerSimulatedMetric(name)) {
			throw new EntityNotFoundException(ContainerSimulatedMetric.class, "name", name);
		}
	}

	private void checkContainerSimulatedMetricDoesntExist(ContainerSimulatedMetric containerSimulatedMetric) {
		String name = containerSimulatedMetric.getName();
		if (containerSimulatedMetrics.hasContainerSimulatedMetric(name)) {
			throw new DataIntegrityViolationException("Simulated container metric '" + name + "' already exists");
		}
	}

	public Double randomizeFieldValue(ContainerSimulatedMetric metric) {
		Random random = new Random();
		double minValue = metric.getMinimumValue();
		double maxValue = metric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}
}
