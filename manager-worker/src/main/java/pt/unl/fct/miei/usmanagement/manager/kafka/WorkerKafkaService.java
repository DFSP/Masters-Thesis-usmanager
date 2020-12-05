package pt.unl.fct.miei.usmanagement.manager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.AppMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.AppRuleMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.AppSimulatedMetricMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.CloudHostMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ComponentTypeMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ConditionMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ContainerMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ContainerRuleMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ContainerSimulatedMetricMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.DecisionMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.EdgeHostMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ElasticIpMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.FieldMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.HostRuleMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.HostSimulatedMetricMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.NodeMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.OperatorMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ServiceMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ServiceRuleMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ServiceSimulatedMetricMessage;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.ValueModeMessage;

@Slf4j
@Service
public class WorkerKafkaService {


	@KafkaListener(groupId = "manager-worker", topics = "apps", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppMessage appMessage) {
		log.info("Received key={} message={}", key, appMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "cloud-hosts", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload CloudHostMessage cloudHostMessage) {
		log.info("Received key={} message={}", key, cloudHostMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "component-types", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ComponentTypeMessage componentTypeMessage) {
		log.info("Received key={} message={}", key, componentTypeMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "conditions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ConditionMessage conditionMessage) {
		log.info("Received key={} message={}", key, conditionMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "containers", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerMessage containerMessage) {
		log.info("Received key={} message={}", key, containerMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "decisions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DecisionMessage decisionMessage) {
		log.info("Received key={} message={}", key, decisionMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "edge-hosts", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload EdgeHostMessage edgeHostMessage) {
		log.info("Received key={} message={}", key, edgeHostMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "eips", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ElasticIpMessage elasticIpMessage) {
		log.info("Received key={} message={}", key, elasticIpMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "fields", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload FieldMessage fieldMessage) {
		log.info("Received key={} message={}", key, fieldMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "nodes", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload NodeMessage nodeMessage) {
		log.info("Received key={} message={}", key, nodeMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "operators", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload OperatorMessage operatorMessage) {
		log.info("Received key={} message={}", key, operatorMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "services", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceMessage serviceMessage) {
		log.info("Received key={} message={}", key, serviceMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-host-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostSimulatedMetricMessage hostSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, hostSimulatedMetricMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-app-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppSimulatedMetricMessage appSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, appSimulatedMetricMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-service-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceSimulatedMetricMessage serviceSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, serviceSimulatedMetricMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-container-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerSimulatedMetricMessage containerSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, containerSimulatedMetricMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "host-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostRuleMessage hostRuleMessage) {
		log.info("Received key={} message={}", key, hostRuleMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "app-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppRuleMessage appRuleMessage) {
		log.info("Received key={} message={}", key, appRuleMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "service-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceRuleMessage serviceRuleMessage) {
		log.info("Received key={} message={}", key, serviceRuleMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "container-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerRuleMessage containerRuleMessage) {
		log.info("Received key={} message={}", key, containerRuleMessage.toString());
	}

	@KafkaListener(groupId = "manager-worker", topics = "value-modes", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ValueModeMessage valueModeMessage) {
		log.info("Received key={} message={}", key, valueModeMessage.toString());
	}

}
