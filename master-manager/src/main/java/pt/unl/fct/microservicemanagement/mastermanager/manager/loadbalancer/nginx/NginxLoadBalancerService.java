/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package pt.unl.fct.microservicemanagement.mastermanager.manager.loadbalancer.nginx;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import pt.unl.fct.microservicemanagement.mastermanager.exceptions.MasterManagerException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.docker.DockerProperties;
import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainerConstants;
import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainerEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainersService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.HostsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.location.RegionsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServiceEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServicesService;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class NginxLoadBalancerService {

  public static final String LOAD_BALANCER = "load-balancer";

  private final ContainersService containersService;
  private final HostsService hostsService;
  private final ServicesService serviceService;
  private final RegionsService regionsService;

  private final String nginxApiUrl;
  private final String dockerApiProxyUsername;
  private final String dockerApiProxyPassword;
  private final HttpHeaders headers;
  private final RestTemplate restTemplate;

  public NginxLoadBalancerService(@Lazy ContainersService containersService, HostsService hostsService,
                                  ServicesService serviceService, RegionsService regionsService,
                                  NginxLoadBalancerProperties nginxLoadBalancerProperties,
                                  DockerProperties dockerProperties) {
    this.containersService = containersService;
    this.hostsService = hostsService;
    this.serviceService = serviceService;
    this.regionsService = regionsService;
    this.nginxApiUrl = nginxLoadBalancerProperties.getApiUrl();
    this.headers = new HttpHeaders();
    this.dockerApiProxyUsername = dockerProperties.getApiProxy().getUsername();
    this.dockerApiProxyPassword = dockerProperties.getApiProxy().getPassword();
    var auth = String.format("%s:%s", dockerApiProxyUsername, dockerApiProxyPassword).getBytes();
    var basicAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
    this.headers.add("Authorization", basicAuthorization);
    this.restTemplate = new RestTemplate();
  }

  public List<ContainerEntity> launchLoadBalancers(String serviceName, String[] regions) {
    ServiceEntity serviceConfig = serviceService.getService(LOAD_BALANCER);
    double expectedMemoryConsumption = serviceConfig.getExpectedMemoryConsumption();
    List<String> availableHostnames = Stream.of(regions)
        .map(regionsService::getRegion)
        .map(region -> hostsService.getAvailableHost(expectedMemoryConsumption, region.getName()))
        .collect(Collectors.toList());
    return availableHostnames.stream()
        .map(hostname -> launchLoadBalancer(hostname, serviceName))
        .collect(Collectors.toList());
  }

  private ContainerEntity launchLoadBalancer(String hostname, String serviceName) {
    //TODO get port from properties
    return launchLoadBalancer(hostname, serviceName, "127.0.0.1:1906", "none", "none", "none", "none");
  }

  private ContainerEntity launchLoadBalancer(String hostname, String serviceName, String serverAddr, String continent,
                                             String region, String country, String city) {
    var environment = List.of(
        String.format("%s=%s", ContainerConstants.Environment.SERVER1, serverAddr),
        String.format("%s=%s", ContainerConstants.Environment.SERVER1_CONTINENT, continent),
        String.format("%s=%s", ContainerConstants.Environment.SERVER1_REGION, region),
        String.format("%s=%s", ContainerConstants.Environment.SERVER1_COUNTRY, country),
        String.format("%s=%s", ContainerConstants.Environment.SERVER1_CITY, city),
        String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_USERNAME, dockerApiProxyUsername),
        String.format("%s=%s", ContainerConstants.Environment.BASIC_AUTH_PASSWORD, dockerApiProxyPassword)
    );
    var labels = Map.of(
        ContainerConstants.Label.FOR_SERVICE, serviceName
    );
    return containersService.launchContainer(hostname, LOAD_BALANCER, environment, labels);
  }

  private List<ContainerEntity> getLoadBalancersFromService(String serviceName) {
    return containersService.getContainersWithLabels(Set.of(
        Pair.of(ContainerConstants.Label.SERVICE_NAME, LOAD_BALANCER),
        Pair.of(ContainerConstants.Label.FOR_SERVICE, serviceName)));
  }

  public void addToLoadBalancer(String hostname, String serviceName, String serverAddr, String continent,
                                String region, String country, String city) {
    List<ContainerEntity> loadBalancers = getLoadBalancersFromService(serviceName);
    if (loadBalancers.isEmpty()) {
      ContainerEntity container = launchLoadBalancer(hostname, serviceName, serverAddr, continent, region, country,
          city);
      loadBalancers = List.of(container);
    }
    loadBalancers.forEach(loadBalancer -> {
      String loadBalancerHostname = loadBalancer.getHostname();
      int loadBalancerPort = loadBalancer.getPorts().get(0).getPublicPort();
      var loadBalancerUrl = String.format("%s:%s", loadBalancerHostname, loadBalancerPort);
      var nginxServer = new NginxServer(serverAddr, continent, region, country, city);
      String url = String.format("http://%s%s/servers", loadBalancerUrl, nginxApiUrl);
      HttpEntity<NginxServer> request = new HttpEntity<>(nginxServer, headers);
      ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.POST, request, Message.class);
      Message responseBody = response.getBody();
      String responseMessage = responseBody.getMessage();
      if (!Objects.equals(responseMessage, "success")) {
        log.info("Failed to add server {} to loadBalancer {}: {}", nginxServer, loadBalancerUrl, responseMessage);
      }
    });
  }

  public void removeFromLoadBalancer(String hostname, String containerId) {
    log.info("Removing container '{}' from load balancer", containerId);
    Map<String, String> labels = containersService.getContainer(containerId).getLabels();
    String serviceType = labels.get(ContainerConstants.Label.SERVICE_TYPE);
    String serviceName = labels.get(ContainerConstants.Label.SERVICE_NAME);
    String serverAddress = labels.get(ContainerConstants.Label.SERVICE_ADDRESS);
    if (serviceType == null || serviceName == null | serverAddress == null) {
      throw new MasterManagerException("Failed to remove container %s from load balancer: "
          + "labels %s, %s and %s are required. Current container labels = %s", containerId,
          ContainerConstants.Label.SERVICE_NAME, ContainerConstants.Label.SERVICE_TYPE,
          ContainerConstants.Label.SERVICE_ADDRESS, labels);
    }
    if (!Objects.equals(serviceType, "frontend")) {
      throw new MasterManagerException("Failed to remove container %s from load balancer: "
          + "%s doesn't support load balancer", containerId, serviceType);
    }
    List<ContainerEntity> loadBalancers = getLoadBalancersFromService(serviceName);
    loadBalancers.forEach(loadBalancer -> {
      String url = String.format("http://%s%s/servers",
          loadBalancer.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS), nginxApiUrl);
      var server = new NginxSimpleServer(serverAddress);
      HttpEntity<NginxSimpleServer> request = new HttpEntity<>(server, headers);
      ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Message.class);
      Message responseBody = response.getBody();
      String responseMessage = responseBody.getMessage();
      if (!Objects.equals(responseMessage, "success")) {
        log.info("Failed to delete server '{}' from load balancer '{}': {}", serverAddress, url, responseMessage);
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
