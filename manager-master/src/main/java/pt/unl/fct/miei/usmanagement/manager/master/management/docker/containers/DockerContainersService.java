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

package pt.unl.fct.miei.usmanagement.manager.master.management.docker.containers;

import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.MachineLocation;
import pt.unl.fct.miei.usmanagement.manager.master.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.master.management.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.management.services.ServiceType;
import pt.unl.fct.miei.usmanagement.manager.master.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.master.management.services.discovery.eureka.EurekaService;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DockerContainersService {

  private static final long DELAY_BETWEEN_CONTAINER_LAUNCH = TimeUnit.SECONDS.toMillis(5);

  private final ContainersService containersService;
  private final DockerCoreService dockerCoreService;
  private final NodesService nodesService;
  private final ServicesService servicesService;
  private final NginxLoadBalancerService nginxLoadBalancerService;
  private final EurekaService eurekaService;
  private final HostsService hostsService;

  private final String dockerHubUsername;
  private final int dockerDelayBeforeStopContainer;

  public DockerContainersService(@Lazy ContainersService containersService, DockerCoreService dockerCoreService,
                                 NodesService nodesService,
                                 ServicesService servicesService, NginxLoadBalancerService nginxLoadBalancerService,
                                 EurekaService eurekaService, HostsService hostsService,
                                 DockerProperties dockerProperties, ContainerProperties containerProperties) {
    this.containersService = containersService;
    this.dockerCoreService = dockerCoreService;
    this.nodesService = nodesService;
    this.servicesService = servicesService;
    this.nginxLoadBalancerService = nginxLoadBalancerService;
    this.eurekaService = eurekaService;
    this.hostsService = hostsService;
    this.dockerHubUsername = dockerProperties.getHub().getUsername();
    this.dockerDelayBeforeStopContainer = containerProperties.getDelayBeforeStop();
  }

  public Map<String, List<DockerContainer>> launchApp(List<ServiceEntity> services,
                                                      String region, String country, String city) {
    var serviceContainers = new HashMap<String, List<DockerContainer>>();
    var dynamicLaunchParams = new HashMap<String, String>();
    services.forEach(service -> {
      List<DockerContainer> containers = launchMicroservice(service, region, country, city, dynamicLaunchParams);
      serviceContainers.put(service.getServiceName(), containers);
      containers.forEach(container -> {
        String hostname = container.getHostname();
        int privatePort = container.getPorts().get(0).getPrivatePort(); //TODO privado ou publico?
        String address = String.format("%s:%d", hostname, privatePort);
        dynamicLaunchParams.put(service.getOutputLabel(), address);
      });
      //TODO rever tempo de espera, é preciso? Timing.sleep(DELAY_BETWEEN_CONTAINER_LAUNCH, TimeUnit.MILLISECONDS);
    });
    return serviceContainers;
  }

  private List<DockerContainer> launchMicroservice(ServiceEntity service, String region, String country,
                                                   String city, Map<String, String> dynamicLaunchParams) {
    List<String> environment = Collections.emptyList();
    Map<String, String> labels = Collections.emptyMap();
    double expectedMemoryConsumption = service.getExpectedMemoryConsumption();
    int minReplicas = servicesService.getMinReplicasByServiceName(service.getServiceName());
    var containers = new ArrayList<DockerContainer>(minReplicas);
    for (int i = 0; i < minReplicas; i++) {
      String hostname = hostsService.getAvailableHost(expectedMemoryConsumption, region, country, city);
      Optional<DockerContainer> container = launchContainer(hostname, service, false, environment, labels,
          dynamicLaunchParams);
      container.ifPresent(containers::add);
    }
    return containers;
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName) {
    return launchContainer(hostname, serviceName, false);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, boolean global) {
    List<String> environment = Collections.emptyList();
    return launchContainer(hostname, serviceName, global, environment);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, List<String> environment) {
    return launchContainer(hostname, serviceName, false, environment);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName,
                                                   boolean global, List<String> environment) {
    Map<String, String> labels = Collections.emptyMap();
    return launchContainer(hostname, serviceName, global, environment, labels);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, Map<String, String> labels) {
    return launchContainer(hostname, serviceName, false, labels);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName,
                                                   boolean global, Map<String, String> labels) {
    List<String> environment = Collections.emptyList();
    return launchContainer(hostname, serviceName, global, environment, labels);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, List<String> environment,
                                                   Map<String, String> labels) {
    return launchContainer(hostname, serviceName, false, environment, labels);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, List<String> environment,
                                                   Map<String, String> labels,
                                                   Map<String, String> dynamicLaunchParams) {
    return launchContainer(hostname, serviceName, false, environment, labels, dynamicLaunchParams);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName,
                                                   boolean global, List<String> environment,
                                                   Map<String, String> labels) {
    Map<String, String> dynamicLaunchParams = Collections.emptyMap();
    return launchContainer(hostname, serviceName, global, environment, labels, dynamicLaunchParams);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, boolean global,
                                                   List<String> environment, Map<String, String> labels,
                                                   Map<String, String> dynamicLaunchParams) {
    ServiceEntity service = servicesService.getService(serviceName);
    return launchContainer(hostname, service, global, environment, labels, dynamicLaunchParams);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, String internalPort,
                                                   String externalPort) {
    return launchContainer(hostname, serviceName, false, internalPort, externalPort);
  }

  public Optional<DockerContainer> launchContainer(String hostname, String serviceName, boolean global,
                                                   String internalPort, String externalPort) {
    ServiceEntity service = servicesService.getService(serviceName).toBuilder()
        .defaultInternalPort(internalPort)
        .defaultExternalPort(externalPort)
        .build();
    List<String> environment = Collections.emptyList();
    Map<String, String> labels = Collections.emptyMap();
    Map<String, String> dynamicLaunchParams = Collections.emptyMap();
    return launchContainer(hostname, service, global, environment, labels, dynamicLaunchParams);
  }

  private Optional<DockerContainer> launchContainer(String hostname, ServiceEntity service,
                                                    boolean global, List<String> environment,
                                                    Map<String, String> labels,
                                                    Map<String, String> dynamicLaunchParams) {
    String serviceName = service.getServiceName();
    log.info("Launching container with service '{}' at '{}'...", serviceName, hostname);
    if (global) {
      List<DockerContainer> containers = List.of();
      try {
        containers = getContainers(
            DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_NAME, serviceName),
            DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_HOSTNAME, hostname)
        );
      } catch (MasterManagerException e) {
        log.error(e.getMessage());
      }
      if (containers.size() > 0) {
        DockerContainer container = containers.get(0);
        log.info("Service {} is already running on container {}", serviceName, container.getId());
        return Optional.of(container);
      }
    }
    String serviceType = service.getServiceType().name();
    String internalPort = service.getDefaultInternalPort();
    String externalPort = hostsService.findAvailableExternalPort(hostname, service.getDefaultExternalPort());
    String serviceAddr = String.format("%s:%s", hostname, externalPort);
    String containerName = String.format("%s_%s_%s", serviceName, hostname, externalPort);
    String dockerRepository = String.format("%s/%s", dockerHubUsername, service.getDockerRepository());
    MachineLocation machineLocation = hostsService.getHostDetails(hostname).getMachineLocation();
    String continent = machineLocation.getContinent();
    String region = machineLocation.getRegion();
    String country = machineLocation.getCountry();
    String city = machineLocation.getCity();
    /*String country = hostDetails instanceof EdgeHostDetails ? ((EdgeHostDetails)hostDetails).getCountry() : "";
    String city = hostDetails instanceof EdgeHostDetails ? ((EdgeHostDetails)hostDetails).getCity() : "";*/
    String launchCommand = service.getLaunchCommand();
    launchCommand = launchCommand
        .replace("${hostname}", hostname)
        .replace("${externalPort}", externalPort)
        .replace("${internalPort}", internalPort);
    log.info("{}", launchCommand);
    if (servicesService.serviceDependsOn(serviceName, EurekaService.EUREKA_SERVER)) {
      String outputLabel = servicesService.getService(EurekaService.EUREKA_SERVER).getOutputLabel();
      Optional<String> eurekaAddress = eurekaService.getEurekaServerAddress(region);
      if (eurekaAddress.isPresent()) {
        launchCommand = launchCommand.replace(outputLabel, eurekaAddress.get());
      } else {
        //TODO apagar depois de ver se não houver erro
        log.error("eureka address at region '{}' not found", region);
      }
    }
    for (ServiceEntity databaseService : servicesService.getDependenciesByType(serviceName, ServiceType.DATABASE)) {
      String databaseServiceName = databaseService.getServiceName();
      String databaseHost = getDatabaseHostForService(hostname, databaseServiceName);
      String outputLabel = databaseService.getOutputLabel();
      launchCommand = launchCommand.replace(outputLabel, databaseHost);
    }
    for (Map.Entry<String, String> param : dynamicLaunchParams.entrySet()) {
      launchCommand = launchCommand.replace(param.getKey(), param.getValue());
    }
    var containerEnvironment = new LinkedList<>(List.of(
        ContainerConstants.Environment.SERVICE_CONTINENT + "=" + continent,
        ContainerConstants.Environment.SERVICE_REGION + "=" + region,
        ContainerConstants.Environment.SERVICE_COUNTRY + "=" + country,
        ContainerConstants.Environment.SERVICE_CITY + "=" + city));
    containerEnvironment.addAll(environment);
    var containerLabels = new HashMap<>(Map.of(
        ContainerConstants.Label.SERVICE_NAME, serviceName,
        ContainerConstants.Label.SERVICE_TYPE, serviceType,
        ContainerConstants.Label.SERVICE_ADDRESS, serviceAddr,
        ContainerConstants.Label.SERVICE_HOSTNAME, hostname,
        ContainerConstants.Label.SERVICE_CONTINENT, continent,
        ContainerConstants.Label.SERVICE_REGION, region,
        ContainerConstants.Label.SERVICE_COUNTRY, country,
        ContainerConstants.Label.SERVICE_CITY, city));
    containerLabels.putAll(labels);
    if (global) {
      containerLabels.put(ContainerConstants.Label.IS_STOPPABLE, String.valueOf(false));
      containerLabels.put(ContainerConstants.Label.IS_REPLICABLE, String.valueOf(false));
    }
    log.info("hostname = '{}', internalPort = '{}', externalPort = '{}', containerName = '{}', "
            + "dockerRepository = '{}', launchCommand = '{}', envs = '{}', labels = '{}'",
        hostname, internalPort, externalPort, containerName, dockerRepository, launchCommand, containerEnvironment,
        containerLabels);
    HostConfig hostConfig = HostConfig.builder()
        .autoRemove(true)
        .portBindings(Map.of(internalPort, List.of(PortBinding.of("", externalPort))))
        .build();
    ContainerConfig.Builder containerBuilder = ContainerConfig.builder()
        .image(dockerRepository)
        .exposedPorts(internalPort)
        .hostConfig(hostConfig)
        .env(containerEnvironment)
        .labels(containerLabels);
    ContainerConfig containerConfig = launchCommand.isEmpty()
        ? containerBuilder.build()
        : containerBuilder.cmd(launchCommand.split(" ")).build();
    try (var dockerClient = dockerCoreService.getDockerClient(hostname)) {
      dockerClient.pull(dockerRepository);
      ContainerCreation containerCreation = dockerClient.createContainer(containerConfig, containerName);
      String containerId = containerCreation.id();
      dockerClient.startContainer(containerId);
      if (Objects.equals(serviceType, ServiceType.FRONTEND.name())) {
        nginxLoadBalancerService.addToLoadBalancer(hostname, serviceName, serviceAddr, continent, region, country,
            city);
      }
      return Objects.equals(containerLabels.get(ContainerConstants.Label.IS_TRACEABLE), "false")
          ? Optional.empty()
          : getContainer(containerId);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new MasterManagerException(e.getMessage());
    }
  }

  private String getDatabaseHostForService(String hostname, String databaseServiceName) {
    ContainerEntity databaseContainer = containersService.getHostContainersWithLabels(hostname,
        Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, databaseServiceName)))
        .stream().findFirst().orElseGet(() -> containersService.launchContainer(hostname, databaseServiceName));
    if (databaseContainer == null) {
      throw new MasterManagerException("Failed to launch database '{}' on host '{}'", databaseServiceName, hostname);
    }
    String address = databaseContainer.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS);
    log.info("Found database '{}' on host '{}'", address, hostname);
    return address;
  }

  public void stopContainer(ContainerEntity container) {
    String containerId = container.getContainerId();
    String containerHostname = container.getHostname();
    stopContainer(containerId, containerHostname);
  }

  public void stopContainer(String id, String hostname) {
    ContainerInfo containerInfo = inspectContainer(id, hostname);
    String serviceType = containerInfo.config().labels().get(ContainerConstants.Label.SERVICE_TYPE);
    if (Objects.equals(serviceType, "frontend")) {
      nginxLoadBalancerService.removeFromLoadBalancer(hostname, id);
    }
    try (var dockerClient = dockerCoreService.getDockerClient(hostname)) {
      //TODO espera duas vezes no caso de migração!?!?
      dockerClient.stopContainer(id, dockerDelayBeforeStopContainer);
      log.info("Stopped container '{}' on host '{}'", id, hostname);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new MasterManagerException(e.getMessage());
    }
  }

  public Optional<DockerContainer> replicateContainer(ContainerEntity container, String toHostname) {
    return replicateContainer(container, toHostname);
  }

  public Optional<DockerContainer> replicateContainer(String id, String fromHostname, String toHostname) {
    ContainerInfo fromContainer = inspectContainer(id, fromHostname);
    String serviceName = fromContainer.name().replace("/", "").split("_")[0];
    Map.Entry<String, List<PortBinding>> port = fromContainer.hostConfig().portBindings().entrySet().iterator().next();
    String internalPort = port.getKey();
    String externalPort = port.getValue().get(0).hostPort();
    ServiceEntity service = servicesService.getService(serviceName).toBuilder()
        .defaultInternalPort(internalPort)
        .defaultExternalPort(externalPort)
        .build();
    List<String> customEnvs = Collections.emptyList();
    Map<String, String> customLabels = Collections.emptyMap();
    Map<String, String> dynamicLaunchParams;
    if (!service.hasLaunchCommand()) {
      dynamicLaunchParams = Collections.emptyMap();
    } else {
      List<String> args = fromContainer.args();
      var params = Arrays.asList(service.getLaunchCommand().split(" "));
      assert args.size() == params.size();
      // Merge the 2 lists into a map
      dynamicLaunchParams = IntStream
          .range(0, params.size())
          .boxed()
          .collect(Collectors.toMap(params::get, args::get));
    }
    return launchContainer(toHostname, service, false, customEnvs, customLabels, dynamicLaunchParams);
  }

  public Optional<DockerContainer> migrateContainer(ContainerEntity container, String hostname, String toHostname) {
    String containerId = container.getContainerId();
    Optional<DockerContainer> replicaContainer = replicateContainer(container, toHostname);
    new Timer("StopContainerTimer").schedule(new TimerTask() {
      @Override
      public void run() {
        if (hostname != null) {
          stopContainer(containerId, hostname);
        } else {
          stopContainer(container);
        }
      }
    }, dockerDelayBeforeStopContainer);
    return replicaContainer;
  }

  public Optional<DockerContainer> migrateContainer(ContainerEntity container, String toHostname) {
    return migrateContainer(container, null, toHostname);
  }

  public List<DockerContainer> getContainers(DockerClient.ListContainersParam... filter) {
    return getAllContainers(filter);
  }

  public List<DockerContainer> getContainers(String hostname, DockerClient.ListContainersParam... filter) {
    try (var dockerClient = dockerCoreService.getDockerClient(hostname)) {
      return dockerClient.listContainers(filter).stream().map(this::buildDockerContainer).collect(Collectors.toList());
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new MasterManagerException(e.getMessage());
    }
  }

  public Optional<DockerContainer> findContainer(String hostname, DockerClient.ListContainersParam... filter) {
    return getContainers(hostname, filter).stream().findFirst();
  }

  private Optional<DockerContainer> findContainer(String id) {
    DockerClient.ListContainersParam idFilter = DockerClient.ListContainersParam.filter("id", id);
    return getContainers(idFilter).stream().findFirst();
  }

  private List<DockerContainer> getAllContainers(DockerClient.ListContainersParam... filter) {
    return nodesService.getReadyNodes().stream()
        .map(node -> getContainers(node.getHostname(), filter))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  public Optional<DockerContainer> getContainer(String id) {
    return findContainer(id);
  }

  public List<DockerContainer> getAppContainers() {
    return getContainers(
        DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "frontend"),
        DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "backend"));
  }

  public List<DockerContainer> getAppContainers(String hostname) {
    return getContainers(hostname,
        DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "frontend"),
        DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "backend"));
  }

  public List<DockerContainer> getDatabaseContainers(String hostname) {
    return getContainers(hostname,
        DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "database"));
  }

  public List<DockerContainer> getSystemContainers(String hostname) {
    return getContainers(hostname,
        DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "system"));
  }

  private ContainerInfo inspectContainer(String containerId, String hostname) {
    try (var dockerClient = dockerCoreService.getDockerClient(hostname)) {
      return dockerClient.inspectContainer(containerId);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new MasterManagerException(e.getMessage());
    }
  }

  public ContainerStats getContainerStats(ContainerEntity container, String hostname) {
    try (var dockerClient = dockerCoreService.getDockerClient(hostname)) {
      return dockerClient.stats(container.getContainerId());
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new MasterManagerException(e.getMessage());
    }
  }

  private DockerContainer buildDockerContainer(Container container) {
    String id = container.id();
    long created = container.created();
    List<String> names = container.names();
    String image = container.image();
    String command = container.command();
    String state = container.state();
    String status = container.status();
    String hostname = container.labels().get(ContainerConstants.Label.SERVICE_HOSTNAME);
    List<ContainerPortMapping> ports = container.ports().stream()
        .map(p -> new ContainerPortMapping(p.privatePort(), p.publicPort(), p.type(), p.ip()))
        .collect(Collectors.toList());
    Map<String, String> labels = container.labels();
    return new DockerContainer(id, created, names, image, command, state, status, hostname, ports, labels);
  }

  public String getContainerLogs(ContainerEntity container) {
    String hostname = container.getHostname();
    String containerId = container.getContainerId();
    String logs = null;
    try (var docker = dockerCoreService.getDockerClient(hostname);
         var stream = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
      logs = stream.readFully();
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
    }
    return logs;
  }

}
