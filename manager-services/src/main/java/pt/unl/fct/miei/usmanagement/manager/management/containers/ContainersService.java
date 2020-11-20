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

package pt.unl.fct.miei.usmanagement.manager.management.containers;

import com.spotify.docker.client.messages.ContainerStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import pt.unl.fct.miei.usmanagement.manager.EnvironmentConstants;
import pt.unl.fct.miei.usmanagement.manager.config.ParallelismProperties;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.containers.Containers;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.configurations.ConfigurationsService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
public class ContainersService {

	private final DockerContainersService dockerContainersService;
	private final ContainerRulesService containerRulesService;
	private final ContainerSimulatedMetricsService containerSimulatedMetricsService;
	private final DockerApiProxyService dockerApiProxyService;
	private final WorkerManagersService workerManagersService;
	private final ServicesService servicesService;
	private final HostsService hostsService;
	private final ConfigurationsService configurationsService;
	private final Environment environment;

	private final Containers containers;

	private final int threads;

	public ContainersService(DockerContainersService dockerContainersService,
							 ContainerRulesService containerRulesService,
							 ContainerSimulatedMetricsService containerSimulatedMetricsService,
							 DockerApiProxyService dockerApiProxyService, WorkerManagersService workerManagersService,
							 ServicesService servicesService, HostsService hostsService, ConfigurationsService configurationsService, Environment environment, Containers containers,
							 ParallelismProperties parallelismProperties) {
		this.dockerContainersService = dockerContainersService;
		this.containerRulesService = containerRulesService;
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
		this.dockerApiProxyService = dockerApiProxyService;
		this.workerManagersService = workerManagersService;
		this.servicesService = servicesService;
		this.hostsService = hostsService;
		this.configurationsService = configurationsService;
		this.environment = environment;
		this.containers = containers;
		this.threads = parallelismProperties.getThreads();
	}

	public Container addContainerFromDockerContainer(DockerContainer dockerContainer) {
		try {
			return getContainer(dockerContainer.getId());
		}
		catch (EntityNotFoundException e) {
			Container container = Container.builder()
				.id(dockerContainer.getId())
				.type(dockerContainer.getType())
				.created(dockerContainer.getCreated())
				.names(dockerContainer.getNames())
				.image(dockerContainer.getImage())
				.command(dockerContainer.getCommand())
				.network(dockerContainer.getNetwork())
				.publicIpAddress(dockerContainer.getPublicIpAddress())
				.privateIpAddress(dockerContainer.getPrivateIpAddress())
				.mounts(dockerContainer.getMounts())
				.ports(dockerContainer.getPorts())
				.labels(dockerContainer.getLabels())
				.coordinates(dockerContainer.getCoordinates())
				.region(dockerContainer.getRegion())
				.managerId(environment.getProperty(EnvironmentConstants.EXTERNAL_ID))
				.build();
			return addContainer(container);
		}
	}

	public Optional<Container> addContainer(String containerId) {
		return dockerContainersService.getContainer(containerId).map(this::addContainerFromDockerContainer);
	}

	public Container addContainer(Container container) {
		checkContainerDoesntExist(container);
		log.info("Saving container {}", ToStringBuilder.reflectionToString(container));
		return containers.save(container);
	}

	public List<Container> addContainers(List<Container> containers) {
		log.info("Saving containers {}", containers);
		return this.containers.saveAll(containers);
	}

	public Container updateContainer(Container container) {
		log.info("Updating container {}", ToStringBuilder.reflectionToString(container));
		return containers.save(container);
	}

	public List<Container> getContainers() {
		return containers.findAll();
	}

	public List<Container> getContainers(WorkerManager workerManager) {
		return containers.findByManagerId(workerManager.getId());
	}

	public Container getContainer(String containerId) {
		return containers.findByIdStartingWith(containerId).orElseThrow(() ->
			new EntityNotFoundException(Container.class, "containerId", containerId));
	}

	public List<Container> getHostContainers(HostAddress hostAddress) {
		return containers.findByPublicIpAddressAndPrivateIpAddress(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress());
	}

	public List<Container> getHostContainersWithLabels(HostAddress hostAddress, Set<Pair<String, String>> labels) {
		List<Container> containers = getHostContainers(hostAddress);
		return filterContainersWithLabels(containers, labels);
	}

	public List<Container> getContainersWithLabels(Set<Pair<String, String>> labels) {
		List<Container> containers = getContainers();
		return filterContainersWithLabels(containers, labels);
	}

	private List<Container> filterContainersWithLabels(List<Container> containers,
													   Set<Pair<String, String>> labels) {
		// TODO try to build a database query instead
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

	public Container launchContainer(Coordinates coordinates, String serviceName, int externalPort, int internalPort) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(coordinates, serviceName, externalPort, internalPort);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, ContainerTypeEnum type) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, List<String> environment) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, environment);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, ContainerTypeEnum type, List<String> environment) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type, environment);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, ContainerTypeEnum type, Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type, labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, List<String> environment,
									 Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, environment,
			labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, List<String> environment,
									 Map<String, String> labels, Map<String, String> dynamicLaunchParams) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, environment,
			labels, dynamicLaunchParams);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, ContainerTypeEnum type,
									 List<String> environment, Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type,
			environment, labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, ContainerTypeEnum type,
									 List<String> environment, Map<String, String> labels,
									 Map<String, String> dynamicLaunchParams) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type,
			environment, labels, dynamicLaunchParams);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, int internalPort, int externalPort) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, externalPort, internalPort);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container launchContainer(HostAddress hostAddress, String serviceName, ContainerTypeEnum type, int internalPort,
									 int externalPort) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type,
			externalPort, internalPort);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to find launched container of service %s", serviceName));
	}

	public Container replicateContainer(String id, HostAddress toHostAddress) {
		Container containerEntity = getContainer(id);
		String managerId = containerEntity.getManagerId();
		if (managerId != null && !managerId.equalsIgnoreCase("manager-master")) {
			try {
				return workerManagersService.replicateContainer(managerId, id, toHostAddress).get();
			}
			catch (InterruptedException | ExecutionException e) {
				throw new ManagerException("Failed to replicate container %s at worker manager %s: %s", id, managerId, e.getMessage());
			}
		}
		else {
			Optional<DockerContainer> container = dockerContainersService.replicateContainer(containerEntity, toHostAddress);
			return container.map(this::addContainerFromDockerContainer)
				.orElseThrow(() -> new ManagerException("Unable to replicate container %s", id));
		}
	}

	public Container migrateContainer(String id, HostAddress hostAddress) {
		Container container = getContainer(id);
		String managerId = container.getManagerId();
		if (managerId != null && !managerId.equalsIgnoreCase("manager-master")) {
			try {
				return workerManagersService.migrateContainer(managerId, id, hostAddress).get();
			}
			catch (InterruptedException | ExecutionException e) {
				throw new ManagerException("Failed to migrate container %s at worker manager %s: %s", id, managerId, e.getMessage());
			}
		}
		else {
			Optional<DockerContainer> dockerContainer = dockerContainersService.migrateContainer(container, hostAddress);
			return dockerContainer.map(this::addContainerFromDockerContainer)
				.orElseThrow(() -> new ManagerException("Unable to migrate container %s", id));
		}
	}

	public Map<String, List<Container>> launchApp(List<Service> services, Coordinates coordinates) {
		return dockerContainersService.launchApp(services, coordinates).entrySet()
			.stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream().map(this::addContainerFromDockerContainer).collect(Collectors.toList())
			));
	}

	public void stopContainer(String id) {
		Container container = getContainer(id);
		try {
			dockerContainersService.stopContainer(container);
			deleteContainer(container);
		}
		catch (ManagerException e) {
			log.error("Failed to stop container {}: {}", id, e.getMessage());
		}
	}

	public void deleteContainer(Container container) {
		containers.delete(container);
	}

	public void deleteContainer(String id) {
		Container container = getContainer(id);
		if (container.getNames().stream().anyMatch(name -> name.contains(WorkerManagerProperties.WORKER_MANAGER))) {
			try {
				workerManagersService.deleteWorkerManagerByContainer(container);
			}
			catch (EntityNotFoundException e) {
				log.error("Failed to delete worker-manager associated with container {}", id);
			}
		}
		containers.delete(container);
	}

	public List<Container> getAppContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.BACKEND.name()))
		).stream().filter(container -> !configurationsService.isConfiguring(container.getId())).collect(Collectors.toList());
	}

	public List<Container> getAppContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.BACKEND.name())))
			.stream().filter(container -> !configurationsService.isConfiguring(container.getId())).collect(Collectors.toList());
	}

	public List<Container> getDatabaseContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.DATABASE.name())));
	}

	public List<Container> getDatabaseContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.DATABASE.name())));
	}

	public List<Container> getServiceContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.BACKEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.DATABASE.name()))
		).stream().filter(container -> !configurationsService.isConfiguring(container.getId())).collect(Collectors.toList());
	}


	public List<Container> getServiceContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.BACKEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.DATABASE.name()))
		).stream().filter(container -> !configurationsService.isConfiguring(container.getId())).collect(Collectors.toList());
	}

	public List<Container> getSystemContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.SYSTEM.name()))
		);
	}

	public List<Container> getSystemContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.SYSTEM.name())));
	}

	public Optional<ContainerStats> getContainerStats(String containerId, HostAddress hostAddress) {
		Container container = getContainer(containerId);
		return dockerContainersService.getContainerStats(container, hostAddress);
	}

	public String getLogs(String containerId) {
		Container container = getContainer(containerId);
		return dockerContainersService.getContainerLogs(container);
	}

	public List<ContainerRule> getRules(String containerId) {
		checkContainerExists(containerId);
		return containers.getRules(containerId);
	}

	public void addRule(String containerId, String ruleName) {
		checkContainerExists(containerId);
		containerRulesService.addContainer(ruleName, containerId);
	}

	public void addRules(String containerId, List<String> ruleNames) {
		checkContainerExists(containerId);
		ruleNames.forEach(rule -> containerRulesService.addContainer(rule, containerId));
	}

	public void removeRule(String containerId, String ruleName) {
		removeRules(containerId, List.of(ruleName));
	}

	public void removeRules(String containerId, List<String> ruleNames) {
		checkContainerExists(containerId);
		ruleNames.forEach(rule -> containerRulesService.removeContainer(rule, containerId));
	}

	public List<ContainerSimulatedMetric> getSimulatedMetrics(String containerId) {
		checkContainerExists(containerId);
		return containers.getSimulatedMetrics(containerId);
	}

	public ContainerSimulatedMetric getSimulatedMetric(String containerId, String simulatedMetricName) {
		checkContainerExists(containerId);
		return containers.getSimulatedMetric(containerId, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetric.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String containerId, String simulatedMetricName) {
		addSimulatedMetrics(containerId, List.of(simulatedMetricName));
	}

	public void addSimulatedMetrics(String containerId, List<String> simulatedMetricNames) {
		checkContainerExists(containerId);
		simulatedMetricNames.forEach(simulatedMetric ->
			containerSimulatedMetricsService.addContainer(simulatedMetric, containerId));
	}

	public void removeSimulatedMetric(String containerId, String simulatedMetricName) {
		removeSimulatedMetrics(containerId, List.of(simulatedMetricName));
	}

	public void removeSimulatedMetrics(String containerId, List<String> simulatedMetricNames) {
		checkContainerExists(containerId);
		simulatedMetricNames.forEach(simulatedMetric ->
			containerSimulatedMetricsService.removeContainer(simulatedMetric, containerId));
	}

	public String launchDockerApiProxy(HostAddress hostAddress, boolean insertIntoDatabase) {
		String containerId = dockerApiProxyService.launchDockerApiProxy(hostAddress);
		if (insertIntoDatabase) {
			addContainer(containerId);
		}
		return containerId;
	}

	public void stopContainers() {
		stopContainers(null);
	}

	public void stopContainers(Predicate<DockerContainer> containerPredicate) {
		List<DockerContainer> containers = dockerContainersService.stopAll(containerPredicate);
		containers.forEach(container -> deleteContainer(container.getId()));
	}

	public void stopDockerApiProxies() {
		List<Container> dockerApiProxies = getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, DockerApiProxyService.DOCKER_API_PROXY)));
		new ForkJoinPool(threads).execute(() ->
			dockerApiProxies.parallelStream().forEach(dockerApiProxy -> {
				dockerApiProxyService.stopDockerApiProxy(dockerApiProxy.getHostAddress());
				containers.delete(dockerApiProxy);
			}));
	}

	public void stopDockerApiProxy(HostAddress hostAddress) {
		getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, DockerApiProxyService.DOCKER_API_PROXY))
		).stream().findFirst().ifPresent(container -> {
			dockerApiProxyService.stopDockerApiProxy(hostAddress);
			containers.delete(container);
		});
	}

	public boolean hasContainer(String containerId) {
		return containers.hasContainer(containerId);
	}

	public boolean hasContainers(RegionEnum region) {
		return getContainersWithLabels(Set.of(Pair.of(ContainerConstants.Label.REGION, region.name()))).size() > 0;
	}

	public boolean hasContainers(RegionEnum region, String serviceName) {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.REGION, region.name()),
			Pair.of(ContainerConstants.Label.SERVICE_NAME, serviceName)
		)).size() > 0;
	}

	private void checkContainerExists(String containerId) {
		if (!hasContainer(containerId)) {
			throw new EntityNotFoundException(Container.class, "containerId", containerId);
		}
	}

	private void checkContainerDoesntExist(Container container) {
		String containerId = container.getId();
		if (containers.hasContainer(containerId)) {
			throw new DataIntegrityViolationException("Container " + containerId + " already exists");
		}
	}

	public List<Container> migrateHostContainers(HostAddress hostAddress) {
		Coordinates coordinates = hostAddress.getCoordinates();
		List<Container> containers = new LinkedList<>();
		getAppContainers(hostAddress).forEach(container -> {
			String containerId = container.getId();
			String serviceName = container.getServiceName();
			double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(serviceName);
			HostAddress toHostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
			Container c = migrateContainer(containerId, toHostAddress);
			containers.add(c);
		});
		return containers;
	}

	public void reset() {
		containers.deleteAll();
	}
}
