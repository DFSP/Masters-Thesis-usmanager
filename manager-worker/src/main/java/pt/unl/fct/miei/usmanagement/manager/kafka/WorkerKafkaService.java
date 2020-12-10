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
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
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
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.RuleConditionsService;
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
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.Set;

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
	private final RuleConditionsService ruleConditionsService;

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
							  ContainerRulesService containerRulesService, ValueModesService valueModesService, RuleConditionsService ruleConditionsService) {
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
		this.ruleConditionsService = ruleConditionsService;
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "apps", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppMessage appMessage) {
		log.info("Received key={} message={}", key, appMessage.toString());
		try {
			App app = appMessage.getEntity();
			if (Objects.equal(key, "DELETE")) {
				Long id = app.getId();
				appsService.deleteApp(id);
			}
			else {
				Set<AppService> appServices = app.getAppServices();
				appServices.forEach(appService -> {
					pt.unl.fct.miei.usmanagement.manager.services.Service service = appService.getService();
					if (!servicesService.hasService(service.getServiceName())) {
						log.info("Saving service {}", ToStringBuilder.reflectionToString(service));
						servicesService.addOrUpdateService(service);
					}
				});
				appsService.addOrUpdateApp(app);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic apps with message {}: {}", ToStringBuilder.reflectionToString(appMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)(noRollbackFor = ConstraintViolationException.class)
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
				cloudHostsService.addOrUpdateCloudHost(cloudHost);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic cloud-hosts with message {}: {}", ToStringBuilder.reflectionToString(cloudHostMessage), e.getMessage());
		}

	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<Decision> decisions = componentType.getDecisions();
				decisions.forEach(decisionsService::addOrUpdateDecision);
				componentTypesService.addOrUpdateComponentType(componentType);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic component-types with message {}: {}", ToStringBuilder.reflectionToString(componentTypeMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				condition.getHostConditions().forEach(hostRuleCondition -> {
					conditionsService.addOrUpdateCondition(hostRuleCondition.getHostCondition());
					hostRulesService.addOrUpdateRule(hostRuleCondition.getHostRule());
				});
				condition.getAppConditions().forEach(appRuleCondition -> {
					conditionsService.addOrUpdateCondition(appRuleCondition.getAppCondition());
					appRulesService.addOrUpdateRule(appRuleCondition.getAppRule());
				});
				condition.getServiceConditions().forEach(serviceRuleCondition -> {
					conditionsService.addOrUpdateCondition(serviceRuleCondition.getServiceCondition());
					serviceRulesService.addOrUpdateRule(serviceRuleCondition.getServiceRule());
				});
				condition.getContainerConditions().forEach(containerRuleCondition -> {
					conditionsService.addOrUpdateCondition(containerRuleCondition.getContainerCondition());
					containerRulesService.addOrUpdateRule(containerRuleCondition.getContainerRule());
				});

				operatorsService.addOrUpdateOperator(condition.getOperator());
				fieldsService.addOrUpdateField(condition.getField());
				valueModesService.addOrUpdateValueMode(condition.getValueMode());

				conditionsService.addOrUpdateCondition(condition);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic conditions with message {}: {}", ToStringBuilder.reflectionToString(conditionMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				containersService.addOrUpdateContainer(container);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic containers with message {}: {}", ToStringBuilder.reflectionToString(containerMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				ComponentType componentType = decision.getComponentType();
				componentTypesService.addOrUpdateComponentType(componentType);
				decisionsService.addOrUpdateDecision(decision);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic decisions with message {}: {}", ToStringBuilder.reflectionToString(decisionMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				edgeHostsService.addOrUpdateEdgeHost(edgeHost);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic edge-hosts with message {}: {}", ToStringBuilder.reflectionToString(edgeHostMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				elasticIpsService.addOrUpdateElasticIp(elasticIp);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic eips with message {}: {}", ToStringBuilder.reflectionToString(elasticIpMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<Condition> conditions = field.getConditions();
				conditions.forEach(condition -> {
					Operator operator = condition.getOperator();
					operatorsService.addOrUpdateOperator(operator);
					conditionsService.addOrUpdateCondition(condition);
				});
				fieldsService.addOrUpdateField(field);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic fields with message {}: {}", ToStringBuilder.reflectionToString(fieldMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				nodesService.addOrUpdateNode(node);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic nodes with message {}: {}", ToStringBuilder.reflectionToString(nodeMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				operatorsService.addOrUpdateOperator(operator);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic operators with message {}: {}", ToStringBuilder.reflectionToString(operatorMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				servicesService.addOrUpdateService(service);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic services with message {}: {}", ToStringBuilder.reflectionToString(serviceMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				fieldsService.addOrUpdateField(hostSimulatedMetric.getField());
				hostSimulatedMetric.getCloudHosts().forEach(cloudHost -> {
					cloudHost.getHostRules().forEach(hostRulesService::addOrUpdateRule);
					cloudHost.getSimulatedHostMetrics().forEach(hostSimulatedMetricsService::addOrUpdateSimulatedMetric);
					cloudHostsService.addOrUpdateCloudHost(cloudHost);
				});
				hostSimulatedMetric.getEdgeHosts().forEach(edgeHost -> {
					edgeHost.getHostRules().forEach(hostRulesService::addOrUpdateRule);
					edgeHost.getSimulatedHostMetrics().forEach(hostSimulatedMetricsService::addOrUpdateSimulatedMetric);
					edgeHostsService.addOrUpdateEdgeHost(edgeHost);
				});
				hostSimulatedMetricsService.addOrUpdateSimulatedMetric(hostSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-host-metrics with message {}: {}", ToStringBuilder.reflectionToString(hostSimulatedMetricMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				fieldsService.addField(appSimulatedMetric.getField());
				appSimulatedMetric.getApps().forEach(app -> {
					app.getAppRules().forEach(appRulesService::addOrUpdateRule);
					app.getSimulatedAppMetrics().forEach(appSimulatedMetricsService::addOrUpdateSimulatedMetric);
					appsService.addOrUpdateApp(app);
				});
				appSimulatedMetricsService.addOrUpdateSimulatedMetric(appSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-app-metrics with message {}: {}", ToStringBuilder.reflectionToString(appSimulatedMetricMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				fieldsService.addOrUpdateField(serviceSimulatedMetric.getField());
				serviceSimulatedMetric.getServices().forEach(service -> {
					service.getServiceRules().forEach(serviceRulesService::addOrUpdateRule);
					service.getSimulatedServiceMetrics().forEach(serviceSimulatedMetricsService::addOrUpdateSimulatedMetric);
					servicesService.addOrUpdateService(service);
				});
				serviceSimulatedMetricsService.addOrUpdateSimulatedMetric(serviceSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-service-metrics with message {}: {}", ToStringBuilder.reflectionToString(serviceSimulatedMetricMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				fieldsService.addOrUpdateField(containerSimulatedMetric.getField());
				containerSimulatedMetric.getContainers().forEach(container -> {
					container.getContainerRules().forEach(containerRulesService::addOrUpdateRule);
					container.getSimulatedContainerMetrics().forEach(containerSimulatedMetricsService::addOrUpdateSimulatedMetric);
					containersService.addOrUpdateContainer(container);
				});
				containerSimulatedMetricsService.addOrUpdateSimulatedMetric(containerSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-container-metrics with message {}: {}", ToStringBuilder.reflectionToString(containerSimulatedMetricMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<HostRuleCondition> hostRuleConditions = hostRule.getConditions();
				hostRuleConditions.forEach(hostRuleCondition -> {
					operatorsService.addOrUpdateOperator(hostRuleCondition.getHostCondition().getOperator());
					fieldsService.addOrUpdateField(hostRuleCondition.getHostCondition().getField());
					valueModesService.addOrUpdateValueMode(hostRuleCondition.getHostCondition().getValueMode());
					ruleConditionsService.saveHostRuleCondition(hostRuleCondition);
				});
				Set<CloudHost> cloudHosts = hostRule.getCloudHosts();
				cloudHosts.forEach(cloudHostsService::addOrUpdateCloudHost);
				Set<EdgeHost> edgeHosts = hostRule.getEdgeHosts();
				edgeHosts.forEach(edgeHostsService::addOrUpdateEdgeHost);
				hostRulesService.addOrUpdateRule(hostRule);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic host-rules with message {}: {}", ToStringBuilder.reflectionToString(hostRuleMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<AppRuleCondition> appRuleConditions = appRule.getConditions();
				appRuleConditions.forEach(ruleConditionsService::saveAppRuleCondition);
				Set<App> apps = appRule.getApps();
				apps.forEach(appsService::addOrUpdateApp);
				appRulesService.addOrUpdateRule(appRule);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic app-rules with message {}: {}", ToStringBuilder.reflectionToString(appRuleMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<ServiceRuleCondition> serviceRuleConditions = serviceRule.getConditions();
				serviceRuleConditions.forEach(serviceRuleCondition -> {
					Decision ruleDecision = serviceRuleCondition.getServiceRule().getDecision();
					componentTypesService.addOrUpdateComponentType(ruleDecision.getComponentType());
					decisionsService.addOrUpdateDecision(ruleDecision);
					Condition ruleCondition = serviceRuleCondition.getServiceCondition();
					valueModesService.addOrUpdateValueMode(ruleCondition.getValueMode());
					operatorsService.addOrUpdateOperator(ruleCondition.getOperator());
					fieldsService.addOrUpdateField(ruleCondition.getField());
					ruleConditionsService.saveServiceRuleCondition(serviceRuleCondition);
				});
				Set<pt.unl.fct.miei.usmanagement.manager.services.Service> services = serviceRule.getServices();
				services.forEach(servicesService::saveService);
				Decision decision = serviceRule.getDecision();
				decisionsService.addOrUpdateDecision(decision);
				serviceRulesService.addOrUpdateRule(serviceRule);
			}
		}
		catch (Exception e) {
			log.error("Error from topic service-rules while saving {}: {}", ToStringBuilder.reflectionToString(serviceRuleMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<ContainerRuleCondition> containerRuleConditions = containerRule.getConditions();
				containerRuleConditions.forEach(ruleConditionsService::saveContainerRuleCondition);
				Set<pt.unl.fct.miei.usmanagement.manager.containers.Container> containers = containerRule.getContainers();
				containers.forEach(containersService::saveContainer);
				containerRulesService.addOrUpdateRule(containerRule);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic container-rules with message {}: {}", ToStringBuilder.reflectionToString(containerRuleMessage), e.getMessage());
		}
	}

	//@Transactional(noRollbackFor = ConstraintViolationException.class)
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
				Set<Condition> conditions = valueMode.getConditions();
				conditions.forEach(condition -> {
					Field field = condition.getField();
					fieldsService.addOrUpdateField(field);
					Operator operator = condition.getOperator();
					operatorsService.addOrUpdateOperator(operator);
					conditionsService.addOrUpdateCondition(condition);
				});
				valueModesService.addOrUpdateValueMode(valueMode);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic value-modes with message {}: {}", ToStringBuilder.reflectionToString(valueModeMessage), e.getMessage());
		}
	}

}
