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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQuery;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class PrometheusService {

	public static final String PROMETHEUS = "prometheus";
	private static final int DEFAULT_PORT = 9090;
	private static final String URL = "http://%s:%d/api/v1/query";

	private final RestTemplate restTemplate;

	public PrometheusService() {
		this.restTemplate = new RestTemplate();
	}

	public Optional<Double> getStat(HostAddress hostAddress, PrometheusQuery prometheusQuery) {
		String value = "";
		URI uri = UriComponentsBuilder
			.fromHttpUrl(String.format(URL, hostAddress.getPublicIpAddress(), DEFAULT_PORT))
			.queryParam("query", URLEncoder.encode(prometheusQuery.getQuery(), StandardCharsets.UTF_8))
			.queryParam("time", Double.toString((System.currentTimeMillis() * 1.0) / 1000.0))
			.build(true).toUri();
		log.debug("Querying {} from prometheus: {}", prometheusQuery.name(), uri.toString());
		try {
			ResponseEntity<QueryOutput> response = restTemplate.getForEntity(uri, QueryOutput.class);
			QueryOutput queryOutput = response.getBody();
			if (queryOutput != null && Objects.equals(queryOutput.getStatus(), "success")) {
				List<QueryResult> results = queryOutput.getData().getResult();
				if (!results.isEmpty()) {
					List<String> values = results.get(0).getValue();
					if (values.size() == 2) {
						// values.get(0) is the timestamp
						value = values.get(1);
					}
				}
			}
		}
		catch (RestClientException e) {
			log.error("Failed to get stat {} from prometheus on {}: {}", prometheusQuery, hostAddress, e.getMessage());
		}
		return value.isEmpty() ? Optional.empty() : Optional.of(Double.parseDouble(value));
	}

}
