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

package pt.unl.fct.miei.usmanagement.manager.services.management.monitoring.prometheus;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class PrometheusService {

	public static final String PROMETHEUS = "prometheus";
	private static final double PERCENT = 0.01;
	private static final String DEFAULT_PORT = "9090";
	private static final String URL_FORMAT = "http://%s:%s/api/v1/query?query=%s&time=%s";
	private static final String HOST_AVAILABLE_MEMORY = "node_memory_MemAvailable_bytes";
	private static final String HOST_TOTAL_MEMORY = "node_memory_MemTotal_bytes";

	private final RestTemplate restTemplate;

	public PrometheusService() {
		this.restTemplate = new RestTemplate();
	}

	public double getAvailableMemory(HostAddress hostAddress) {
		return getStat(hostAddress, HOST_AVAILABLE_MEMORY);
	}

	public double getTotalMemory(HostAddress hostAddress) {
		return getStat(hostAddress, HOST_TOTAL_MEMORY);
	}

	public double getMemoryUsagePercent(HostAddress hostAddress) {
		double availableMemory = getStat(hostAddress, HOST_AVAILABLE_MEMORY + "/" + HOST_TOTAL_MEMORY);
		return availableMemory < 0 ? availableMemory : 100.0 - (availableMemory / PERCENT);
	}

	public double getCpuUsagePercent(HostAddress hostAddress) {
		String queryParam = "100 - (avg by (instance) (irate(node_cpu_seconds_total"
			+ "{job=\"node_exporter\", mode=\"idle\"}[5m])) * 100)";
		return getStat(hostAddress, "{query}", queryParam);
	}

	private double getStat(HostAddress hostAddress, String statId) {
		return getStat(hostAddress, statId, null);
	}

	private double getStat(HostAddress hostAddress, String statId, String queryParam) {
		String currentTime = Double.toString((System.currentTimeMillis() * 1.0) / 1000.0);
		String url = String.format(URL_FORMAT, hostAddress.getPublicIpAddress(), DEFAULT_PORT, statId, currentTime);
		String value = "";
		try {
			QueryOutput queryOutput;
			if (queryParam == null) {
				queryOutput = restTemplate.getForEntity(url, QueryOutput.class).getBody();
			}
			else {
				queryOutput = restTemplate.getForEntity(url, QueryOutput.class, Map.of("query", queryParam)).getBody();
			}
			if (queryOutput != null && Objects.equals(queryOutput.getStatus(), "success")) {
				final List<QueryResult> results = queryOutput.getData().getResult();
				if (!results.isEmpty()) {
					final List<String> values = results.get(0).getValue();
					if (values.size() == 2) {
						value = values.get(1);
					}
				}
			}
		}
		catch (RestClientException e) {
			e.printStackTrace();
		}
		return value.isEmpty() ? -1 : Double.parseDouble(value);
	}

}
