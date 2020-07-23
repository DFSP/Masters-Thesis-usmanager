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

package pt.unl.fct.microservicemanagement.managermaster.manager.hosts;

import pt.unl.fct.microservicemanagement.managermaster.MasterManagerProperties;
import pt.unl.fct.microservicemanagement.managermaster.Mode;
import pt.unl.fct.microservicemanagement.managermaster.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.managermaster.exceptions.MasterManagerException;
import pt.unl.fct.microservicemanagement.managermaster.manager.bash.BashCommandResult;
import pt.unl.fct.microservicemanagement.managermaster.manager.bash.BashService;
import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainerConstants;
import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainersService;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.DockerProperties;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.proxy.DockerApiProxyService;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.swarm.DockerSwarmService;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.swarm.nodes.NodeRole;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.swarm.nodes.NodesService;
import pt.unl.fct.microservicemanagement.managermaster.manager.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.cloud.CloudHostsService;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.cloud.aws.AwsProperties;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.edge.EdgeHostsService;
import pt.unl.fct.microservicemanagement.managermaster.manager.location.LocationRequestService;
import pt.unl.fct.microservicemanagement.managermaster.manager.location.RegionEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.location.RegionsService;
import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.HostMetricsService;
import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.prometheus.PrometheusService;
import pt.unl.fct.microservicemanagement.managermaster.manager.remote.ssh.SshCommandResult;
import pt.unl.fct.microservicemanagement.managermaster.manager.remote.ssh.SshService;
import pt.unl.fct.microservicemanagement.managermaster.util.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HostsService {

  private final NodesService nodesService;
  private final ContainersService containersService;
  private final DockerSwarmService dockerSwarmService;
  private final EdgeHostsService edgeHostsService;
  private final CloudHostsService cloudHostsService;
  private final SshService sshService;
  private final BashService bashService;
  private final HostMetricsService hostMetricsService;
  private final PrometheusService prometheusService;
  private final LocationRequestService locationRequestService;
  private final RegionsService regionsService;
  private final String localMachineDns;
  private final int maxWorkers;
  private final int maxInstances;
  private final Mode mode;
  private String publicIp;
  private String privateIp;

  public HostsService(@Lazy NodesService nodesService, @Lazy ContainersService containersService,
                      DockerSwarmService dockerSwarmService, EdgeHostsService edgeHostsService,
                      CloudHostsService cloudHostsService, SshService sshService, BashService bashService,
                      HostMetricsService hostMetricsService,
                      PrometheusService prometheusService, @Lazy LocationRequestService locationRequestService,
                      RegionsService regionsService, DockerProperties dockerProperties, AwsProperties awsProperties,
                      MasterManagerProperties masterManagerProperties,
                      HostProperties hostProperties) {
    this.nodesService = nodesService;
    this.containersService = containersService;
    this.dockerSwarmService = dockerSwarmService;
    this.edgeHostsService = edgeHostsService;
    this.cloudHostsService = cloudHostsService;
    this.sshService = sshService;
    this.bashService = bashService;
    this.hostMetricsService = hostMetricsService;
    this.prometheusService = prometheusService;
    this.locationRequestService = locationRequestService;
    this.regionsService = regionsService;
    this.maxWorkers = dockerProperties.getSwarm().getMaxWorkers();
    this.maxInstances = awsProperties.getInitialMaxInstances();
    this.localMachineDns = hostProperties.getLocalMachineDns();
    this.mode = masterManagerProperties.getMode();
  }

  public void setMachineInfo() {
    String username = bashService.getUsername();
    this.publicIp = bashService.getPublicIp();
    this.privateIp = bashService.getPrivateIp();
    if (mode == Mode.LOCAL && !edgeHostsService.hasEdgeHost(localMachineDns)) {
      edgeHostsService.addEdgeHost(EdgeHostEntity.builder()
          .username(username)
          .publicDnsName(localMachineDns)
          .publicIpAddress(publicIp)
          .privateIpAddress(privateIp)
          .region(regionsService.getRegion("eu-central"))
          .country("pt")
          .city("lisbon")
          .build());
    }
  }

  public String getPrivateIp() {
    return this.privateIp;
  }

  public String getPublicIP() {
    return this.publicIp;
  }

  public void clusterHosts() {
    log.info("Clustering hosts into the swarm...");
    setupHost(publicIp, privateIp, NodeRole.MANAGER);
    if (mode == Mode.LOCAL) {
      getLocalWorkerNodes().forEach(edgeHost ->
          setupHost(edgeHost.getHostname(), edgeHost.getPrivateIpAddress(), NodeRole.WORKER)
      );
    } else if (mode == Mode.GLOBAL) {
      getCloudWorkerNodes().forEach(cloudHost ->
          setupHost(cloudHost.getPublicIpAddress(), cloudHost.getPrivateIpAddress(), NodeRole.WORKER)
      );
    }
  }

  private List<CloudHostEntity> getCloudWorkerNodes() {
    int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
    int maxInstances = Math.max(this.maxInstances, maxWorkers);
    List<CloudHostEntity> cloudHosts = new ArrayList<>(maxInstances);
    for (var i = 0; i < maxInstances; i++) {
      cloudHosts.add(chooseCloudHost());
    }
    return cloudHosts;
  }

  private List<EdgeHostEntity> getLocalWorkerNodes() {
    int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
    return edgeHostsService.getEdgeHosts().stream()
        .filter(edgeHost -> Objects.equals(edgeHost.getPublicIpAddress(), this.publicIp))
        .filter(edgeHost -> !Objects.equals(edgeHost.getPrivateIpAddress(), this.privateIp))
        .filter(this::isEdgeHostRunning)
        .limit(maxWorkers)
        .collect(Collectors.toList());
  }

  private SimpleNode setupHost(String publicIpAddress, String privateIpAddress, NodeRole role) {
    log.info("Setting up host {} ({}) with role {}", publicIpAddress, privateIpAddress, role);
    String dockerApiProxyContainerId = containersService.launchDockerApiProxy(publicIpAddress, false);
    SimpleNode node;
    switch (role) {
      case MANAGER:
        node = setupSwarmManager(publicIpAddress, privateIpAddress);
        break;
      case WORKER:
        node = setupSwarmWorker(publicIpAddress, privateIpAddress);
        break;
      default:
        throw new UnsupportedOperationException();
    }
    containersService.addContainer(dockerApiProxyContainerId);
    prometheusService.launchPrometheus(publicIpAddress);
    locationRequestService.launchRequestLocationMonitor(publicIpAddress);
    return node;
  }

  private SimpleNode setupSwarmManager(String publicIpAddress, String privateIpAddress) {
    SimpleNode node;
    boolean isLeader = Objects.equals(publicIpAddress, this.publicIp)
        && Objects.equals(privateIpAddress, this.privateIp);
    log.info(isLeader + "");
    if (isLeader) {
      dockerSwarmService.leaveSwarm(privateIpAddress);
      node = dockerSwarmService.initSwarm();
    } else {
      node = joinSwarm(publicIpAddress, privateIpAddress, NodeRole.MANAGER);
    }
    return node;
  }

  private SimpleNode setupSwarmWorker(String publicIpAddress, String privateIpAddress) {
    return joinSwarm(publicIpAddress, privateIpAddress, NodeRole.WORKER);
  }

  private SimpleNode joinSwarm(String publicIpAddress, String privateIpAddress, NodeRole role) {
    return dockerSwarmService.joinSwarm(publicIpAddress, privateIpAddress, role);
  }

  public String getAvailableHost(double avgContainerMem, HostDetails hostDetails) {
    /*if (hostDetails instanceof EdgeHostDetails) {
      final var edgeHostDetails = (EdgeHostDetails) hostDetails;
      return getAvailableNodeHostname(avgContainerMem, edgeHostDetails.getRegion(),
          edgeHostDetails.getCountry(), edgeHostDetails.getCity());
    }
    else if (hostDetails instanceof AwsHostDetails) {
      final var awsHostDetails = (AwsHostDetails) hostDetails;
      return getAvailableNodeHostname(avgContainerMem, awsHostDetails.getRegion());
    }
    else {
      throw new NotImplementedException();
    }*/
    return getAvailableHost(avgContainerMem, hostDetails.getRegion(),
        hostDetails.getCountry(), hostDetails.getCity());
  }

  public String getAvailableHost(double avgContainerMem, String region) {
    return getAvailableHost(avgContainerMem, region, "", "");
  }

  //FIXME
  public String getAvailableHost(double expectedMemoryConsumption, String region, String country, String city) {
    //TODO try to improve method
    log.info("Looking for available nodes to host container with at least '{}' memory at region '{}', country '{}', "
        + "city '{}'", expectedMemoryConsumption, region, country, city);
    var otherRegionsHosts = new LinkedList<String>();
    var sameRegionHosts = new LinkedList<String>();
    var sameCountryHosts = new LinkedList<String>();
    var sameCityHosts = new LinkedList<String>();
    List<SimpleNode> nodes = nodesService.getActiveNodes();
    nodes.stream()
        .map(SimpleNode::getHostname)
        .filter(hostname -> hostMetricsService.nodeHasAvailableResources(hostname, expectedMemoryConsumption))
        .forEach(hostname -> {
          HostDetails hostDetails = getHostDetails(hostname);
          if (Objects.equals(hostDetails.getRegion(), region)) {
            sameRegionHosts.add(hostname);
            /*if (hostDetails instanceof EdgeHostDetails) {
              final var edgeHostDetails = (EdgeHostDetails) hostDetails;
              if (Objects.equals(country, edgeHostDetails.getCountry())) { //TODO confirm that country is never empty
                sameCountryHosts.add(hostname);
                if (Objects.equals(city, edgeHostDetails.getCity())) {  //TODO confirm that city is never empty
                  sameCityHosts.add(hostname);
                }
              }
            }*/
            if (!Text.isNullOrEmpty(country) && hostDetails.getCountry().equalsIgnoreCase(country)) {
              sameCountryHosts.add(hostname);
            }
            if (!Text.isNullOrEmpty(city) && hostDetails.getCity().equalsIgnoreCase(city)) {
              sameCityHosts.add(hostname);
            }
          } else {
            otherRegionsHosts.add(hostname);
          }
        });
    //TODO https://developers.google.com/maps/documentation/geocoding/start?csw=1
    log.info("Found hosts {} on same region", sameRegionHosts.toString());
    log.info("Found hosts {} on same country", sameCountryHosts.toString());
    log.info("Found hosts {} on same city", sameCityHosts.toString());
    log.info("Found hosts {} on other regions", otherRegionsHosts.toString());
    var random = new Random();
    if (!sameCityHosts.isEmpty()) {
      return sameCityHosts.get(random.nextInt(sameCityHosts.size()));
    } else if (!sameCountryHosts.isEmpty()) {
      return sameCountryHosts.get(random.nextInt(sameCountryHosts.size()));
    } else if (!sameRegionHosts.isEmpty()) {
      return sameRegionHosts.get(random.nextInt(sameRegionHosts.size()));
    } else if (!otherRegionsHosts.isEmpty() && !"us-east-1".equals(region)) {
      //TODO porquê excluir a região us-east-1?
      // TODO: review otherHostRegion and region us-east-1
      return otherRegionsHosts.get(random.nextInt(otherRegionsHosts.size()));
    } else {
      log.info("Didn't find any available node");
      return addHost(regionsService.getRegion(region), country, city, NodeRole.WORKER).getHostname();
    }
  }

  public HostDetails getHostDetails(String hostname) {
    /*final var edgeHost = edgeHostsService.getEdgeHostByHostname(hostname);
    if (edgeHost != null) {
      return new EdgeHostDetails(hostname, getContinent(edgeHost.getRegion()),
          edgeHost.getRegion(), edgeHost.getCountry(), edgeHost.getCity());
    }
    else {
      final var instance = awsService.getInstanceByPublicIpAddr(hostname);
      final var zone = instance.getPlacement().getAvailabilityZone();
      final var region = Character.isDigit(zone.charAt(zone.length() - 1)) ?
          zone :
          zone.substring(0, zone.length() - 1);
      return new AwsHostDetails(hostname, getContinent(region), region);
    }*/
    String publicDnsName;
    String publicIpAddress;
    String privateIpAddress;
    String city;
    String country;
    String region;
    String continent;
    try {
      EdgeHostEntity edgeHost = edgeHostsService.getEdgeHost(hostname);
      publicDnsName = edgeHost.getPublicDnsName();
      publicIpAddress = edgeHost.getPublicIpAddress();
      privateIpAddress = edgeHost.getPrivateIpAddress();
      city = edgeHost.getCity();
      country = edgeHost.getCountry();
      region = edgeHost.getRegion().getName();
      continent = getContinent(region);
    } catch (EntityNotFoundException e) {
      CloudHostEntity cloudHost = cloudHostsService.getCloudHostByHostname(hostname);
      publicDnsName = cloudHost.getPublicDnsName();
      publicIpAddress = cloudHost.getPublicIpAddress();
      privateIpAddress = cloudHost.getPrivateIpAddress();
      city = "";
      country = "";
      String zone = cloudHost.getPlacement().getAvailabilityZone();
      region = Character.isDigit(zone.charAt(zone.length() - 1)) ? zone : zone.substring(0, zone.length() - 1);
      continent = getContinent(zone);
    }
    return new HostDetails(publicDnsName, publicIpAddress, privateIpAddress, city, country, region, continent);
  }

  private String getContinent(String region) {
    String continent;
    //TODO remove the "none" region
    if (Text.isNullOrEmpty(region) || Objects.equals(region, "none")) {
      continent = "";
    } else {
      String zone = region.substring(0, region.indexOf('-'));
      //TODO convert strings into enum
      if (zone.startsWith("us") || zone.startsWith("ca")) {
        continent = "na";
      } else if (region.startsWith("sa")) {
        continent = "sa";
      } else if (region.startsWith("eu")) {
        continent = "eu";
      } else if (region.contains("ap-southeast-1")) {
        continent = "oc";
      } else if (region.startsWith("ap")) {
        continent = "as";
      } else {
        continent = "";
      }
    }
    return continent;
  }

  public SimpleNode addHost(RegionEntity region, String country, String city, NodeRole role) {
    String publicIpAddress;
    String privateIpAddress;
    Optional<EdgeHostEntity> edgeHost = chooseEdgeHost(region, country, city);
    if (edgeHost.isPresent()) {
      publicIpAddress = edgeHost.get().getPublicIpAddress();
      privateIpAddress = edgeHost.get().getPrivateIpAddress();
    } else {
      CloudHostEntity cloudHost = chooseCloudHost();
      publicIpAddress = cloudHost.getPublicIpAddress();
      privateIpAddress = cloudHost.getPrivateIpAddress();
    }
    log.info("Found host {} ({}) to join swarm at {}/{}/{} as {}", publicIpAddress, privateIpAddress,
        region.getName(), country, city, role.name());
    return setupHost(publicIpAddress, privateIpAddress, role);
  }

  public SimpleNode addHost(String host, NodeRole role) {
    String publicIpAddress;
    String privateIpAddress;
    try {
      EdgeHostEntity edgeHost = edgeHostsService.getEdgeHost(host);
      publicIpAddress = edgeHost.getPublicIpAddress();
      privateIpAddress = edgeHost.getPrivateIpAddress();
    } catch (EntityNotFoundException e) {
      CloudHostEntity cloudHost = cloudHostsService.getCloudHost(host);
      if (cloudHost.getState().getCode() != AwsInstanceState.RUNNING.getCode()) {
        cloudHost = cloudHostsService.startCloudHost(host, false);
      }
      publicIpAddress = cloudHost.getPublicIpAddress();
      privateIpAddress = cloudHost.getPrivateIpAddress();
    }
    return setupHost(publicIpAddress, privateIpAddress, role);
  }

  public void removeHost(String hostname) {
    containersService.getSystemContainers(hostname).stream()
        .filter(c -> !Objects.equals(c.getLabels().get(ContainerConstants.Label.SERVICE_NAME),
            DockerApiProxyService.DOCKER_API_PROXY))
        .forEach(c -> containersService.stopContainer(c.getContainerId()));
    dockerSwarmService.leaveSwarm(hostname);
  }

  private boolean isCloudHost(String hostname) {
    return cloudHostsService.hasCloudHostByHostname(hostname);
  }

  private boolean isEdgeHostRunning(EdgeHostEntity edgeHost) {
    return sshService.hasConnection(edgeHost.getHostname());
  }

  private Optional<EdgeHostEntity> chooseEdgeHost(RegionEntity region, String country, String city) {
    List<EdgeHostEntity> edgeHosts = List.of();
    if (!Text.isNullOrEmpty(city)) {
      edgeHosts = edgeHostsService.getHostsByCity(city);
    } else if (!Text.isNullOrEmpty(country)) {
      edgeHosts = edgeHostsService.getHostsByCountry(country);
    } else if (region != null) {
      edgeHosts = edgeHostsService.getHostsByRegion(region);
    }
    return edgeHosts.stream()
        .filter(edgeHost -> !nodesService.isPartOfSwarm(edgeHost.getHostname()))
        .filter(this::isEdgeHostRunning)
        .findFirst();
  }

  //TODO choose cloud host based on region
  private CloudHostEntity chooseCloudHost() {
    for (var cloudHost : cloudHostsService.getCloudHosts()) {
      int stateCode = cloudHost.getState().getCode();
      if (stateCode == AwsInstanceState.RUNNING.getCode()) {
        String hostname = cloudHost.getPublicIpAddress();
        if (!nodesService.isPartOfSwarm(hostname)) {
          return cloudHost;
        }
      } else if (stateCode == AwsInstanceState.STOPPED.getCode()) {
        return cloudHostsService.startCloudHost(cloudHost, false);
      }
    }
    return cloudHostsService.startCloudHost();
  }

  public List<String> executeCommand(String command, String hostname) {
    List<String> result = null;
    String error = null;
    if (this.privateIp.equalsIgnoreCase(hostname) || this.publicIp.equalsIgnoreCase(hostname)) {
      BashCommandResult bashCommandResult = bashService.executeCommand(command);
      if (!bashCommandResult.isSuccessful()) {
        error = String.join("\n", bashCommandResult.getError());
      } else {
        result = bashCommandResult.getOutput();
      }
    } else {
      SshCommandResult sshCommandResult = sshService.executeCommand(hostname, command);
      if (!sshCommandResult.isSuccessful()) {
        error = String.join("\n", sshCommandResult.getError());
      } else {
        result = sshCommandResult.getOutput();
      }
    }
    if (error != null) {
      throw new MasterManagerException("%s", error);
    }
    return result;
  }

  public String findAvailableExternalPort(String startExternalPort) {
    return findAvailableExternalPort("127.0.0.1", startExternalPort);
  }

  public String findAvailableExternalPort(String hostname, String startExternalPort) {
    var command = "sudo lsof -i -P -n | grep LISTEN | awk '{print $9}' | cut -d: -f2";
    try {
      List<Integer> usedExternalPorts = executeCommand(command, hostname).stream()
          .filter(v -> Pattern.compile("-?\\d+(\\.\\d+)?").matcher(v).matches())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      for (var i = Integer.parseInt(startExternalPort); ; i++) {
        if (!usedExternalPorts.contains(i)) {
          return String.valueOf(i);
        }
      }
    } catch (MasterManagerException e) {
      throw new MasterManagerException("Unable to find currently used external ports at %s: %s ", hostname,
          e.getMessage());
    }
  }

}
