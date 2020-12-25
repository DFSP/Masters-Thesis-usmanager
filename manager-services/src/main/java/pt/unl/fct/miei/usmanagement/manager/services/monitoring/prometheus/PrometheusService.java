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

package pt.unl.fct.miei.usmanagement.manager.services.monitoring.prometheus;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.services.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;

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

	private final HostsService hostsService;
	
	private final int port;
	private final String dockerHubUsername;
	private final RestTemplate restTemplate;

	public PrometheusService(@Lazy HostsService hostsService, DockerProperties dockerProperties, PrometheusProperties prometheusProperties) {
		this.hostsService = hostsService;
		this.port = prometheusProperties.getPort();
		this.dockerHubUsername = dockerProperties.getHub().getUsername();
		this.restTemplate = new RestTemplate();
	}

	@Async
	public CompletableFuture<Optional<Double>> getStat(HostAddress hostAddress, int port, PrometheusQueryEnum prometheusQuery) {
		String value = "";
		URI uri = UriComponentsBuilder
			.fromHttpUrl(String.format(PrometheusProperties.URL, hostAddress.getPublicIpAddress(), port))
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

	public String launchPrometheus(HostAddress hostAddress) {
		String serviceName = ServiceConstants.Name.DOCKER_API_PROXY;
		String dockerRepository = dockerHubUsername + "/" + serviceName;
		int externalPort = hostsService.findAvailableExternalPort(hostAddress, port);
		Gson gson = new Gson();
		String command = String.format("PROMETHEUS=$(docker ps -q -f 'name=%s') && "
				+ "if [ $PROMETHEUS ]; then echo $PROMETHEUS; "
				+ "else docker pull %s && "
				+ "docker run -itd --name=%s -p %d:%d --hostname %s --rm "
				+ "-l %s=%b -l %s=%s -l %s=%s -l %s=%s -l %s='%s' -l %s=%s %s; fi",
			serviceName, dockerRepository, serviceName, externalPort, port, serviceName,
			ContainerConstants.Label.US_MANAGER, true,
			ContainerConstants.Label.CONTAINER_TYPE, ContainerTypeEnum.SINGLETON,
			ContainerConstants.Label.SERVICE_NAME, serviceName,
			ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.SYSTEM,
			ContainerConstants.Label.COORDINATES, gson.toJson(hostAddress.getCoordinates()),
			ContainerConstants.Label.REGION, hostAddress.getRegion().name(),
			dockerRepository);
		List<String> output = hostsService.executeCommandSync(command, hostAddress);
		return output.get(output.size() - 1);
	}

}
