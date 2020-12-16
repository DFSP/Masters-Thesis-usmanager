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

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationRequestsService {

	public static final String REQUEST_LOCATION_MONITOR = "request-location-monitor";

	private final NodesService nodesService;
	private final int locationRequestsPort;
	private final RestTemplate restTemplate;
	private long lastRequestTime;

	public LocationRequestsService(NodesService nodesService, LocationRequestsProperties locationRequestsProperties) {
		this.nodesService = nodesService;
		this.locationRequestsPort = locationRequestsProperties.getPort();
		this.restTemplate = new RestTemplate();
		this.lastRequestTime = -1;
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
			requests.getLocationRequests().forEach((service, count) -> {
				List<LocationWeight> locationWeights = servicesLocationsWeights.get(service);
				if (locationWeights == null) {
					locationWeights = new ArrayList<>(1);
				}
				LocationWeight locationWeight = new LocationWeight(node, count);
				locationWeights.add(locationWeight);
			});
		}

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
		double longitude = Math.atan2(y, x);
		double hypersphere = Math.sqrt(x * x + y * y);
		double latitude = Math.atan2(z, hypersphere);

		return new Coordinates(latitude, longitude);
	}

	public List<NodeLocationRequests> getNodesLocationRequests() {
		List<FutureNodeLocationRequests> futureNodeLocationRequests = nodesService.getReadyNodes().stream()
			.map(node -> new FutureNodeLocationRequests(node, getNodeLocationRequests(node.getPublicIpAddress())))
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
	@SuppressWarnings("unchecked")
	public CompletableFuture<Map<String, Integer>> getNodeLocationRequests(String hostname) {
		String url = String.format("http://%s:%s/api/locations/requests?aggregation", hostname, locationRequestsPort);
		long currentRequestTime = System.currentTimeMillis();
		if (lastRequestTime >= 0) {
			int interval = (int) (currentRequestTime - lastRequestTime);
			url += String.format("&interval=%d", interval);
		}
		lastRequestTime = currentRequestTime;

		Map<String, Integer> locationMonitoringData = new HashMap<>();
		try {
			locationMonitoringData = restTemplate.getForObject(url, Map.class);
		}
		catch (RestClientException e) {
			log.error("Failed to get node {} location requests: {}", hostname, e.getMessage());
		}

		return CompletableFuture.completedFuture(locationMonitoringData);
	}

}
