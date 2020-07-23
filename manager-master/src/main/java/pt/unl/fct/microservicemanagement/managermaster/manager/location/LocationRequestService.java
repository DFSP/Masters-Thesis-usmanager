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

package pt.unl.fct.microservicemanagement.managermaster.manager.location;

import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainerEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainersService;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.swarm.nodes.NodesService;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.HostDetails;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.HostsService;
import pt.unl.fct.microservicemanagement.managermaster.manager.rulesystem.decision.ServiceDecisionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class LocationRequestService {

  // TODO refactor
  public static final String REQUEST_LOCATION_MONITOR = "request-location-monitor";
  private static final double PERCENT = 0.01;

  private final NodesService nodesService;
  private final ContainersService containersService;
  private final RegionsService regionsService;
  private final HostsService hostsService;

  private final int defaultPort;
  private final double minimumRequestCountPercentage;
  private final RestTemplate restTemplate;
  private final HttpHeaders headers;

  public LocationRequestService(ContainersService containersService, NodesService nodesService,
                                RegionsService regionsService, HostsService hostsService,
                                LocationRequestProperties locationRequestProperties) {
    this.nodesService = nodesService;
    this.containersService = containersService;
    this.regionsService = regionsService;
    this.hostsService = hostsService;
    this.defaultPort = locationRequestProperties.getPort();
    this.minimumRequestCountPercentage = locationRequestProperties.getMinimumRequestCountPercentage();
    this.restTemplate = new RestTemplate();
    this.headers = new HttpHeaders();
  }

  public ContainerEntity launchRequestLocationMonitor(String hostname) {
    return containersService.launchContainer(hostname, REQUEST_LOCATION_MONITOR, true);
  }

  public List<LocationMonitoringResp> getAllMonitoringDataTop(String requestLocationHostname, int seconds) {
    String url = String.format("http://%s:%s/api/monitoringinfo/all/top/%d", requestLocationHostname, defaultPort,
        seconds);
    HttpEntity<String> request = new HttpEntity<>(headers);
    List<LocationMonitoringResp> locationMonitoringResps = new ArrayList<>();
    try {
      ResponseEntity<LocationMonitoringResp[]> response = restTemplate.exchange(url, HttpMethod.GET, request,
          LocationMonitoringResp[].class);
      locationMonitoringResps = Arrays.asList(response.getBody());
    } catch (RestClientException e) {
      e.printStackTrace();
    }
    return locationMonitoringResps;
  }

  public Map<String, HostDetails> getBestLocationToStartServices(
      Map<String, List<ServiceDecisionResult>> allServicesDecisions, int secondsFromLastRun) {
    Pair<Map<String, Map<String, Integer>>, Map<String, Integer>> servicesLocationMonitoring = getLocationMonitoring(
        secondsFromLastRun);
    Map<String, HostDetails> finalLocations = new HashMap<>();
    for (Entry<String, List<ServiceDecisionResult>> services : allServicesDecisions.entrySet()) {
      String serviceName = services.getKey();
      List<ServiceDecisionResult> serviceAllDecisions = services.getValue();
      if (servicesLocationMonitoring.getSecond().containsKey(serviceName)) {
        if (!serviceAllDecisions.isEmpty()) {
          HostDetails location = getBestLocationByService(serviceName,
              servicesLocationMonitoring.getFirst().get(serviceName),
              servicesLocationMonitoring.getSecond().get(serviceName),
              getLocationsByRunningContainers(serviceAllDecisions));
          if (location != null) {
            finalLocations.put(serviceName, location);
          }
        }
      }
    }
    return finalLocations;
  }

  private Pair<Map<String, Map<String, Integer>>, Map<String, Integer>> getLocationMonitoring(int seconds) {
    List<SimpleNode> nodes = nodesService.getReadyNodes();
    Map<String, Map<String, Integer>> serviceCountLocations = new HashMap<>();
    Map<String, Integer> serviceTotalRequestCount = new HashMap<>();
    List<LocationMonitoringResp> allLocationMonitoringData = new ArrayList<>();

    for (SimpleNode node : nodes) {
      String hostname = node.getHostname();
      allLocationMonitoringData.addAll(getAllMonitoringDataTop(hostname, seconds));
    }
    for (LocationMonitoringResp locationMonitoringResp : allLocationMonitoringData) {
      int count = locationMonitoringResp.getLocationData().getCount();
      String serviceName = locationMonitoringResp.getToService();
      String fromContinent = locationMonitoringResp.getLocationData().getFromContinent();
      String fromRegion = locationMonitoringResp.getLocationData().getFromRegion();
      String fromCountry = locationMonitoringResp.getLocationData().getFromCountry();
      String fromCity = locationMonitoringResp.getLocationData().getFromCity();

      List<String> locationKeys = getLocationsKeys(fromCity, fromCountry, fromRegion, fromContinent);

      if (serviceCountLocations.containsKey(serviceName)) {
        int newTotalReqCount = serviceTotalRequestCount.get(serviceName) + count;
        serviceTotalRequestCount.put(serviceName, newTotalReqCount);
        Map<String, Integer> countLocations = serviceCountLocations.get(serviceName);
        for (String locationKey : locationKeys) {
          if (countLocations.containsKey(locationKey)) {
            int newCount = countLocations.get(locationKey) + count;
            countLocations.put(locationKey, newCount);
          } else {
            countLocations.put(locationKey, count);
          }
        }
        serviceCountLocations.put(serviceName, countLocations);
      } else {
        serviceTotalRequestCount.put(serviceName, count);
        Map<String, Integer> countLocations = new HashMap<>();
        for (String locationKey : locationKeys) {
          countLocations.put(locationKey, count);
        }
        serviceCountLocations.put(serviceName, countLocations);
      }
    }

    return Pair.of(serviceCountLocations, serviceTotalRequestCount);
  }

  private Map<String, Integer> getLocationsByRunningContainers(List<ServiceDecisionResult> allDecisions) {
    Map<String, Integer> availableLocations = new HashMap<>();
    Map<String, HostDetails> hostnamesFound = new HashMap<>();

    for (ServiceDecisionResult serviceDecisionResult : allDecisions) {
      String serviceHostname = serviceDecisionResult.getHostname();
      if (!hostnamesFound.containsKey(serviceHostname)) {
        HostDetails hostDetails = hostsService.getHostDetails(serviceHostname);
        hostnamesFound.put(serviceHostname, hostDetails);
      }
      HostDetails hostDetails = hostnamesFound.get(serviceHostname);
      List<String> locationKeys = getLocationsKeys(hostDetails.getCity(), hostDetails.getCountry(),
          hostDetails.getRegion(), hostDetails.getContinent());
      for (String locationKey : locationKeys) {
        if (availableLocations.containsKey(locationKey)) {
          int newLocationCount = availableLocations.get(locationKey) + 1;
          availableLocations.put(locationKey, newLocationCount);
        } else {
          availableLocations.put(locationKey, 1);
        }
      }
    }
    return availableLocations;
  }

  private HostDetails getBestLocationByService(String serviceName, Map<String, Integer> locationMonitoring,
                                               int totalCount, Map<String, Integer> locationsbyRunningContainers) {
    List<LocationCount> locationsWithMinReqPerc = new ArrayList<>();
    for (Entry<String, Integer> locationReqCount : locationMonitoring.entrySet()) {
      double currentPercentage = ((locationReqCount.getValue() * 1.0) / (totalCount * 1.0)) / PERCENT;
      if (currentPercentage >= minimumRequestCountPercentage) {
        HostDetails locationDetails = getHostDetailsByLocationKey(locationReqCount.getKey());
        String region = locationDetails.getRegion();
        String country = locationDetails.getCountry();
        String city = locationDetails.getCity();
        /*if (locationDetails instanceof EdgeHostDetails) {
          final var edgeHostDetails = (EdgeHostDetails)locationDetails;
          country = edgeHostDetails.getCountry();
          city = edgeHostDetails.getCity();
        }*/
        int runningContainerOnRegion = locationsbyRunningContainers.getOrDefault(region, 0);
        int runningContainerOnCountry = locationsbyRunningContainers.getOrDefault(region + "_" + country, 0);
        int runingContainersOnLocal = locationsbyRunningContainers.getOrDefault(locationReqCount.getKey(), 0);
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

  private HostDetails getHostDetailsByLocationKey(String locationKey) {
    String[] serviceLocationSplit = locationKey.split("_");
    String region = "";
    String country = "";
    String city = "";
    for (int i = 0; i < serviceLocationSplit.length; i++) {
      if (i == 0) {
        region = serviceLocationSplit[i];
      } else if (i == 1) {
        country = serviceLocationSplit[i];
      } else if (i == 2) {
        city = serviceLocationSplit[i];
      }
    }
    return new HostDetails(city, country, region);
  }

  /*public String getRegionByLocationKey(String locationKey) {
    return getHostDetailsByLocationKey(locationKey).getRegion();
  }*/

  private List<String> getLocationsKeys(String city, String country, String region, String continent) {
    String finalRegion = region;
    List<String> locationKeys = new ArrayList<>();
    if ("none".equals(region)) {
      finalRegion = getBestRegionByLocationInfo(continent, country, city);
    }
    if (!country.isEmpty()) {
      if (!city.isEmpty()) {
        locationKeys.add(finalRegion + "_" + country + "_" + city);
      }
      locationKeys.add(finalRegion + "_" + country);
    }
    locationKeys.add(finalRegion);
    return locationKeys;
  }

  private String getLocationKey(String region, String country, String city) {
    return region + (!country.isEmpty() ? "_" + country : "") + (!city.isEmpty() ? "_" + city : "");
  }

  // TODO: improve region choice, using country and city
  private String getBestRegionByLocationInfo(String continent, String country, String city) {
    switch (country) {
      case "pt":
        return "eu-central-1";
      case "gb":
        return "eu-west-2";
      case "us":
        return "us-east-1";
      default:
    }
    List<RegionEntity> regions = regionsService.getRegions();
    List<String> foundRegion = new ArrayList<>();
    String regionNameBegin = "";
    switch (continent) {
      case "na":
        regionNameBegin = "us-";
        break;
      case "sa":
        regionNameBegin = "sa-";
        break;
      case "eu":
      case "af":
        regionNameBegin = "eu-";
        break;
      case "as":
      case "oc":
        regionNameBegin = "ap-";
        break;
      default:
    }
    for (RegionEntity region : regions) {
      String regionName = region.getName();
      if (regionName.contains(regionNameBegin)) {
        foundRegion.add(regionName);
      }
    }
    if (!foundRegion.isEmpty()) {
      Random rand = new Random();
      int index = rand.nextInt(foundRegion.size());
      return foundRegion.get(index);
    }
    return "";
  }
}
