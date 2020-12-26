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

package pt.unl.fct.miei.usmanagement.manager.services.location;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.config.RequestLocationRequestInterceptor;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationRequestsService {

	private final NodesService nodesService;
	private final HostsService hostsService;
	private final ContainersService containersService;

	private final String managerId;
	private final int port;
	private final String dockerHubUsername;
	private final RestTemplate restTemplate;
	private final Map<String, Long> lastRequestTime;

	public LocationRequestsService(NodesService nodesService, @Lazy HostsService hostsService,
								   @Lazy ContainersService containersService, DockerProperties dockerProperties,
								   LocationRequestsProperties locationRequestsProperties, RequestLocationRequestInterceptor requestInterceptor,
								   Environment environment) {
		this.nodesService = nodesService;
		this.hostsService = hostsService;
		this.containersService = containersService;
		this.dockerHubUsername = dockerProperties.getHub().getUsername();
		this.managerId = environment.getProperty(ContainerConstants.Environment.Manager.ID);
		this.port = locationRequestsProperties.getPort();
		this.restTemplate = new RestTemplate();
		this.restTemplate.setInterceptors(List.of(requestInterceptor));
		this.lastRequestTime = new HashMap<>();
	}

	public Map<String, Coordinates> getServicesWeightedMiddlePoint() {
		return getLocationsWeight().entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> getServiceWeightedMiddlePoint(e.getValue())));
	}

	public Map<String, List<LocationWeight>> getLocationsWeight() {
		List<NodeLocationRequests> nodeLocationRequests = getNodesLocationRequests();

		Map<String, List<LocationWeight>> servicesLocationsWeights = new HashMap<>();
		for (NodeLocationRequests requests : nodeLocationRequests) {
			Node node = requests.getNode();
			requests.getRequests().forEach((service, count) -> {
				List<LocationWeight> locationWeights = servicesLocationsWeights.get(service);
				if (locationWeights == null) {
					locationWeights = new ArrayList<>(1);
				}
				LocationWeight locationWeight = new LocationWeight(node, count);
				locationWeights.add(locationWeight);
				servicesLocationsWeights.put(service, locationWeights);
			});
		}

		log.info("Location weights: {}", servicesLocationsWeights);
		return servicesLocationsWeights;
	}

	public Coordinates getServiceWeightedMiddlePoint(List<LocationWeight> locationWeights) {
		int totalWeight = locationWeights.stream().mapToInt(LocationWeight::getWeight).sum();

		double x = 0, y = 0, z = 0;

		for (LocationWeight locationWeight : locationWeights) {
			Node node = locationWeight.getNode();
			int weight = locationWeight.getWeight();
			Coordinates coordinates = node.getCoordinates();
			double latitude = coordinates.getLatitude();
			double longitude = coordinates.getLongitude();

			// Convert latitude/longitude from degrees to radians
			double latitudeRadians = latitude * Math.PI / 180;
			double longitudeRadians = longitude * Math.PI / 180;

			// Convert latitudeRadians/longitudeRadians to Cartesian coordinates
			double xn = Math.cos(latitudeRadians) * Math.cos(longitudeRadians);
			double yn = Math.cos(latitudeRadians) * Math.sin(longitudeRadians);
			double zn = Math.sin(latitudeRadians);

			// Sum this location weight
			x += xn * weight;
			y += yn * weight;
			z += zn * weight;
		}

		x /= totalWeight;
		y /= totalWeight;
		z /= totalWeight;

		// Convert average x, y, z coordinate to latitude and longitude
		double hypersphere = Math.sqrt(x * x + y * y);
		double latitude = Math.atan2(z, hypersphere);
		double longitude = Math.atan2(y, x);

		// Convert back from radians to degrees
		latitude = latitude * 180 / Math.PI;
		longitude = longitude * 180 / Math.PI;

		Coordinates coordinates = new Coordinates(latitude, longitude);

		log.info("Middle point for location weights {}: {}", locationWeights, coordinates);

		return coordinates;
	}

	public List<NodeLocationRequests> getNodesLocationRequests() {
		List<FutureNodeLocationRequests> futureNodeLocationRequests = nodesService.getReadyNodes().stream()
			.map(node -> {
				String hostname = node.getPublicIpAddress();
				Optional<Integer> port = containersService.getSingletonContainer(node.getHostAddress(), ServiceConstants.Name.PROMETHEUS)
					.map(c -> c.getPorts().stream().findFirst().get().getPublicPort());
				CompletableFuture<Map<String, Integer>> futureLocationRequests;
				if (port.isPresent()) {
					futureLocationRequests = getNodeLocationRequests(hostname, port.get());
				}
				else {
					futureLocationRequests = CompletableFuture.completedFuture(Collections.emptyMap());
				}
				return new FutureNodeLocationRequests(node, futureLocationRequests);
			})
			.collect(Collectors.toList());

		CompletableFuture.allOf(futureNodeLocationRequests.stream().map(FutureNodeLocationRequests::getRequests)
			.toArray(CompletableFuture[]::new)).join();

		List<NodeLocationRequests> locationRequests = new ArrayList<>(futureNodeLocationRequests.size());
		for (FutureNodeLocationRequests futureNodeLocationRequest : futureNodeLocationRequests) {
			Node node = futureNodeLocationRequest.getNode();
			try {
				Map<String, Integer> locationRequest = futureNodeLocationRequest.getRequests().get();
				locationRequests.add(new NodeLocationRequests(node, locationRequest));
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		return locationRequests;
	}

	@Async
	public CompletableFuture<Map<String, Integer>> getNodeLocationRequests(String hostname, int port) {
		String url = String.format("http://%s:%s/api/location/requests?aggregation", hostname, port);
		long currentRequestTime = System.currentTimeMillis();
		Long interval = lastRequestTime.get(hostname);
		if (interval != null && interval > 0) {
			interval = currentRequestTime - interval;
			url += String.format("&interval=%d", interval);
		}
		lastRequestTime.put(hostname, interval);

		log.info("Requesting location requests from {}", url);

		Map<String, Integer> locationMonitoringData = new HashMap<>();
		try {
			locationMonitoringData = restTemplate.getForObject(url, Map.class);
			log.info("Got reply from {}: {}", url, locationMonitoringData);
		}
		catch (RestClientException e) {
			log.error("Failed to get node {} location requests: {}", hostname, e.getMessage());
		}

		return CompletableFuture.completedFuture(locationMonitoringData);
	}

	public String launchRequestLocationMonitor(HostAddress hostAddress) {
		String serviceName = ServiceConstants.Name.REQUEST_LOCATION_MONITOR;
		String dockerRepository = dockerHubUsername + "/" + serviceName;
		int externalPort = hostsService.findAvailableExternalPort(hostAddress, port);
		Gson gson = new Gson();
		String command = String.format("REQUEST_LOCATION_MONITOR=$(docker ps -q -f 'name=%s') && "
				+ "if [ $REQUEST_LOCATION_MONITOR ]; then echo $REQUEST_LOCATION_MONITOR; "
				+ "else docker pull %s && "
				+ "docker run -itd --name=%s -p %d:%d --hostname %s --rm "
				+ "-l %s=%b -l %s=%s -l %s=%s -l %s=%s -l %s='%s' -l %s=%s -l %s='%s' %s; fi",
			serviceName, dockerRepository, serviceName, externalPort, port, serviceName,
			ContainerConstants.Label.US_MANAGER, true,
			ContainerConstants.Label.CONTAINER_TYPE, ContainerTypeEnum.SINGLETON,
			ContainerConstants.Label.SERVICE_NAME, serviceName,
			ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.SYSTEM,
			ContainerConstants.Label.COORDINATES, gson.toJson(hostAddress.getCoordinates()),
			ContainerConstants.Label.REGION, hostAddress.getRegion().name(),
			ContainerConstants.Label.MANAGER_ID, managerId,
			dockerRepository);
		List<String> output = hostsService.executeCommandSync(command, hostAddress);
		return output.get(output.size() - 1);
	}
}
