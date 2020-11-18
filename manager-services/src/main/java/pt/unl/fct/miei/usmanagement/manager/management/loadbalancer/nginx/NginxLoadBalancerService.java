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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.config.ParallelismProperties;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
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
	private final int threads;

	private final Map<RegionEnum, Timer> stopLoadBalancerTimers;

	public NginxLoadBalancerService(@Lazy ContainersService containersService, HostsService hostsService,
									ServicesService servicesService,
									NginxLoadBalancerProperties nginxLoadBalancerProperties,
									DockerProperties dockerProperties, ParallelismProperties parallelismProperties) {
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
		this.threads = parallelismProperties.getThreads();
	}

	public List<Container> launchLoadBalancers(List<RegionEnum> regions) {
		log.info("Launching load balancers at regions {}", regions);

		double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(LOAD_BALANCER);

		Gson gson = new Gson();
		try {
			return new ForkJoinPool(threads).submit(() ->
				regions.parallelStream().map(region -> {
					List<Container> regionLoadBalancers = containersService.getContainersWithLabels(Set.of(
						Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER),
						Pair.of(ContainerConstants.Label.REGION, gson.toJson(region))
					));
					if (regionLoadBalancers.size() > 0) {
						return regionLoadBalancers.get(0);
					}
					else {
						HostAddress hostAddress = hostsService.getCapableHost(expectedMemoryConsumption, region);
						return launchLoadBalancer(hostAddress);
					}
				}).collect(Collectors.toList())).get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new ManagerException("Unable to launch load balancers at regions %s: %s", regions, e.getMessage());
		}

	}

	public Container launchLoadBalancer(HostAddress hostAddress) {
		return launchLoadBalancer(hostAddress, null);
	}

	private Container launchLoadBalancer(HostAddress hostAddress, NginxServer[] nginxServers) {
		List<String> environment = new ArrayList<>();
		environment.add(String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_USERNAME, dockerApiProxyUsername));
		environment.add(String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_PASSWORD, dockerApiProxyPassword));
		if (nginxServers != null) {
			environment.add(String.format("%s=%s", ContainerConstants.Environment.LoadBalancer.SERVER, new Gson().toJson(nginxServers)));
		}
		return containersService.launchContainer(hostAddress, LOAD_BALANCER, environment);
	}

	private Container launchLoadBalancer(RegionEnum region, NginxServer[] nginxServers) {
		double availableMemory = servicesService.getExpectedMemoryConsumption(LOAD_BALANCER);
		HostAddress hostAddress = hostsService.getCapableHost(availableMemory, region);
		return launchLoadBalancer(hostAddress, nginxServers);
	}

	private void initStopLoadBalancerTimer(RegionEnum region) {
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

	private List<Container> getLoadBalancers() {
		return containersService.getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER)));
	}

	private List<Container> getLoadBalancers(RegionEnum region) {
		return containersService.getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER),
			Pair.of(ContainerConstants.Label.REGION, region.name())));
	}

	public List<NginxServiceServers> getServers() {
		List<CompletableFuture<List<NginxServiceServers>>> futureLoadBalancerServers = new LinkedList<>();
		List<Container> loadBalancers = getLoadBalancers();
		for (Container loadBalancer : loadBalancers) {
			CompletableFuture<List<NginxServiceServers>> futureServers = getServers(loadBalancer);
			futureLoadBalancerServers.add(futureServers);
		}

		CompletableFuture.allOf(futureLoadBalancerServers.toArray(new CompletableFuture[0])).join();

		List<NginxServiceServers> servicesServers = new ArrayList<>();
		for (CompletableFuture<List<NginxServiceServers>> loadBalancerServers : futureLoadBalancerServers) {
			try {
				List<NginxServiceServers> nginxServiceServers = loadBalancerServers.get();
				servicesServers.addAll(nginxServiceServers);
			}
			catch (InterruptedException | ExecutionException e) {
				log.error("Failed to get servers from all load-balancers: {}", e.getMessage());
			}
		}

		return servicesServers;
	}

	@Async
	public CompletableFuture<List<NginxServiceServers>> getServers(Container loadBalancer) {
		List<NginxServiceServers> servers = new ArrayList<>();
		String url = String.format("%s/servers", getLoadBalancerApiUrl(loadBalancer));
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<NginxServiceServers[]> response = restTemplate.exchange(url, HttpMethod.GET, request, NginxServiceServers[].class);
		NginxServiceServers[] responseBody = response.getBody();
		if (responseBody != null) {
			servers.addAll(Arrays.asList(responseBody));
		}
		return CompletableFuture.completedFuture(servers);
	}

	@Async
	public CompletableFuture<List<NginxServer>> getServers(Container loadBalancer, String serviceName) {
		List<NginxServer> servers = new ArrayList<>();
		String url = String.format("%s/%s/servers", getLoadBalancerApiUrl(loadBalancer), serviceName);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<NginxServer[]> response = restTemplate.exchange(url, HttpMethod.GET, request, NginxServer[].class);
		NginxServer[] responseBody = response.getBody();
		if (responseBody != null) {
			servers.addAll(Arrays.asList(responseBody));
		}

		return CompletableFuture.completedFuture(servers);
	}

	public List<NginxServer> getServers(RegionEnum region, String serviceName) {
		List<Container> loadBalancers = getLoadBalancers(region);

		List<CompletableFuture<List<NginxServer>>> futureLoadBalancerServers = new ArrayList<>();
		for (Container loadBalancer : loadBalancers) {
			futureLoadBalancerServers.add(getServers(loadBalancer, serviceName));
		}

		CompletableFuture.allOf(futureLoadBalancerServers.toArray(new CompletableFuture[0])).join();

		List<NginxServer> servers = new ArrayList<>();
		for (CompletableFuture<List<NginxServer>> loadBalancerServers : futureLoadBalancerServers) {
			try {
				List<NginxServer> nginxServers = loadBalancerServers.get();
				servers.addAll(nginxServers);
			}
			catch (InterruptedException | ExecutionException e) {
				log.error("Failed to get servers for service {} from load-balancer", serviceName);
			}
		}

		return servers;
	}

	public void addServer(String serviceName, String server, Coordinates coordinates, RegionEnum region) {
		NginxServer nginxServer = new NginxServer(server, coordinates.getLatitude(), coordinates.getLongitude(), region.name());
		List<Container> loadBalancers = getLoadBalancers(region);
		if (loadBalancers.isEmpty()) {
			Container container = launchLoadBalancer(region, new NginxServer[]{nginxServer});
			loadBalancers.add(container);
		}
		loadBalancers.forEach(loadBalancer -> addServer(loadBalancer, nginxServer, serviceName));
	}

	@Async
	public void addServer(Container loadBalancer, NginxServer nginxServer, String serviceName) {
		String url = String.format("%s/%s/servers", getLoadBalancerApiUrl(loadBalancer), serviceName);
		HttpEntity<NginxServer[]> request = new HttpEntity<>(new NginxServer[]{nginxServer}, headers);
		try {
			restTemplate.postForEntity(url, request, NginxServer[].class);
			log.info("Added server {} to load balancer {}", nginxServer, url);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException("Failed to add server %s to load balancer %s: %s", nginxServer, url, e.getMessage());
		}
	}

	public void removeServer(String serviceName, String server, RegionEnum region) {
		log.info("Removing server {} of service {} from load balancer", server, serviceName);
		List<Container> loadBalancers = getLoadBalancers(region);
		loadBalancers.stream().forEach(loadBalancer -> removeServer(loadBalancer, serviceName, server));
		if (!containersService.hasContainers(region)) {
			initStopLoadBalancerTimer(region);
		}
	}

	@Async
	public void removeServer(Container loadBalancer, String serviceName, String server) {
		String url = String.format("%s/%s/servers/%s", getLoadBalancerApiUrl(loadBalancer), serviceName, server);
		try {
			HttpEntity<String> request = new HttpEntity<>(headers);
			restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
			log.info("Removed server {} from service {} from load balancer {}", server, serviceName, url);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException("Failed to remove server %s of service %s from load balancer %s: %s", server,
				serviceName, url, e.getMessage());
		}
	}

	private String getLoadBalancerApiUrl(Container loadBalancer) {
		String publicIpAddress = loadBalancer.getHostAddress().getPublicIpAddress();
		int port = loadBalancer.getPorts().get(0).getPublicPort();
		return String.format("http://%s:%s/_/api", publicIpAddress, port);
	}

}
