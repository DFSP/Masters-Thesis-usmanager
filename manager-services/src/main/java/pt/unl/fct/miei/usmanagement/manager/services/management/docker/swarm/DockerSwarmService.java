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

package pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.SwarmJoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.services.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.HostsService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DockerSwarmService {

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
		HostAddress hostAddress = hostsService.getHostAddress();
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
		HostAddress hostAddress = hostsService.getHostAddress();
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
		nodesService.addLabel(nodeId, NodeConstants.Label.PRIVATE_IP_ADDRESS, listenAddress);
		return nodesService.getNode(nodeId);
	}

	public SimpleNode rejoinSwarm(String nodeId) {
		SimpleNode node = nodesService.getNode(nodeId);
		HostAddress hostAddress = node.getHostAddress();
		NodeRole role = node.getRole();
		nodesService.removeNode(nodeId);
		return joinSwarm(hostAddress, role);
	}

	public SimpleNode joinSwarm(HostAddress hostAddress, NodeRole role) {
		String leaderAddress = hostsService.getHostAddress().getPublicIpAddress();
		try (DockerClient leaderClient = getSwarmLeader();
			 DockerClient nodeClient = dockerCoreService.getDockerClient(hostAddress)) {
			leaveSwarm(nodeClient);
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
			nodesService.addLabel(nodeId, NodeConstants.Label.PRIVATE_IP_ADDRESS, privateIpAddress);
			return nodesService.getNode(nodeId);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public void leaveSwarm(HostAddress hostAddress) {
		try (DockerClient docker = dockerCoreService.getDockerClient(hostAddress)) {
			leaveSwarm(docker);
		}
	}

	private void leaveSwarm(DockerClient docker) {
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
			}
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public void destroy() {
		try {
			getSwarmLeader().listNodes().parallelStream()
				.map(node -> new HostAddress(node.status().addr())).forEach(this::leaveSwarm);
		}
		catch (DockerException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
