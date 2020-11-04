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

import com.amazonaws.regions.Regions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NginxLoadBalancerService {

	public static final String LOAD_BALANCER = "load-balancer";

	private final ContainersService containersService;
	private final HostsService hostsService;
	private final ServicesService servicesService;

	private final int stopDelay;
	private final String dockerApiProxyUsername;
	private final String dockerApiProxyPassword;
	private final HttpHeaders headers;
	private final RestTemplate restTemplate;

	private final Map<Region, Timer> stopLoadBalancerTimers;

	public NginxLoadBalancerService(@Lazy ContainersService containersService, HostsService hostsService,
									ServicesService servicesService,
									NginxLoadBalancerProperties nginxLoadBalancerProperties,
									DockerProperties dockerProperties) {
		this.containersService = containersService;
		this.hostsService = hostsService;
		this.servicesService = servicesService;
		this.stopDelay = nginxLoadBalancerProperties.getStopDelay();
		this.dockerApiProxyUsername = dockerProperties.getApiProxy().getUsername();
		this.dockerApiProxyPassword = dockerProperties.getApiProxy().getPassword();
		byte[] auth = String.format("%s:%s", dockerApiProxyUsername, dockerApiProxyPassword).getBytes();
		String basicAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
		this.headers = new HttpHeaders();
		this.headers.add("Authorization", basicAuthorization);
		this.restTemplate = new RestTemplate();
		this.stopLoadBalancerTimers = new HashMap<>(Regions.values().length);
	}

	public List<ContainerEntity> launchLoadBalancers(String serviceName, List<Region> regions) {
		log.info("Launching load balancers at regions {}", regions);

		double expectedMemoryConsumption = servicesService.getService(LOAD_BALANCER).getExpectedMemoryConsumption();

		Gson gson = new Gson();
		return regions.parallelStream()
			.map(region -> hostsService.getCapableNode(expectedMemoryConsumption, region))
			.distinct()
			.map(hostAddress -> {
				// avoid launching another load balancer on the same region
				List<ContainerEntity> containers = containersService.getContainersWithLabels(Set.of(
					Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER),
					Pair.of(ContainerConstants.Label.REGION, gson.toJson(hostAddress.getRegion()))
				));
				return !containers.isEmpty() ?
					containers.get(0)
					: launchLoadBalancer(serviceName, hostAddress);
			}).collect(Collectors.toList());
	}

	public ContainerEntity launchLoadBalancer(String serviceName, HostAddress hostAddress) {
		return launchLoadBalancer(hostAddress, serviceName, null);
	}

	private ContainerEntity launchLoadBalancer(HostAddress hostAddress, String serviceName, NginxServer[] nginxServers) {
		List<String> environment = new ArrayList<>();
		environment.add(String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_USERNAME, dockerApiProxyUsername));
		environment.add(String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_PASSWORD, dockerApiProxyPassword));
		environment.add(String.format("%s=%s", ContainerConstants.Environment.LoadBalancer.SERVER_NAME, serviceName));
		if (nginxServers != null) {
			environment.add(String.format("%s=%s", ContainerConstants.Environment.LoadBalancer.SERVER, new Gson().toJson(nginxServers)));
		}
		return containersService.launchContainer(hostAddress, LOAD_BALANCER, environment);
	}

	private ContainerEntity launchLoadBalancer(Region region, String serviceName, NginxServer[] nginxServers) {
		double availableMemory = servicesService.getService(LOAD_BALANCER).getExpectedMemoryConsumption();
		HostAddress hostAddress = hostsService.getCapableNode(availableMemory, region);
		return launchLoadBalancer(hostAddress, serviceName, nginxServers);
	}

	private void initStopLoadBalancerTimer(Region region) {
		Timer currentTimer = stopLoadBalancerTimers.get(region);
		if (currentTimer != null) {
			currentTimer.cancel();
		}

		String timerName = String.format("stop-load-balancer-timer-%s", region.name().toLowerCase());
		Timer stopLoadBalancerTimer = new Timer(timerName, true);

		stopLoadBalancerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					containersService.stopContainers((dockerContainer ->
						dockerContainer.getNames().contains(LOAD_BALANCER) && dockerContainer.getRegion() == region));
				}
				catch (ManagerException e) {
					log.error("Failed to stop load balancers on region {}: {}. Retrying in {} minutes", region, e.getMessage(),
						TimeUnit.MILLISECONDS.toMinutes(stopDelay));
					initStopLoadBalancerTimer(region);
				}
			}
		}, stopDelay);

		stopLoadBalancerTimers.put(region, stopLoadBalancerTimer);
	}

	private List<ContainerEntity> getLoadBalancers() {
		return containersService.getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER)));
	}

	private List<ContainerEntity> getLoadBalancers(Region region) {
		return containersService.getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER),
			Pair.of(ContainerConstants.Label.REGION, region.name())));
	}

	public List<NginxServiceServers> getServers() {
		List<NginxServiceServers> servicesServers = new ArrayList<>();
		List<ContainerEntity> loadBalancers = getLoadBalancers();
		loadBalancers.parallelStream().forEach(loadBalancer -> {
			String url = String.format("http://%s/servers", getLoadBalancerApiUrl(loadBalancer));
			HttpEntity<String> request = new HttpEntity<>(headers);
			ResponseEntity<NginxServiceServers[]> response = restTemplate.exchange(url, HttpMethod.GET, request, NginxServiceServers[].class);
			NginxServiceServers[] responseBody = response.getBody();
			if (responseBody != null) {
				servicesServers.addAll(Arrays.asList(responseBody));
			}
		});

		return servicesServers;
	}

	private List<NginxServer> getServers(String serviceName, Region region) {
		List<NginxServer> servers = new ArrayList<>();
		List<ContainerEntity> loadBalancers = getLoadBalancers(region);
		loadBalancers.parallelStream().forEach(loadBalancer -> {
			String url = String.format("http://%s/%s/servers", getLoadBalancerApiUrl(loadBalancer), serviceName);
			HttpEntity<String> request = new HttpEntity<>(headers);
			ResponseEntity<NginxServer[]> response = restTemplate.exchange(url, HttpMethod.GET, request, NginxServer[].class);
			NginxServer[] responseBody = response.getBody();
			if (responseBody != null) {
				servers.addAll(Arrays.asList(responseBody));
			}
		});
		return servers;
	}

	public void addServer(String serviceName, String server, Coordinates coordinates, Region region) {
		NginxServer nginxServer = new NginxServer(server, coordinates.getLatitude(), coordinates.getLongitude(), region.name());
		List<ContainerEntity> loadBalancers = getLoadBalancers(region);
		if (loadBalancers.isEmpty()) {
			ContainerEntity container = launchLoadBalancer(region, serviceName, new NginxServer[]{ nginxServer });
			loadBalancers.add(container);
		}
		loadBalancers.parallelStream().forEach(loadBalancer -> {
			String url = String.format("http://%s/%s/servers", getLoadBalancerApiUrl(loadBalancer), serviceName);
			HttpEntity<NginxServer[]> request = new HttpEntity<>(new NginxServer[]{nginxServer}, headers);
			ResponseEntity<NginxServer[]> response = restTemplate.postForEntity(url, request, NginxServer[].class);
			HttpStatus status = response.getStatusCode();
			if (status != HttpStatus.ACCEPTED) {
				throw new ManagerException("Failed to add server %s to load balancer %s: %s", nginxServer, url, status.getReasonPhrase());
			}
			else {
				log.info("Added server {} to load balancer {}", nginxServer, url);
			}
		});
	}

	public void removeServer(String serviceName, String server, Region region) {
		log.info("Removing server {} of service {} from load balancer", server, serviceName);
		List<ContainerEntity> loadBalancers = getLoadBalancers(region);
		loadBalancers.parallelStream().forEach(loadBalancer -> {
			String url = String.format("%s/%s/servers/%s", getLoadBalancerApiUrl(loadBalancer), serviceName, server);
			restTemplate.delete(url);
		});
		if (!containersService.hasContainers(region, serviceName)) {
			initStopLoadBalancerTimer(region);
		}
	}

	private String getLoadBalancerApiUrl(ContainerEntity loadBalancer) {
		String publicIpAddress = loadBalancer.getHostAddress().getPublicIpAddress();
		int port = loadBalancer.getPorts().get(0).getPublicPort();
		return String.format("http://%s:%s/_/api", publicIpAddress, port);
	}

}
