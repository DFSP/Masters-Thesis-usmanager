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

import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.monitoring.metrics.PrometheusQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class HostMetricsService {

	private final PrometheusService prometheusService;
	private final FieldsService fieldsService;
	private final double maximumRamPercentage;

	public HostMetricsService(PrometheusService prometheusService, FieldsService fieldsService, HostProperties hostProperties) {
		this.prometheusService = prometheusService;
		this.fieldsService = fieldsService;
		this.maximumRamPercentage = hostProperties.getMaximumRamPercentage();
	}

	public boolean hostHasEnoughMemory(HostAddress hostAddress, double expectedMemoryConsumption) {
		Optional<Double> totalRam = prometheusService.getStat(hostAddress, PrometheusQuery.TOTAL_MEMORY);
		Optional<Double> availableRam = prometheusService.getStat(hostAddress, PrometheusQuery.AVAILABLE_MEMORY);
		if (totalRam.isPresent() && availableRam.isPresent()) {
			double totalRamValue = totalRam.get();
			double availableRamValue = availableRam.get();
			double predictedRamUsage = (1.0 - ((availableRamValue - expectedMemoryConsumption) / totalRamValue)) * 100.0;
			return predictedRamUsage < maximumRamPercentage;
		}
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
