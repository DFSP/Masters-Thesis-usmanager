package pt.unl.fct.miei.usmanagement.manager.sync;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.management.configurations.ConfigurationsService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsSimpleInstance;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncService {

	private final static int CONTAINERS_DATABASE_SYNC_INTERVAL = 10000;
	private final static int NODES_DATABASE_SYNC_INTERVAL = 10000;

	private final ContainersService containersService;
	private final DockerContainersService dockerContainersService;
	private final NodesService nodesService;
	private final DockerSwarmService dockerSwarmService;
	private final ConfigurationsService configurationsService;

	private Timer containersDatabaseSyncTimer;
	private Timer nodesDatabseSyncTimer;

	public SyncService(ContainersService containersService, DockerContainersService dockerContainersService,
					   NodesService nodesService, DockerSwarmService dockerSwarmService,
					   ConfigurationsService configurationsService) {
		this.containersService = containersService;
		this.dockerContainersService = dockerContainersService;
		this.nodesService = nodesService;
		this.dockerSwarmService = dockerSwarmService;
		this.configurationsService = configurationsService;
	}

	public void startContainersDatabaseSynchronization() {
		containersDatabaseSyncTimer = new Timer("containers-database-synchronization", true);
		containersDatabaseSyncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronizeContainersDatabase();
				}
				catch (ManagerException ignored) { }
			}
		}, CONTAINERS_DATABASE_SYNC_INTERVAL, CONTAINERS_DATABASE_SYNC_INTERVAL);
	}

	public void stopContainersDatabaseSynchronization() {
		if (containersDatabaseSyncTimer != null) {
			containersDatabaseSyncTimer.cancel();
			log.info("Stopped containers database synchronization");
		}
	}

	public List<Container> synchronizeContainersDatabase() {
		log.debug("Synchronizing containers database with docker swarm");

		List<Container> containers = containersService.getContainers();
		List<DockerContainer> dockerContainers = dockerContainersService.getContainers();

		// Add missing containers
		List<String> containerIds = containers.stream().map(Container::getId).collect(Collectors.toList());
		for (DockerContainer dockerContainer : dockerContainers) {
			String containerId = dockerContainer.getId();
			if (configurationsService.isConfiguring(containerId)) {
				log.debug("Container {} is currently being configured, skipping", containerId);
				continue;
			}
			if (!containerIds.contains(containerId)) {
				containersService.addContainerFromDockerContainer(dockerContainer);
				log.info("Added missing container {} to the database", containerId);
			}
		}

		// Remove invalid containers
		List<String> dockerContainerIds = dockerContainers.stream().map(DockerContainer::getId).collect(Collectors.toList());
		Iterator<Container> containerIterator = containers.iterator();
		while (containerIterator.hasNext()) {
			Container container = containerIterator.next();
			String containerId = container.getId();
			if (configurationsService.isConfiguring(containerId)) {
				continue;
			}
			if (!dockerContainerIds.contains(containerId)) {
				containersService.deleteContainer(containerId);
				containerIterator.remove();
				log.info("Removed invalid container {}", containerId);
			}
		}

		log.debug("Finished containers synchronization");
		return containers;
	}

	public void startNodesDatabaseSynchronization() {
		nodesDatabseSyncTimer = new Timer("nodes-database-synchronization", true);
		nodesDatabseSyncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronizeNodesDatabase();
				}
				catch (ManagerException ignored) { }
			}
		}, NODES_DATABASE_SYNC_INTERVAL, NODES_DATABASE_SYNC_INTERVAL);
	}

	public void stopNodesDatabaseSynchronization() {
		if (nodesDatabseSyncTimer != null) {
			nodesDatabseSyncTimer.cancel();
			log.info("Stopped nodes database synchronization");
		}
	}

	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> synchronizeNodesDatabase() {
		log.debug("Synchronizing nodes database with docker swarm");

		List<Node> swarmNodes = dockerSwarmService.getNodes();
		List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodes = nodesService.getNodes();

		// Add missing nodes
		List<String> nodeIds = nodes.stream().map(pt.unl.fct.miei.usmanagement.manager.nodes.Node::getId).collect(Collectors.toList());
		for (Node swarmNode : swarmNodes) {
			String nodeId = swarmNode.id();
			if (configurationsService.isConfiguring(nodeId)) {
				log.debug("Node {} is currently being configured, skipping", nodeId);
				continue;
			}
			if (!nodeIds.contains(nodeId)) {
				nodesService.addNode(swarmNode);
				log.info("Added missing node {} to the database", nodeId);
			}
		}

		// Remove invalid node entities and update existing ones
		Map<String, Node> swarmNodesIds = swarmNodes.stream().collect(Collectors.toMap(Node::id, node -> node));
		Iterator<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodesIterator = nodes.iterator();
		while (nodesIterator.hasNext()) {
			pt.unl.fct.miei.usmanagement.manager.nodes.Node node = nodesIterator.next();
			String nodeId = node.getId();
			if (configurationsService.isConfiguring(nodeId)) {
				continue;
			}
			if (!swarmNodesIds.containsKey(nodeId)) {
				nodesService.deleteNode(nodeId);
				nodesIterator.remove();
				log.info("Removed invalid node {}", nodeId);
			}
			else {
				boolean updated = false;
				Node swarmNode = swarmNodesIds.get(nodeId);
				NodeAvailability savedAvailability = node.getAvailability();
				NodeAvailability currentAvailability = NodeAvailability.getNodeAvailability(swarmNode.spec().availability());
				if (currentAvailability != savedAvailability) {
					node.setAvailability(currentAvailability);
					log.info("Synchronized node {} availability from {} to {}", nodeId, savedAvailability, currentAvailability);
					updated = true;
				}
				ManagerStatus savedManagerStatus = node.getManagerStatus();
				com.spotify.docker.client.messages.swarm.ManagerStatus swarmNodeStatus = swarmNode.managerStatus();
				ManagerStatus currentManagerStatus = swarmNodeStatus == null ? null :
					new ManagerStatus(swarmNodeStatus.leader(), swarmNodeStatus.reachability(), swarmNodeStatus.addr());
				if (!Objects.equals(currentManagerStatus, savedManagerStatus)) {
					node.setManagerStatus(currentManagerStatus);
					log.info("Synchronized node {} manager status from {} to {}", nodeId, savedManagerStatus, currentManagerStatus);
					updated = true;
				}
				String savedState = node.getState();
				String currentState = swarmNode.status().state();
				if (!currentState.equalsIgnoreCase(savedState)) {
					node.setState(currentState);
					log.info("Synchronized node {} state from {} to {}", nodeId, savedState, currentState);
					updated = true;
				}
				if (updated) {
					nodesService.updateNode(node);
				}
			}
		}

		log.debug("Finished nodes synchronization");
		return nodes;
	}

}

