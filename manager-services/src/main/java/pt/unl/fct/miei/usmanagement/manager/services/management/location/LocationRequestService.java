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

package pt.unl.fct.miei.usmanagement.manager.services.management.location;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.services.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodesService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationRequestService {

	public static final String REQUEST_LOCATION_MONITOR = "request-location-monitor";
	private static final double PERCENT = 0.01;

	private final NodesService nodesService;

	private final int defaultPort;
	private final double minimumRequestCountPercentage;
	private final RestTemplate restTemplate;

	private long lastRequestTime;

	public LocationRequestService(NodesService nodesService, LocationRequestProperties locationRequestProperties) {
		this.nodesService = nodesService;
		this.defaultPort = locationRequestProperties.getPort();
		this.minimumRequestCountPercentage = locationRequestProperties.getMinimumRequestCountPercentage();
		this.restTemplate = new RestTemplate();
		this.lastRequestTime = -1;
	}

	public Coordinates getWeightedMiddlePoint(List<NodeLocationMonitoring> nodesLocationMonitoring) {

	}

	public Map<String, HostDetails> findHostsToStartServices(Map<String, List<ServiceDecisionResult>> allServicesDecisions) {
		List<NodeLocationMonitoring> nodesLocationMonitoring = getNodesLocationMonitoring();

		for (NodeLocationMonitoring nodeLocationMonitoring : nodesLocationMonitoring) {

		}

		Map<String, HostDetails> finalLocations = new HashMap<>();
		for (Entry<String, List<ServiceDecisionResult>> services : allServicesDecisions.entrySet()) {
			String serviceName = services.getKey();
			List<ServiceDecisionResult> serviceDecisions = services.getValue();
			if (servicesLocationMonitoring.getSecond().containsKey(serviceName)) {
				if (!serviceDecisions.isEmpty()) {
					HostDetails location = getBestLocationByService(serviceName,
						servicesLocationMonitoring.getFirst().get(serviceName),
						servicesLocationMonitoring.getSecond().get(serviceName),
						getLocationsForServiceDecisions(serviceDecisions));
					if (location != null) {
						finalLocations.put(serviceName, location);
					}
				}
			}
		}
		return finalLocations;
	}

	@SuppressWarnings("unchecked")
	private Map<String, LocationMonitoring> getLocationMonitoringData(String hostname) {
		String url = String.format("http://%s:%s/api/locations/requests?aggregation", hostname, defaultPort);
		long currentRequestTime = System.currentTimeMillis();
		if (lastRequestTime >= 0) {
			int interval = (int) (currentRequestTime - lastRequestTime);
			url += String.format("&interval=%d", interval);
		}
		lastRequestTime = currentRequestTime;

		Map<String, LocationMonitoring> locationMonitoringData = new HashMap<>();
		try {
			locationMonitoringData = restTemplate.getForObject(url, Map.class);
		}
		catch (RestClientException e) {
			e.printStackTrace();
		}

		return locationMonitoringData;
	}

	private List<NodeLocationMonitoring> getNodesLocationMonitoring() {
		return nodesService.getReadyNodes().parallelStream()
			.map(node -> new NodeLocationMonitoring(node, getLocationMonitoringData(node.getPublicIpAddress())))
			.collect(Collectors.toList());
	}

	private Map<String, Integer> getLocationsForServiceDecisions(List<ServiceDecisionResult> allDecisions) {
		Map<String, Integer> availableLocations = new HashMap<>();
		for (ServiceDecisionResult serviceDecisionResult : allDecisions) {
			HostLocation serviceLocation = serviceDecisionResult.getHostDetails().getLocation();
			for (String locationKey : getLocationsKeys(serviceLocation)) {
				if (availableLocations.containsKey(locationKey)) {
					int newLocationCount = availableLocations.get(locationKey) + 1;
					availableLocations.put(locationKey, newLocationCount);
				}
				else {
					availableLocations.put(locationKey, 1);
				}
			}
		}
		return availableLocations;
	}

	private HostDetails getBestLocationByService(String serviceName, Map<String, Integer> locationMonitoring,
												 int totalCount, Map<String, Integer> locationsByRunningContainers) {
		List<LocationCount> locationsWithMinReqPerc = new ArrayList<>();
		for (Entry<String, Integer> locationReqCount : locationMonitoring.entrySet()) {
			double currentPercentage = ((locationReqCount.getValue() * 1.0) / (totalCount * 1.0)) / PERCENT;
			if (currentPercentage >= minimumRequestCountPercentage) {
				HostLocation hostLocation = getHostDetailsByLocationKey(locationReqCount.getKey()).getLocation();
				String region = hostLocation.getRegion();
				String country = hostLocation.getCountry();
				String city = hostLocation.getCity();
				int runningContainerOnRegion = locationsByRunningContainers.getOrDefault(region, 0);
				int runningContainerOnCountry = locationsByRunningContainers.getOrDefault(region + "_" + country, 0);
				int runingContainersOnLocal = locationsByRunningContainers.getOrDefault(locationReqCount.getKey(), 0);
				LocationCount locCount = new LocationCount(locationReqCount.getKey(), city, country, region, currentPercentage,
					runingContainersOnLocal, runningContainerOnCountry, runningContainerOnRegion);
				locationsWithMinReqPerc.add(locCount);
			}
		}
		Collections.sort(locationsWithMinReqPerc);

		if (!locationsWithMinReqPerc.isEmpty()) {
			log.info("Best location for {} : {}", serviceName, locationsWithMinReqPerc.get(0).toString());
			return getHostDetailsByLocationKey(locationsWithMinReqPerc.get(0).getLocationKey());
		}

		return null;
	}

}
