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

package pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NginxLoadBalancerService {

	public static final String LOAD_BALANCER = "load-balancer";

	private final ContainersService containersService;
	private final HostsService hostsService;
	private final ServicesService serviceService;

	private final String nginxApiUrl;
	private final String dockerApiProxyUsername;
	private final String dockerApiProxyPassword;
	private final HttpHeaders headers;
	private final RestTemplate restTemplate;

	public NginxLoadBalancerService(@Lazy ContainersService containersService, HostsService hostsService,
									ServicesService serviceService,
									NginxLoadBalancerProperties nginxLoadBalancerProperties,
									DockerProperties dockerProperties) {
		this.containersService = containersService;
		this.hostsService = hostsService;
		this.serviceService = serviceService;
		this.nginxApiUrl = nginxLoadBalancerProperties.getApiUrl();
		this.headers = new HttpHeaders();
		this.dockerApiProxyUsername = dockerProperties.getApiProxy().getUsername();
		this.dockerApiProxyPassword = dockerProperties.getApiProxy().getPassword();
		byte[] auth = String.format("%s:%s", dockerApiProxyUsername, dockerApiProxyPassword).getBytes();
		String basicAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
		this.headers.add("Authorization", basicAuthorization);
		this.restTemplate = new RestTemplate();
	}

	public List<ContainerEntity> launchLoadBalancers(String serviceName, List<Region> regions) {
		ServiceEntity serviceConfig = serviceService.getService(LOAD_BALANCER);
		double expectedMemoryConsumption = serviceConfig.getExpectedMemoryConsumption();
		return regions.stream()
			.map(region -> hostsService.getClosestCapableHost(expectedMemoryConsumption, region))
			.map(hostAddress -> launchLoadBalancer(hostAddress, serviceName))
			.collect(Collectors.toList());
	}

	private ContainerEntity launchLoadBalancer(HostAddress hostAddress, String serviceName) {
		//TODO get port from properties
		return launchLoadBalancer(hostAddress, serviceName, "127.0.0.1:1906");
	}

	private ContainerEntity launchLoadBalancer(HostAddress hostAddress, String serviceName, String serverAddress) {
		List<String> environment = List.of(
			String.format("%s=%s", ContainerConstants.Environment.SERVER1, serverAddress),
			String.format("%s=%s", ContainerConstants.Environment.SERVER1_REGION, hostAddress.getRegion()),
			String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_USERNAME, dockerApiProxyUsername),
			String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_PASSWORD, dockerApiProxyPassword)
		);
		Map<String, String> labels = Map.of(
			ContainerConstants.Label.SERVICE, serviceName
		);
		return containersService.launchContainer(hostAddress, LOAD_BALANCER, environment, labels);
	}

	private List<ContainerEntity> getLoadBalancersFromService(String serviceName) {
		return containersService.getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER),
			Pair.of(ContainerConstants.Label.SERVICE, serviceName)));
	}

	public void addServiceToLoadBalancer(HostAddress hostAddress, String serviceName, String serverAddress) {
		List<ContainerEntity> loadBalancers = getLoadBalancersFromService(serviceName);
		if (loadBalancers.isEmpty()) {
			ContainerEntity container = launchLoadBalancer(hostAddress, serviceName, serverAddress);
			loadBalancers = List.of(container);
		}
		loadBalancers.forEach(loadBalancer -> {
			String loadBalancerHostname = loadBalancer.getHostAddress().getPublicIpAddress();
			int loadBalancerPort = loadBalancer.getPorts().get(0).getPublicPort();
			String loadBalancerUrl = String.format("%s:%s", loadBalancerHostname, loadBalancerPort);
			NginxServer nginxServer = new NginxServer(serverAddress, hostAddress.getRegion());
			String url = String.format("http://%s%s/servers", loadBalancerUrl, nginxApiUrl);
			HttpEntity<NginxServer> request = new HttpEntity<>(nginxServer, headers);
			ResponseEntity<Message> response = restTemplate.postForEntity(url, request, Message.class);
			Message responseBody = response.getBody();
			if (responseBody == null) {
				throw new ManagerException("Failed to add server %s to load balancer %s: responseBody is null",
					nginxServer, loadBalancerUrl);
			}
			String responseMessage = responseBody.getMessage();
			if (!Objects.equals(responseMessage, "success")) {
				throw new ManagerException("Failed to add server %s to load balancer %s: %s", nginxServer, loadBalancerUrl,
					responseMessage);
			}
		});
	}

	public void removeContainerFromLoadBalancer(String containerId) {
		log.info("Removing container {} from load balancer", containerId);
		Map<String, String> labels = containersService.getContainer(containerId).getLabels();
		String serviceType = labels.get(ContainerConstants.Label.SERVICE_TYPE);
		String serviceName = labels.get(ContainerConstants.Label.SERVICE_NAME);
		String serverAddress = labels.get(ContainerConstants.Label.SERVICE_ADDRESS);
		if (serviceType == null || serviceName == null | serverAddress == null) {
			throw new ManagerException("Failed to remove container %s from load balancer: labels %s, %s and %s are "
				+ "required. Current container labels = %s", containerId, ContainerConstants.Label.SERVICE_NAME,
				ContainerConstants.Label.SERVICE_TYPE, ContainerConstants.Label.SERVICE_ADDRESS, labels);
		}
		if (!Objects.equals(serviceType, "frontend")) {
			throw new ManagerException("Failed to remove container %s from load balancer: %s doesn't support load "
				+ "balancer", containerId, serviceType);
		}
		List<ContainerEntity> loadBalancers = getLoadBalancersFromService(serviceName);
		loadBalancers.forEach(loadBalancer -> {
			String url = String.format("http://%s%s/servers",
				loadBalancer.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS), nginxApiUrl);
			NginxSimpleServer server = new NginxSimpleServer(serverAddress);
			HttpEntity<NginxSimpleServer> request = new HttpEntity<>(server, headers);
			ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Message.class); // TODO
			Message responseBody = response.getBody();
			if (responseBody == null) {
				throw new ManagerException("Failed to remove container %s from load balancer: responseBody is null",
					containerId);
			}
			String responseMessage = responseBody.getMessage();
			if (!Objects.equals(responseMessage, "success")) {
				throw new ManagerException("Failed to delete server %s from load balancer %s: %s", serverAddress, url,
					responseMessage);
			}
		});
	}

  /*private List<NginxServer> getServers(String loadBalancerUrl) {
    String url = String.format("http://%s%s/servers", loadBalancerUrl, nginxApiUrl);
    HttpEntity<String> request = new HttpEntity<>(headers);
    ResponseEntity<NginxServer[]> response = restTemplate.exchange(url, HttpMethod.GET, request, NginxServer[].class);
    final var responseBody = response.getBody();
    return responseBody == null ? Collections.emptyList() : Arrays.asList(responseBody);
  }*/

  /*private boolean addServer(String loadBalancerUrl, NginxServer server) {
    String url = String.format("http://%s%s/servers", loadBalancerUrl, nginxApiUrl);
    HttpEntity<NginxServer> request = new HttpEntity<>(server, headers);
    ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.POST, request, Message.class);
    final var responseBody = response.getBody();
    return responseBody != null && Objects.equals(responseBody.getMessage(), "success");
  }*/

  /*private boolean deleteServer(String loadBalancerUrl, NginxServer server) {
    String url = String.format("http://%s%s/servers", loadBalancerUrl, nginxApiUrl);
    HttpEntity<NginxSimpleServer> request = new HttpEntity<>(server, headers);
    ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Message.class);
    Message responseBody = response.getBody();
    return responseBody != null && Objects.equals(responseBody.getMessage(), "success");
  }*/

}
