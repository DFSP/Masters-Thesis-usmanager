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

package pt.unl.fct.miei.usmanagement.manager.management.hosts;

import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.config.ParallelismProperties;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.MethodNotAllowedException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusProperties;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	private final int maxWorkers;
	private final int maxInstances;
	private HostAddress managerHostAddress;
	private final int threads;

	public HostsService(@Lazy NodesService nodesService, @Lazy ContainersService containersService,
						DockerSwarmService dockerSwarmService, EdgeHostsService edgeHostsService,
						CloudHostsService cloudHostsService, SshService sshService, BashService bashService,
						HostMetricsService hostMetricsService, DockerProperties dockerProperties,
						AwsProperties awsProperties, ParallelismProperties parallelismProperties) {
		this.nodesService = nodesService;
		this.containersService = containersService;
		this.dockerSwarmService = dockerSwarmService;
		this.edgeHostsService = edgeHostsService;
		this.cloudHostsService = cloudHostsService;
		this.sshService = sshService;
		this.bashService = bashService;
		this.hostMetricsService = hostMetricsService;
		this.maxWorkers = dockerProperties.getSwarm().getInitialMaxWorkers();
		this.maxInstances = awsProperties.getInitialMaxInstances();
		this.threads = parallelismProperties.getThreads();
	}

	public HostAddress setManagerHostAddress() {
		String username = bashService.getUsername();
		String publicIp = bashService.getPublicIp();
		String privateIp = bashService.getPrivateIp();
		this.managerHostAddress = completeHostAddress(new HostAddress(username, publicIp, privateIp));
		log.info("Setting manager host address: {}", managerHostAddress.toString());
		return managerHostAddress;
	}

	public HostAddress setManagerHostAddress(HostAddress hostAddress) {
		managerHostAddress = hostAddress;
		return managerHostAddress;
	}

	public HostAddress getManagerHostAddress() {
		if (managerHostAddress == null) {
			throw new MethodNotAllowedException("Manager initialization did not finish");
		}
		return managerHostAddress;
	}

	public void clusterHosts() {
		log.info("Clustering edge worker hosts into the swarm");
		List<HostAddress> workerHosts = getLocalWorkerNodes().stream().map(EdgeHost::getAddress)
			.collect(Collectors.toCollection(LinkedList::new));
		if (getLocalWorkerNodes().size() < 1) {
			log.info("No edge worker hosts found");
		}
		log.info("Clustering cloud hosts into the swarm");
		workerHosts.addAll(getCloudWorkerNodes().stream().map(CloudHost::getAddress).collect(Collectors.toList()));
		if (getCloudWorkerNodes().size() < 1) {
			log.info("No cloud worker hosts found");
		}
		new ForkJoinPool(threads).execute(() ->
			workerHosts.parallelStream().forEach(host -> setupHost(host, NodeRole.WORKER)));
	}

	private List<CloudHost> getCloudWorkerNodes() {
		int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
		int maxInstances = Math.min(maxWorkers, this.maxInstances);
		List<CloudHost> cloudHosts = new ArrayList<>(maxInstances);
		for (int i = 0; i < maxInstances; i++) {
			cloudHosts.add(chooseCloudHost(null, false));
		}
		return cloudHosts;
	}

	private List<EdgeHost> getLocalWorkerNodes() {
		int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
		return edgeHostsService.getEdgeHosts().stream()
			.filter(edgeHost -> Objects.equals(edgeHost.getPublicIpAddress(), this.managerHostAddress.getPublicIpAddress()))
			.filter(edgeHost -> !Objects.equals(edgeHost.getPrivateIpAddress(), this.managerHostAddress.getPrivateIpAddress()))
			.filter(this::isEdgeHostRunning)
			.limit(maxWorkers)
			.collect(Collectors.toList());
	}

	public boolean isLocalhost(HostAddress hostAddress) {
		String machinePublicIp = this.managerHostAddress.getPublicIpAddress();
		String machinePrivateIp = this.managerHostAddress.getPrivateIpAddress();
		return Objects.equals(machinePublicIp, hostAddress.getPublicIpAddress())
			&& Objects.equals(machinePrivateIp, hostAddress.getPrivateIpAddress());
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node setupHost(HostAddress hostAddress, NodeRole role) {
		log.info("Setting up {} with role {}", hostAddress.toSimpleString(), role);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = null;
		String dockerApiProxyContainerId = "";
		final int retries = 5;
		int tries = 0;
		do {
			log.info("Launching docker api proxy container on host {}, attempt {}/{}", hostAddress.toSimpleString(), tries + 1, retries);
			dockerApiProxyContainerId = containersService.launchDockerApiProxy(hostAddress, false);
			Timing.sleep(tries, TimeUnit.SECONDS); // waits 0 seconds, then 1 seconds, then 2 seconds, etc
		} while (dockerApiProxyContainerId.isEmpty() && ++tries < retries);
		if (dockerApiProxyContainerId.isEmpty()) {
			throw new ManagerException("Failed to launch docker api proxy at %s", hostAddress.toSimpleString());
		}
		tries = 0;
		do {
			try {
				switch (role) {
					case MANAGER:
						node = setupSwarmManager(hostAddress);
						break;
					case WORKER:
						node = setupSwarmWorker(hostAddress);
						break;
					default:
						throw new UnsupportedOperationException();
				}
			}
			catch (ManagerException e) {
				log.error("Failed to setup {} with role {}: {}... Retrying ({}/{})", hostAddress.toSimpleString(), role, e.getMessage(), tries + 1, retries);
				Timing.sleep(tries + 1, TimeUnit.SECONDS); // waits 1 seconds, then 2 seconds, then 3 seconds, etc
			}
		} while (node == null && ++tries < retries);
		if (node == null) {
			throw new ManagerException("Failed to setup %s with role %s", hostAddress.toSimpleString(), role.name());
		}
		containersService.addContainer(dockerApiProxyContainerId);
		new ForkJoinPool(threads).execute(() ->
			List.of(LocationRequestsService.REQUEST_LOCATION_MONITOR, PrometheusService.PROMETHEUS).parallelStream()
				.forEach(service -> containersService.launchContainer(hostAddress, service, ContainerTypeEnum.SINGLETON)));
		executeBackgroundProcess(PrometheusProperties.NODE_EXPORTER, hostAddress, PrometheusProperties.NODE_EXPORTER);
		return node;
	}

	private pt.unl.fct.miei.usmanagement.manager.nodes.Node setupSwarmManager(HostAddress hostAddress) {
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node;
		if (isLocalhost(hostAddress)) {
			log.info("Setting up docker swarm leader");
			dockerSwarmService.leaveSwarm(hostAddress);
			node = dockerSwarmService.initSwarm();
		}
		else {
			node = joinSwarm(hostAddress, NodeRole.MANAGER);
		}
		return node;
	}

	private pt.unl.fct.miei.usmanagement.manager.nodes.Node setupSwarmWorker(HostAddress hostAddress) {
		return joinSwarm(hostAddress, NodeRole.WORKER);
	}

	private pt.unl.fct.miei.usmanagement.manager.nodes.Node joinSwarm(HostAddress hostAddress, NodeRole role) {
		log.info("Host {} is joining swarm as role {}", hostAddress.toSimpleString(), role);
		try {
			Node node = dockerSwarmService.getHostNode(hostAddress);
			if (node.status().state().equalsIgnoreCase("down")) {
				log.info("Host {} is already part of the swarm as node {}, but state down", hostAddress.toSimpleString(), node.id());
				pt.unl.fct.miei.usmanagement.manager.nodes.Node newNode = dockerSwarmService.joinSwarm(hostAddress, role, true);
				nodesService.removeNode(node.id());
				return newNode;
			}
			try {
				log.info("Host {} is already part of the swarm as node {}", hostAddress.toSimpleString(), node.id());
				return nodesService.getNode(node.id());
			}
			catch (EntityNotFoundException ignored) {
				return nodesService.addNode(node);
			}
		}
		catch (EntityNotFoundException ignored) {
			return dockerSwarmService.joinSwarm(hostAddress, role, false);
		}
	}

	public HostAddress getClosestCapableHost(double availableMemory, RegionEnum region) {
		return getClosestCapableHost(availableMemory, region.getCoordinates());
	}

	public HostAddress getCapableHost(double availableMemory, RegionEnum region) {
		return getCapableHost(availableMemory, region, null);
	}

	public HostAddress getCapableHost(double availableMemory, RegionEnum region, Predicate<HostAddress> nodeFilter) {
		log.info("Looking for node on region {} with <90% memory available and <90% cpu usage to launch service with {} expected ram usage",
			region.getRegion(), availableMemory);
		List<HostAddress> nodes = nodesService.getReadyNodes().stream()
			.filter(node -> node.getRegion() == region && hostMetricsService.hostHasEnoughResources(node.getHostAddress(), availableMemory)
				&& (nodeFilter == null || nodeFilter.test(node.getHostAddress())))
			.map(pt.unl.fct.miei.usmanagement.manager.nodes.Node::getHostAddress)
			.collect(Collectors.toList());
		HostAddress hostAddress;
		if (nodes.size() > 0) {
			Random random = new Random();
			hostAddress = nodes.get(random.nextInt(nodes.size()));
			log.info("Found node {}", hostAddress);
		}
		else {
			log.info("No nodes found, joining a new cloud node at {}", region);
			hostAddress = chooseCloudHost(region, true).getAddress();
		}
		return hostAddress;
	}

	public HostAddress getClosestCapableHost(double availableMemory, Coordinates coordinates) {
		return getClosestCapableHostIgnoring(availableMemory, coordinates, List.of());
	}

	public HostAddress getClosestCapableHostIgnoring(double availableMemory, Coordinates coordinates, List<HostAddress> hostAddresses) {
		List<EdgeHost> edgeHosts = edgeHostsService.getEdgeHosts().parallelStream().filter(host -> {
			HostAddress hostAddress = host.getAddress();
			return !hostAddresses.contains(hostAddress) && hostMetricsService.hostHasEnoughResources(host.getAddress(), availableMemory);
		}).collect(Collectors.toList());
		List<CloudHost> cloudHosts;
		try {
			cloudHosts = new ForkJoinPool(threads).submit(() ->
				cloudHostsService.getCloudHosts().parallelStream().filter(host ->
					hostMetricsService.hostHasEnoughResources(host.getAddress(), availableMemory)
				).collect(Collectors.toList())).get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new ManagerException("Unable to get closest capable hosts: %s", e.getMessage());
		}
		return getClosestHost(coordinates, edgeHosts, cloudHosts);
	}

	public HostAddress getClosestHost(Coordinates coordinates) {
		List<EdgeHost> edgeHosts = edgeHostsService.getEdgeHosts();
		List<CloudHost> cloudHosts = cloudHostsService.getCloudHosts();
		return getClosestHost(coordinates, edgeHosts, cloudHosts);
	}

	public HostAddress getClosestInactiveHost(Coordinates coordinates) {
		List<EdgeHost> inactiveEdgeHosts = edgeHostsService.getInactiveEdgeHosts();
		List<CloudHost> inactiveCloudHosts = cloudHostsService.getInactiveCloudHosts();
		return getClosestHost(coordinates, inactiveEdgeHosts, inactiveCloudHosts);
	}

	public HostAddress getClosestHost(Coordinates coordinates, List<EdgeHost> edgeHosts, List<CloudHost> cloudHosts) {
		edgeHosts.sort((oneEdgeHost, anotherEdgeHost) -> {
			double oneDistance = oneEdgeHost.getCoordinates().distanceTo(coordinates);
			double anotherDistance = anotherEdgeHost.getCoordinates().distanceTo(coordinates);
			return Double.compare(oneDistance, anotherDistance);
		});

		cloudHosts.sort((oneCloudHost, anotherCloudHost) -> {
			double oneDistance = oneCloudHost.getAwsRegion().getCoordinates().distanceTo(coordinates);
			double anotherDistance = anotherCloudHost.getAwsRegion().getCoordinates().distanceTo(coordinates);
			return Double.compare(oneDistance, anotherDistance);
		});

		final HostAddress hostAddress;
		if (!edgeHosts.isEmpty() && !cloudHosts.isEmpty()) {
			EdgeHost edgeHost = edgeHosts.get(0);
			double distanceToEdgeHost = edgeHost.getCoordinates().distanceTo(coordinates);
			CloudHost cloudHost = cloudHosts.get(0);
			double distanceToCloudHost = cloudHost.getAwsRegion().getCoordinates().distanceTo(coordinates);
			hostAddress = distanceToEdgeHost <= distanceToCloudHost ? edgeHost.getAddress() : cloudHost.getAddress();
		}
		else if (!edgeHosts.isEmpty()) {
			hostAddress = edgeHosts.get(0).getAddress();
		}
		else if (!cloudHosts.isEmpty()) {
			hostAddress = cloudHosts.get(0).getAddress();
		}
		else {
			hostAddress = cloudHostsService.launchInstance(coordinates).getAddress();
		}

		return hostAddress;
	}

	public HostAddress completeConnectionInfo(HostAddress hostAddress) {
		if (hostAddress.hasConnectionInfo()) {
			return hostAddress;
		}
		return completeHostAddress(hostAddress);
	}

	public HostAddress completeHostAddress(HostAddress hostAddress) {
		if (hostAddress.isComplete()) {
			return hostAddress;
		}
		try {
			return cloudHostsService.getCloudHostByAddress(hostAddress).getAddress();
		}
		catch (EntityNotFoundException ignored1) {
			try {
				return cloudHostsService.getCloudHostByIp(hostAddress.getPublicIpAddress()).getAddress();
			}
			catch (EntityNotFoundException ignored2) {
				try {
					return edgeHostsService.getEdgeHostByAddress(hostAddress).getAddress();
				}
				catch (EntityNotFoundException ignored3) {
					try {
						return edgeHostsService.getEdgeHostByHostname(hostAddress.getHostname()).getAddress();
					}
					catch (EntityNotFoundException e) {
						throw new EntityNotFoundException("Host", "hostAddress", hostAddress.toString());
					}
				}
			}
		}
	}

	public HostAddress getHostAddress(String hostname) {
		try {
			return edgeHostsService.getEdgeHostByHostname(hostname).getAddress();
		}
		catch (EntityNotFoundException ignored) {
			try {
				return cloudHostsService.getCloudHostByIp(hostname).getAddress();
			}
			catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Host", "hostAddress", hostname);
			}
		}
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node addHost(String host, NodeRole role) {
		HostAddress hostAddress;
		try {
			CloudHost cloudHost = cloudHostsService.getCloudHostByIdOrIp(host);
			if (cloudHost.getState().getCode() != AwsInstanceState.RUNNING.getCode()) {
				cloudHost = cloudHostsService.startInstance(host, false);
			}
			hostAddress = cloudHost.getAddress();
		}
		catch (EntityNotFoundException ignored) {
			try {
				EdgeHost edgeHost = edgeHostsService.getEdgeHostByHostname(host);
				hostAddress = edgeHost.getAddress();
			}
			catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Host", "host", host);
			}
		}
		return setupHost(hostAddress, role);
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node addHost(Coordinates coordinates, NodeRole role) {
		HostAddress hostAddress = getClosestInactiveHost(coordinates);
		return setupHost(hostAddress, role);
	}

	public void removeHost(HostAddress hostAddress) {
		if (nodesService.isPartOfSwarm(hostAddress)) {
			/*containersService.getSystemContainers(hostAddress).parallelStream()
				.filter(c -> !Objects.equals(c.getServiceName(), DockerApiProxyService.DOCKER_API_PROXY))
				.forEach(c -> containersService.stopContainer(c.getContainerId()));*/
			nodesService.leaveHost(hostAddress);
			nodesService.removeHost(hostAddress);
			/*containersService.getSystemContainers(hostAddress).stream()
				.filter(c -> Objects.equals(c.getServiceName(), DockerApiProxyService.DOCKER_API_PROXY))
				.forEach(c -> containersService.stopContainer(c.getContainerId()));*/
		}
	}

	private boolean isEdgeHostRunning(EdgeHost edgeHost) {
		return sshService.hasConnection(edgeHost.getAddress());
	}

	private CloudHost chooseCloudHost(RegionEnum region, boolean addToSwarm) {
		for (CloudHost cloudHost : cloudHostsService.getCloudHosts()) {
			int stateCode = cloudHost.getState().getCode();
			if (stateCode == AwsInstanceState.RUNNING.getCode()) {
				HostAddress hostAddress = cloudHost.getAddress();
				if (!nodesService.isPartOfSwarm(hostAddress)) {
					return cloudHost;
				}
			}
			else if (stateCode == AwsInstanceState.STOPPED.getCode()) {
				return cloudHostsService.startInstance(cloudHost, addToSwarm);
			}
		}
		Coordinates coordinates = region == null ? managerHostAddress.getRegion().getCoordinates() : region.getCoordinates();
		return cloudHostsService.launchInstance(coordinates);
	}

	public List<String> executeCommandSync(String command, HostAddress hostAddress) {
		return executeCommand(command, hostAddress, true);
	}

	public List<String> executeCommandAsync(String command, HostAddress hostAddress) {
		return executeCommand(command, hostAddress, false);
	}

	private List<String> executeCommand(String command, HostAddress hostAddress, boolean wait) {
		List<String> result = null;
		String error = null;
		if (Objects.equals(this.managerHostAddress, hostAddress)) {
			// execute local command
			if (wait) {
				BashCommandResult bashCommandResult = bashService.executeCommandSync(command);
				if (!bashCommandResult.isSuccessful()) {
					error = String.join("\n", bashCommandResult.getError());
				}
				else {
					result = bashCommandResult.getOutput();
				}
			}
			else {
				bashService.executeCommandAsync(command);
			}
		}
		else {
			// execute remote command
			if (!wait) {
				sshService.executeCommandAsync(command, hostAddress);
			}
			else {
				SshCommandResult sshCommandResult = sshService.executeCommandSync(command, hostAddress);
				if (!sshCommandResult.isSuccessful()) {
					error = String.join("\n", sshCommandResult.getError());
				}
				else {
					result = sshCommandResult.getOutput();
				}
			}
		}
		if (error != null) {
			throw new ManagerException("%s", error);
		}
		return result;
	}

	public void executeBackgroundProcess(String command, HostAddress hostAddress, String outputFile) {
		if (Objects.equals(this.managerHostAddress, hostAddress)) {
			bashService.executeBackgroundProcess(command, outputFile);
		}
		else {
			sshService.executeBackgroundProcess(command, hostAddress, outputFile);
		}
	}

	public void stopBackgroundProcesses(HostAddress hostAddress) {
		if (Objects.equals(this.managerHostAddress, hostAddress)) {
			bashService.stopBackgroundProcesses();
		}
		else {
			sshService.stopBackgroundProcesses(hostAddress);
		}
	}

	public int findAvailableExternalPort(HostAddress hostAddress, int startExternalPort) {
		String command = "lsof -i -P -n | grep LISTEN | awk '{print $9}' | cut -d: -f2";
		try {
			List<Integer> usedExternalPorts = executeCommandSync(command, hostAddress).stream()
				.filter(v -> Pattern.compile("-?\\d+(\\.\\d+)?").matcher(v).matches())
				.map(Integer::parseInt)
				.collect(Collectors.toList());
			for (int i = startExternalPort; ; i++) {
				if (!usedExternalPorts.contains(i)) {
					return i;
				}
			}
		}
		catch (ManagerException e) {
			throw new ManagerException("Unable to find currently used external ports at %s: %s ", hostAddress, e.getMessage());
		}
	}
}
