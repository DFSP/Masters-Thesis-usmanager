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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PrometheusService {

	private final RestTemplate restTemplate;

	public PrometheusService() {
		this.restTemplate = new RestTemplate();
	}

	@Async
	public CompletableFuture<Optional<Double>> getStat(HostAddress hostAddress, PrometheusQueryEnum prometheusQuery) {
		String value = "";
		URI uri = UriComponentsBuilder
			.fromHttpUrl(String.format(PrometheusProperties.URL, hostAddress.getPublicIpAddress(), PrometheusProperties.PORT))
			.queryParam("query", URLEncoder.encode(prometheusQuery.getQuery(), StandardCharsets.UTF_8))
			.queryParam("time", Double.toString((System.currentTimeMillis() * 1.0) / 1000.0))
			.build(true).toUri();

		QueryOutput queryOutput = restTemplate.getForObject(uri, QueryOutput.class);
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
		Optional<Double> stat = value.isEmpty() ? Optional.empty() : Optional.of(Double.parseDouble(value));
		return CompletableFuture.completedFuture(stat);
	}

}
