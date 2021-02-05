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

package pt.unl.fct.miei.usmanagement.manager.services.docker.swarm;

import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.NodeInfo;
import com.spotify.docker.client.messages.swarm.NodeSpec;
import com.spotify.docker.client.messages.swarm.SwarmJoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.configurations.Configuration;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.PlaceEnum;
import pt.unl.fct.miei.usmanagement.manager.services.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.services.configurations.ConfigurationsService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.remote.ssh.SshService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DockerSwarmService {

	public static final String NETWORK_OVERLAY = "usmanager-network-overlay";

	private final DockerCoreService dockerCoreService;
	private final HostsService hostsService;
	private final BashService bashService;
	private final NodesService nodesService;
	private final ConfigurationsService configurationsService;

	public DockerSwarmService(DockerCoreService dockerCoreService, @Lazy HostsService hostsService,
							  BashService bashService, @Lazy NodesService nodesService, ConfigurationsService configurationsService) {
		this.dockerCoreService = dockerCoreService;
		this.hostsService = hostsService;
		this.bashService = bashService;
		this.nodesService = nodesService;
		this.configurationsService = configurationsService;
	}

	public DockerClient getSwarmLeader() {
		HostAddress hostAddress = hostsService.getManagerHostAddress();
		return dockerCoreService.getDockerClient(hostAddress);
	}

	public Optional<String> getSwarmManagerNodeId(HostAddress hostAddress) {
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress)) {
			return Objects.equals(docker.info().swarm().localNodeState(), "active")
				&& docker.info().swarm().controlAvailable()
				? Optional.of(getHostNode(hostAddress).id())
				: Optional.empty();
		}
		catch (DockerException | InterruptedException e) {
			return Optional.empty();
		}
	}

	public Optional<String> getSwarmWorkerNodeId(HostAddress hostAddress) {
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress)) {
			return Objects.equals(docker.info().swarm().localNodeState(), "active")
				&& !docker.info().swarm().controlAvailable()
				? Optional.of(docker.info().swarm().nodeId())
				: Optional.empty();
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to get swarm work node id from %s: %s", hostAddress.toSimpleString(), e.getMessage());
		}
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node initSwarm() {
		HostAddress hostAddress = hostsService.getManagerHostAddress();
		String username = hostAddress.getUsername();
		String advertiseAddress = hostAddress.getPublicIpAddress();
		String listenAddress = hostAddress.getPrivateIpAddress();
		log.info("Initializing docker swarm at {}", advertiseAddress);
		List<String> output = bashService.initDockerSwarm(advertiseAddress, listenAddress);
		String outputMessage = String.join("\n", output);
		String nodeIdRegex = "(?<=Swarm initialized: current node \\()(.*)(?=\\) is now a manager)";
		Matcher nodeIdRegexExpression = Pattern.compile(nodeIdRegex).matcher(outputMessage);
		if (!nodeIdRegexExpression.find()) {
			throw new ManagerException("Unable to get docker swarm node id");
		}
		String nodeId = nodeIdRegexExpression.group(0);
		configurationsService.addConfiguration(nodeId);
		try {
			setNodeLabels(nodeId, listenAddress, username, hostAddress.getCoordinates(), hostAddress.getRegion(),
				hostAddress.getPlace(), Collections.singletonMap(NodeConstants.Label.MASTER_MANAGER, String.valueOf(true)));
			createNetworkOverlay(hostAddress);
			Node swarmNode = getNode(nodeId);
			return nodesService.addNode(swarmNode);
		}
		finally {
			configurationsService.removeConfiguration(nodeId);
		}
	}

	private void createNetworkOverlay(HostAddress hostAddress) {
		log.info("Creating network {}", NETWORK_OVERLAY);
		NetworkConfig networkConfig = NetworkConfig.builder()
			.driver("overlay")
			.attachable(true)
			.name(NETWORK_OVERLAY)
			.checkDuplicate(true)
			.build();
		try (DockerClient client = dockerCoreService.getDockerClient(hostAddress)) {
			client.createNetwork(networkConfig);
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to get create overlay network: %s", e.getMessage());
		}
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node rejoinSwarm(String nodeId) {
		Node node = getNode(nodeId);
		HostAddress hostAddress = getNodeAddress(node);
		NodeRole role = NodeRole.getNodeRole(node.spec().role());
		return hostsService.addHost(hostAddress.getPublicIpAddress(), role);
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node joinSwarm(HostAddress hostAddress, NodeRole role, boolean rejoin) {
		String leaderAddress = hostsService.getManagerHostAddress().getPublicIpAddress();
		try (DockerClient leaderClient = getSwarmLeader();
			 DockerClient nodeClient = dockerCoreService.getDockerClient(hostAddress)) {
			if (!rejoin) {
				leaveSwarm(nodeClient, hostAddress);
				nodesService.removeHost(hostAddress);
			}
			log.info("{} is joining the swarm as {}", hostAddress, role);
			String joinToken;
			switch (role) {
				case MANAGER:
					joinToken = leaderClient.inspectSwarm().joinTokens().manager();
					break;
				case WORKER:
					joinToken = leaderClient.inspectSwarm().joinTokens().worker();
					break;
				default:
					throw new UnsupportedOperationException();
			}
			String username = hostAddress.getUsername();
			String publicIpAddress = hostAddress.getPublicIpAddress();
			String privateIpAddress = hostAddress.getPrivateIpAddress();
			SwarmJoin swarmJoin = SwarmJoin.builder()
				.advertiseAddr(publicIpAddress)
				.listenAddr(privateIpAddress)
				.joinToken(joinToken)
				.remoteAddrs(List.of(leaderAddress))
				.build();
			nodeClient.joinSwarm(swarmJoin);
			Configuration config = null;
			try {
				String nodeId = nodeClient.info().swarm().nodeId();
				config = configurationsService.addConfiguration(nodeId);
				log.info("Host {} ({}) has joined the swarm as node {}", publicIpAddress, privateIpAddress, nodeId);
				setNodeLabels(nodeId, privateIpAddress, username, hostAddress.getCoordinates(), hostAddress.getRegion(), hostAddress.getPlace());
				Node node = getNode(nodeId);
				return nodesService.addNode(node);
			}
			finally {
				if (config != null) {
					configurationsService.removeConfiguration(config);
				}
			}

		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("%s was unable to join swarm: %s", hostAddress.toSimpleString(), e.getMessage());
		}
	}

	public void leaveSwarm(Node node) {
		HostAddress hostAddress = new HostAddress(
			node.spec().labels().get(NodeConstants.Label.USERNAME),
			node.status().addr(),
			node.spec().labels().get(NodeConstants.Label.PRIVATE_IP_ADDRESS));
		leaveSwarm(hostAddress);
	}

	public Optional<String> leaveSwarm(HostAddress hostAddress) {
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress)) {
			return leaveSwarm(docker, hostAddress);
		}
	}

	private Optional<String> leaveSwarm(DockerClient docker, HostAddress hostAddress) {
		try {
			log.info(docker.info().swarm().toString());
			boolean isNode = !Objects.equals(docker.info().swarm().localNodeState(), "inactive");
			if (isNode) {
				String nodeId = docker.info().swarm().nodeId();
				boolean isManager = docker.info().swarm().controlAvailable();
				Integer managers = docker.info().swarm().managers();
				if (isManager && managers != null && managers > 1) {
					changeRole(nodeId, NodeRole.WORKER);
				}
				// docker.leaveSwarm(); bug when docker client threw exception with 'node is not part of the swarm'
				List<String> result = hostsService.executeCommandSync("docker swarm leave", hostAddress);
				log.info("{} ({}) left the swarm: {}", docker.getHost(), nodeId, result);
				return Optional.of(nodeId);
			}
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Host %s failed to leave swarm: %s", docker.getHost(), e.getMessage());
		}
		return Optional.empty();
	}

	public void destroySwarm() {
		try {
			DockerClient swarmLeader = getSwarmLeader();
			swarmLeader.leaveSwarm(true);
		}
		catch (DockerException | InterruptedException e) {
			log.error("Failed to destroy swarm: {}", e.getMessage());
		}
		nodesService.reset();
	}

	public List<Node> getNodes() {
		return getNodes(null);
	}

	private List<Node> getNodes(Predicate<Node> filter) {
		try (DockerClient swarmManager = getSwarmLeader()) {
			Stream<Node> nodeStream = swarmManager.listNodes().stream();
			if (filter != null) {
				nodeStream = nodeStream.filter(filter);
			}
			return nodeStream.collect(Collectors.toList());
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to get nodes: %s", e.getMessage());
		}
	}

	public Node getNode(String id) {
		return getNodes(node -> Objects.equals(node.id(), id)).stream()
			.findFirst()
			.orElseThrow(() -> new EntityNotFoundException(Node.class, "id", id));
	}

	public Node getHostNode(HostAddress hostAddress) {
		String publicIpAddress = hostAddress.getPublicIpAddress();
		return getNodes(node -> Objects.equals(node.status().addr(), publicIpAddress)).stream()
			.findFirst()
			.orElseThrow(() -> new EntityNotFoundException(Node.class, "hostAddress", hostAddress.toString()));
	}

	public List<Node> getActiveNodes() {
		return getActiveNodes(null);
	}

	public List<Node> getActiveNodes(Predicate<Node> filter) {
		Predicate<Node> activeFilter = n -> n.spec().availability().equalsIgnoreCase("active");
		Predicate<Node> nodesFilter = filter == null ? activeFilter : filter.and(activeFilter);
		return getReadyNodes(nodesFilter);
	}

	public List<Node> getReadyNodes() {
		return getReadyNodes(null);
	}

	public List<Node> getReadyNodes(Predicate<Node> filter) {
		Predicate<Node> readyFilter = n -> n.status().state().equals("ready");
		Predicate<Node> nodesFilter = filter == null ? readyFilter : filter.and(readyFilter);
		return getNodes(nodesFilter).stream()
			.filter(node -> !configurationsService.isConfiguring(node.id()))
			.collect(Collectors.toList());
	}

	public List<Node> getReadyManagers() {
		return getReadyNodes(node -> node.managerStatus() != null);
	}

	public List<Node> getReadyWorkers() {
		return getReadyNodes(node -> node.managerStatus() == null);
	}

	public void removeNodes(Predicate<Node> filter) {
		getNodes(filter).forEach(n -> nodesService.removeNode(n.id()));
	}

	/*public void removeHostNodes(HostAddress hostAddress) {
		removeNodes(n -> Objects.equals(n.status().addr(), hostAddress.getPublicIpAddress())
			&& Objects.equals(n.spec().labels().get(NodeConstants.Label.PRIVATE_IP_ADDRESS), hostAddress.getPrivateIpAddress()));
	}*/

	public void removeNode(String nodeId) {
		try (DockerClient swarmManager = getSwarmLeader()) {
			swarmManager.deleteNode(nodeId, true);
			log.info("Deleted node {}", nodeId);
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable remove node %s from the swarm: %s", nodeId, e.getMessage());
		}
	}

	public boolean isPartOfSwarm(HostAddress hostAddress) {
		return getNodes(n -> Objects.equals(n.status().addr(), hostAddress.getPublicIpAddress())).size() > 0;
	}

	public boolean isManager(String nodeId) {
		try (DockerClient swarmManager = getSwarmLeader()) {
			NodeInfo nodeInfo = swarmManager.inspectNode(nodeId);
			return nodeInfo.managerStatus() != null;
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to check if node %s is a manager: %s", nodeId, e.getMessage());
		}
	}

	public boolean isWorker(String nodeId) {
		try (DockerClient swarmManager = getSwarmLeader()) {
			NodeInfo nodeInfo = swarmManager.inspectNode(nodeId);
			return nodeInfo.managerStatus() == null;
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to check if node %s is a worker: %s", nodeId, e.getMessage());
		}
	}

	public Node changeAvailability(String nodeId, NodeAvailability newAvailability) {
		Node node = getNode(nodeId);
		NodeSpec nodeSpec = NodeSpec.builder()
			.availability(newAvailability.name().toUpperCase())
			.role(node.spec().role().toUpperCase())
			.build();
		return updateNode(node, nodeSpec);
	}

	public Node changeRole(String nodeId, NodeRole newRole) {
		Node node = getNode(nodeId);
		NodeSpec nodeSpec = NodeSpec.builder()
			.availability(node.spec().availability().toUpperCase())
			.role(newRole.name().toUpperCase())
			.build();
		return updateNode(node, nodeSpec);
	}

	private void setNodeLabels(String nodeId, String privateIpAddress, String username, Coordinates coordinates,
							   RegionEnum region, PlaceEnum place) {
		setNodeLabels(nodeId, privateIpAddress, username, coordinates, region, place, Collections.emptyMap());
	}

	private void setNodeLabels(String nodeId, String privateIpAddress, String username, Coordinates coordinates, RegionEnum region,
							   PlaceEnum place, Map<String, String> customLabels) {
		Map<String, String> labels = new HashMap<>(customLabels);
		labels.put(NodeConstants.Label.PRIVATE_IP_ADDRESS, privateIpAddress);
		labels.put(NodeConstants.Label.USERNAME, username);
		labels.put(NodeConstants.Label.COORDINATES, new Gson().toJson(coordinates));
		labels.put(NodeConstants.Label.REGION, region.name());
		labels.put(NodeConstants.Label.PLACE, place.name());
		addLabels(nodeId, labels);
	}

	public Node addLabel(String nodeId, String label, String value) {
		return addLabels(nodeId, Map.of(label, value));
	}

	public Node addLabels(String nodeId, Map<String, String> labels) {
		log.info("Adding labels {} to node {}", labels, nodeId);
		Node node = getNode(nodeId);
		Map<String, String> nodeLabels = new HashMap<>(node.spec().labels());
		nodeLabels.putAll(labels);
		NodeSpec nodeSpec = NodeSpec.builder()
			.availability(node.spec().availability().toUpperCase())
			.role(node.spec().role().toUpperCase())
			.labels(nodeLabels)
			.build();
		return updateNode(node, nodeSpec);
	}

	public Node removeLabel(String nodeId, String label) {
		log.info("Removing label {} from node {}", label, nodeId);
		Node node = getNode(nodeId);
		Map<String, String> labels = new HashMap<>(node.spec().labels());
		labels.remove(label);
		NodeSpec nodeSpec = NodeSpec.builder()
			.availability(node.spec().availability().toUpperCase())
			.role(node.spec().role())
			.labels(labels)
			.build();
		return updateNode(node, nodeSpec);
	}

	public Node updateNode(String nodeId, String availability, String role, Map<String, String> labels) {
		Node node = getNode(nodeId);
		NodeSpec nodeSpec = NodeSpec.builder()
			.availability(availability)
			.role(role)
			.labels(labels)
			.build();
		return updateNode(node, nodeSpec);
	}

	private Node updateNode(Node node, NodeSpec nodeSpec) {
		String nodeId = node.id();
		try (DockerClient swarmManager = getSwarmLeader()) {
			swarmManager.updateNode(nodeId, node.version().index(), nodeSpec);
			return getNode(nodeId);
		}
		catch (DockerException | InterruptedException e) {
			throw new ManagerException("Unable to update node %s: %s", nodeId, e.getMessage());
		}
	}

	private HostAddress getNodeAddress(Node node) {
		final Map<String, String> labels = node.spec().labels();
		final String username = labels.get(NodeConstants.Label.USERNAME);
		final String privateIpAddress = labels.get(NodeConstants.Label.PRIVATE_IP_ADDRESS);
		final Coordinates coordinates = new Gson().fromJson(labels.get(NodeConstants.Label.COORDINATES), Coordinates.class);
		String regionName = labels.get(NodeConstants.Label.REGION);
		RegionEnum region = regionName == null ? null : RegionEnum.getRegion(regionName);
		return new HostAddress(username, node.status().addr(), privateIpAddress, coordinates, region);
	}
}
