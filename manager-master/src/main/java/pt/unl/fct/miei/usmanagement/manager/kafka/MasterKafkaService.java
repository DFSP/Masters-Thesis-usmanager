package pt.unl.fct.miei.usmanagement.manager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostEventDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceEventDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceMonitoringLogDTO;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;

@Slf4j
@Service
public class MasterKafkaService {

	private final HostsEventsService hostsEventsService;
	private final ServicesEventsService servicesEventsService;
	private final HostsMonitoringService hostsMonitoringService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final DecisionsService decisionsService;

	public MasterKafkaService(HostsEventsService hostsEventsService, ServicesEventsService servicesEventsService,
							  HostsMonitoringService hostsMonitoringService, ServicesMonitoringService servicesMonitoringService,
							  DecisionsService decisionsService) {
		this.hostsEventsService = hostsEventsService;
		this.servicesEventsService = servicesEventsService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.decisionsService = decisionsService;
	}

	@KafkaListener(groupId = "manager-master", topics = "host-events", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostEventDTO hostEventMessage) {
		log.info("Received key={} value={}", key, hostEventMessage.toString());
		HostEvent hostEvent = hostEventMessage.get();
		try {
			hostsEventsService.addHostEvent(hostEvent);
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostEvent), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-master", topics = "service-events", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceEventDTO serviceEventMessage) {
		log.info("Received key={} value={}", key, serviceEventMessage.toString());
		ServiceEvent serviceEvent = serviceEventMessage.toEntity();
		try {
			servicesEventsService.addServiceEvent(serviceEvent);
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceEvent), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-master", topics = "host-monitoring-logs", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostMonitoringLogDTO hostMonitoringLogMessage) {
		log.info("Received key={} value={}", key, hostMonitoringLogMessage.toString());
		HostMonitoringLog hostMonitoringLog = hostMonitoringLogMessage.get();
		try {
			hostsMonitoringService.addHostMonitoringLog(hostMonitoringLog);
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostMonitoringLog), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-master", topics = "service-monitoring-logs", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceMonitoringLogDTO serviceMonitoringLogMessage) {
		log.info("Received key={} value={}", key, serviceMonitoringLogMessage.toString());
		ServiceMonitoringLog serviceMonitoringLog = serviceMonitoringLogMessage.get();
		try {
			servicesMonitoringService.addServiceMonitoringLog(serviceMonitoringLog);
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceMonitoringLog), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-master", topics = "host-decisions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostDecisionDTO hostDecisionMessage) {
		log.info("Received key={} value={}", key, hostDecisionMessage.toString());
		HostDecision hostDecision = hostDecisionMessage.get();
		try {
			decisionsService.saveHostDecision(hostDecision);
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostDecision), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-master", topics = "service-decisions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceDecisionDTO serviceDecisionMessage) {
		log.info("Received key={} value={}", key, serviceDecisionMessage.toString());
		ServiceDecision serviceDecision = serviceDecisionMessage.get();
		try {
			decisionsService.saveServiceDecision(serviceDecision);
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceDecision), e.getMessage());
		}
	}

}