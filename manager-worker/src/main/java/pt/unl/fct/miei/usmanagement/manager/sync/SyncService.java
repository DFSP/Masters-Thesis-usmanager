package pt.unl.fct.miei.usmanagement.manager.sync;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.services.configurations.ConfigurationsService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.services.docker.containers.DockerContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.nodes.ManagerStatus;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeAvailability;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.aws.AwsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.aws.AwsSimpleInstance;

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

	private final static int CLOUD_HOSTS_DATABASE_SYNC_INTERVAL = 45000;
	private final static int CONTAINERS_DATABASE_SYNC_INTERVAL = 10000;
	private final static int NODES_DATABASE_SYNC_INTERVAL = 10000;

	private final CloudHostsService cloudHostsService;
	private final AwsService awsService;
	private final ContainersService containersService;
	private final DockerContainersService dockerContainersService;
	private final NodesService nodesService;
	private final DockerSwarmService dockerSwarmService;
	private final ConfigurationsService configurationsService;

	private Timer cloudHostsDatabaseSyncTimer;
	private Timer containersDatabaseSyncTimer;
	private Timer nodesDatabaseSyncTimer;
	private final String externalId;

	public SyncService(CloudHostsService cloudHostsService, AwsService awsService, ContainersService containersService,
					   DockerContainersService dockerContainersService,
					   NodesService nodesService, DockerSwarmService dockerSwarmService,
					   ConfigurationsService configurationsService, Environment environment) {
		this.cloudHostsService = cloudHostsService;
		this.awsService = awsService;
		this.containersService = containersService;
		this.dockerContainersService = dockerContainersService;
		this.nodesService = nodesService;
		this.dockerSwarmService = dockerSwarmService;
		this.configurationsService = configurationsService;
		this.externalId = environment.getProperty(ContainerConstants.Environment.Manager.ID);
	}

	public void startCloudHostsDatabaseSynchronization() {
		cloudHostsDatabaseSyncTimer = new Timer("cloud-hosts-database-synchronization", true);
		cloudHostsDatabaseSyncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronizeCloudHostsDatabase();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, CLOUD_HOSTS_DATABASE_SYNC_INTERVAL, CLOUD_HOSTS_DATABASE_SYNC_INTERVAL);
	}

	public void stopCloudHostsDatabaseSynchronization() {
		if (cloudHostsDatabaseSyncTimer != null) {
			cloudHostsDatabaseSyncTimer.cancel();
			log.info("Stopped database cloud hosts synchronization");
		}
	}

	public List<CloudHost> synchronizeCloudHostsDatabase() {
		log.info("Synchronizing cloud hosts data with amazon");
		List<CloudHost> cloudHosts = cloudHostsService.getCloudHostsAndRelations();
		List<Instance> awsInstances = awsService.getInstances();
		Map<String, Instance> awsInstancesIds = awsInstances.stream()
			.collect(Collectors.toMap(Instance::getInstanceId, instance -> instance));
		Iterator<CloudHost> cloudHostsIterator = cloudHosts.iterator();
		// Remove invalid and update cloud host entities
		while (cloudHostsIterator.hasNext()) {
			CloudHost cloudHost = cloudHostsIterator.next();
			String instanceId = cloudHost.getInstanceId();
			if (configurationsService.isConfiguring(instanceId)) {
				log.info("Instance {} is currently being configured, skipping", instanceId);
				continue;
			}
			if (!awsInstancesIds.containsKey(instanceId)) {
				cloudHostsService.deleteCloudHost(cloudHost);
				cloudHostsIterator.remove();
				log.info("Removing invalid cloud host {}", instanceId);
			}
			else {
				Instance instance = awsInstancesIds.get(instanceId);
				InstanceState currentState = instance.getState();
				InstanceState savedState = cloudHost.getState();
				if (Objects.equals(currentState.getCode(), AwsInstanceState.TERMINATED.getCode())) {
					cloudHostsService.deleteCloudHost(cloudHost);
					log.info("Removing terminated cloud host {}", instanceId);
				}
				else {
					if (!Objects.equals(currentState, savedState)) {
						log.info("Updating state of cloud host {}", cloudHost.getInstanceId());
						cloudHost.setState(currentState);
						cloudHostsService.updateCloudHost(cloudHost);
					}
					String currentPublicIpAddress = instance.getPublicIpAddress();
					String savedPublicIpAddress = cloudHost.getPublicIpAddress();
					if (!Objects.equals(currentPublicIpAddress, savedPublicIpAddress)) {
						log.info("Updating public ip address of cloud host {}", cloudHost.getInstanceId());
						cloudHostsService.updateAddress(cloudHost, currentPublicIpAddress);
					}
				}

			}
		}
		// Add missing cloud host entities
		awsInstances.forEach(instance -> {
			String instanceId = instance.getInstanceId();
			if (!configurationsService.isConfiguring(instanceId) && instance.getState().getCode() != AwsInstanceState.TERMINATED.getCode()
				&& !cloudHostsService.hasCloudHost(instanceId)) {
				CloudHost cloudHost = cloudHostsService.addCloudHostFromSimpleInstance(new AwsSimpleInstance(instance));
				cloudHosts.add(cloudHost);
			}
		});
		log.debug("Finished cloud hosts synchronization");
		return cloudHosts;
	}

	public void startContainersDatabaseSynchronization() {
		containersDatabaseSyncTimer = new Timer("containers-database-synchronization", true);
		containersDatabaseSyncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronizeContainersDatabase();
				}
				catch (Exception e) {
					log.error("Error while synchronizing containers database: {}", e.getMessage());
				}
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
		Map<String, DockerContainer> dockerContainerIds = dockerContainers.stream().distinct().collect(Collectors.toMap(DockerContainer::getId, container -> container));
		Iterator<Container> containerIterator = containers.iterator();
		while (containerIterator.hasNext()) {
			Container container = containerIterator.next();
			String containerId = container.getId();
			if (configurationsService.isConfiguring(containerId)) {
				continue;
			}
			String managerId = container.getManagerId();
			if (!Objects.equals(managerId, externalId)) {
				continue;
			}
			if (!dockerContainerIds.containsKey(containerId)) {
				containersService.deleteContainer(containerId);
				containerIterator.remove();
				log.info("Removed invalid container {}", containerId);
			}
			else {
				boolean updated = false;
				DockerContainer dockerContainer = dockerContainerIds.get(containerId);
				if (dockerContainer == null) {
					continue;
				}
				String currentPublicIpAddress = dockerContainer.getHostAddress().getPublicIpAddress();
				String savedPublicIpAddress = container.getHostAddress().getPublicIpAddress();
				if (!Objects.equals(currentPublicIpAddress, savedPublicIpAddress)) {
					container.setPublicIpAddress(currentPublicIpAddress);
					log.info("Synchronized container {} public ip address from {} to {}", containerId, savedPublicIpAddress, currentPublicIpAddress);
					updated = true;
				}
				if (updated) {
					containersService.updateContainer(container);
				}
			}
		}

		log.debug("Finished containers synchronization");
		return containers;
	}

	public void startNodesDatabaseSynchronization() {
		nodesDatabaseSyncTimer = new Timer("nodes-database-synchronization", true);
		nodesDatabaseSyncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronizeNodesDatabase();
				}
				catch (Exception e) {
					log.error("Error while synchronizing nodes database: {}", e.getMessage());
				}
			}
		}, NODES_DATABASE_SYNC_INTERVAL, NODES_DATABASE_SYNC_INTERVAL);
	}

	public void stopNodesDatabaseSynchronization() {
		if (nodesDatabaseSyncTimer != null) {
			nodesDatabaseSyncTimer.cancel();
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
			if (!nodeIds.contains(nodeId) && !swarmNode.status().state().equalsIgnoreCase("down")
					&& swarmNode.spec().labels().size() > 0) {
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
			String managerId = node.getManagerId();
			if (!Objects.equals(managerId, externalId)) {
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
				if (swarmNode == null) {
					continue;
				}
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
				String currentPublicIpAddress = swarmNode.status().addr();
				String savedPublicIpAddress = node.getPublicIpAddress();
				if (!Objects.equals(currentPublicIpAddress, savedPublicIpAddress)) {
					node.setPublicIpAddress(currentPublicIpAddress);
					log.info("Synchronized node {} public ip address from {} to {}", nodeId, savedPublicIpAddress, currentPublicIpAddress);
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

