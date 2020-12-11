package pt.unl.fct.miei.usmanagement.manager.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.CloudHostDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ComponentTypeDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.DecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.EdgeHostDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ElasticIpDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.FieldDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.NodeDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.OperatorDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ValueModeDTO;
import pt.unl.fct.miei.usmanagement.manager.services.componenttypes.ComponentTypesService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.services.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.operators.OperatorsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.RuleConditionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.services.valuemodes.ValueModesService;
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

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "apps", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppDTO appDTO) {
		log.info("Received key={} message={}", key, appDTO.toString());
		try {
			App app = appDTO.toEntity();
			if (Objects.equal(key, "DELETE")) {
				Long id = app.getId();
				appsService.deleteApp(id);
			}
			else {
				Set<AppServiceDTO> appServices = appDTO.getAppServices();
				appServices.forEach(appService -> {
					pt.unl.fct.miei.usmanagement.manager.services.Service service = appService.getService().toEntity();
					if (!servicesService.hasService(service.getServiceName())) {
						log.info("Saving service {}", ToStringBuilder.reflectionToString(service));
						servicesService.addOrUpdateService(service);
					}
				});
				appsService.addOrUpdateApp(app);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic apps with message {}: {}", ToStringBuilder.reflectionToString(appDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "cloud-hosts", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload CloudHostDTO cloudHostDTO) {
		log.info("Received key={} message={}", key, cloudHostDTO.toString());
		CloudHost cloudHost = cloudHostDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = cloudHost.getId();
				cloudHostsService.deleteCloudHost(id);
			}
			else {
				/*Set<HostRule> rules = cloudHostDTO.getHostRules();
				rules.forEach(rule -> {
					decisionsService.addOrUpdateDecision(rule.getDecision());
					//rule.getCloudHosts().forEach(cloudHostsService::addOrUpdateCloudHost);
					rule.getEdgeHosts().forEach(edgeHostsService::addOrUpdateEdgeHost);
					rule.getConditions().forEach(ruleConditionsService::saveHostRuleCondition);
				});
				Set<HostSimulatedMetric> hostSimulatedMetrics = cloudHostDTO.getSimulatedHostMetrics();
				hostSimulatedMetrics.forEach(hostSimulatedMetric -> {
					fieldsService.addOrUpdateField(hostSimulatedMetric.getField());
					//hostSimulatedMetric.getCloudHosts().forEach(cloudHostsService::addOrUpdateCloudHost);
					hostSimulatedMetric.getEdgeHosts().forEach(edgeHostsService::addOrUpdateEdgeHost);
				});*/
				cloudHostsService.addOrUpdateCloudHost(cloudHost);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic cloud-hosts with message {}: {}", ToStringBuilder.reflectionToString(cloudHostDTO), e.getMessage());
		}

	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "component-types", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ComponentTypeDTO componentTypeDTO) {
		log.info("Received key={} message={}", key, ToStringBuilder.reflectionToString(componentTypeDTO));
		ComponentType componentType = componentTypeDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = componentType.getId();
				componentTypesService.deleteComponentType(id);
			}
			else {
				componentTypeDTO.getDecisions().forEach(decision -> {
					componentTypesService.addOrUpdateComponentType(decision.getComponentType().toEntity());
					decisionsService.addOrUpdateDecision(decision.toEntity());
				});
				componentTypesService.addOrUpdateComponentType(componentType);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic component-types with message {}: {}", ToStringBuilder.reflectionToString(componentTypeDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "conditions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ConditionDTO conditionDTO) {
		log.info("Received key={} message={}", key, ToStringBuilder.reflectionToString(conditionDTO));
		Condition condition = conditionDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = condition.getId();
				conditionsService.deleteCondition(id);
			}
			else {
				conditionDTO.getHostConditions().forEach(hostRuleCondition -> {
					conditionsService.addOrUpdateCondition(hostRuleCondition.getCondition().toEntity());
					decisionsService.addOrUpdateDecision(hostRuleCondition.getHostRule().getDecision().toEntity());
					hostRulesService.addOrUpdateRule(hostRuleCondition.getHostRule().toEntity());
				});
				conditionDTO.getAppConditions().forEach(appRuleCondition -> {
					conditionsService.addOrUpdateCondition(appRuleCondition.getCondition().toEntity());
					decisionsService.addOrUpdateDecision(appRuleCondition.getAppRule().getDecision().toEntity());
					appRulesService.addOrUpdateRule(appRuleCondition.getAppRule().toEntity());
				});
				conditionDTO.getServiceConditions().forEach(serviceRuleCondition -> {
					conditionsService.addOrUpdateCondition(serviceRuleCondition.getCondition().toEntity());
					decisionsService.addOrUpdateDecision(serviceRuleCondition.getServiceRule().getDecision());
					serviceRulesService.addOrUpdateRule(serviceRuleCondition.getServiceRule().toEntity());
				});
				conditionDTO.getContainerConditions().forEach(containerRuleCondition -> {
					conditionsService.addOrUpdateCondition(containerRuleCondition.getCondition().toEntity());
					decisionsService.addOrUpdateDecision(containerRuleCondition.getContainerRule().getDecision().toEntity());
					containerRulesService.addOrUpdateRule(containerRuleCondition.getContainerRule().toEntity());
				});

				operatorsService.addOrUpdateOperator(condition.getOperator());
				fieldsService.addOrUpdateField(condition.getField());
				valueModesService.addOrUpdateValueMode(condition.getValueMode());

				conditionsService.addOrUpdateCondition(condition);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic conditions with message {}: {}", ToStringBuilder.reflectionToString(conditionDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "containers", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerDTO containerDTO) {
		log.info("Received key={} message={}", key, containerDTO.toString());
		Container container = containerDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				String id = container.getId();
				containersService.deleteContainer(id);
			}
			else {
				/*Set<ContainerRule> rules = containerDTO.getContainerRules();
				rules.forEach(rule -> {
					decisionsService.addOrUpdateDecision(rule.getDecision());
					//rule.getContainers().forEach(containersService::addOrUpdateContainer);
					rule.getConditions().forEach(ruleConditionsService::saveContainerRuleCondition);
				});
				Set<ContainerSimulatedMetric> hostSimulatedMetrics = containerDTO.getSimulatedContainerMetrics();
				hostSimulatedMetrics.forEach(hostSimulatedMetric -> {
					fieldsService.addOrUpdateField(hostSimulatedMetric.getField());
					//hostSimulatedMetric.getContainers().forEach(containersService::addOrUpdateContainer);
				});*/
				containersService.addOrUpdateContainer(container);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic containers with message {}: {}", ToStringBuilder.reflectionToString(containerDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "decisions", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DecisionDTO decisionDTO) {
		log.info("Received key={} message={}", key, decisionDTO.toString());
		Decision decision = decisionDTO.toEntity();
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
			log.error("Error while processing topic decisions with message {}: {}", ToStringBuilder.reflectionToString(decisionDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "edge-hosts", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload EdgeHostDTO edgeHostDTO) {
		log.info("Received key={} message={}", key, edgeHostDTO.toString());
		EdgeHost edgeHost = edgeHostDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = edgeHost.getId();
				edgeHostsService.deleteEdgeHost(id);
			}
			else {
				/*Set<HostRule> rules = edgeHostDTO.getHostRules();
				rules.forEach(rule -> {
					decisionsService.addOrUpdateDecision(rule.getDecision());
					rule.getCloudHosts().forEach(cloudHostsService::addOrUpdateCloudHost);
					//rule.getEdgeHosts().forEach(edgeHostsService::addOrUpdateEdgeHost);
					rule.getConditions().forEach(ruleConditionsService::saveHostRuleCondition);
				});
				Set<HostSimulatedMetric> hostSimulatedMetrics = edgeHostDTO.getSimulatedHostMetrics();
				hostSimulatedMetrics.forEach(hostSimulatedMetric -> {
					fieldsService.addOrUpdateField(hostSimulatedMetric.getField());
					hostSimulatedMetric.getCloudHosts().forEach(cloudHostsService::addOrUpdateCloudHost);
					//hostSimulatedMetric.getEdgeHosts().forEach(edgeHostsService::addOrUpdateEdgeHost);
				});*/
				edgeHostsService.addOrUpdateEdgeHost(edgeHost);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic edge-hosts with message {}: {}", ToStringBuilder.reflectionToString(edgeHostDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "eips", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ElasticIpDTO elasticIpDTO) {
		log.info("Received key={} message={}", key, elasticIpDTO.toString());
		ElasticIp elasticIp = elasticIpDTO.toEntity();
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
			log.error("Error while processing topic eips with message {}: {}", ToStringBuilder.reflectionToString(elasticIpDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "fields", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload FieldDTO fieldDTO) {
		log.info("Received key={} message={}", key, fieldDTO.toString());
		Field field = fieldDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = field.getId();
				fieldsService.deleteField(id);
			}
			else {
				Set<Condition> conditions = fieldDTO.getConditions();
				conditions.forEach(condition -> {
					Operator operator = condition.getOperator();
					operatorsService.addOrUpdateOperator(operator);
					conditionsService.addOrUpdateCondition(condition);
				});
				fieldsService.addOrUpdateField(field);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic fields with message {}: {}", ToStringBuilder.reflectionToString(fieldDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "nodes", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload NodeDTO nodeDTO) {
		log.info("Received key={} message={}", key, nodeDTO.toString());
		Node node = nodeDTO.toEntity();
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
			log.error("Error while processing topic nodes with message {}: {}", ToStringBuilder.reflectionToString(nodeDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "operators", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload OperatorDTO operatorDTO) {
		log.info("Received key={} message={}", key, operatorDTO.toString());
		Operator operator = operatorDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = operator.getId();
				operatorsService.deleteOperator(id);
			}
			else {
				operatorDTO.getConditions().forEach(condition -> {
					fieldsService.addOrUpdateField(condition.getField().toEntity());
					valueModesService.addOrUpdateValueMode(condition.getValueMode().toEntity());
					conditionsService.addOrUpdateCondition(condition.toEntity());
				});
				operatorsService.addOrUpdateOperator(operator);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic operators with message {}: {}", ToStringBuilder.reflectionToString(operatorDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "services", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceDTO serviceDTO) {
		log.info("Received key={} message={}", key, serviceDTO.toString());
		pt.unl.fct.miei.usmanagement.manager.services.Service service = serviceDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = service.getId();
				servicesService.deleteService(id);
			}
			else {
				/*Set<ServiceRule> rules = serviceDTO.getServiceRules();
				rules.forEach(rule -> {
					decisionsService.addOrUpdateDecision(rule.getDecision());
					//rule.getServices().forEach(servicesService::addOrUpdateService);
					rule.getConditions().forEach(ruleConditionsService::saveServiceRuleCondition);
				});
				Set<ServiceSimulatedMetric> hostSimulatedMetrics = serviceDTO.getSimulatedServiceMetrics();
				hostSimulatedMetrics.forEach(hostSimulatedMetric -> {
					fieldsService.addOrUpdateField(hostSimulatedMetric.getField());
					//hostSimulatedMetric.getServices().forEach(servicesService::addOrUpdateService);
				});*/
				servicesService.addOrUpdateService(service);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic services with message {}: {}", ToStringBuilder.reflectionToString(serviceDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "simulated-host-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostSimulatedMetricDTO hostSimulatedMetricDTO) {
		log.info("Received key={} message={}", key, hostSimulatedMetricDTO.toString());
		HostSimulatedMetric hostSimulatedMetric = hostSimulatedMetricDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = hostSimulatedMetric.getId();
				hostSimulatedMetricsService.deleteHostSimulatedMetric(id);
			}
			else {
				fieldsService.addOrUpdateField(hostSimulatedMetric.getField());
				hostSimulatedMetricDTO.getCloudHosts().forEach(cloudHost -> {
					/*cloudHost.getHostRules().forEach(hostRulesService::addOrUpdateRule);*/
					cloudHost.getSimulatedHostMetrics().forEach(simulatedMetric ->
						hostSimulatedMetricsService.addOrUpdateSimulatedMetric(simulatedMetric.toEntity()));
					cloudHostsService.addOrUpdateCloudHost(cloudHost.toEntity());
				});
				hostSimulatedMetricDTO.getEdgeHosts().forEach(edgeHost -> {
					/*edgeHost.getHostRules().forEach(hostRulesService::addOrUpdateRule);*/
					edgeHost.getSimulatedHostMetrics().forEach(simulatedMetric ->
						hostSimulatedMetricsService.addOrUpdateSimulatedMetric(simulatedMetric.toEntity()));
					edgeHostsService.addOrUpdateEdgeHost(edgeHost.toEntity());
				});
				hostSimulatedMetricsService.addOrUpdateSimulatedMetric(hostSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-host-metrics with message {}: {}", ToStringBuilder.reflectionToString(hostSimulatedMetricDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "simulated-app-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppSimulatedMetricDTO appSimulatedMetricDTO) {
		log.info("Received key={} message={}", key, appSimulatedMetricDTO.toString());
		AppSimulatedMetric appSimulatedMetric = appSimulatedMetricDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = appSimulatedMetric.getId();
				appSimulatedMetricsService.deleteAppSimulatedMetric(id);
			}
			else {
				fieldsService.addField(appSimulatedMetric.getField());
				appSimulatedMetricDTO.getApps().forEach(app -> {
					/*app.getAppRules().forEach(appRulesService::addOrUpdateRule);*/
					app.getSimulatedAppMetrics().forEach(simulatedMetric -> {
						appSimulatedMetricsService.addOrUpdateSimulatedMetric(simulatedMetric.toEntity());
					});
					appsService.addOrUpdateApp(app.toEntity());
				});
				appSimulatedMetricsService.addOrUpdateSimulatedMetric(appSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-app-metrics with message {}: {}", ToStringBuilder.reflectionToString(appSimulatedMetricDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "simulated-service-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceSimulatedMetricDTO serviceSimulatedMetricDTO) {
		log.info("Received key={} message={}", key, serviceSimulatedMetricDTO.toString());
		ServiceSimulatedMetric serviceSimulatedMetric = serviceSimulatedMetricDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = serviceSimulatedMetric.getId();
				serviceSimulatedMetricsService.deleteServiceSimulatedMetric(id);
			}
			else {
				fieldsService.addOrUpdateField(serviceSimulatedMetric.getField());
				serviceSimulatedMetricDTO.getServices().forEach(service -> {
					/*service.getServiceRules().forEach(serviceRulesService::addOrUpdateRule);*/
					service.getSimulatedServiceMetrics().forEach(simulatedMetric -> {
						serviceSimulatedMetricsService.addOrUpdateSimulatedMetric(simulatedMetric.toEntity());
					});
					servicesService.addOrUpdateService(service.toEntity());
				});
				serviceSimulatedMetricsService.addOrUpdateSimulatedMetric(serviceSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-service-metrics with message {}: {}", ToStringBuilder.reflectionToString(serviceSimulatedMetricDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "simulated-container-metrics", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerSimulatedMetricDTO containerSimulatedMetricDTO) {
		log.info("Received key={} message={}", key, containerSimulatedMetricDTO.toString());
		ContainerSimulatedMetric containerSimulatedMetric = containerSimulatedMetricDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = containerSimulatedMetric.getId();
				containerSimulatedMetricsService.deleteContainerSimulatedMetric(id);
			}
			else {
				fieldsService.addOrUpdateField(containerSimulatedMetric.getField());
				containerSimulatedMetricDTO.getContainers().forEach(container -> {
					/*container.getContainerRules().forEach(containerRulesService::addOrUpdateRule);*/
					container.getSimulatedContainerMetrics().forEach(simulatedMetric -> {
						containerSimulatedMetricsService.addOrUpdateSimulatedMetric(simulatedMetric.toEntity());
					});
					containersService.addOrUpdateContainer(container.toEntity());
				});
				containerSimulatedMetricsService.addOrUpdateSimulatedMetric(containerSimulatedMetric);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic simulated-container-metrics with message {}: {}", ToStringBuilder.reflectionToString(containerSimulatedMetricDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "host-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostRuleDTO hostRuleDTO) {
		log.info("Received key={} message={}", key, hostRuleDTO.toString());
		HostRule hostRule = hostRuleDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = hostRule.getId();
				hostRulesService.deleteRule(id);
			}
			else {
				hostRuleDTO.getConditions().forEach(hostRuleCondition -> {
					operatorsService.addOrUpdateOperator(hostRuleCondition.getCondition().getOperator().toEntity());
					fieldsService.addOrUpdateField(hostRuleCondition.getCondition().getField().toEntity());
					valueModesService.addOrUpdateValueMode(hostRuleCondition.getCondition().getValueMode().toEntity());
					ruleConditionsService.saveHostRuleCondition(hostRuleCondition.toEntity());
				});
				hostRuleDTO.getCloudHosts().forEach(cloudHost -> cloudHostsService.addOrUpdateCloudHost(cloudHost.toEntity()));
				hostRuleDTO.getEdgeHosts().forEach(edgeHost -> edgeHostsService.addOrUpdateEdgeHost(edgeHost.toEntity()));
				hostRulesService.addOrUpdateRule(hostRule);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic host-rules with message {}: {}", ToStringBuilder.reflectionToString(hostRuleDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "app-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppRuleDTO appRuleDTO) {
		log.info("Received key={} message={}", key, appRuleDTO.toString());
		AppRule appRule = appRuleDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = appRule.getId();
				appRulesService.deleteRule(id);
			}
			else {
				appRuleDTO.getConditions().forEach(appRuleCondition -> {
					operatorsService.addOrUpdateOperator(appRuleCondition.getCondition().getOperator().toEntity());
					fieldsService.addOrUpdateField(appRuleCondition.getCondition().getField().toEntity());
					valueModesService.addOrUpdateValueMode(appRuleCondition.getCondition().getValueMode().toEntity());
					ruleConditionsService.saveAppRuleCondition(appRuleCondition.toEntity());
				});
				appRuleDTO.getApps().forEach(app -> appsService.addOrUpdateApp(app.toEntity()));
				appRulesService.addOrUpdateRule(appRule);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic app-rules with message {}: {}", ToStringBuilder.reflectionToString(appRuleDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "service-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ServiceRuleDTO serviceRuleDTO) {
		log.info("Received key={} message={}", key, serviceRuleDTO.toString());
		ServiceRule serviceRule = serviceRuleDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = serviceRule.getId();
				serviceRulesService.deleteRule(id);
			}
			else {
				serviceRuleDTO.getConditions().forEach(serviceRuleCondition -> {
					Decision ruleDecision = serviceRuleCondition.getServiceRule().getDecision();
					componentTypesService.addOrUpdateComponentType(ruleDecision.getComponentType());
					decisionsService.addOrUpdateDecision(ruleDecision);
					Condition ruleCondition = serviceRuleCondition.getCondition().toEntity();
					valueModesService.addOrUpdateValueMode(ruleCondition.getValueMode());
					operatorsService.addOrUpdateOperator(ruleCondition.getOperator());
					fieldsService.addOrUpdateField(ruleCondition.getField());
					ruleConditionsService.saveServiceRuleCondition(serviceRuleCondition.toEntity());
				});
				serviceRuleDTO.getServices().forEach(service -> servicesService.addOrUpdateService(service.toEntity()));
				decisionsService.addOrUpdateDecision(serviceRule.getDecision());
				serviceRulesService.addOrUpdateRule(serviceRule);
			}
		}
		catch (Exception e) {
			log.error("Error from topic service-rules while saving {}: {}", ToStringBuilder.reflectionToString(serviceRuleDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "container-rules", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ContainerRuleDTO containerRuleDTO) {
		log.info("Received key={} message={}", key, containerRuleDTO.toString());
		ContainerRule containerRule = containerRuleDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = containerRule.getId();
				containerRulesService.deleteRule(id);
			}
			else {
				containerRuleDTO.getConditions().forEach(containerRuleCondition -> {
					operatorsService.addOrUpdateOperator(containerRuleCondition.getCondition().getOperator().toEntity());
					fieldsService.addOrUpdateField(containerRuleCondition.getCondition().getField().toEntity());
					valueModesService.addOrUpdateValueMode(containerRuleCondition.getCondition().getValueMode().toEntity());
					ruleConditionsService.saveContainerRuleCondition(containerRuleCondition.toEntity());
				});
				containerRuleDTO.getContainers().forEach(container -> containersService.addOrUpdateContainer(container.toEntity()));
				containerRulesService.addOrUpdateRule(containerRule);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic container-rules with message {}: {}", ToStringBuilder.reflectionToString(containerRuleDTO), e.getMessage());
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(groupId = "manager-worker", topics = "value-modes", autoStartup = "false")
	public void listen(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ValueModeDTO valueModeDTO) {
		log.info("Received key={} message={}", key, valueModeDTO.toString());
		ValueMode valueMode = valueModeDTO.toEntity();
		try {
			if (Objects.equal(key, "DELETE")) {
				Long id = valueMode.getId();
				valueModesService.deleteValueMode(id);
			}
			else {
				valueModeDTO.getConditions().forEach(condition -> {
					operatorsService.addOrUpdateOperator(condition.getOperator().toEntity());
					fieldsService.addOrUpdateField(condition.getField().toEntity());
					conditionsService.addOrUpdateCondition(condition.toEntity());
				});
				valueModesService.addOrUpdateValueMode(valueMode);
			}
		}
		catch (Exception e) {
			log.error("Error while processing topic value-modes with message {}: {}", ToStringBuilder.reflectionToString(valueModeDTO), e.getMessage());
		}
	}

}
