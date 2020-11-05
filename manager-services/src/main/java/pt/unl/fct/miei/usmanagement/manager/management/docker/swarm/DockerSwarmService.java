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

package pt.unl.fct.miei.usmanagement.manager.management.docker.swarm;

import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.SwarmJoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DockerSwarmService {

	public static final String NETWORK_OVERLAY = "usmanager-network-overlay";

	private final DockerCoreService dockerCoreService;
	private final NodesService nodesService;
	private final HostsService hostsService;
	private final BashService bashService;

	public DockerSwarmService(DockerCoreService dockerCoreService,
							  @Lazy NodesService nodesService,
							  @Lazy HostsService hostsService,
							  BashService bashService) {
		this.dockerCoreService = dockerCoreService;
		this.nodesService = nodesService;
		this.hostsService = hostsService;
		this.bashService = bashService;
	}

	public DockerClient getSwarmLeader() {
		HostAddress hostAddress = hostsService.getMasterHostAddress();
		return dockerCoreService.getDockerClient(hostAddress);
	}

	public Optional<String> getSwarmManagerNodeId(HostAddress hostAddress) {
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress)) {
			return Objects.equals(docker.info().swarm().localNodeState(), "active")
				&& docker.info().swarm().controlAvailable()
				? Optional.of(nodesService.getHostNode(hostAddress).getId())
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
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public SimpleNode initSwarm() {
		HostAddress hostAddress = hostsService.getMasterHostAddress();
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
		setNodeLabels(nodeId, listenAddress, username, hostAddress.getCoordinates(), hostAddress.getRegion(),
			Collections.singletonMap(NodeConstants.Label.MASTER_MANAGER, String.valueOf(true)));
		createNetworkOverlay(hostAddress);
		return nodesService.getNode(nodeId);
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
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public SimpleNode rejoinSwarm(String nodeId) {
		SimpleNode node = nodesService.getNode(nodeId);
		HostAddress hostAddress = node.getHostAddress();
		NodeRole role = node.getRole();
		nodesService.removeNode(nodeId);
		return joinSwarm(hostAddress, role);
	}

	public SimpleNode joinSwarm(HostAddress hostAddress, NodeRole role) {
		String leaderAddress = hostsService.getMasterHostAddress().getPublicIpAddress();
		try (DockerClient leaderClient = getSwarmLeader();
			 DockerClient nodeClient = dockerCoreService.getDockerClient(hostAddress)) {
			/*try {*/
			leaveSwarm(nodeClient);
			/*} catch (Exception e) {
				e.printStackTrace();
			}*/
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
			String nodeId = nodeClient.info().swarm().nodeId();
			log.info("Host {} ({}) has joined the swarm as node {}", publicIpAddress, privateIpAddress, nodeId);
			setNodeLabels(nodeId, privateIpAddress, username, hostAddress.getCoordinates(), hostAddress.getRegion());
			return nodesService.getNode(nodeId);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public Optional<String> leaveSwarm(HostAddress hostAddress) {
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress)) {
			return leaveSwarm(docker);
		}
	}

	public void leaveSwarm(Node node) {
		HostAddress hostAddress = new HostAddress(
			node.spec().labels().get(NodeConstants.Label.USERNAME),
			node.status().addr(),
			node.spec().labels().get(NodeConstants.Label.PRIVATE_IP_ADDRESS));
		leaveSwarm(hostAddress);
	}

	private Optional<String> leaveSwarm(DockerClient docker) {
		try {
			boolean isNode = !Objects.equals(docker.info().swarm().localNodeState(), "inactive");
			if (isNode) {
				String nodeId = docker.info().swarm().nodeId();
				boolean isManager = docker.info().swarm().controlAvailable();
				Integer managers = docker.info().swarm().managers();
				if (isManager && managers != null && managers > 1) {
					nodesService.changeRole(nodeId, NodeRole.WORKER);
				}
				docker.leaveSwarm(true);
				log.info("{} ({}) left the swarm", docker.getHost(), nodeId);
				return Optional.of(nodeId);
			}
		}
		catch (DockerException | InterruptedException e) {
			log.error("Host {} failed to leave swarm: {}", docker.getHost(), e.getMessage());
			throw new ManagerException(e.getMessage());
		}
		return Optional.empty();
	}

	public void destroySwarm() {
		try {
			DockerClient swarmLeader = getSwarmLeader();
			swarmLeader.listNodes().parallelStream()
				.filter(node -> node.spec().labels().get(NodeConstants.Label.MASTER_MANAGER) == null)
				.forEach(this::leaveSwarm);
			// leader must be the last one to leave
			leaveSwarm(swarmLeader);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setNodeLabels(String nodeId, String privateIpAddress, String username, Coordinates coordinates, Region region) {
		setNodeLabels(nodeId, privateIpAddress, username, coordinates, region, Collections.emptyMap());
	}

	private void setNodeLabels(String nodeId, String privateIpAddress, String username, Coordinates coordinates, Region region,
							   Map<String, String> customLabels) {
		Map<String, String> labels = new HashMap<>(customLabels);
		labels.put(NodeConstants.Label.PRIVATE_IP_ADDRESS, privateIpAddress);
		labels.put(NodeConstants.Label.USERNAME, username);
		labels.put(NodeConstants.Label.COORDINATES, new Gson().toJson(coordinates));
		labels.put(NodeConstants.Label.REGION, region.name());
		nodesService.addLabels(nodeId, labels);
	}

}
