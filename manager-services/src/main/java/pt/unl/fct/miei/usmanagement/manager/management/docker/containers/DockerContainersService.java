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

package pt.unl.fct.miei.usmanagement.manager.management.docker.containers;

import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerMount;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.shaded.com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import pt.unl.fct.miei.usmanagement.manager.config.ParallelismProperties;
import pt.unl.fct.miei.usmanagement.manager.configurations.Configuration;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.configurations.ConfigurationsService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServiceDependenciesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.discovery.registration.RegistrationServerService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@org.springframework.stereotype.Service
public class DockerContainersService {

	private static final long DELAY_BETWEEN_CONTAINER_LAUNCH = TimeUnit.SECONDS.toMillis(5);

	private final static Pattern CONTAINER_ID_PATTERN = Pattern.compile("is already in use by container \\\\\"(.*)\\\\\"");

	private final ContainersService containersService;
	private final DockerCoreService dockerCoreService;
	private final NodesService nodesService;
	private final ServicesService servicesService;
	private final ServiceDependenciesService serviceDependenciesService;
	private final NginxLoadBalancerService nginxLoadBalancerService;
	private final RegistrationServerService registrationServerService;
	private final HostsService hostsService;

	private final int dockerDelayBeforeStopContainer;
	private final ConfigurationsService configurationsService;

	private final int threads;

	public DockerContainersService(@Lazy ContainersService containersService, DockerCoreService dockerCoreService,
								   NodesService nodesService, ServicesService servicesService,
								   ServiceDependenciesService serviceDependenciesService, NginxLoadBalancerService nginxLoadBalancerService,
								   RegistrationServerService registrationServerService, HostsService hostsService,
								   ContainerProperties containerProperties, ConfigurationsService configurationsService,
								   ParallelismProperties parallelismProperties) {
		this.containersService = containersService;
		this.dockerCoreService = dockerCoreService;
		this.nodesService = nodesService;
		this.servicesService = servicesService;
		this.serviceDependenciesService = serviceDependenciesService;
		this.nginxLoadBalancerService = nginxLoadBalancerService;
		this.registrationServerService = registrationServerService;
		this.hostsService = hostsService;
		this.dockerDelayBeforeStopContainer = containerProperties.getDelayBeforeStop();
		this.configurationsService = configurationsService;
		this.threads = parallelismProperties.getThreads();
	}

	public Map<String, List<DockerContainer>> launchApp(List<Service> services, Coordinates coordinates) {
		Map<String, List<DockerContainer>> serviceContainers = new HashMap<>();
		Map<String, String> dynamicLaunchParams = new HashMap<>();
		services.forEach(service -> {
			List<DockerContainer> containers = launchMicroservice(service, coordinates, dynamicLaunchParams);
			serviceContainers.put(service.getServiceName(), containers);
			containers.forEach(container -> {
				String hostname = container.getPublicIpAddress();
				Optional<ContainerPortMapping> portMapping = container.getPorts().stream().findFirst();
				if (portMapping.isEmpty()) {
					throw new ManagerException("Unable to launch app, port of service {} is unknown", service.getServiceName());
				}
				int publicPort = portMapping.get().getPublicPort();
				String address = String.format("%s:%d", hostname, publicPort);
				dynamicLaunchParams.put(service.getOutputLabel(), address);
			});
			//TODO rever tempo de espera, é preciso? Timing.sleep(DELAY_BETWEEN_CONTAINER_LAUNCH, TimeUnit.MILLISECONDS);
		});
		return serviceContainers;
	}

	private List<DockerContainer> launchMicroservice(Service service, Coordinates coordinates,
													 Map<String, String> dynamicLaunchParams) {
		List<String> environment = Collections.emptyList();
		Map<String, String> labels = Collections.emptyMap();
		Double memoryConsumption = service.getExpectedMemoryConsumption();
		double expectedMemoryConsumption = memoryConsumption == null ? 0 : memoryConsumption;
		int minimumReplicas = servicesService.getMinimumReplicasByServiceName(service.getServiceName());
		List<DockerContainer> containers = new ArrayList<>(minimumReplicas);
		for (int i = 0; i < minimumReplicas; i++) {
			HostAddress address = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
			Optional<DockerContainer> container = launchContainer(address, service, ContainerTypeEnum.BY_REQUEST, environment,
				labels, dynamicLaunchParams);
			container.ifPresent(containers::add);
		}
		return containers;
	}

	public Optional<DockerContainer> launchContainer(Coordinates coordinates, String serviceName, int externalPort, int internalPort) {
		double expectedMemoryUsage = servicesService.getExpectedMemoryConsumption(serviceName);
		HostAddress hostAddress = hostsService.getClosestCapableHost(expectedMemoryUsage, coordinates);
		return launchContainer(hostAddress, serviceName, externalPort, internalPort);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, ContainerTypeEnum containerType) {
		List<String> environment = Collections.emptyList();
		return launchContainer(address, serviceName, containerType, environment);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, List<String> environment) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST, environment);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName,
													 ContainerTypeEnum containerType, List<String> environment) {
		Map<String, String> labels = Collections.emptyMap();
		return launchContainer(address, serviceName, containerType, environment, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, Map<String, String> labels) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName,
													 ContainerTypeEnum containerType, Map<String, String> labels) {
		List<String> environment = Collections.emptyList();
		return launchContainer(address, serviceName, containerType, environment, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, List<String> environment,
													 Map<String, String> labels) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST, environment, labels);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, List<String> environment,
													 Map<String, String> labels,
													 Map<String, String> dynamicLaunchParams) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST, environment, labels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName,
													 ContainerTypeEnum containerType, List<String> environment,
													 Map<String, String> labels) {
		Map<String, String> dynamicLaunchParams = Collections.emptyMap();
		return launchContainer(address, serviceName, containerType, environment, labels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, ContainerTypeEnum containerType,
													 List<String> environment, Map<String, String> labels,
													 Map<String, String> dynamicLaunchParams) {
		Service service = servicesService.getService(serviceName);
		return launchContainer(address, service, containerType, environment, labels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, int externalPort, int internalPort) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST, externalPort, internalPort);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, int internalPort,
													 int externalPort, List<String> environment) {
		return launchContainer(address, serviceName, ContainerTypeEnum.BY_REQUEST, externalPort, internalPort, environment);
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, ContainerTypeEnum containerType,
													 int externalPort, int internalPort) {
		return launchContainer(address, serviceName, containerType, externalPort, internalPort, Collections.emptyList());
	}

	public Optional<DockerContainer> launchContainer(HostAddress address, String serviceName, ContainerTypeEnum containerType,
													 int externalPort, int internalPort, List<String> environment) {
		Service service = servicesService.getService(serviceName).toBuilder()
			.defaultExternalPort(externalPort)
			.defaultInternalPort(internalPort)
			.build();
		Map<String, String> labels = Collections.emptyMap();
		Map<String, String> dynamicLaunchParams = Collections.emptyMap();
		return launchContainer(address, service, containerType, environment, labels, dynamicLaunchParams);
	}

	private Optional<DockerContainer> launchContainer(HostAddress hostAddress, Service service,
													  ContainerTypeEnum containerType, List<String> environment,
													  Map<String, String> labels, Map<String, String> dynamicLaunchParams) {
		final int retries = 3;
		String errorMessage = null;
		Configuration config = null;
		try {
			for (var i = 0; i < retries; i++) {
				if (!hostAddress.isComplete()) {
					hostAddress = hostsService.completeHostAddress(hostAddress);
				}
				String serviceName = service.getServiceName();
				log.info("Launching container on mode {} with service {} at {}", containerType, serviceName, hostAddress);

				if (containerType == ContainerTypeEnum.SINGLETON) {
					List<DockerContainer> containers = List.of();
					try {
						containers = getContainers(
							DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_NAME, serviceName),
							DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_PUBLIC_IP_ADDRESS, hostAddress.getPublicIpAddress()),
							DockerClient.ListContainersParam.withLabel(ContainerConstants.Label.SERVICE_PRIVATE_IP_ADDRESS, hostAddress.getPrivateIpAddress()));
					}
					catch (ManagerException ignored) {
					}
					if (containers.size() > 0) {
						DockerContainer container = containers.get(0);
						log.info("Service {} is already running on container {} on host {}", serviceName, container.getId(), hostAddress);
						return Optional.of(container);
					}
				}

				String serviceType = service.getServiceType().name();
				int externalPort = hostsService.findAvailableExternalPort(hostAddress, service.getDefaultExternalPort());
				int internalPort = service.getDefaultInternalPort();
				String containerName = containerType == ContainerTypeEnum.SINGLETON
					? serviceName
					: String.format("%s_%s_%s_%s", serviceName, hostAddress.getPublicIpAddress(), hostAddress.getPrivateIpAddress(), externalPort);
				String serviceAddress = String.format("%s:%s", hostAddress.getPublicIpAddress(), externalPort);
				String dockerRepository = service.getDockerRepository();
				String launchCommand = service.getLaunchCommand();
				if (launchCommand == null) {
					launchCommand = "";
				}
				launchCommand = launchCommand
					.replace("${hostname}", hostAddress.getPublicIpAddress())
					.replace("${externalPort}", String.valueOf(externalPort))
					.replace("${internalPort}", String.valueOf(internalPort));
				if (!launchCommand.isEmpty()) {
					log.info("Launch command: {}", launchCommand);
				}

				RegionEnum region = hostAddress.getRegion();
				if (serviceDependenciesService.hasDependencies(serviceName)) {
					String outputLabel = servicesService.getService(RegistrationServerService.REGISTRATION_SERVER).getOutputLabel();
					String registrationAddress = registrationServerService
						.getRegistrationServerAddress(region)
						.orElseGet(() -> registrationServerService.launchRegistrationServer(region).getContainer().getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS));
					launchCommand = launchCommand.replace(outputLabel, registrationAddress);
				}

				for (Service databaseService : servicesService.getDependenciesByType(serviceName, ServiceTypeEnum.DATABASE)) {
					String databaseServiceName = databaseService.getServiceName();
					String databaseHost = getDatabaseHostForService(hostAddress, databaseServiceName);
					String outputLabel = databaseService.getOutputLabel();
					launchCommand = launchCommand.replace(outputLabel, databaseHost);
				}
				for (Map.Entry<String, String> param : dynamicLaunchParams.entrySet()) {
					launchCommand = launchCommand.replace(param.getKey(), param.getValue());
				}

				Optional<Service> optionalMemcached = servicesService.getDependencies(serviceName).stream()
					.filter(s -> s.getServiceName().contains("memcached"))
					.collect(Collectors.toList()).stream().findFirst();
				if (optionalMemcached.isPresent()) {
					Service memcached = optionalMemcached.get();
					String memcachedServiceName = memcached.getServiceName();
					String memcachedHost = getMemcachedHostForService(hostAddress, memcachedServiceName);
					String outputLabel = memcached.getOutputLabel();
					launchCommand = launchCommand.replace(outputLabel, memcachedHost);
				}

				for (Map.Entry<String, String> param : dynamicLaunchParams.entrySet()) {
					launchCommand = launchCommand.replace(param.getKey(), param.getValue());
				}

				List<String> containerEnvironment = new LinkedList<>();
				containerEnvironment.add(ContainerConstants.Environment.SERVICE_REGION + "=" + region);
				containerEnvironment.addAll(service.getEnvironment());
				containerEnvironment.addAll(environment);

				Map<String, String> containerLabels = new HashMap<>();
				containerLabels.put(ContainerConstants.Label.US_MANAGER, String.valueOf(true));
				containerLabels.put(ContainerConstants.Label.CONTAINER_TYPE, containerType.name());
				containerLabels.put(ContainerConstants.Label.SERVICE_NAME, serviceName);
				containerLabels.put(ContainerConstants.Label.SERVICE_TYPE, serviceType);
				containerLabels.put(ContainerConstants.Label.SERVICE_ADDRESS, serviceAddress);
				containerLabels.put(ContainerConstants.Label.SERVICE_PUBLIC_IP_ADDRESS, hostAddress.getPublicIpAddress());
				containerLabels.put(ContainerConstants.Label.SERVICE_PRIVATE_IP_ADDRESS, hostAddress.getPrivateIpAddress());
				containerLabels.put(ContainerConstants.Label.COORDINATES, new Gson().toJson(hostAddress.getCoordinates()));
				containerLabels.put(ContainerConstants.Label.REGION, region.name());
				if (containerType == ContainerTypeEnum.SINGLETON && hostAddress.equals(hostsService.getManagerHostAddress())) {
					containerLabels.put(ContainerConstants.Label.MASTER_MANAGER, String.valueOf(true));
				}
				containerLabels.putAll(labels);

				Set<String> volumes = service.getVolumes();

				log.info("host = {}, internalPort = {}, externalPort = {}, containerName = {}, "
						+ "dockerRepository = {}, launchCommand = {}, envs = {}, labels = {}, volumes = {}",
					hostAddress, internalPort, externalPort, containerName, dockerRepository, launchCommand, containerEnvironment,
					containerLabels, volumes);

				HostConfig.Bind[] binds = volumes.stream()
					.map(v -> HostConfig.Bind.from(v.split(":")[0]).to(v.split(":")[1]).readOnly(false).build())
					.toArray(HostConfig.Bind[]::new);
				HostConfig hostConfig = HostConfig.builder()
					.autoRemove(true)
					.portBindings(Map.of(String.valueOf(internalPort), List.of(PortBinding.of("", String.valueOf(externalPort)))))
					.appendBinds(binds)
					.build();
				ContainerConfig.Builder containerBuilder = ContainerConfig.builder()
					.image(dockerRepository)
					.exposedPorts(String.valueOf(internalPort))
					.hostConfig(hostConfig)
					.hostname(serviceName)
					.env(containerEnvironment)
					.labels(containerLabels);
				ContainerConfig containerConfig = launchCommand.isEmpty()
					? containerBuilder.build()
					: containerBuilder.cmd(launchCommand.split(" ")).build();

				try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
					dockerClient.pull(dockerRepository);
					ContainerCreation containerCreation = dockerClient.createContainer(containerConfig, containerName);
					String containerId = containerCreation.id();
					config = configurationsService.addConfiguration(containerId);
					if (containerType != ContainerTypeEnum.SINGLETON) {
						dockerClient.connectToNetwork(containerId, DockerSwarmService.NETWORK_OVERLAY);
					}
					dockerClient.startContainer(containerId);
					if (ServiceTypeEnum.getServiceType(serviceType) == ServiceTypeEnum.FRONTEND) {
						nginxLoadBalancerService.addServer(serviceName, serviceAddress, hostAddress.getCoordinates(), hostAddress.getRegion());
					}
					return getContainer(containerId);
				}
				catch (DockerException | InterruptedException e) {
					errorMessage = e.getMessage();
					if (errorMessage.toLowerCase().contains("image not found")) {
						throw new EntityNotFoundException("image", "name", serviceName);
					}
					log.error("Failed to start container: {}. Retrying... ({}/{})", errorMessage, i + 1, retries);
				}
				Timing.sleep(i + 1, TimeUnit.SECONDS);
			}
		}
		finally {
			if (config != null) {
				configurationsService.removeConfiguration(config);
			}
		}
		if (errorMessage.contains("is already in use by container")) {
			log.info(errorMessage);
			Matcher containerIdRegexExpression = CONTAINER_ID_PATTERN.matcher(errorMessage);
			boolean found = containerIdRegexExpression.find();
			if (found) {
				log.info(containerIdRegexExpression.group(1));
				return findContainer(containerIdRegexExpression.group(1));
			}
		}
		throw new ManagerException("Failed to start container: %s", errorMessage);
	}

	private String getDatabaseHostForService(HostAddress hostAddress, String databaseServiceName) {
		Container databaseContainer = containersService.getHostContainersWithLabels(hostAddress,
			Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, databaseServiceName)))
			.stream().findFirst().orElseGet(() -> containersService.launchContainer(hostAddress, databaseServiceName));
		if (databaseContainer == null) {
			throw new ManagerException("Failed to launch database %s on host %s", databaseServiceName, hostAddress);
		}
		String address = databaseContainer.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS);
		log.info("Using database {} on host {}", address, hostAddress);
		return address;
	}

	private String getMemcachedHostForService(HostAddress hostAddress, String memcachedService) {
		Container memcached = containersService.getHostContainersWithLabels(hostAddress,
			Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, memcachedService)))
			.stream().findFirst().orElseGet(() -> containersService.launchContainer(hostAddress, memcachedService));
		if (memcached == null) {
			throw new ManagerException("Failed to launch memcached %s on host %s", memcachedService, hostAddress);
		}
		String address = memcached.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS);
		log.info("Using memcached {} on host {}", address, hostAddress);
		return address;
	}

	public void stopContainer(Container container) {
		String containerId = container.getId();
		HostAddress hostAddress = container.getHostAddress();
		stopContainer(containerId, hostAddress);
	}

	public void stopContainer(String id, HostAddress hostAddress) {
		ContainerInfo containerInfo = inspectContainer(id, hostAddress);
		String serviceName = containerInfo.config().labels().get(ContainerConstants.Label.SERVICE_NAME);
		ServiceTypeEnum serviceType = ServiceTypeEnum.getServiceType(containerInfo.config().labels().get(ContainerConstants.Label.SERVICE_TYPE));
		if (serviceType == ServiceTypeEnum.FRONTEND) {
			String serviceAddress = containerInfo.config().labels().get(ContainerConstants.Label.SERVICE_ADDRESS);
			RegionEnum region = RegionEnum.getRegion(containerInfo.config().labels().get(ContainerConstants.Label.REGION));
			nginxLoadBalancerService.removeServer(serviceName, serviceAddress, region);
		}
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			dockerClient.stopContainer(id, 0);
			log.info("Stopped container {} ({}) on host {}", serviceName, id, hostAddress.toString());
		}
		catch (DockerException | InterruptedException e) {
			log.error("Failed to stop container {}: {}", id, e.getMessage());
		}
	}

	public Optional<DockerContainer> replicateContainer(Container container, HostAddress toHostAddress) {
		return replicateContainer(container.getId(), container.getHostAddress(), toHostAddress);
	}

	public Optional<DockerContainer> replicateContainer(String id, HostAddress fromHostAddress, HostAddress toHostAddress) {
		if (!toHostAddress.isComplete()) {
			toHostAddress = hostsService.completeHostAddress(toHostAddress);
		}
		ContainerInfo fromContainer = inspectContainer(id, fromHostAddress);
		String serviceName = fromContainer.name().replace("/", "").split("_")[0];
		Map.Entry<String, List<PortBinding>> port = fromContainer.hostConfig().portBindings().entrySet().iterator().next();
		int externalPort = Integer.parseInt(port.getValue().get(0).hostPort());
		int internalPort = Integer.parseInt(port.getKey());
		Service service = servicesService.getService(serviceName).toBuilder()
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
		return launchContainer(toHostAddress, service, ContainerTypeEnum.BY_REQUEST, customEnvs, customLabels, dynamicLaunchParams);
	}

	public Optional<DockerContainer> migrateContainer(Container container, HostAddress toHostAddress) {
		if (!toHostAddress.isComplete()) {
			toHostAddress = hostsService.completeHostAddress(toHostAddress);
		}
		Optional<DockerContainer> replicaContainer = replicateContainer(container, toHostAddress);
		new Timer("stop-container-timer").schedule(new TimerTask() {
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
		List<DockerClient.ListContainersParam> filtersList = filter == null
			? new ArrayList<>(1)
			: new ArrayList<>(Arrays.asList(filter));
		filtersList.add(DockerClient.ListContainersFilterParam.withLabel(ContainerConstants.Label.US_MANAGER, String.valueOf(true)));
		filter = filtersList.toArray(new DockerClient.ListContainersParam[0]);
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			return dockerClient.listContainers(filter).stream().map(this::buildDockerContainer).collect(Collectors.toList());
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Failed to get containers running at %s: %s", hostAddress.toSimpleString(), e.getMessage());
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

	private ContainerInfo inspectContainer(String containerId, HostAddress hostAddress) {
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			return dockerClient.inspectContainer(containerId);
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to get inspect container %s: %s", containerId, e.getMessage());
		}
	}

	public Optional<ContainerStats> getContainerStats(Container container, HostAddress hostAddress) {
		try (DockerClient dockerClient = dockerCoreService.getDockerClient(hostAddress)) {
			return Optional.of(dockerClient.stats(container.getId()));
		}
		catch (DockerException | InterruptedException e) {
			log.error("Unable to get stats of container {}: {}", container.getId(), e.getMessage());
		}
		return Optional.empty();
	}

	private DockerContainer buildDockerContainer(com.spotify.docker.client.messages.Container container) {
		Gson gson = new Gson();
		String id = container.id();
		long created = container.created();
		Optional<String> optionalName = container.names().stream().findFirst();
		String name = "";
		if (optionalName.isPresent()) {
			name = optionalName.get();
			if (name.startsWith("/")) {
				name = name.substring(1);
			}
		}
		String image = container.image();
		String command = container.command();
		AttachedNetwork attachedNetwork = container.networkSettings().networks().get(DockerSwarmService.NETWORK_OVERLAY);
		String network = attachedNetwork == null
			? null
			: String.format("%s=%s", DockerSwarmService.NETWORK_OVERLAY, attachedNetwork.networkId().substring(0, 10));
		String state = container.state();
		String status = container.status();
		ContainerTypeEnum type = ContainerTypeEnum.getContainerType(container.labels().get(ContainerConstants.Label.CONTAINER_TYPE));
		String publicIpAddress = container.labels().get(ContainerConstants.Label.SERVICE_PUBLIC_IP_ADDRESS);
		String privateIpAddress = container.labels().get(ContainerConstants.Label.SERVICE_PRIVATE_IP_ADDRESS);
		ImmutableList<ContainerMount> containerMounts = container.mounts();
		Set<String> mounts = new HashSet<>();
		containerMounts.forEach(mount -> mounts.add(mount.source() + ":" + mount.destination()));
		Coordinates coordinates = gson.fromJson(container.labels().get(ContainerConstants.Label.COORDINATES), Coordinates.class);
		RegionEnum region = RegionEnum.getRegion(container.labels().get(ContainerConstants.Label.REGION));
		Set<ContainerPortMapping> ports = container.ports().stream()
			.map(p -> new ContainerPortMapping(p.privatePort(), p.publicPort(), p.type(), p.ip()))
			.collect(Collectors.toSet());
		Map<String, String> labels = container.labels();
		return new DockerContainer(id, type, created, name, image, command, network, state, status, publicIpAddress, privateIpAddress,
			mounts, coordinates, region, ports, labels);
	}

	public String getContainerLogs(Container container) {
		HostAddress hostAddress = container.getHostAddress();
		String containerId = container.getId();
		String logs = null;
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress);
			 LogStream stream = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
			logs = stream.readFully();
			// remove ANSI escape codes
			logs = logs.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
		}
		catch (DockerException | InterruptedException e) {
			log.error("Failed to get logs of container {}: {}", containerId, e.getMessage());
		}
		return logs;
	}

	public List<DockerContainer> stopAll(Predicate<DockerContainer> containersPredicate) {
		List<DockerContainer> containers = getContainers();
		if (containersPredicate != null) {
			containers.removeIf(Predicate.not(containersPredicate));
		}
		new ForkJoinPool(threads).execute(() ->
			containers.parallelStream().forEach(container -> stopContainer(container.getId(), container.getHostAddress()))
		);
		return containers;
	}

	public List<DockerContainer> getHostContainers(HostAddress hostAddress) {
		return getContainers().stream()
			.filter(container -> container.getHostAddress().equals(hostAddress))
			.collect(Collectors.toList());
	}

	public List<DockerContainer> getHostContainersWithLabels(HostAddress hostAddress, Set<Pair<String, String>> labels) {
		List<DockerContainer> containers = getHostContainers(hostAddress);
		return filterContainersWithLabels(containers, labels);
	}

	public List<DockerContainer> getDatabaseContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.DATABASE.name())));
	}

	public List<DockerContainer> getDatabaseContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.DATABASE.name())));
	}

	public List<DockerContainer> getSystemContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.SYSTEM.name()))
		);
	}

	public List<DockerContainer> getAppContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.BACKEND.name()))
		);
	}

	public List<DockerContainer> getContainersWithLabels(Set<Pair<String, String>> labels) {
		List<DockerContainer> containers = getContainers();
		return filterContainersWithLabels(containers, labels);
	}

	private List<DockerContainer> filterContainersWithLabels(List<DockerContainer> containers,
															 Set<Pair<String, String>> labels) {
		List<String> labelKeys = labels.stream().map(Pair::getFirst).collect(Collectors.toList());
		return containers.stream()
			.filter(container -> {
				for (Map.Entry<String, String> containerLabel : container.getLabels().entrySet()) {
					String key = containerLabel.getKey();
					String value = containerLabel.getValue();
					if (labelKeys.contains(key) && !labels.contains(Pair.of(key, value))) {
						return false;
					}
				}
				return true;
			})
			.collect(Collectors.toList());
	}

}
