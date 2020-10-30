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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostMetricsService {

	private final PrometheusService prometheusService;
	private final FieldsService fieldsService;
	private final double maximumRamPercentage;
	private final double maximumCpuPercentage;

	public HostMetricsService(PrometheusService prometheusService, FieldsService fieldsService, HostProperties hostProperties) {
		this.prometheusService = prometheusService;
		this.fieldsService = fieldsService;
		this.maximumRamPercentage = hostProperties.getMaximumRamPercentage();
		this.maximumCpuPercentage = hostProperties.getMaximumCpuPercentage();
	}

	public boolean hostHasEnoughResources(HostAddress hostAddress, double expectedMemoryConsumption) {
		List<Optional<Double>> metrics = List.of(
			PrometheusQuery.TOTAL_MEMORY,
			PrometheusQuery.AVAILABLE_MEMORY,
			PrometheusQuery.CPU_USAGE_PERCENTAGE)
			.parallelStream().map(stat -> prometheusService.getStat(hostAddress, stat))
			.collect(Collectors.toList());
		Optional<Double> totalRam = metrics.get(0);
		Optional<Double> availableRam = metrics.get(1);
		Optional<Double> cpuUsage = metrics.get(2);
		if (totalRam.isPresent() && availableRam.isPresent() && cpuUsage.isPresent()) {
			double totalRamValue = totalRam.get();
			double availableRamValue = availableRam.get();
			double predictedRamUsage = (1.0 - ((availableRamValue - expectedMemoryConsumption) / totalRamValue)) * 100.0;
			boolean hasEnoughMemory = predictedRamUsage < maximumRamPercentage;
			log.info("Node {} {} enough ram, predictedRamUsage={} {} maximumRamPercentage={}",
				hostAddress, hasEnoughMemory ? "has" : "doesn't have", predictedRamUsage, hasEnoughMemory ? "<" : ">=", maximumRamPercentage);
			double cpuUsageValue = cpuUsage.get();
			boolean hasEnoughCpu = cpuUsageValue < maximumCpuPercentage;
			log.info("Node {} {} enough cpu, cpuUsage={} {} maximumCpuPercentage={}",
				hostAddress, hasEnoughCpu ? "has" : "doesn't have", cpuUsageValue, hasEnoughCpu ? "<" : ">=",maximumCpuPercentage);
			return hasEnoughMemory && hasEnoughCpu;
		}
		log.info("Node {} doesn't have enough capacity: failed to fetch metrics", hostAddress);
		return false;
	}

	public Map<String, Double> getHostStats(HostAddress hostAddress) {
		Map<String, Double> fieldsValues = new HashMap<>();

		// Stats from prometheus (node exporter)
		fieldsService.getFields().parallelStream()
			.filter(field -> field.getQuery() != null)
			.forEach(field -> {
				Optional<Double> value = prometheusService.getStat(hostAddress, field.getQuery());
				value.ifPresent(v -> fieldsValues.put(field.getName(), v));
			});

		return fieldsValues;
	}

}
