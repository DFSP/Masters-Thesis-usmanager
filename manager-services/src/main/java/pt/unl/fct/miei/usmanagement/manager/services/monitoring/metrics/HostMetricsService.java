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

package pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.MonitoringProperties;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostMetricsService {

	private final PrometheusService prometheusService;
	private final ContainersService containersService;
	private final FieldsService fieldsService;
	private final double maximumRamPercentage;
	private final double maximumCpuPercentage;

	public HostMetricsService(PrometheusService prometheusService, @Lazy ContainersService containersService,
							  FieldsService fieldsService, MonitoringProperties monitoringProperties) {
		this.prometheusService = prometheusService;
		this.containersService = containersService;
		this.fieldsService = fieldsService;
		this.maximumRamPercentage = monitoringProperties.getHosts().getMaximumRamPercentage();
		this.maximumCpuPercentage = monitoringProperties.getHosts().getMaximumCpuPercentage();
	}

	public boolean hostHasEnoughResources(HostAddress hostAddress, double expectedMemoryConsumption) {
		Optional<Integer> port = containersService.getSingletonContainer(hostAddress, ServiceConstants.Name.PROMETHEUS)
			.map(c -> c.getPorts().stream().findFirst().get().getPublicPort());
		if (port.isEmpty()) {
			log.info("Failed to find prometheus container on host {}", hostAddress);
			return false;
		}

		List<PrometheusQueryEnum> prometheusQueries = List.of(
			PrometheusQueryEnum.TOTAL_MEMORY,
			PrometheusQueryEnum.AVAILABLE_MEMORY,
			PrometheusQueryEnum.CPU_USAGE_PERCENTAGE,
			PrometheusQueryEnum.CPU_CORES);

		Map<PrometheusQueryEnum, Optional<Double>> metrics = new HashMap<>(prometheusQueries.size());

		CompletableFuture<?>[] requests = new CompletableFuture[prometheusQueries.size()];
		int count = 0;
		for (PrometheusQueryEnum prometheusQuery : prometheusQueries) {
			CompletableFuture<?> future = CompletableFuture
				.supplyAsync(() -> prometheusService.getStat(hostAddress, port.get(), prometheusQuery))
				.exceptionally(ex -> Optional.empty())
				.thenAccept(metric -> metrics.put(prometheusQuery, metric));
			requests[count++] = future;
		}
		CompletableFuture.allOf(requests).join();

		Optional<Double> totalRam = metrics.get(PrometheusQueryEnum.TOTAL_MEMORY);
		Optional<Double> availableRam = metrics.get(PrometheusQueryEnum.AVAILABLE_MEMORY);
		Optional<Double> cpuUsage = metrics.get(PrometheusQueryEnum.CPU_USAGE_PERCENTAGE);
		Optional<Double> cpuCores = metrics.get(PrometheusQueryEnum.CPU_CORES);
		if (totalRam != null && totalRam.isPresent()
			&& availableRam != null
			&& availableRam.isPresent()
			&& cpuUsage != null && cpuUsage.isPresent()
			&& cpuCores != null && cpuCores.isPresent()) {
			double totalRamValue = totalRam.get();
			double availableRamValue = availableRam.get();
			double predictedRamUsage = (1.0 - ((availableRamValue - expectedMemoryConsumption) / totalRamValue)) * 100.0;
			boolean hasEnoughMemory = predictedRamUsage < maximumRamPercentage;
			log.info("Node {} {} enough ram, predictedRamUsage={} {} maximumRamPercentage={} (total ram={}, available ram={})",
				hostAddress, hasEnoughMemory ? "has" : "doesn't have", predictedRamUsage, hasEnoughMemory ? "<" : ">=",
				maximumRamPercentage, totalRamValue, availableRamValue);
			double cpuCoresNumber = cpuCores.get();
			double cpuUsageValue = cpuCoresNumber == 0 ? cpuUsage.get() : cpuUsage.get() / cpuCoresNumber;
			boolean hasEnoughCpu = cpuUsageValue < maximumCpuPercentage;
			log.info("Node {} {} enough cpu, cpuUsage={} {} maximumCpuPercentage={}",
				hostAddress, hasEnoughCpu ? "has" : "doesn't have", cpuUsageValue, hasEnoughCpu ? "<" : ">=",
				maximumCpuPercentage);
			return hasEnoughMemory && hasEnoughCpu;
		}
		log.info("Node {} doesn't have enough capacity: failed to fetch metrics", hostAddress);
		return false;
	}

	public Map<String, Optional<Double>> getHostStats(HostAddress hostAddress) {
		Optional<Integer> port = containersService.getSingletonContainer(hostAddress, ServiceConstants.Name.PROMETHEUS)
			.map(c -> c.getPorts().stream().findFirst().get().getPublicPort());
		// Stats from prometheus (node exporter)
		return fieldsService.getFields().stream()
			.filter(field -> field.getPrometheusQuery() != null)
			.collect(Collectors.toMap(Field::getName, field -> {
				if (port.isPresent()) {
					return prometheusService.getStat(hostAddress, port.get(), field.getPrometheusQuery());
				}
				else {
					return Optional.empty();
				}
			}));
	}

}
