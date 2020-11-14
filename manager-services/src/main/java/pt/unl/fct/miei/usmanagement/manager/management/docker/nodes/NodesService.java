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

package pt.unl.fct.miei.usmanagement.manager.management.docker.nodes;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.nodes.Nodes;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.PlaceEnum;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NodesService {

	private final DockerSwarmService dockerSwarmService;
	private final HostsService hostsService;
	private final ContainersService containersService;
	private final DockerApiProxyService dockerApiProxyService;

	private final Nodes nodes;

	public NodesService(@Lazy DockerSwarmService dockerSwarmService, @Lazy HostsService hostsService,
						@Lazy ContainersService containersService, DockerApiProxyService dockerApiProxyService, Nodes nodes) {
		this.dockerSwarmService = dockerSwarmService;
		this.hostsService = hostsService;
		this.containersService = containersService;
		this.dockerApiProxyService = dockerApiProxyService;
		this.nodes = nodes;
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getNodes() {
		return nodes.findAll();
	}

	private List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getNodes(Predicate<pt.unl.fct.miei.usmanagement.manager.nodes.Node> filter) {
		return filter == null ? getNodes() : getNodes().stream().filter(filter).collect(Collectors.toList());
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node getNode(String id) {
		return nodes.findNodeByNodeId(id).orElseThrow(() ->
			new EntityNotFoundException(Node.class, "id", id));
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getHostNodes(HostAddress hostAddress) {
		return nodes.findByPublicIpAddress(hostAddress.getPublicIpAddress());
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getActiveNodes() {
		return nodes.findByAvailability(NodeAvailability.ACTIVE);
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getActiveNodes(Predicate<pt.unl.fct.miei.usmanagement.manager.nodes.Node> filter) {
		return filter == null ? getActiveNodes() : getActiveNodes().stream().filter(filter).collect(Collectors.toList());
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getReadyNodes() {
		return nodes.findByState("ready");
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getReadyNodes(Predicate<pt.unl.fct.miei.usmanagement.manager.nodes.Node> filter) {
		return filter == null ? getReadyNodes() : getReadyNodes().stream().filter(node -> node.getRegion() != null).collect(Collectors.toList());
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getReadyManagers() {
		return nodes.findByStateAndManagerStatusIsNotNull("ready");
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getReadyWorkers() {
		return nodes.findByStateAndManagerStatusIsNull("ready");
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node addNode(Node swarmNode) {
		checkNodeDoesntExist(swarmNode);
		log.info("Saving node {}", ToStringBuilder.reflectionToString(swarmNode));
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = fromSwarmNode(swarmNode);
		return nodes.save(node);
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> addNodes(NodeRole role, String host, List<Coordinates> coordinates) {
		List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodes = new ArrayList<>();
		if (host != null) {
			pt.unl.fct.miei.usmanagement.manager.nodes.Node node = hostsService.addHost(host, role);
			nodes.add(node);
			/*Node n = hostsService.addHost(host, role);
			pt.unl.fct.miei.usmanagement.manager.nodes.Node node = fromSwarmNode(n);
			nodes.add(this.nodes.save(node));*/
		}
		else if (coordinates != null) {
			for (Coordinates coordinate : coordinates) {
				pt.unl.fct.miei.usmanagement.manager.nodes.Node node = hostsService.addHost(coordinate, role);
				nodes.add(node);
			}
		}
		return nodes;
	}

	public void removeHost(HostAddress hostAddress) {
		dockerSwarmService.removeHostNodes(hostAddress);
		List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodes = getHostNodes(hostAddress);
		this.nodes.deleteAll(nodes);
	}

	public void removeNode(String nodeId) {
		dockerSwarmService.removeNode(nodeId);
		deleteNode(nodeId);
	}

	public void deleteNode(String nodeId) {
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		nodes.delete(node);
	}

	public boolean isPartOfSwarm(HostAddress hostAddress) {
		return dockerSwarmService.isPartOfSwarm(hostAddress);
	}

	public boolean isManager(String nodeId) {
		return dockerSwarmService.isManager(nodeId);
	}

	public boolean isWorker(String nodeId) {
		return dockerSwarmService.isWorker(nodeId);
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node changeAvailability(String nodeId, NodeAvailability newAvailability) {
		dockerSwarmService.changeAvailability(nodeId, newAvailability);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		node.setAvailability(newAvailability);
		nodes.save(node);
		return node;
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node changeRole(String nodeId, NodeRole newRole) {
		dockerSwarmService.changeRole(nodeId, newRole);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		node.setRole(newRole);
		nodes.save(node);
		return node;
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node addLabel(String nodeId, String label, String value) {
		return addLabels(nodeId, Map.of(label, value));
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node addLabels(String nodeId, Map<String, String> newLabels) {
		dockerSwarmService.addLabels(nodeId, newLabels);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		Map<String, String> labels = node.getLabels();
		labels.putAll(newLabels);
		node.setLabels(labels);
		nodes.save(node);
		return node;
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node removeLabel(String nodeId, String label) {
		dockerSwarmService.removeLabel(nodeId, label);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		Map<String, String> labels = node.getLabels();
		labels.remove(label);
		node.setLabels(labels);
		nodes.save(node);
		return node;
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node updateNode(pt.unl.fct.miei.usmanagement.manager.nodes.Node node) {
		return nodes.save(node);
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node updateNodeSpecs(String nodeId,
																		   pt.unl.fct.miei.usmanagement.manager.nodes.Node newNode) {
		Node swarmNode = dockerSwarmService.updateNode(nodeId, newNode.getAvailability().name(), newNode.getRole().name(),
			newNode.getLabels());
		newNode = fromSwarmNode(swarmNode);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		log.info("Updating node {} with {}", ToStringBuilder.reflectionToString(node), ToStringBuilder.reflectionToString(newNode));
		log.info("Node before copying properties: {}", ToStringBuilder.reflectionToString(node));
		ObjectUtils.copyValidProperties(newNode, node);
		log.info("Node after copying properties: {}", ToStringBuilder.reflectionToString(node));
		return nodes.save(node);
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node rejoinSwarm(String nodeId) {
		return dockerSwarmService.rejoinSwarm(nodeId);
		/*Node swarmNode = dockerSwarmService.rejoinSwarm(nodeId);
		pt.unl.fct.miei.usmanagement.manager.nodes.Node node = getNode(nodeId);
		nodes.delete(node);
		return addNodeFromSwarmNode(swarmNode);*/
	}

	public pt.unl.fct.miei.usmanagement.manager.nodes.Node addNodeFromSwarmNode(Node swarmNode) {
		pt.unl.fct.miei.usmanagement.manager.nodes.Node newNode = fromSwarmNode(swarmNode);
		return nodes.save(newNode);
	}

	public boolean hasNode(String nodeId) {
		return nodes.hasNode(nodeId);
	}

	public void reset() {
		nodes.deleteAll();
		log.info("Clearing all nodes");
	}

	private void checkNodeDoesntExist(Node node) {
		String nodeId = node.id();
		if (nodes.hasNode(nodeId)) {
			throw new DataIntegrityViolationException("Node " + nodeId + " already exists");
		}
	}

	private pt.unl.fct.miei.usmanagement.manager.nodes.Node fromSwarmNode(Node node) {
		com.spotify.docker.client.messages.swarm.ManagerStatus status = node.managerStatus();
		return pt.unl.fct.miei.usmanagement.manager.nodes.Node.builder()
			.nodeId(node.id())
			.publicIpAddress(node.status().addr())
			.availability(NodeAvailability.getNodeAvailability(node.spec().availability()))
			.role(NodeRole.getNodeRole(node.spec().role()))
			.version(node.version().index())
			.state(node.status().state())
			.managerStatus(status == null ? null : new ManagerStatus(status.leader(), status.reachability(), status.addr()))
			.labels(node.spec().labels())
			.build();
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> leaveHost(HostAddress hostAddress) {
		containersService.migrateHostContainers(hostAddress);
		containersService.getSystemContainers(hostAddress).parallelStream()
			.filter(c -> !Objects.equals(c.getServiceName(), DockerApiProxyService.DOCKER_API_PROXY))
			.forEach(c -> containersService.stopContainer(c.getContainerId()));
		List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodes = getHostNodes(hostAddress);
		dockerSwarmService.leaveSwarm(hostAddress);
		dockerApiProxyService.stopDockerApiProxy(hostAddress);
		hostsService.stopBackgroundProcesses(hostAddress);
		nodes.forEach(node -> node.setState("down"));
		this.nodes.saveAll(nodes);
		return nodes;
	}

	public HostAddress getNodeAddress(Node node) {
		String username = node.spec().labels().get(NodeConstants.Label.USERNAME);
		String publicIpAddress = node.status().addr();
		String privateIpAddress = node.spec().labels().get(NodeConstants.Label.PRIVATE_IP_ADDRESS);
		RegionEnum region = RegionEnum.getRegion(node.spec().labels().get(NodeConstants.Label.REGION));
		Gson gson = new Gson();
		Coordinates coordinates = gson.fromJson(node.spec().labels().get(NodeConstants.Label.COORDINATES), Coordinates.class);
		PlaceEnum place = PlaceEnum.getPlace(node.spec().labels().get(NodeConstants.Label.PLACE));
		return new HostAddress(username, publicIpAddress, privateIpAddress, coordinates, region);
	}
}
