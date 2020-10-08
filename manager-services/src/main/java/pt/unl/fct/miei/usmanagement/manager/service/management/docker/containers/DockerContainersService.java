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

package pt.unl.fct.miei.usmanagement.manager.service.management.docker.containers;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
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
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceType;
import pt.unl.fct.miei.usmanagement.manager.service.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.service.management.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.service.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.service.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.service.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.services.discovery.eureka.EurekaService;

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

	private final int dockerDelayBeforeStopContainer;

	public DockerContainersService(@Lazy ContainersService containersService, DockerCoreService dockerCoreService,
								   NodesService nodesService,
								   ServicesService servicesService, NginxLoadBalancerService nginxLoadBalancerService,
								   EurekaService eurekaService, HostsService hostsService,
								   ContainerProperties containerProperties) {
		this.containersService = containersService;
		this.dockerCoreService = dockerCoreService;
		this.nodesService = nodesService;
		this.servicesService = servicesService;
		this.nginxLoadBalancerService = nginxLoadBalancerService;
		this.eurekaService = eurekaService;
		this.hostsService = hostsService;
		this.dockerDelayBeforeStopContainer = containerProperties.getDelayBeforeStop();
	}

	public Map<String, List<DockerContainer>> launchApp(List<ServiceEntity> services, Coordinates coordinates) {
		Map<String, List<DockerContainer>> serviceContainers = new HashMap<>();
		Map<String, String> dynamicLaunchParams = new HashMap<>();
		services.forEach(service -> {
			List<DockerContainer> containers = launchMicroservice(service, coordinates, dynamicLaunchParams);
			serviceContainers.put(service.getServiceName(), containers);
			containers.forEach(container -> {
				String hostname = container.getPublicIpAddress();
				int privatePort = container.getPorts().get(0).getPrivatePort(); //TODO privado ou publico?
				String address = String.format("%s:%d", hostname, privatePort);
				dynamicLaunchParams.put(service.getOutputLabel(), address);
			});
			//TODO rever tempo de espera, é preciso? Timing.sleep(DELAY_BETWEEN_CONTAINER_LAUNCH, TimeUnit.MILLISECONDS);
		});
		return serviceContainers;
	}

	private List<DockerContainer> launchMicroservice(ServiceEntity service, Coordinates coordinates,
													 Map<String, String> dynamicLaunchParams) {
		List<String> environment = Collections.emptyList();
		Map<String, String> labels = Collections.emptyMap();
		double expectedMemoryConsumption = service.getExpectedMemoryConsumption();
		int minReplicas = servicesService.getMinReplicasByServiceName(service.getServiceName());
		List<DockerContainer> containers = new ArrayList<>(minReplicas);
		for (int i = 0; i < minReplicas; i++) {
			HostAddress address = hostsService.getAvailableHost(expectedMemoryConsumption, coordinates);
			Optional<DockerContainer> container = launchContainer(address, service, false, environment, labels,
				dynamicLaunchParams);
			container.ifPresent(containers::add);
		}
		return containers;
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName) {
		return launchContainer(address, serviceName, false);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, boolean global) {
		List<String> environment = Collections.emptyList();
		return launchContainer(address, serviceName, global, environment);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, List<String> environment) {
		return launchContainer(address, serviceName, false, environment);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName,
													 boolean global, List<String> environment) {
		Map<String, String> labels = Collections.emptyMap();
		return launchContainer(address, serviceName, global, environment, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, Map<String, String> labels) {
		return launchContainer(address, serviceName, false, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName,
													 boolean global, Map<String, String> labels) {
		List<String> environment = Collections.emptyList();
		return launchContainer(address, serviceName, global, environment, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, List<String> environment,
													 Map<String, String> labels) {
		return launchContainer(address, serviceName, false, environment, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, List<String> environment,
													 Map<String, String> labels,
													 Map<String, String> dynamicLaunchParams) {
		return launchContainer(address, serviceName, false, environment, labels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName,
													 boolean global, List<String> environment,
													 Map<String, String> labels) {
		Map<String, String> dynamicLaunchParams = Collections.emptyMap();
		return launchContainer(address, serviceName, global, environment, labels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, boolean global,
													 List<String> environment, Map<String, String> labels,
													 Map<String, String> dynamicLaunchParams) {
		ServiceEntity service = servicesService.getService(serviceName);
		return launchContainer(address, service, global, environment, labels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, String internalPort,
													 String externalPort) {
		return launchContainer(address, serviceName, false, internalPort, externalPort);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, String internalPort,
													 String externalPort, List<String> environment) {
		return launchContainer(address, serviceName, false, internalPort, externalPort, environment);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, boolean global,
													 String internalPort, String externalPort) {
		return launchContainer(address, serviceName, global, internalPort, externalPort, Collections.emptyList());
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, boolean global,
													 String internalPort, String externalPort, List<String> environment) {
		ServiceEntity service = servicesService.getService(serviceName).toBuilder()
			.defaultInternalPort(internalPort)
			.defaultExternalPort(externalPort)
			.build();
		Map<String, String> labels = Collections.emptyMap();
		Map<String, String> dynamicLaunchParams = Collections.emptyMap();
		return launchContainer(address, service, global, environment, labels, dynamicLaunchParams);
	}

	private Optional<DockerContainer> launchContainer(HostAddress hostAddress, ServiceEntity service,
													  boolean global, List<String> environment,
													  Map<String, String> labels,
													  Map<String, String> dynamicLaunchParams) {
		String serviceName = service.getServiceName();
		log.info("Launching container with service {} at {}...", serviceName, hostAddress);
		if (global) {
			List<DockerContainer> containers = List.of();
			try {
				containers = getContainers(
					DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_NAME, serviceName),
					DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_PUBLIC_IP_ADDRESS, hostAddress.getPublicIpAddress()),
					DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_PRIVATE_IP_ADDRESS, hostAddress.getPrivateIpAddress()));
			}
			catch (ManagerException e) {
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
		String externalPort = hostsService.findAvailableExternalPort(hostAddress, service.getDefaultExternalPort());
		String serviceAddr = String.format("%s:%s", hostAddress.getPublicIpAddress(), externalPort);
		String dockerRepository = service.getDockerRepository();
		HostLocation hostLocation = hostsService.getHostLocation(hostAddress);
		String continent = hostLocation.getContinent();
		String region = hostLocation.getRegion();
		String country = hostLocation.getCountry();
		String city = hostLocation.getCity();
		String launchCommand = service.getLaunchCommand();
		launchCommand = launchCommand
			.replace("${hostname}", hostAddress.getPublicIpAddress())
			.replace("${externalPort}", externalPort)
			.replace("${internalPort}", internalPort);
		log.info("{}", launchCommand);
		if (servicesService.serviceDependsOn(serviceName, EurekaService.EUREKA_SERVER)) {
			String outputLabel = servicesService.getService(EurekaService.EUREKA_SERVER).getOutputLabel();
			String eurekaAddress = eurekaService
				.getEurekaServerAddress(region)
				.orElse(eurekaService.launchEurekaServer(region).getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS));
			launchCommand = launchCommand.replace(outputLabel, eurekaAddress);
		}
		for (ServiceEntity databaseService : servicesService.getDependenciesByType(serviceName, ServiceType.DATABASE)) {
			String databaseServiceName = databaseService.getServiceName();
			String databaseHost = getDatabaseHostForService(hostAddress, databaseServiceName);
			String outputLabel = databaseService.getOutputLabel();
			launchCommand = launchCommand.replace(outputLabel, databaseHost);
		}
		for (Map.Entry<String, String> param : dynamicLaunchParams.entrySet()) {
			launchCommand = launchCommand.replace(param.getKey(), param.getValue());
		}
		List<String> containerEnvironment = new LinkedList<>(List.of(
			ContainerConstants.Environment.SERVICE_CONTINENT + "=" + continent,
			ContainerConstants.Environment.SERVICE_REGION + "=" + region,
			ContainerConstants.Environment.SERVICE_COUNTRY + "=" + country,
			ContainerConstants.Environment.SERVICE_CITY + "=" + city));
		containerEnvironment.addAll(environment);
		Map<String, String> containerLabels = new HashMap<>(Map.of(
			ContainerConstants.Label.SERVICE_NAME, serviceName,
			ContainerConstants.Label.SERVICE_TYPE, serviceType,
			ContainerConstants.Label.SERVICE_ADDRESS, serviceAddr,
			ContainerConstants.Label.SERVICE_PUBLIC_IP_ADDRESS, hostAddress.getPublicIpAddress(),
			ContainerConstants.Label.SERVICE_PRIVATE_IP_ADDRESS, hostAddress.getPrivateIpAddress(),
			ContainerConstants.Label.SERVICE_CONTINENT, continent,
			ContainerConstants.Label.SERVICE_REGION, region,
			ContainerConstants.Label.SERVICE_COUNTRY, country,
			ContainerConstants.Label.SERVICE_CITY, city));
		containerLabels.putAll(labels);
		if (global) {
			containerLabels.put(ContainerConstants.Label.IS_STOPPABLE, String.valueOf(false));
			containerLabels.put(ContainerConstants.Label.IS_REPLICABLE, String.valueOf(false));
		}
		log.info("host = {}, internalPort = {}, externalPort = {}, containerName = {}, "
				+ "dockerRepository = {}, launchCommand = {}, envs = {}, labels = {}",
			hostAddress, internalPort, externalPort, serviceName, dockerRepository, launchCommand, containerEnvironment,
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
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			dockerClient.pull(dockerRepository);
			ContainerCreation containerCreation = dockerClient.createContainer(containerConfig, serviceName);
			String containerId = containerCreation.id();
			dockerClient.startContainer(containerId);
			if (Objects.equals(serviceType, ServiceType.FRONTEND.name())) {
				nginxLoadBalancerService.addServiceToLoadBalancer(hostAddress, serviceName, serviceAddr, continent, region, country, city);
			}
			return Objects.equals(containerLabels.get(ContainerConstants.Label.IS_TRACEABLE), "false")
				? Optional.empty()
				: getContainer(containerId);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	private String getDatabaseHostForService(HostAddress hostAddress, String databaseServiceName) {
		ContainerEntity databaseContainer = containersService.getHostContainersWithLabels(hostAddress,
			Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, databaseServiceName)))
			.stream().findFirst().orElseGet(() -> containersService.launchContainer(hostAddress, databaseServiceName));
		if (databaseContainer == null) {
			throw new ManagerException("Failed to launch database {} on host {}", databaseServiceName, hostAddress);
		}
		String address = databaseContainer.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS);
		log.info("Found database {} on host {}", address, hostAddress);
		return address;
	}

	public void stopContainer(ContainerEntity container) {
		String containerId = container.getContainerId();
		HostAddress hostAddress = container.getHostAddress();
		stopContainer(containerId, hostAddress);
	}

	public void stopContainer(String id, HostAddress hostAddress) {
		this.stopContainer(id, hostAddress, null);
	}

	public void stopContainer(String id, HostAddress hostAddress, Integer delay) {
		ContainerInfo containerInfo = inspectContainer(id, hostAddress);
		String serviceType = containerInfo.config().labels().get(ContainerConstants.Label.SERVICE_TYPE);
		if (Objects.equals(serviceType, "frontend")) {
			nginxLoadBalancerService.removeContainerFromLoadBalancer(id);
		}
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			//TODO espera duas vezes no caso de migração!?!?
			String serviceName = containerInfo.config().labels().get(ContainerConstants.Label.SERVICE_NAME);
			int delayBeforeStop = delay == null ? dockerDelayBeforeStopContainer : delay;
			dockerClient.stopContainer(id, delayBeforeStop);
			log.info("Stopped container {} ({}) on host {}", serviceName, id, hostAddress);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public Optional<DockerContainer> replicateContainer(ContainerEntity container, HostAddress toHostAddress) {
		return replicateContainer(container.getContainerId(), container.getHostAddress(), toHostAddress);
	}

	public Optional<DockerContainer> replicateContainer(String id, HostAddress fromHostAddress, HostAddress toHostAddress) {
		ContainerInfo fromContainer = inspectContainer(id, fromHostAddress);
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
		}
		else {
			List<String> args = fromContainer.args();
			List<String> params = Arrays.asList(service.getLaunchCommand().split(" "));
			assert args.size() == params.size();
			// Merge the 2 lists into a map
			dynamicLaunchParams = IntStream
				.range(0, params.size())
				.boxed()
				.collect(Collectors.toMap(params::get, args::get));
		}
		return launchContainer(toHostAddress, service, false, customEnvs, customLabels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> migrateContainer(ContainerEntity container, HostAddress toHostAddress) {
		String containerId = container.getContainerId();
		Optional<DockerContainer> replicaContainer = replicateContainer(container, toHostAddress);
		new Timer("StopContainerTimer").schedule(new TimerTask() {
			@Override
			public void run() {
				stopContainer(container);
			}
		}, dockerDelayBeforeStopContainer);
		return replicaContainer;
	}

	public List<DockerContainer> getContainers(DockerClient.ListContainersParam... filter) {
		return getAllContainers(filter);
	}

	public List<DockerContainer> getContainers(HostAddress hostAddress, DockerClient.ListContainersParam... filter) {
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			return dockerClient.listContainers(filter).stream().map(this::buildDockerContainer).collect(Collectors.toList());
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public Optional<DockerContainer> findContainer(HostAddress hostAddress, DockerClient.ListContainersParam... filter) {
		return getContainers(hostAddress, filter).stream().findFirst();
	}

	private Optional<DockerContainer> findContainer(String id) {
		DockerClient.ListContainersParam idFilter = DockerClient.ListContainersParam.filter("id", id);
		return getContainers(idFilter).stream().findFirst();
	}

	private List<DockerContainer> getAllContainers(DockerClient.ListContainersParam... filter) {
		return nodesService.getReadyNodes().stream()
			.map(node -> getContainers(node.getHostAddress(), filter))
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

	public List<DockerContainer> getAppContainers(HostAddress hostAddress) {
		return getContainers(hostAddress,
			DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "frontend"),
			DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "backend"));
	}

	public List<DockerContainer> getDatabaseContainers(HostAddress hostAddress) {
		return getContainers(hostAddress,
			DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "database"));
	}

	public List<DockerContainer> getSystemContainers(HostAddress hostAddress) {
		return getContainers(hostAddress,
			DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_TYPE, "system"));
	}

	private ContainerInfo inspectContainer(String containerId, HostAddress hostAddress) {
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			return dockerClient.inspectContainer(containerId);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public ContainerStats getContainerStats(ContainerEntity container, HostAddress hostAddress) {
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			return dockerClient.stats(container.getContainerId());
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
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
		String publicIpAddress = container.labels().get(ContainerConstants.Label.SERVICE_PUBLIC_IP_ADDRESS);
		String privateIpAddress = container.labels().get(ContainerConstants.Label.SERVICE_PRIVATE_IP_ADDRESS);
		List<ContainerPortMapping> ports = container.ports().stream()
			.map(p -> new ContainerPortMapping(p.privatePort(), p.publicPort(), p.type(), p.ip()))
			.collect(Collectors.toList());
		Map<String, String> labels = container.labels();
		return new DockerContainer(id, created, names, image, command, state, status, publicIpAddress, privateIpAddress,
			ports, labels);
	}

	public String getContainerLogs(ContainerEntity container) {
		HostAddress hostAddress = container.getHostAddress();
		String containerId = container.getContainerId();
		String logs = null;
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress);
			 LogStream stream = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
			logs = stream.readFully();
			// remove ANSI escape codes
			logs = logs.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
		}
		return logs;
	}

	public void stopAll() {
		this.stopAllExcept(List.of(DockerApiProxyService.DOCKER_API_PROXY));
	}

	public void stopAllExcept(List<String> services) {
		List<DockerContainer> containers = getContainers();
		containers.removeIf(dockerContainer -> {
			String serviceName = dockerContainer.getLabels().getOrDefault(ContainerConstants.Label.SERVICE_NAME, "");
			return services.contains(serviceName);
		});
		containers.forEach(container -> stopContainer(container.getId(), container.getHostAddress(), 0));
	}

}
