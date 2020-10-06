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

package pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.hosts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricsRepository;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostSimulatedMetricsService {

	private final CloudHostsService cloudHostsService;
	private final EdgeHostsService edgeHostsService;

	private final HostSimulatedMetricsRepository hostSimulatedMetrics;

	public HostSimulatedMetricsService(CloudHostsService cloudHostsService, EdgeHostsService edgeHostsService,
									   HostSimulatedMetricsRepository hostSimulatedMetrics) {
		this.cloudHostsService = cloudHostsService;
		this.edgeHostsService = edgeHostsService;
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

	public HostSimulatedMetricEntity addHostSimulatedMetric(HostSimulatedMetricEntity simulatedHostMetric) {
		assertHostSimulatedMetricDoesntExist(simulatedHostMetric);
		log.info("Saving simulated host metric {}", ToStringBuilder.reflectionToString(simulatedHostMetric));
		return hostSimulatedMetrics.save(simulatedHostMetric);
	}

	public HostSimulatedMetricEntity updateHostSimulatedMetric(String simulatedMetricName,
															   HostSimulatedMetricEntity newHostSimulatedMetric) {
		log.info("Updating simulated host metric {} with {}", simulatedMetricName,
			ToStringBuilder.reflectionToString(newHostSimulatedMetric));
		HostSimulatedMetricEntity simulatedHostMetric = getHostSimulatedMetric(simulatedMetricName);
		ObjectUtils.copyValidProperties(newHostSimulatedMetric, simulatedHostMetric);
		return hostSimulatedMetrics.save(simulatedHostMetric);
	}

	public void deleteHostSimulatedMetric(String simulatedMetricName) {
		log.info("Deleting simulated host metric {}", simulatedMetricName);
		HostSimulatedMetricEntity simulatedHostMetric = getHostSimulatedMetric(simulatedMetricName);
		simulatedHostMetric.removeAssociations();
		hostSimulatedMetrics.delete(simulatedHostMetric);
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

	public void addCloudHost(String simulatedMetricName, String instanceId) {
		addCloudHosts(simulatedMetricName, List.of(instanceId));
	}

	public void addCloudHosts(String simulatedMetricName, List<String> instanceIds) {
		log.info("Adding cloud hosts {} to simulated metric {}", instanceIds, simulatedMetricName);
		HostSimulatedMetricEntity hostMetric = getHostSimulatedMetric(simulatedMetricName);
		instanceIds.forEach(instanceId -> {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIdOrIp(instanceId);
			cloudHost.addHostSimulatedMetric(hostMetric);
		});
		hostSimulatedMetrics.save(hostMetric);
	}

	public void removeCloudHost(String simulatedMetricName, String instanceId) {
		removeCloudHosts(simulatedMetricName, List.of(instanceId));
	}

	public void removeCloudHosts(String simulatedMetricName, List<String> instanceIds) {
		log.info("Removing cloud hosts {} from simulated metric {}", instanceIds, simulatedMetricName);
		HostSimulatedMetricEntity hostMetric = getHostSimulatedMetric(simulatedMetricName);
		instanceIds.forEach(instanceId -> cloudHostsService.getCloudHostByIdOrIp(instanceId).removeHostSimulatedMetric(hostMetric));
		hostSimulatedMetrics.save(hostMetric);
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

	public void addEdgeHost(String simulatedMetricName, String hostname) {
		addEdgeHosts(simulatedMetricName, List.of(hostname));
	}

	public void addEdgeHosts(String simulatedMetricName, List<String> hostnames) {
		log.info("Adding edge hosts {} to simulated metric {}", hostnames, simulatedMetricName);
		HostSimulatedMetricEntity hostMetric = getHostSimulatedMetric(simulatedMetricName);
		hostnames.forEach(hostname -> {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(hostname);
			edgeHost.addHostSimulatedMetric(hostMetric);
		});
		hostSimulatedMetrics.save(hostMetric);
	}

	public void removeEdgeHost(String simulatedMetricName, String instanceId) {
		removeEdgeHosts(simulatedMetricName, List.of(instanceId));
	}

	public void removeEdgeHosts(String simulatedMetricName, List<String> instanceIds) {
		log.info("Removing edge hosts {} from simulated metric {}", instanceIds, simulatedMetricName);
		HostSimulatedMetricEntity hostMetric = getHostSimulatedMetric(simulatedMetricName);
		instanceIds.forEach(instanceId -> edgeHostsService.getEdgeHostByDnsOrIp(instanceId).removeHostSimulatedMetric(hostMetric));
		hostSimulatedMetrics.save(hostMetric);
	}

	private void assertHostSimulatedMetricExists(String simulatedMetricName) {
		if (!hostSimulatedMetrics.hasHostSimulatedMetric(simulatedMetricName)) {
			throw new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName);
		}
	}

	private void assertHostSimulatedMetricDoesntExist(HostSimulatedMetricEntity simulatedHostMetric) {
		String name = simulatedHostMetric.getName();
		if (hostSimulatedMetrics.hasHostSimulatedMetric(name)) {
			throw new DataIntegrityViolationException("Simulated host metric '" + name + "' already exists");
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
