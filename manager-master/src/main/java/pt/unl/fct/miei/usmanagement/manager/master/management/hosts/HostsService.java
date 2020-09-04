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

package pt.unl.fct.miei.usmanagement.manager.master.management.hosts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.master.ManagerMasterProperties;
import pt.unl.fct.miei.usmanagement.manager.master.Mode;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.bash.BashCommandResult;
import pt.unl.fct.miei.usmanagement.manager.master.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.aws.AwsProperties;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.location.LocationRequestService;
import pt.unl.fct.miei.usmanagement.manager.master.management.location.RegionsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.master.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.master.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.master.util.Text;

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
  @Getter
  private HostAddress hostAddress;

  public HostsService(@Lazy NodesService nodesService, @Lazy ContainersService containersService,
                      DockerSwarmService dockerSwarmService, EdgeHostsService edgeHostsService,
                      CloudHostsService cloudHostsService, SshService sshService, BashService bashService,
                      HostMetricsService hostMetricsService,
                      PrometheusService prometheusService, @Lazy LocationRequestService locationRequestService,
                      RegionsService regionsService, DockerProperties dockerProperties, AwsProperties awsProperties,
                      ManagerMasterProperties managerMasterProperties,
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
    this.maxWorkers = dockerProperties.getSwarm().getInitialMaxWorkers();
    this.maxInstances = awsProperties.getInitialMaxInstances();
    this.localMachineDns = hostProperties.getLocalMachineDns();
    this.mode = managerMasterProperties.getMode();
  }

  public HostAddress setHostAddress() {
    String username = bashService.getUsername();
    String publicIp = bashService.getPublicIp();
    String privateIp = bashService.getPrivateIp();
    if ((mode == null || mode == Mode.LOCAL) && !edgeHostsService.hasEdgeHost(localMachineDns)) {
      edgeHostsService.addEdgeHost(EdgeHostEntity.builder()
          .username(username)
          .publicDnsName(localMachineDns)
          .publicIpAddress(publicIp)
          .privateIpAddress(privateIp)
          .region(regionsService.getRegion("eu-central"))
          .country("pt")
          .city("lisbon")
          .build());
      this.hostAddress = new HostAddress(username, localMachineDns, publicIp, privateIp);
    } else {
      this.hostAddress = new HostAddress(username, publicIp, privateIp);
    }
    return hostAddress;
  }

  public void clusterHosts() {
    log.info("Clustering hosts into the swarm on mode {}...", mode);
    setupHost(hostAddress.getPublicIpAddress(), hostAddress.getPrivateIpAddress(), NodeRole.MANAGER);
    if (mode == null || mode == Mode.LOCAL) {
      getLocalWorkerNodes().forEach(edgeHost ->
          setupHost(edgeHost.getHostname(), edgeHost.getPrivateIpAddress(), NodeRole.WORKER));
    }
    if (mode == null || mode == Mode.GLOBAL) {
      getCloudWorkerNodes().forEach(cloudHost ->
          setupHost(cloudHost.getPublicIpAddress(), cloudHost.getPrivateIpAddress(), NodeRole.WORKER));
    }
  }

  private List<CloudHostEntity> getCloudWorkerNodes() {
    int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
    int maxInstances = Math.min(this.maxInstances, maxWorkers);
    List<CloudHostEntity> cloudHosts = new ArrayList<>(maxInstances);
    for (var i = 0; i < maxInstances; i++) {
      cloudHosts.add(chooseCloudHost());
    }
    return cloudHosts;
  }

  private List<EdgeHostEntity> getLocalWorkerNodes() {
    int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
    return edgeHostsService.getEdgeHosts().stream()
        .filter(edgeHost -> Objects.equals(edgeHost.getPublicIpAddress(), this.hostAddress.getPublicIpAddress()))
        .filter(edgeHost -> !Objects.equals(edgeHost.getPrivateIpAddress(), this.hostAddress.getPrivateIpAddress()))
        .filter(this::isEdgeHostRunning)
        .limit(maxWorkers)
        .collect(Collectors.toList());
  }

  public boolean isLocalhost(String publicIp, String privateIp) {
    String machinePublicIp = hostAddress.getPublicIpAddress();
    String machinePrivateIp = hostAddress.getPrivateIpAddress();
    return Objects.equals(publicIp, machinePublicIp) && Objects.equals(privateIp, machinePrivateIp);
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
    if (isLocalhost(publicIpAddress, privateIpAddress)) {
      log.info("Setting up docker swarm leader");
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
    try {
      return nodesService.getHostNode(publicIpAddress);
      /*if (node.getRole() == role) {
        return dockerSwarmService.rejoinSwarm(node.getId());
      } else {
        dockerSwarmService.leaveSwarm(publicIpAddress);
        //nodesService.removeNode(node.getId());
        return dockerSwarmService.joinSwarm(publicIpAddress, privateIpAddress, role);
      }*/
    } catch (EntityNotFoundException nodeNotFound) {
      return dockerSwarmService.joinSwarm(publicIpAddress, privateIpAddress, role);
    }
  }

  public List<String> getAvailableHostsOnRegions(double expectedMemoryConsumption, List<String> regions) {
    return regions.stream()
        .map(regionsService::getRegion)
        .map(regionEntity -> new HostLocation(null, null, regionEntity.getName(), null))
        .map(location -> getAvailableHost(expectedMemoryConsumption, location))
        .collect(Collectors.toList());
  }

  public String getAvailableHost(double expectedMemoryConsumption, Coordinates coordinates) {
    // TODO implement algorithm to get the closest machine based on coordinates
    return null;
  }

  //FIXME
  @Deprecated
  public String getAvailableHost(double expectedMemoryConsumption, HostLocation hostLocation) {
    //TODO try to improve method
    String region = hostLocation.getRegion();
    String country = hostLocation.getCountry();
    String city = hostLocation.getCity();
    log.info("Looking for available nodes to host container with at least '{}' memory at region '{}', country '{}', "
        + "city '{}'", expectedMemoryConsumption, region, country, city);
    var otherRegionsHosts = new LinkedList<String>();
    var sameRegionHosts = new LinkedList<String>();
    var sameCountryHosts = new LinkedList<String>();
    var sameCityHosts = new LinkedList<String>();
    nodesService.getActiveNodes().stream()
        .map(SimpleNode::getHostname)
        .filter(hostname -> hostMetricsService.nodeHasAvailableResources(hostname, expectedMemoryConsumption))
        .forEach(hostname -> {
          HostLocation nodeLocation = getHostDetails(hostname).getHostLocation();
          String nodeRegion = nodeLocation.getRegion();
          String nodeCountry = nodeLocation.getCountry();
          String nodeCity = nodeLocation.getCity();
          if (Objects.equals(nodeRegion, region)) {
            sameRegionHosts.add(hostname);
            if (!Text.isNullOrEmpty(country) && nodeCountry.equalsIgnoreCase(country)) {
              sameCountryHosts.add(hostname);
            }
            if (!Text.isNullOrEmpty(city) && nodeCity.equalsIgnoreCase(city)) {
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
    HostAddress hostAddress;
    HostLocation hostLocation;
    try {
      EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(hostname);
      hostAddress = edgeHost.getAddress();
      hostLocation = edgeHost.getLocation();
    } catch (EntityNotFoundException e) {
      CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIp(hostname);
      hostAddress = cloudHost.getAddress();
      hostLocation = cloudHost.getLocation();
    }
    return new HostDetails(hostAddress, hostLocation);
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
      CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIdOrIp(host);
      if (cloudHost.getState().getCode() != AwsInstanceState.RUNNING.getCode()) {
        cloudHost = cloudHostsService.startCloudHost(host, false);
      }
      publicIpAddress = cloudHost.getPublicIpAddress();
      privateIpAddress = cloudHost.getPrivateIpAddress();
    } catch (EntityNotFoundException e) {
      EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(host);
      publicIpAddress = edgeHost.getPublicIpAddress();
      privateIpAddress = edgeHost.getPrivateIpAddress();
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
    if (this.hostAddress.getPrivateIpAddress().equalsIgnoreCase(hostname)
        || this.hostAddress.getPublicIpAddress().equalsIgnoreCase(hostname)) {
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

  public boolean isValidIPAddress(String ip) {
    // Regex for digit from 0 to 255.
    String zeroTo255
        = "(\\d{1,2}|(0|1)\\"
        + "d{2}|2[0-4]\\d|25[0-5])";

    // Regex for a digit from 0 to 255 and
    // followed by a dot, repeat 4 times.
    // this is the regex to validate an IP address.
    String regex
        = zeroTo255 + "\\."
        + zeroTo255 + "\\."
        + zeroTo255 + "\\."
        + zeroTo255;

    // Compile the ReGex
    Pattern p = Pattern.compile(regex);

    // If the IP address is empty
    // return false
    if (ip == null) {
      return false;
    }

    // Pattern class contains matcher() method
    // to find matching between given IP address
    // and regular expression.
    Matcher m = p.matcher(ip);

    // Return if the IP address
    // matched the ReGex
    return m.matches();
  }

  public String getPublicIpAddressFromHost(String host) {
    try {
      return cloudHostsService.getCloudHostByIdOrDns(host).getPublicIpAddress();
    } catch (EntityNotFoundException ignored) {
      return edgeHostsService.getEdgeHostByDns(host).getPublicIpAddress();
    }
  }

}
