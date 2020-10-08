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

package pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.metrics;

import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.fields.FieldEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.service.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.metrics.simulated.hosts.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.prometheus.PrometheusService;

import java.util.HashMap;
import java.util.Map;

@Service
public class HostMetricsService {

	private final PrometheusService prometheusService;
	private final HostSimulatedMetricsService hostSimulatedMetricsService;
	private final FieldsService fieldsService;
	private final double maximumRamPercentage;

	public HostMetricsService(PrometheusService prometheusService,
							  HostSimulatedMetricsService hostSimulatedMetricsService,
							  FieldsService fieldsService,
							  HostProperties hostProperties) {
		this.prometheusService = prometheusService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.fieldsService = fieldsService;
		this.maximumRamPercentage = hostProperties.getMaximumRamPercentage();
	}

	public boolean hostHasAvailableResources(HostAddress hostAddress, double avgContainerMem) {
		double totalRam = prometheusService.getTotalMemory(hostAddress);
		double availableRam = prometheusService.getAvailableMemory(hostAddress);
		//double cpuUsagePerc = prometheusService.getCpuUsagePercent(hostname);
		double predictedRamUsage = (1.0 - ((availableRam - avgContainerMem) / totalRam)) * 100.0;
		//TODO Ignoring CPU: cpuUsagePerc < maxCpuPerc
		return predictedRamUsage < maximumRamPercentage;
	}

	public Map<String, Double> getHostStats(HostAddress hostAddress) {
		Map<String, Double> fieldsValues = new HashMap<>();
		double cpuPercentage = prometheusService.getCpuUsagePercent(hostAddress);
		if (cpuPercentage != -1) {
			// just to make sure cpu-% is a valid field name
			FieldEntity field = fieldsService.getField("cpu-%");
			fieldsValues.put(field.getName(), cpuPercentage);
		}
		double ramPercentage = prometheusService.getMemoryUsagePercent(hostAddress);
		if (ramPercentage != -1) {
			// just to make sure ram-% is a valid field name
			FieldEntity field = fieldsService.getField("ram-%");
			fieldsValues.put(field.getName(), ramPercentage);
		}
		fieldsValues.putAll(hostSimulatedMetricsService.getSimulatedFieldsValues(hostAddress));
		return fieldsValues;
	}

}
