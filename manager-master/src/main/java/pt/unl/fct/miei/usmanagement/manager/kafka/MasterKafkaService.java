package pt.unl.fct.miei.usmanagement.manager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostEventDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceEventDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostDecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostEventMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostMonitoringLogMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceDecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceEventMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMonitoringLogMapper;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;

import java.util.List;

@Slf4j
@Service
public class MasterKafkaService {

	private final HostsEventsService hostsEventsService;
	private final ServicesEventsService servicesEventsService;
	private final HostsMonitoringService hostsMonitoringService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final DecisionsService decisionsService;

	private final CycleAvoidingMappingContext context;

	public MasterKafkaService(HostsEventsService hostsEventsService, ServicesEventsService servicesEventsService,
							  HostsMonitoringService hostsMonitoringService, ServicesMonitoringService servicesMonitoringService,
							  DecisionsService decisionsService) {
		this.hostsEventsService = hostsEventsService;
		this.servicesEventsService = servicesEventsService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.decisionsService = decisionsService;
		this.context = new CycleAvoidingMappingContext();
	}

	@KafkaListener(topics = "host-events", autoStartup = "false")
	public void listenHostEvents(List<HostEventDTO> hostEventDTOs) {
		for (HostEventDTO hostEventDTO : hostEventDTOs) {
			log.debug("Received message={}", hostEventDTO.toString());
			HostEvent hostEvent = HostEventMapper.MAPPER.toHostEvent(hostEventDTO, context);
			try {
				hostsEventsService.addHostEvent(hostEvent);
			}
			catch (Exception e) {
				log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostEvent), e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "service-events", autoStartup = "false")
	public void listenServiceEvents(List<ServiceEventDTO> serviceEventDTOs) {
		for (ServiceEventDTO serviceEventDTO : serviceEventDTOs) {
			log.debug("Received message={}", serviceEventDTO.toString());
			ServiceEvent serviceEvent = ServiceEventMapper.MAPPER.toServiceEvent(serviceEventDTO, context);
			try {
				servicesEventsService.addServiceEvent(serviceEvent);
			}
			catch (Exception e) {
				log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceEvent), e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "host-monitoring-logs", autoStartup = "false")
	public void listenHostMonitoringLogs(List<HostMonitoringLogDTO> hostMonitoringLogDTOs) {
		for (HostMonitoringLogDTO hostMonitoringLogDTO : hostMonitoringLogDTOs) {
			log.debug("Received message={}", hostMonitoringLogDTO.toString());
			HostMonitoringLog hostMonitoringLog = HostMonitoringLogMapper.MAPPER.toHostMonitoringLog(hostMonitoringLogDTO, context);
			try {
				hostsMonitoringService.addHostMonitoringLog(hostMonitoringLog);
			}
			catch (Exception e) {
				log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostMonitoringLog), e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "service-monitoring-logs", autoStartup = "false")
	public void listenServiceMonitoringLogs(List<ServiceMonitoringLogDTO> serviceMonitoringLogDTOs) {
		for (ServiceMonitoringLogDTO serviceMonitoringLogDTO : serviceMonitoringLogDTOs) {
			log.debug("Received message={}", serviceMonitoringLogDTO.toString());
			ServiceMonitoringLog serviceMonitoringLog = ServiceMonitoringLogMapper.MAPPER.toServiceMonitoringLog(serviceMonitoringLogDTO, context);
			try {
				servicesMonitoringService.addServiceMonitoringLog(serviceMonitoringLog);
			}
			catch (Exception e) {
				log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceMonitoringLog), e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "host-decisions", autoStartup = "false")
	public void listenHostDecisions(List<HostDecisionDTO> hostDecisionDTOs) {
		for (HostDecisionDTO hostDecisionDTO : hostDecisionDTOs) {
			log.debug("Received value={}", hostDecisionDTO.toString());
			HostDecision hostDecision = HostDecisionMapper.MAPPER.toHostDecision(hostDecisionDTO, context);
			try {
				decisionsService.saveHostDecision(hostDecision);
			}
			catch (Exception e) {
				log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostDecision), e.getMessage());
			}
		}
	}

	@KafkaListener(topics = "service-decisions", autoStartup = "false")
	public void listenServiceDecisions(List<ServiceDecisionDTO> serviceDecisionDTOs) {
		for (ServiceDecisionDTO serviceDecisionDTO : serviceDecisionDTOs) {
			log.debug("Received message={}", serviceDecisionDTO.toString());
			ServiceDecision serviceDecision = ServiceDecisionMapper.MAPPER.toServiceDecision(serviceDecisionDTO, context);
			try {
				decisionsService.saveServiceDecision(serviceDecision);
			}
			catch (Exception e) {
				log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceDecision), e.getMessage());
			}
		}
	}

}
