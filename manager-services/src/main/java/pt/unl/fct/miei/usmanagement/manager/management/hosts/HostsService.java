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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.ManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.Mode;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.MethodNotAllowedException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainerType;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.SimpleNode;
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
import pt.unl.fct.miei.usmanagement.manager.regions.Region;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
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
	private final String localMachineDns;
	private final int maxWorkers;
	private final int maxInstances;
	private final Mode mode;
	private HostAddress hostAddress;

	public HostsService(@Lazy NodesService nodesService, @Lazy ContainersService containersService,
						DockerSwarmService dockerSwarmService, EdgeHostsService edgeHostsService,
						CloudHostsService cloudHostsService, SshService sshService, BashService bashService,
						HostMetricsService hostMetricsService, DockerProperties dockerProperties,
						AwsProperties awsProperties, ManagerProperties managerProperties, HostProperties hostProperties) {
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
		this.localMachineDns = hostProperties.getLocalMachineDns();
		this.mode = managerProperties.getMode();
	}

	public HostAddress setHostAddress() {
		String username = bashService.getUsername();
		String publicIp = bashService.getPublicIp();
		String privateIp = bashService.getPrivateIp();
		this.hostAddress = getFullHostAddress(new HostAddress(username, publicIp, privateIp));
		log.info("Setting local address: {}", hostAddress.toString());
		return hostAddress;
	}

	public HostAddress getHostAddress() {
		if (hostAddress == null) {
			throw new MethodNotAllowedException("manager initialization did not finish");
		}
		return hostAddress;
	}

	public void clusterHosts() {
		List<HostAddress> workerHosts = new LinkedList<>();
		if (mode == null || mode == Mode.LOCAL) {
			log.info("Clustering edge worker hosts into the swarm");
			workerHosts.addAll(getLocalWorkerNodes().stream().map(EdgeHostEntity::getAddress).collect(Collectors.toList()));
			if (getLocalWorkerNodes().size() < 1) {
				log.info("No edge worker hosts found");
			}
		}
		if (mode == null || mode == Mode.GLOBAL) {
			log.info("Clustering cloud hosts into the swarm");
			workerHosts.addAll(getCloudWorkerNodes().stream().map(CloudHostEntity::getAddress).collect(Collectors.toList()));
			if (getCloudWorkerNodes().size() < 1) {
				log.info("No cloud worker hosts found");
			}
		}
		workerHosts.parallelStream().forEach(host -> setupHost(host, NodeRole.WORKER));
	}

	private List<CloudHostEntity> getCloudWorkerNodes() {
		int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
		int maxInstances = Math.min(this.maxInstances, maxWorkers);
		List<CloudHostEntity> cloudHosts = new ArrayList<>(maxInstances);
		for (int i = 0; i < maxInstances; i++) {
			cloudHosts.add(chooseCloudHost(null, false));
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

	public boolean isLocalhost(HostAddress hostAddress) {
		String machinePublicIp = this.hostAddress.getPublicIpAddress();
		String machinePrivateIp = this.hostAddress.getPrivateIpAddress();
		return Objects.equals(machinePublicIp, hostAddress.getPublicIpAddress())
			&& Objects.equals(machinePrivateIp, hostAddress.getPrivateIpAddress());
	}

	public SimpleNode setupHost(HostAddress hostAddress, NodeRole role) {
		log.info("Setting up {} with role {}", hostAddress.toSimpleString(), role);
		String dockerApiProxyContainerId = containersService.launchDockerApiProxy(hostAddress, false);
		SimpleNode node = null;
		int tries = 0;
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
				log.error("Failed to setup {} with role {}: {}", hostAddress.toSimpleString(), role, e.getMessage());
				Timing.sleep(tries + 1, TimeUnit.SECONDS); // waits 1 second, then 2 seconds, then 3, etc
			}
		} while (node == null && ++tries < 5);
		if (node == null) {
			throw new ManagerException("Failed to setup %s with role %s", hostAddress.toSimpleString(), role.name());
		}
		containersService.addContainer(dockerApiProxyContainerId);
		containersService.launchContainer(hostAddress, LocationRequestsService.REQUEST_LOCATION_MONITOR, ContainerType.SINGLETON);
		containersService.launchContainer(hostAddress, PrometheusService.PROMETHEUS, ContainerType.SINGLETON);
		executeCommandAsync(PrometheusProperties.NODE_EXPORTER, hostAddress);
		return node;
	}

	private SimpleNode setupSwarmManager(HostAddress hostAddress) {
		SimpleNode node;
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

	private SimpleNode setupSwarmWorker(HostAddress hostAddress) {
		return joinSwarm(hostAddress, NodeRole.WORKER);
	}

	private SimpleNode joinSwarm(HostAddress hostAddress, NodeRole role) {
		try {
			return nodesService.getHostNode(hostAddress);
		}
		catch (EntityNotFoundException ignored) {
			return dockerSwarmService.joinSwarm(hostAddress, role);
		}
	}

	public HostAddress getClosestCapableHost(double availableMemory, Region region) {
		return getClosestCapableHost(availableMemory, region.getCoordinates());
	}

	public HostAddress getCapableNode(double availableMemory, Region region) {
		log.info("Looking for node on region {} with at least {} memory available and <90% cpu usage", region.getName(), availableMemory);
		List<HostAddress> nodes = nodesService.getReadyNodes().stream()
			.filter(node -> node.getRegion() == region && hostMetricsService.hostHasEnoughResources(node.getHostAddress(), availableMemory))
			.map(SimpleNode::getHostAddress)
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
			setupHost(hostAddress, NodeRole.WORKER);
		}
		return hostAddress;
	}

	public HostAddress getClosestCapableHost(double availableMemory, Coordinates coordinates) {
		List<EdgeHostEntity> edgeHosts = edgeHostsService.getEdgeHosts().parallelStream().filter(host ->
			hostMetricsService.hostHasEnoughResources(hostAddress, availableMemory)
		).collect(Collectors.toList());
		List<CloudHostEntity> cloudHosts = cloudHostsService.getCloudHosts().parallelStream().filter(host ->
			hostMetricsService.hostHasEnoughResources(hostAddress, availableMemory)
		).collect(Collectors.toList());
		return getClosestHost(coordinates, edgeHosts, cloudHosts);
	}

	public HostAddress getClosestHost(Coordinates coordinates) {
		List<EdgeHostEntity> edgeHosts = edgeHostsService.getEdgeHosts();
		List<CloudHostEntity> cloudHosts = cloudHostsService.getCloudHosts();
		return getClosestHost(coordinates, edgeHosts, cloudHosts);
	}

	public HostAddress getClosestInactiveHost(Coordinates coordinates) {
		List<EdgeHostEntity> inactiveEdgeHosts = edgeHostsService.getInactiveEdgeHosts();
		List<CloudHostEntity> inactiveCloudHosts = cloudHostsService.getInactiveCloudHosts();
		return getClosestHost(coordinates, inactiveEdgeHosts, inactiveCloudHosts);
	}

	public HostAddress getClosestHost(Coordinates coordinates, List<EdgeHostEntity> edgeHosts, List<CloudHostEntity> cloudHosts) {
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
			EdgeHostEntity edgeHost = edgeHosts.get(0);
			double distanceToEdgeHost = edgeHost.getCoordinates().distanceTo(coordinates);
			CloudHostEntity cloudHost = cloudHosts.get(0);
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

	public HostAddress getFullHostAddress(HostAddress hostAddress) {
		if (hostAddress.isComplete()) {
			return hostAddress;
		}
		try {
			return cloudHostsService.getCloudHostByAddress(hostAddress).getAddress();
		}
		catch (EntityNotFoundException ignored) {
			try {
				return edgeHostsService.getEdgeHostByAddress(hostAddress).getAddress();
			}
			catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Host", "hostAddress", hostAddress.toString());
			}
		}
	}

	public HostAddress getHostAddress(String hostname) {
		try {
			return edgeHostsService.getEdgeHostByDnsOrIp(hostname).getAddress();
		}
		catch (EntityNotFoundException ignored) {
			try {
				return cloudHostsService.getCloudHostByIp(hostname).getAddress();
			}
			catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Host", "hostAddress", hostAddress.toString());
			}
		}
	}

	public SimpleNode addHost(String host, NodeRole role) {
		HostAddress hostAddress;
		try {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIdOrIp(host);
			if (cloudHost.getState().getCode() != AwsInstanceState.RUNNING.getCode()) {
				cloudHost = cloudHostsService.startInstance(host, false);
			}
			hostAddress = cloudHost.getAddress();
		}
		catch (EntityNotFoundException ignored) {
			try {
				EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(host);
				hostAddress = edgeHost.getAddress();
			}
			catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Host", "host", host);
			}
		}
		return setupHost(hostAddress, role);
	}

	public SimpleNode addHost(Coordinates coordinates, NodeRole role) {
		HostAddress hostAddress = getClosestInactiveHost(coordinates);
		return setupHost(hostAddress, role);
	}

	public void removeHost(HostAddress hostAddress) {
		containersService.getSystemContainers(hostAddress).stream()
			.filter(c -> !Objects.equals(c.getServiceName(), DockerApiProxyService.DOCKER_API_PROXY))
			.forEach(c -> containersService.stopContainer(c.getContainerId()));
		dockerSwarmService.leaveSwarm(hostAddress);
	}

	private boolean isEdgeHostRunning(EdgeHostEntity edgeHost) {
		return sshService.hasConnection(edgeHost.getAddress());
	}

	private CloudHostEntity chooseCloudHost(Region region, boolean addToSwarm) {
		for (CloudHostEntity cloudHost : cloudHostsService.getCloudHosts()) {
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
		Coordinates coordinates = region == null ? hostAddress.getRegion().getCoordinates() : region.getCoordinates();
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
		if (Objects.equals(this.hostAddress, hostAddress)) {
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

	public String findAvailableExternalPort(HostAddress hostAddress, String startExternalPort) {
		String command = "sudo lsof -i -P -n | grep LISTEN | awk '{print $9}' | cut -d: -f2";
		try {
			List<Integer> usedExternalPorts = executeCommandSync(command, hostAddress).stream()
				.filter(v -> Pattern.compile("-?\\d+(\\.\\d+)?").matcher(v).matches())
				.map(Integer::parseInt)
				.collect(Collectors.toList());
			for (int i = Integer.parseInt(startExternalPort); ; i++) {
				if (!usedExternalPorts.contains(i)) {
					return String.valueOf(i);
				}
			}
		}
		catch (ManagerException e) {
			throw new ManagerException("Unable to find currently used external ports at %s: %s ", hostAddress,
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
		}
		catch (EntityNotFoundException ignored) {
			return edgeHostsService.getEdgeHostByDns(host).getPublicIpAddress();
		}
	}

}
