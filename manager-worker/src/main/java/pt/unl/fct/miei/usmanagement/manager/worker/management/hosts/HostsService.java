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

package pt.unl.fct.miei.usmanagement.manager.worker.management.hosts;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.bash.BashCommandResult;
import pt.unl.fct.miei.usmanagement.manager.worker.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.location.LocationRequestService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.location.RegionsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.worker.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.worker.util.Text;

@Slf4j
@Service
public class HostsService {

  private static final int DELAY_STOP_HOST = 60 * 1000;
  // TODO como adicionar o novo host após de ser atribuído ao worker?
  //e remover?

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
  private final ServicesService servicesService;

  @Getter
  private HostAddress hostAddress;

  @Value("${external-id}")
  private String workerManagerId;

  public HostsService(@Lazy NodesService nodesService, @Lazy ContainersService containersService,
                      DockerSwarmService dockerSwarmService, EdgeHostsService edgeHostsService,
                      CloudHostsService cloudHostsService, SshService sshService, BashService bashService,
                      HostMetricsService hostMetricsService,
                      PrometheusService prometheusService, @Lazy LocationRequestService locationRequestService,
                      RegionsService regionsService, ServicesService servicesService) {
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
    this.servicesService = servicesService;
  }

  public HostAddress setHostAddress() {
    String publicIp = bashService.getPublicIp();
    String privateIp = bashService.getPrivateIp();
    String username = bashService.getUsername();
    this.hostAddress = new HostAddress(workerManagerId, publicIp, privateIp, username);
    return hostAddress;
  }

  public void clusterHosts() {
    log.info("Clustering hosts into the swarm...");
    String publicIp = hostAddress.getPublicIpAddress();
    String privateIp = hostAddress.getPrivateIpAddress();
    setupHost(publicIp, privateIp, NodeRole.MANAGER);
    edgeHostsService.getEdgeHosts().stream()
        .filter(edgeHost -> !isLocalhost(edgeHost.getPublicIpAddress(), edgeHost.getPrivateIpAddress()))
        // TODO remove filter after each worker receiving only the assigned hosts
        .filter(edgeHost -> edgeHost.getManagedByWorker() != null
            && edgeHost.getManagedByWorker().getId().equals(workerManagerId))
        .forEach(edgeHost ->
            setupHost(edgeHost.getHostname(), edgeHost.getPrivateIpAddress(), NodeRole.WORKER));
    cloudHostsService.getCloudHosts().stream()
        .filter(cloudHost -> !isLocalhost(cloudHost.getPublicIpAddress(), cloudHost.getPrivateIpAddress()))
        // TODO remove filter after each worker receiving only the assigned hosts
        .filter(cloudHost -> cloudHost.getManagedByWorker() != null
            && cloudHost.getManagedByWorker().getId().equals(workerManagerId))
        .forEach(cloudHost ->
            setupHost(cloudHost.getPublicIpAddress(), cloudHost.getPrivateIpAddress(), NodeRole.WORKER));
  }

  // TODO change decisions, rules, etc, to store public and privateIp
  public boolean isLocalhost(String localhost) {
    String machinePublicIp = hostAddress.getPublicIpAddress();
    return Objects.equals(localhost, machinePublicIp);
  }

  public boolean isLocalhost(String publicIp, String privateIp) {
    String machinePublicIp = hostAddress.getPublicIpAddress();
    String machinePrivateIp = hostAddress.getPrivateIpAddress();
    return Objects.equals(publicIp, machinePublicIp) && Objects.equals(privateIp, machinePrivateIp);
  }

  public SimpleNode setupHostEntity(Long id) {
    try {
      CloudHostEntity cloudHost = cloudHostsService.getCloudHostById(id);
      return setupHost(cloudHost.getPublicIpAddress(), cloudHost.getPrivateIpAddress(), NodeRole.WORKER);
    } catch (EntityNotFoundException ignored) {
      EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostById(id);
      return setupHost(edgeHost.getPublicIpAddress(), edgeHost.getPrivateIpAddress(), NodeRole.WORKER);
    }
  }

  public SimpleNode setupHost(String publicIpAddress, String privateIpAddress, NodeRole role) {
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
    return dockerSwarmService.joinSwarm(publicIpAddress, privateIpAddress, role);
  }

  public List<HostDetails> getAvailableHostsOnRegions(double expectedMemoryConsumption, List<String> regions) {
    return regions.stream()
        .map(regionsService::getRegion)
        .map(regionEntity -> new HostLocation(null, null, regionEntity.getName(), null))
        .map(location -> getAvailableHost(expectedMemoryConsumption, location))
        .collect(Collectors.toList());
  }

  //FIXME
  public HostDetails getAvailableHost(double expectedMemoryConsumption, HostLocation hostLocation) {
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
      return getHostDetails(sameCityHosts.get(random.nextInt(sameCityHosts.size())));
    } else if (!sameCountryHosts.isEmpty()) {
      return getHostDetails(sameCountryHosts.get(random.nextInt(sameCountryHosts.size())));
    } else if (!sameRegionHosts.isEmpty()) {
      return getHostDetails(sameRegionHosts.get(random.nextInt(sameRegionHosts.size())));
    } else if (!otherRegionsHosts.isEmpty() && !"us-east-1".equals(region)) {
      //TODO porquê excluir a região us-east-1?
      // TODO: review otherHostRegion and region us-east-1
      return getHostDetails(otherRegionsHosts.get(random.nextInt(otherRegionsHosts.size())));
    } else {
      log.info("Didn't find any available node");
      return getHostDetails(addHost(regionsService.getRegion(region), country, city, NodeRole.WORKER).getHostname());
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
      CloudHostEntity cloudHost = cloudHostsService.getCloudHostByPublicIpAddress(hostname);
      hostAddress = cloudHost.getAddress();
      hostLocation = cloudHost.getLocation();
    }
    return new HostDetails(hostAddress, hostLocation);
  }

  public SimpleNode addHost(Coordinates coordinates, NodeRole role) {
    return null;
    // TODO
  }

  @Deprecated //replace with addHost with coordinates
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

  public void removeHost(String hostname) {
    containersService.getHostContainers(hostname).forEach(c -> containersService.stopContainer(c.getContainerId()));
    dockerSwarmService.leaveSwarm(hostname);
  }

  private boolean isCloudHost(String hostname) {
    return cloudHostsService.hasCloudHostByHostname(hostname);
  }

  private boolean isEdgeHostRunning(EdgeHostEntity edgeHost) {
    return sshService.hasConnection(edgeHost.getHostname());
  }

  //FIXME
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
      throw new WorkerManagerException("%s", error);
    }
    return result;
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
    } catch (WorkerManagerException e) {
      throw new WorkerManagerException("Unable to find currently used external ports at %s: %s ", hostname,
          e.getMessage());
    }
  }

  public void startHostCloseTo(String hostname) {
    HostLocation hostLocation = getHostDetails(hostname).getHostLocation();
    // TODO porquê migrar logo um container?
    getRandomContainerToMigrate(hostname)
        .ifPresent(container -> {
          String serviceName = container.getLabels().get(ContainerConstants.Label.SERVICE_NAME);
          String containerId = container.getContainerId();
          ServiceEntity service = servicesService.getService(serviceName);
          double expectedMemoryConsumption = service.getExpectedMemoryConsumption();
          String toHostname = this.getAvailableHost(expectedMemoryConsumption, hostLocation).getHostAddress()
              .getPublicIpAddress();
          containersService.migrateContainer(containerId, toHostname);
          log.info("RuleDecision executed: Started host '{}' and migrated container '{}' to it", hostname, containerId);
        });
  }

  public void stopHost(String hostname) {
    double expectedMemoryConsumption = containersService.getHostContainers(hostname).stream()
        .map(containerEntity ->
            servicesService.getService(containerEntity.getLabels().get(ContainerConstants.Label.SERVICE_NAME)))
        .mapToDouble(ServiceEntity::getExpectedMemoryConsumption).sum();
    HostLocation hostLocation = this.getHostDetails(hostname).getHostLocation();
    String migrateToHostname =
        this.getAvailableHost(expectedMemoryConsumption, hostLocation).getHostAddress().getPublicIpAddress();
    List<ContainerEntity> containers = containersService.migrateAppContainers(hostname, migrateToHostname);
    //TODO garantir que o host é removido dinamicamente só depois de serem migrados todos os containers
    new Timer("RemoveHostFromSwarmTimer").schedule(new TimerTask() {
      @Override
      public void run() {
        removeHost(hostname);
      }
    }, containers.size() * DELAY_STOP_HOST);
    log.info("RuleDecision executed: Stopped host '{}' and migrated containers to host '{}'", hostname,
        migrateToHostname);
  }

  // TODO: Improve container choice
  private Optional<ContainerEntity> getRandomContainerToMigrate(String hostname) {
    return containersService.getAppContainers(hostname).stream().findFirst();
  }

}
