package pt.unl.fct.miei.usmanagement.manager.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
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
import pt.unl.fct.miei.usmanagement.manager.management.componenttypes.ComponentTypesService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.operators.OperatorsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.valuemodes.ValueModesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

@Slf4j
@Service
public class WorkerKafkaService {

	private final AppsService appsService;
	private final CloudHostsService cloudHostsService;
	private final ComponentTypesService componentTypesService;
	private final ConditionsService conditionsService;
	private final ContainersService containersService;
	private final DecisionsService decisionsService;
	private final EdgeHostsService edgeHostsService;
	private final ElasticIpsService elasticIpsService;
	private final FieldsService fieldsService;
	private final NodesService nodesService;
	private final OperatorsService operatorsService;
	private final ServicesService servicesService;
	private final HostSimulatedMetricsService hostSimulatedMetricsService;
	private final AppSimulatedMetricsService appSimulatedMetricsService;
	private final ServiceSimulatedMetricsService serviceSimulatedMetricsService;
	private final ContainerSimulatedMetricsService containerSimulatedMetricsService;
	private final HostRulesService hostRulesService;
	private final AppRulesService appRulesService;
	private final ServiceRulesService serviceRulesService;
	private final ContainerRulesService containerRulesService;
	private final ValueModesService valueModesService;

	public WorkerKafkaService(AppsService appsService, CloudHostsService cloudHostsService,
							  ComponentTypesService componentTypesService, ConditionsService conditionsService,
							  ContainersService containersService, DecisionsService decisionsService,
							  EdgeHostsService edgeHostsService, ElasticIpsService elasticIpsService,
							  FieldsService fieldsService, NodesService nodesService, OperatorsService operatorsService,
							  ServicesService servicesService, HostSimulatedMetricsService hostSimulatedMetricsService,
							  AppSimulatedMetricsService appSimulatedMetricsService,
							  ServiceSimulatedMetricsService serviceSimulatedMetricsService,
							  ContainerSimulatedMetricsService containerSimulatedMetricsService,
							  HostRulesService hostRulesService, AppRulesService appRulesService, ServiceRulesService serviceRulesService,
							  ContainerRulesService containerRulesService, ValueModesService valueModesService) {
		this.appsService = appsService;
		this.cloudHostsService = cloudHostsService;
		this.componentTypesService = componentTypesService;
		this.conditionsService = conditionsService;
		this.containersService = containersService;
		this.decisionsService = decisionsService;
		this.edgeHostsService = edgeHostsService;
		this.elasticIpsService = elasticIpsService;
		this.fieldsService = fieldsService;
		this.nodesService = nodesService;
		this.operatorsService = operatorsService;
		this.servicesService = servicesService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.appSimulatedMetricsService = appSimulatedMetricsService;
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
		this.hostRulesService = hostRulesService;
		this.appRulesService = appRulesService;
		this.serviceRulesService = serviceRulesService;
		this.containerRulesService = containerRulesService;
		this.valueModesService = valueModesService;
	}

	@KafkaListener(groupId = "manager-worker", topics = "apps", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppMessage appMessage) {
		log.info("Received key={} message={}", key, appMessage.toString());
		try {
			App app = appMessage.get();
			if (Objects.equal(key, "DELETE")) {
				Long id = app.getId();
				appsService.deleteApp(id);
			}
			else {
				appsService.saveApp(app);
			}
		} catch (Exception e) {
			log.error("Error while processing {}: {}", ToStringBuilder.reflectionToString(appMessage), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "cloud-hosts", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload CloudHostMessage cloudHostMessage) {
		log.info("Received key={} message={}", key, cloudHostMessage.toString());
		CloudHost cloudHost = cloudHostMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = cloudHost.getId();
				cloudHostsService.deleteCloudHost(id);
			}
			else {
				cloudHostsService.saveCloudHost(cloudHost);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(cloudHost), e.getMessage());
		}

	}

	@KafkaListener(groupId = "manager-worker", topics = "component-types", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ComponentTypeMessage componentTypeMessage) {
		log.info("Received key={} message={}", key, ToStringBuilder.reflectionToString(componentTypeMessage));
		ComponentType componentType = componentTypeMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = componentType.getId();
				componentTypesService.deleteComponentType(id);
			}
			else {
				componentTypesService.saveComponentType(componentType);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(componentType), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "conditions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ConditionMessage conditionMessage) {
		log.info("Received key={} message={}", key, ToStringBuilder.reflectionToString(conditionMessage));
		Condition condition = conditionMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = condition.getId();
				conditionsService.deleteCondition(id);
			}
			else {
				conditionsService.saveCondition(condition);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(condition), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "containers", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerMessage containerMessage) {
		log.info("Received key={} message={}", key, containerMessage.toString());
		Container container = containerMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				String id = container.getId();
				containersService.deleteContainer(id);
			}
			else {
				containersService.saveContainer(container);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(container), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "decisions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DecisionMessage decisionMessage) {
		log.info("Received key={} message={}", key, decisionMessage.toString());
		Decision decision = decisionMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = decision.getId();
				decisionsService.deleteDecision(id);
			}
			else {
				decisionsService.saveDecision(decision);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(decision), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "edge-hosts", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload EdgeHostMessage edgeHostMessage) {
		log.info("Received key={} message={}", key, edgeHostMessage.toString());
		EdgeHost edgeHost = edgeHostMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = edgeHost.getId();
				edgeHostsService.deleteEdgeHost(id);
			}
			else {
				edgeHostsService.saveEdgeHost(edgeHost);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(edgeHost), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "eips", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ElasticIpMessage elasticIpMessage) {
		log.info("Received key={} message={}", key, elasticIpMessage.toString());
		ElasticIp elasticIp = elasticIpMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = elasticIp.getId();
				elasticIpsService.deleteElasticIp(id);
			}
			else {
				elasticIpsService.saveElasticIp(elasticIp);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(elasticIp), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "fields", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload FieldMessage fieldMessage) {
		log.info("Received key={} message={}", key, fieldMessage.toString());
		Field field = fieldMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = field.getId();
				fieldsService.deleteField(id);
			}
			else {
				fieldsService.saveField(field);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(field), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "nodes", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload NodeMessage nodeMessage) {
		log.info("Received key={} message={}", key, nodeMessage.toString());
		Node node = nodeMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				String id = node.getId();
				nodesService.deleteNode(id);
			}
			else {
				nodesService.saveNode(node);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(node), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "operators", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload OperatorMessage operatorMessage) {
		log.info("Received key={} message={}", key, operatorMessage.toString());
		Operator operator = operatorMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = operator.getId();
				operatorsService.deleteOperator(id);
			}
			else {
				operatorsService.saveOperator(operator);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(operator), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "services", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceMessage serviceMessage) {
		log.info("Received key={} message={}", key, serviceMessage.toString());
		pt.unl.fct.miei.usmanagement.manager.services.Service service = serviceMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = service.getId();
				servicesService.deleteService(id);
			}
			else {
				servicesService.saveService(service);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(service), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-host-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostSimulatedMetricMessage hostSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, hostSimulatedMetricMessage.toString());
		HostSimulatedMetric hostSimulatedMetric = hostSimulatedMetricMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = hostSimulatedMetric.getId();
				hostSimulatedMetricsService.deleteHostSimulatedMetric(id);
			}
			else {
				hostSimulatedMetricsService.saveHostSimulatedMetric(hostSimulatedMetric);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostSimulatedMetric), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-app-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppSimulatedMetricMessage appSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, appSimulatedMetricMessage.toString());
		AppSimulatedMetric appSimulatedMetric = appSimulatedMetricMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = appSimulatedMetric.getId();
				appSimulatedMetricsService.deleteAppSimulatedMetric(id);
			}
			else {
				appSimulatedMetricsService.saveAppSimulatedMetric(appSimulatedMetric);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(appSimulatedMetric), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-service-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceSimulatedMetricMessage serviceSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, serviceSimulatedMetricMessage.toString());
		ServiceSimulatedMetric serviceSimulatedMetric = serviceSimulatedMetricMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = serviceSimulatedMetric.getId();
				serviceSimulatedMetricsService.deleteServiceSimulatedMetric(id);
			}
			else {
				serviceSimulatedMetricsService.saveServiceSimulatedMetric(serviceSimulatedMetric);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceSimulatedMetric), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "simulated-container-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerSimulatedMetricMessage containerSimulatedMetricMessage) {
		log.info("Received key={} message={}", key, containerSimulatedMetricMessage.toString());
		ContainerSimulatedMetric containerSimulatedMetric = containerSimulatedMetricMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = containerSimulatedMetric.getId();
				containerSimulatedMetricsService.deleteContainerSimulatedMetric(id);
			}
			else {
				containerSimulatedMetricsService.saveContainerSimulatedMetric(containerSimulatedMetric);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(containerSimulatedMetric), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "host-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostRuleMessage hostRuleMessage) {
		log.info("Received key={} message={}", key, hostRuleMessage.toString());
		HostRule hostRule = hostRuleMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = hostRule.getId();
				hostRulesService.deleteRule(id);
			}
			else {
				hostRulesService.saveRule(hostRule);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(hostRule), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "app-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppRuleMessage appRuleMessage) {
		log.info("Received key={} message={}", key, appRuleMessage.toString());
		AppRule appRule = appRuleMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = appRule.getId();
				appRulesService.deleteRule(id);
			}
			else {
				appRulesService.saveRule(appRule);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(appRule), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "service-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceRuleMessage serviceRuleMessage) {
		log.info("Received key={} message={}", key, serviceRuleMessage.toString());
		ServiceRule serviceRule = serviceRuleMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = serviceRule.getId();
				serviceRulesService.deleteRule(id);
			}
			else {
				serviceRulesService.saveRule(serviceRule);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(serviceRule), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "container-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerRuleMessage containerRuleMessage) {
		log.info("Received key={} message={}", key, containerRuleMessage.toString());
		ContainerRule containerRule = containerRuleMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = containerRule.getId();
				containerRulesService.deleteRule(id);
			}
			else {
				containerRulesService.saveRule(containerRule);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(containerRule), e.getMessage());
		}
	}

	@KafkaListener(groupId = "manager-worker", topics = "value-modes", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ValueModeMessage valueModeMessage) {
		log.info("Received key={} message={}", key, valueModeMessage.toString());
		ValueMode valueMode = valueModeMessage.get();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = valueMode.getId();
				valueModesService.deleteValueMode(id);
			}
			else {
				valueModesService.saveValueMode(valueMode);
			}
		} catch (Exception e) {
			log.error("Error while saving {}: {}", ToStringBuilder.reflectionToString(valueMode), e.getMessage());
		}
	}

}
