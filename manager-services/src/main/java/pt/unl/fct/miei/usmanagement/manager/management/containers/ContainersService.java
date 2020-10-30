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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerRepository;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContainersService {

	private final static int CONTAINERS_DATABASE_SYNC_INTERVAL = 15000;

	private final DockerContainersService dockerContainersService;
	private final ContainerRulesService containerRulesService;
	private final ContainerSimulatedMetricsService containerSimulatedMetricsService;
	private final DockerApiProxyService dockerApiProxyService;
	private final WorkerManagersService workerManagersService;

	private final ContainerRepository containers;

	private Timer syncDatabaseContainersTimer;

	public ContainersService(DockerContainersService dockerContainersService,
							 ContainerRulesService containerRulesService,
							 ContainerSimulatedMetricsService containerSimulatedMetricsService,
							 DockerApiProxyService dockerApiProxyService, WorkerManagersService workerManagersService,
							 ContainerRepository containers) {
		this.dockerContainersService = dockerContainersService;
		this.containerRulesService = containerRulesService;
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
		this.dockerApiProxyService = dockerApiProxyService;
		this.containers = containers;
		this.workerManagersService = workerManagersService;
	}

	public ContainerEntity addContainerFromDockerContainer(DockerContainer dockerContainer) {
		try {
			return getContainer(dockerContainer.getId());
		}
		catch (EntityNotFoundException e) {
			ContainerEntity container = ContainerEntity.builder()
				.containerId(dockerContainer.getId())
				.created(dockerContainer.getCreated())
				.names(dockerContainer.getNames())
				.image(dockerContainer.getImage())
				.command(dockerContainer.getCommand())
				.publicIpAddress(dockerContainer.getPublicIpAddress())
				.privateIpAddress(dockerContainer.getPrivateIpAddress())
				.ports(dockerContainer.getPorts())
				.labels(dockerContainer.getLabels())
				.coordinates(dockerContainer.getCoordinates())
				.region(dockerContainer.getRegion())
				.build();
			return addContainer(container);
		}
	}

	public Optional<ContainerEntity> addContainer(String containerId) {
		return dockerContainersService.getContainer(containerId).map(this::addContainerFromDockerContainer);
	}

	public ContainerEntity addContainer(ContainerEntity container) {
		checkContainerDoesntExist(container);
		log.info("Saving container {}", ToStringBuilder.reflectionToString(container));
		return containers.save(container);
	}

	public List<ContainerEntity> getContainers() {
		return containers.findAll();
	}

	public ContainerEntity getContainer(String containerId) {
		return containers.findByContainerIdStartingWith(containerId).orElseThrow(() ->
			new EntityNotFoundException(ContainerEntity.class, "containerId", containerId));
	}

	public List<ContainerEntity> getHostContainers(HostAddress hostAddress) {
		return containers.findByPublicIpAddressAndPrivateIpAddress(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress());
	}

	public List<ContainerEntity> getHostContainersWithLabels(HostAddress hostAddress, Set<Pair<String, String>> labels) {
		List<ContainerEntity> containers = getHostContainers(hostAddress);
		return filterContainersWithLabels(containers, labels);
	}

	public List<ContainerEntity> getContainersWithLabels(Set<Pair<String, String>> labels) {
		List<ContainerEntity> containers = getContainers();
		return filterContainersWithLabels(containers, labels);
	}

	private List<ContainerEntity> filterContainersWithLabels(List<ContainerEntity> containers,
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

	public List<ContainerEntity> synchronizeDatabaseContainers() {
		List<ContainerEntity> containers = getContainers();
		if (dockerContainersService.isLaunchingContainer()) {
			return containers;
		}
		List<DockerContainer> dockerContainers = dockerContainersService.getContainers();
		List<String> dockerContainerIds = dockerContainers
			.stream().map(DockerContainer::getId).collect(Collectors.toList());
		Iterator<ContainerEntity> containerIterator = containers.iterator();
		// Remove invalid container entities
		while (containerIterator.hasNext()) {
			ContainerEntity container = containerIterator.next();
			String containerId = container.getContainerId();
			if (!dockerContainerIds.contains(containerId)) {
				deleteContainer(containerId);
				containerIterator.remove();
				log.info("Removed invalid container {}", containerId);
			}
		}
		// Add missing container entities
		dockerContainers.forEach(dockerContainer -> {
			String containerId = dockerContainer.getId();
			if (!hasContainer(containerId)) {
				ContainerEntity containerEntity = addContainerFromDockerContainer(dockerContainer);
				containers.add(containerEntity);
				log.info("Added missing container {}", containerId);
			}
		});
		return containers;
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, ContainerType type) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, List<String> environment) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, environment);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, ContainerType type, List<String> environment) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type, environment);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, ContainerType type, Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type, labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, List<String> environment,
										   Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, environment,
			labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, List<String> environment,
										   Map<String, String> labels, Map<String, String> dynamicLaunchParams) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, environment,
			labels, dynamicLaunchParams);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, ContainerType type,
										   List<String> environment, Map<String, String> labels) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type,
			environment, labels);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, ContainerType type,
										   List<String> environment, Map<String, String> labels,
										   Map<String, String> dynamicLaunchParams) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type,
			environment, labels, dynamicLaunchParams);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, String internalPort,
										   String externalPort) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, internalPort,
			externalPort);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity launchContainer(HostAddress hostAddress, String serviceName, ContainerType type, String internalPort,
										   String externalPort) {
		Optional<DockerContainer> container = dockerContainersService.launchContainer(hostAddress, serviceName, type,
			internalPort, externalPort);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Container of service %s crashed on startup", serviceName));
	}

	public ContainerEntity replicateContainer(String id, HostAddress toHostAddress) {
		ContainerEntity containerEntity = getContainer(id);
		Optional<DockerContainer> container = dockerContainersService.replicateContainer(containerEntity, toHostAddress);
		return container.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to replicate container %s", id));
	}

	public ContainerEntity migrateContainer(String id, HostAddress hostAddress) {
		ContainerEntity container = getContainer(id);
		Optional<DockerContainer> dockerContainer = dockerContainersService.migrateContainer(container, hostAddress);
		return dockerContainer.map(this::addContainerFromDockerContainer)
			.orElseThrow(() -> new ManagerException("Unable to migrate container %s", id));
	}

	public Map<String, List<ContainerEntity>> launchApp(List<ServiceEntity> services, Coordinates coordinates) {
		return dockerContainersService.launchApp(services, coordinates).entrySet()
			.stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream().map(this::addContainerFromDockerContainer).collect(Collectors.toList())
			));
	}

	public void stopContainer(String id) {
		ContainerEntity container = getContainer(id);
		dockerContainersService.stopContainer(container);
		deleteContainer(id);
	}

	public void deleteContainer(String id) {
		ContainerEntity container = getContainer(id);
		if (container.getNames().stream().anyMatch(name -> name.contains(WorkerManagerProperties.WORKER_MANAGER))) {
			workerManagersService.deleteWorkerManagerByContainer(container);
		}
		containers.delete(container);
	}

	public List<ContainerEntity> getAppContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.BACKEND.name()))
		);
	}

	public List<ContainerEntity> getAppContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.FRONTEND.name()),
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.BACKEND.name())));
	}

	public List<ContainerEntity> getDatabaseContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.DATABASE.name())));
	}

	public List<ContainerEntity> getDatabaseContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.DATABASE.name())));
	}

	public List<ContainerEntity> getSystemContainers() {
		return getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.SYSTEM.name()))
		);
	}

	public List<ContainerEntity> getSystemContainers(HostAddress hostAddress) {
		return getHostContainersWithLabels(hostAddress, Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_TYPE, ServiceType.SYSTEM.name())));
	}

	public ContainerStats getContainerStats(String containerId, HostAddress hostAddress) {
		ContainerEntity container = getContainer(containerId);
		return dockerContainersService.getContainerStats(container, hostAddress);
	}

	public String getLogs(String containerId) {
		ContainerEntity container = getContainer(containerId);
		String logs = dockerContainersService.getContainerLogs(container);
		if (logs != null) {
			String path = String.format("./logs/containers/%s%s.log", container.getPublicIpAddress(), container.getNames().get(0));
			Path logsPath = Paths.get(path);
			try {
				Files.createDirectories(logsPath.getParent());
				Files.write(logsPath, logs.getBytes());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logs;
	}

	public List<ContainerRuleEntity> getRules(String containerId) {
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
		checkContainerExists(containerId);
		containerRulesService.removeContainer(ruleName, containerId);
	}

	public void removeRules(String containerId, List<String> ruleNames) {
		checkContainerExists(containerId);
		ruleNames.forEach(rule -> containerRulesService.removeContainer(rule, containerId));
	}

	public List<ContainerSimulatedMetricEntity> getSimulatedMetrics(String containerId) {
		checkContainerExists(containerId);
		return containers.getSimulatedMetrics(containerId);
	}

	public ContainerSimulatedMetricEntity getSimulatedMetric(String containerId, String simulatedMetricName) {
		checkContainerExists(containerId);
		return containers.getSimulatedMetric(containerId, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String containerId, String simulatedMetricName) {
		checkContainerExists(containerId);
		containerSimulatedMetricsService.addContainer(simulatedMetricName, containerId);
	}

	public void addSimulatedMetrics(String containerId, List<String> simulatedMetricNames) {
		checkContainerExists(containerId);
		simulatedMetricNames.forEach(simulatedMetric ->
			containerSimulatedMetricsService.addContainer(simulatedMetric, containerId));
	}

	public void removeSimulatedMetric(String containerId, String simulatedMetricName) {
		checkContainerExists(containerId);
		containerSimulatedMetricsService.removeContainer(simulatedMetricName, containerId);
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
		List<DockerContainer> containers = dockerContainersService.stopAll();
		containers.forEach(container -> deleteContainer(container.getId()));
	}

	public boolean hasContainer(String containerId) {
		return containers.hasContainer(containerId);
	}

	private void checkContainerExists(String containerId) {
		if (!hasContainer(containerId)) {
			throw new EntityNotFoundException(ContainerEntity.class, "containerId", containerId);
		}
	}

	private void checkContainerDoesntExist(ContainerEntity container) {
		String containerId = container.getContainerId();
		if (containers.hasContainer(containerId)) {
			throw new DataIntegrityViolationException("Container '" + containerId + "' already exists");
		}
	}

	public void initSyncDatabaseContainersTimer() {
		syncDatabaseContainersTimer = new Timer("SyncDatabaseContainersTimer", true);
		syncDatabaseContainersTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronizeDatabaseContainers();
				}
				catch (ManagerException e) {
					log.error(e.getMessage());
				}
			}
		}, CONTAINERS_DATABASE_SYNC_INTERVAL, CONTAINERS_DATABASE_SYNC_INTERVAL);
	}

	public void stopSyncDatabaseContainersTimer() {
		if (syncDatabaseContainersTimer != null) {
			syncDatabaseContainersTimer.cancel();
			log.info("Stopped containers database synchronization");
		}
	}

}
