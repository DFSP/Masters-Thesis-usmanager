package pt.unl.fct.miei.usmanagement.manager.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.CloudHostDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HeartbeatDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostEventDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.NodeDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceEventDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.WorkerManagerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CloudHostMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ContainerMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HeartbeatMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostDecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostEventMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostMonitoringLogMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.NodeMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceDecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceEventMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMonitoringLogMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.WorkerManagerMapper;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeat;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaTopicKey;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.heartbeats.HeartbeatService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagersService;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class MasterKafkaService {

	private final HostsEventsService hostsEventsService;
	private final ServicesEventsService servicesEventsService;
	private final HostsMonitoringService hostsMonitoringService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final DecisionsService decisionsService;
	private final HeartbeatService heartbeatService;
	private final ContainersService containersService;
	private final NodesService nodesService;
	private final CloudHostsService cloudHostsService;
	private final WorkerManagersService workerManagersService;

	private final CycleAvoidingMappingContext context;

	public MasterKafkaService(HostsEventsService hostsEventsService, ServicesEventsService servicesEventsService,
							  HostsMonitoringService hostsMonitoringService, ServicesMonitoringService servicesMonitoringService,
							  DecisionsService decisionsService, HeartbeatService heartbeatService,
							  ContainersService containersService, NodesService nodesService, CloudHostsService cloudHostsService,
							  WorkerManagersService workerManagersService) {
		this.hostsEventsService = hostsEventsService;
		this.servicesEventsService = servicesEventsService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.decisionsService = decisionsService;
		this.heartbeatService = heartbeatService;
		this.containersService = containersService;
		this.nodesService = nodesService;
		this.cloudHostsService = cloudHostsService;
		this.workerManagersService = workerManagersService;
		this.context = new CycleAvoidingMappingContext();
	}

	@KafkaListener(topics = "containers", autoStartup = "false")
	public void listenContainers(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ContainerDTO> containerDTOs) {
		int i = 0;
		for (ContainerDTO containerDTO : containerDTOs) {
			KafkaTopicKey key = keys.get(i++);
			if (key != null && key.getManagerId() != null && key.getManagerId().equalsIgnoreCase("manager-master")) {
				continue;
			}
			log.debug("Received key={} message={}", key, containerDTO);
			Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					String id = container.getId();
					containersService.deleteContainer(id);
				}
				else {
					containersService.addOrUpdateContainer(container);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic containers with key={} and message={}: {}", key, containerDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "nodes", autoStartup = "false")
	public void listenNodes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<NodeDTO> nodeDTOs) {
		int i = 0;
		for (NodeDTO nodeDTO : nodeDTOs) {
			String managerId = nodeDTO.getManagerId();
			if (managerId == null || managerId.equalsIgnoreCase("manager-master")) {
				i++;
				continue;
			}
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, nodeDTO.toString());
			Node node = NodeMapper.MAPPER.toNode(nodeDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					String id = node.getId();
					nodesService.deleteNode(id);
				}
				else {
					nodesService.addOrUpdateNode(node);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic nodes with key={} and message={}: {}", key, nodeDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = org.hibernate.exception.ConstraintViolationException.class)
	@KafkaListener(topics = "cloud-hosts", autoStartup = "false")
	public void listenCloudHosts(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<CloudHostDTO> cloudHostDTOs) {
		int i = 0;
		for (CloudHostDTO cloudHostDTO : cloudHostDTOs) {
			WorkerManagerDTO workerManagerDTO = cloudHostDTO.getManagedByWorker();
			if (workerManagerDTO == null) {
				i++;
				continue;
			}
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, cloudHostDTO.toString());
			CloudHost cloudHost = CloudHostMapper.MAPPER.toCloudHost(cloudHostDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = cloudHost.getId();
					cloudHostsService.deleteCloudHost(id);
				}
				else {
					if (cloudHostDTO.getManagedByWorker() != null) {
						workerManagersService.addIfNotPresent(WorkerManagerMapper.MAPPER.toWorkerManager(cloudHostDTO.getManagedByWorker(), context));
					}
					cloudHostsService.addOrUpdateCloudHost(cloudHost);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic cloud-hosts with key={} and message={}: {}", key, cloudHostDTO.toString(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@KafkaListener(topics = "host-events", autoStartup = "false")
	public void listenHostEvents(List<HostEventDTO> hostEventDTOs) {
		for (HostEventDTO hostEventDTO : hostEventDTOs) {
			log.debug("Received message={}", hostEventDTO);
			HostEvent hostEvent = HostEventMapper.MAPPER.toHostEvent(hostEventDTO, context);
			try {
				hostsEventsService.addHostEvent(hostEvent);
			}
			catch (Exception e) {
				log.error("Error while processing topic host-events with message {}: {}", hostEventDTO, e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "service-events", autoStartup = "false")
	public void listenServiceEvents(List<ServiceEventDTO> serviceEventDTOs) {
		for (ServiceEventDTO serviceEventDTO : serviceEventDTOs) {
			log.debug("Received message={}", serviceEventDTO);
			ServiceEvent serviceEvent = ServiceEventMapper.MAPPER.toServiceEvent(serviceEventDTO, context);
			try {
				servicesEventsService.addServiceEvent(serviceEvent);
			}
			catch (Exception e) {
				log.error("Error while processing topic service-events with message {}: {}", serviceEventDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@KafkaListener(topics = "host-monitoring-logs", autoStartup = "false")
	public void listenHostMonitoringLogs(List<HostMonitoringLogDTO> hostMonitoringLogDTOs) {
		for (HostMonitoringLogDTO hostMonitoringLogDTO : hostMonitoringLogDTOs) {
			log.debug("Received message={}", hostMonitoringLogDTO);
			HostMonitoringLog hostMonitoringLog = HostMonitoringLogMapper.MAPPER.toHostMonitoringLog(hostMonitoringLogDTO, context);
			try {
				hostsMonitoringService.addHostMonitoringLog(hostMonitoringLog);
			}
			catch (Exception e) {
				log.error("Error while processing topic host-monitoring-logs with message {}: {}", hostMonitoringLogDTO, e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "service-monitoring-logs", autoStartup = "false")
	public void listenServiceMonitoringLogs(List<ServiceMonitoringLogDTO> serviceMonitoringLogDTOs) {
		for (ServiceMonitoringLogDTO serviceMonitoringLogDTO : serviceMonitoringLogDTOs) {
			log.debug("Received message={}", serviceMonitoringLogDTO);
			ServiceMonitoringLog serviceMonitoringLog = ServiceMonitoringLogMapper.MAPPER.toServiceMonitoringLog(serviceMonitoringLogDTO, context);
			try {
				servicesMonitoringService.addServiceMonitoringLog(serviceMonitoringLog);
			}
			catch (Exception e) {
				log.error("Error while processing topic service-monitoring-logs with message {}: {}", serviceMonitoringLogDTO, e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "host-decisions", autoStartup = "false")
	public void listenHostDecisions(List<HostDecisionDTO> hostDecisionDTOs) {
		for (HostDecisionDTO hostDecisionDTO : hostDecisionDTOs) {
			log.debug("Received value={}", hostDecisionDTO);
			HostDecision hostDecision = HostDecisionMapper.MAPPER.toHostDecision(hostDecisionDTO, context);
			try {
				decisionsService.saveHostDecision(hostDecision);
			}
			catch (Exception e) {
				log.error("Error while processing topic host-decisions with message {}: {}", hostDecisionDTO, e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "service-decisions", autoStartup = "false")
	public void listenServiceDecisions(List<ServiceDecisionDTO> serviceDecisionDTOs) {
		for (ServiceDecisionDTO serviceDecisionDTO : serviceDecisionDTOs) {
			log.debug("Received message={}", serviceDecisionDTO);
			ServiceDecision serviceDecision = ServiceDecisionMapper.MAPPER.toServiceDecision(serviceDecisionDTO, context);
			try {
				decisionsService.saveServiceDecision(serviceDecision);
			}
			catch (Exception e) {
				log.error("Error while processing topic service-decisions with message {}: {}", serviceDecisionDTO, e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "heartbeats", autoStartup = "false")
	public void listenWorkerHeartbeats(List<HeartbeatDTO> heartbeatDTOs) {
		for (HeartbeatDTO heartbeatDTO : heartbeatDTOs) {
			log.debug("Received message={}", heartbeatDTOs);
			Heartbeat heartbeat = HeartbeatMapper.MAPPER.toHeartbeat(heartbeatDTO, context);
			try {
				heartbeatService.saveWorkerHeartbeat(heartbeat);
			}
			catch (Exception e) {
				log.error("Error while processing topic heartbeats with message {}: {}", heartbeatDTO, e.getMessage());
			}
		}
	}

}
