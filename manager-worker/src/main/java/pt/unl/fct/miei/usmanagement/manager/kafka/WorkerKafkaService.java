package pt.unl.fct.miei.usmanagement.manager.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;
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
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDependencyDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ValueModeDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.AppMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.AppRuleMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.AppSimulatedMetricMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CloudHostMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ComponentTypeMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ConditionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ContainerMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ContainerRuleMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ContainerSimulatedMetricMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.CycleAvoidingMappingContext;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.DecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.EdgeHostMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ElasticIpMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.FieldMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostRuleMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostSimulatedMetricMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.NodeMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.OperatorMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceRuleMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceSimulatedMetricMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ValueModeMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.WorkerManagerMapper;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
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
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
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
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.HashSet;
import java.util.List;
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
	private final WorkerManagersService workerManagersService;

	private final CycleAvoidingMappingContext context;

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
							  ContainerRulesService containerRulesService, ValueModesService valueModesService,
							  RuleConditionsService ruleConditionsService, WorkerManagersService workerManagersService) {
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
		this.workerManagersService = workerManagersService;
		this.context = new CycleAvoidingMappingContext();
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "apps", autoStartup = "false")
	public void listenApps(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<AppDTO> appDTOs) {
		int i = 0;
		for (AppDTO appDTO : appDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, appDTO.toString());
			try {
				App app = AppMapper.MAPPER.toApp(appDTO, context);
				if (Objects.equal(key, "DELETE")) {
					Long id = app.getId();
					appsService.deleteApp(id);
				}
				else {
					Set<AppServiceDTO> appServices = appDTO.getAppServices();
					appServices.forEach(appService -> {
						pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(appService.getService(), context);
						servicesService.addIfNotPresent(service);
					});
					appsService.addOrUpdateApp(app);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic apps with message {}: {}", appDTO.toString(), e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "cloud-hosts", autoStartup = "false")
	public void listenCloudHosts(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<CloudHostDTO> cloudHostDTOs) {
		int i = 0;
		for (CloudHostDTO cloudHostDTO : cloudHostDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, cloudHostDTO.toString());
			CloudHost cloudHost = CloudHostMapper.MAPPER.toCloudHost(cloudHostDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = cloudHost.getId();
					cloudHostsService.deleteCloudHost(id);
				}
				else {
					workerManagersService.addIfNotPresent(WorkerManagerMapper.MAPPER.toWorkerManager(cloudHostDTO.getManagedByWorker(), context));
					cloudHostsService.addOrUpdateCloudHost(cloudHost);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic cloud-hosts with message {}: {}", cloudHostDTO.toString(), e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "component-types", autoStartup = "false")
	public void listenComponentTypes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ComponentTypeDTO> componentTypeDTOs) {
		int i = 0;
		for (ComponentTypeDTO componentTypeDTO : componentTypeDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, componentTypeDTO);
			ComponentType componentType = ComponentTypeMapper.MAPPER.toComponentType(componentTypeDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = componentType.getId();
					componentTypesService.deleteComponentType(id);
				}
				else {
					componentTypeDTO.getDecisions().forEach(decisionDTO -> {
						Decision decision = DecisionMapper.MAPPER.toDecision(decisionDTO, context);
						decisionsService.addIfNotPresent(decision);
					});
					componentTypesService.addOrUpdateComponentType(componentType);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic component-types with message {}: {}", componentTypeDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "conditions", autoStartup = "false")
	public void listenConditions(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ConditionDTO> conditionDTOs) {
		int i = 0;
		for (ConditionDTO conditionDTO : conditionDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, conditionDTO);
			Condition condition = ConditionMapper.MAPPER.toCondition(conditionDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = condition.getId();
					conditionsService.deleteCondition(id);
				}
				else {
				/*conditionDTO.getHostConditions().forEach(hostRuleCondition -> {
					conditionsService.addOrUpdateCondition(ConditionMapper.MAPPER.toCondition(hostRuleCondition.getCondition(), context));
					decisionsService.addOrUpdateDecision(DecisionMapper.MAPPER.toDecision(hostRuleCondition.getHostRule().getDecision(), context));
					hostRulesService.addOrUpdateRule(HostRuleMapper.MAPPER.toHostRule(hostRuleCondition.getHostRule(), context));
				});
				conditionDTO.getAppConditions().forEach(appRuleCondition -> {
					conditionsService.addOrUpdateCondition(ConditionMapper.MAPPER.toCondition(appRuleCondition.getCondition(), context));
					decisionsService.addOrUpdateDecision(DecisionMapper.MAPPER.toDecision(appRuleCondition.getAppRule().getDecision(), context));
					appRulesService.addOrUpdateRule(AppRuleMapper.MAPPER.toAppRule(appRuleCondition.getAppRule(), context));
				});
				conditionDTO.getServiceConditions().forEach(serviceRuleCondition -> {
					conditionsService.addOrUpdateCondition(ConditionMapper.MAPPER.toCondition(serviceRuleCondition.getCondition(), context));
					decisionsService.addOrUpdateDecision(DecisionMapper.MAPPER.toDecision(serviceRuleCondition.getServiceRule().getDecision(), context));
					serviceRulesService.addOrUpdateRule(ServiceRuleMapper.MAPPER.toServiceRule(serviceRuleCondition.getServiceRule(), context));
				});
				conditionDTO.getContainerConditions().forEach(containerRuleCondition -> {
					conditionsService.addOrUpdateCondition(ConditionMapper.MAPPER.toCondition(containerRuleCondition.getCondition(), context));
					decisionsService.addOrUpdateDecision(DecisionMapper.MAPPER.toDecision(containerRuleCondition.getContainerRule().getDecision(), context));
					containerRulesService.addOrUpdateRule(ContainerRuleMapper.MAPPER.toContainerRule(containerRuleCondition.getContainerRule(), context));
				});*/

					operatorsService.addIfNotPresent(OperatorMapper.MAPPER.toOperator(conditionDTO.getOperator(), context));
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(conditionDTO.getField(), context));
					valueModesService.addIfNotPresent(ValueModeMapper.MAPPER.toValueMode(conditionDTO.getValueMode(), context));

					conditionsService.addOrUpdateCondition(condition);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic conditions with message {}: {}", conditionDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "containers", autoStartup = "false")
	public void listenContainers(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ContainerDTO> containerDTOs) {
		int i = 0;
		for (ContainerDTO containerDTO : containerDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, containerDTO);
			Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
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
				log.error("Error while processing topic containers with message {}: {}", containerDTO, e.getMessage());
			}
		}

	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "decisions", autoStartup = "false")
	public void listenDecisions(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<DecisionDTO> decisionDTOs) {
		int i = 0;
		for (DecisionDTO decisionDTO : decisionDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, decisionDTO.toString());
			Decision decision = DecisionMapper.MAPPER.toDecision(decisionDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = decision.getId();
					decisionsService.deleteDecision(id);
				}
				else {
					componentTypesService.addIfNotPresent(ComponentTypeMapper.MAPPER.toComponentType(decisionDTO.getComponentType(), context));
					decisionsService.addOrUpdateDecision(decision);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic decisions with message {}: {}", decisionDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "edge-hosts", autoStartup = "false")
	public void listenEdgeHosts(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<EdgeHostDTO> edgeHostDTOs) {
		int i = 0;
		for (EdgeHostDTO edgeHostDTO : edgeHostDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, edgeHostDTO.toString());
			EdgeHost edgeHost = EdgeHostMapper.MAPPER.toEdgeHost(edgeHostDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = edgeHost.getId();
					edgeHostsService.deleteEdgeHost(id);
				}
				else {
					workerManagersService.addIfNotPresent(WorkerManagerMapper.MAPPER.toWorkerManager(edgeHostDTO.getManagedByWorker(), context));
					edgeHostsService.addOrUpdateEdgeHost(edgeHost);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic edge-hosts with message {}: {}", edgeHostDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "eips", autoStartup = "false")
	public void listenElasticIps(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ElasticIpDTO> elasticIpDTOs) {
		int i = 0;
		for (ElasticIpDTO elasticIpDTO : elasticIpDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, elasticIpDTO.toString());
			ElasticIp elasticIp = ElasticIpMapper.MAPPER.toElasticIp(elasticIpDTO, context);
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
				log.error("Error while processing topic eips with message {}: {}", elasticIpDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "fields", autoStartup = "false")
	public void listenFields(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<FieldDTO> fieldDTOs) {
		int i = 0;
		for (FieldDTO fieldDTO : fieldDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, fieldDTO.toString());
			Field field = FieldMapper.MAPPER.toField(fieldDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = field.getId();
					fieldsService.deleteField(id);
				}
				else {
					fieldsService.addOrUpdateField(field);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic fields with message {}: {}", fieldDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "nodes", autoStartup = "false")
	public void listenNodes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<NodeDTO> nodeDTOs) {
		int i = 0;
		for (NodeDTO nodeDTO : nodeDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, nodeDTO.toString());
			Node node = NodeMapper.MAPPER.toNode(nodeDTO, context);
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
				log.error("Error while processing topic nodes with message {}: {}", nodeDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "operators", autoStartup = "false")
	public void listenOperators(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<OperatorDTO> operatorDTOs) {
		int i = 0;
		for (OperatorDTO operatorDTO : operatorDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, operatorDTO.toString());
			Operator operator = OperatorMapper.MAPPER.toOperator(operatorDTO, context);
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
				log.error("Error while processing topic operators with message {}: {}", operatorDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "services", autoStartup = "false")
	public void listenService(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ServiceDTO> serviceDTOs) {
		int i = 0;
		for (ServiceDTO serviceDTO : serviceDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, serviceDTO.toString());
			try {
				pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
				if (Objects.equal(key, "DELETE")) {
					Long id = service.getId();
					servicesService.deleteService(id);
				}
				else {
					for (AppServiceDTO appService : serviceDTO.getAppServices()) {
						App app = AppMapper.MAPPER.toApp(appService.getApp(), context);
						appsService.addIfNotPresent(app);
					}
					for (ServiceDependencyDTO dependencyDTO : serviceDTO.getDependencies()) {
						pt.unl.fct.miei.usmanagement.manager.services.Service dependencyService = ServiceMapper.MAPPER.toService(dependencyDTO.getDependency(), context);
						servicesService.addIfNotPresent(dependencyService);
					}
					for (ServiceDependencyDTO dependentDTO : serviceDTO.getDependents()) {
						pt.unl.fct.miei.usmanagement.manager.services.Service dependentService = ServiceMapper.MAPPER.toService(dependentDTO.getService(), context);
						servicesService.addIfNotPresent(dependentService);
					}
					servicesService.addOrUpdateService(service);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic services with message {}: {}", serviceDTO.toString(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-host-metrics", autoStartup = "false")
	public void listenSimulatedHostMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
										   List<HostSimulatedMetricDTO> hostSimulatedMetricDTOs) {
		int i = 0;
		for (HostSimulatedMetricDTO hostSimulatedMetricDTO : hostSimulatedMetricDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, hostSimulatedMetricDTO.toString());
			HostSimulatedMetric hostSimulatedMetric = HostSimulatedMetricMapper.MAPPER.toHostSimulatedMetric(hostSimulatedMetricDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = hostSimulatedMetric.getId();
					hostSimulatedMetricsService.deleteHostSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(hostSimulatedMetricDTO.getField(), context));
					for (CloudHostDTO cloudHostDTO : hostSimulatedMetricDTO.getCloudHosts()) {
						CloudHost cloudHost = CloudHostMapper.MAPPER.toCloudHost(cloudHostDTO, context);
						cloudHost = cloudHostsService.addIfNotPresent(cloudHost);
						cloudHost.addHostSimulatedMetric(hostSimulatedMetric);
					}
					for (EdgeHostDTO edgeHostDTO : hostSimulatedMetricDTO.getEdgeHosts()) {
						EdgeHost edgeHost = EdgeHostMapper.MAPPER.toEdgeHost(edgeHostDTO, context);
						edgeHost = edgeHostsService.addIfNotPresent(edgeHost);
						edgeHost.addHostSimulatedMetric(hostSimulatedMetric);
					}
					hostSimulatedMetricsService.addOrUpdateSimulatedMetric(hostSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-host-metrics with message {}: {}", hostSimulatedMetricDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-app-metrics", autoStartup = "false")
	public void listenSimulatedAppMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
										  List<AppSimulatedMetricDTO> appSimulatedMetricDTOs) {
		int i = 0;
		for (AppSimulatedMetricDTO appSimulatedMetricDTO : appSimulatedMetricDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, appSimulatedMetricDTO.toString());
			AppSimulatedMetric appSimulatedMetric = AppSimulatedMetricMapper.MAPPER.toAppSimulatedMetric(appSimulatedMetricDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = appSimulatedMetric.getId();
					appSimulatedMetricsService.deleteAppSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(appSimulatedMetricDTO.getField(), context));
					for (AppDTO appDTO : appSimulatedMetricDTO.getApps()) {
						pt.unl.fct.miei.usmanagement.manager.apps.App app = AppMapper.MAPPER.toApp(appDTO, context);
						app = appsService.addIfNotPresent(app);
						app.addAppSimulatedMetric(appSimulatedMetric);
					}
					appSimulatedMetricsService.addOrUpdateSimulatedMetric(appSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-app-metrics with message {}: {}", appSimulatedMetricDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-service-metrics", autoStartup = "false")
	public void listenSimulatedServiceMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
											  List<ServiceSimulatedMetricDTO> serviceSimulatedMetricDTOs) {
		int i = 0;
		for (ServiceSimulatedMetricDTO serviceSimulatedMetricDTO : serviceSimulatedMetricDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, serviceSimulatedMetricDTO.toString());
			ServiceSimulatedMetric serviceSimulatedMetric = ServiceSimulatedMetricMapper.MAPPER.toServiceSimulatedMetric(serviceSimulatedMetricDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = serviceSimulatedMetric.getId();
					serviceSimulatedMetricsService.deleteServiceSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(serviceSimulatedMetricDTO.getField(), context));
					for (ServiceDTO serviceDTO : serviceSimulatedMetricDTO.getServices()) {
						pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
						service = servicesService.addIfNotPresent(service);
						service.addServiceSimulatedMetric(serviceSimulatedMetric);
					}
					serviceSimulatedMetricsService.addOrUpdateSimulatedMetric(serviceSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-service-metrics with message {}: {}", serviceSimulatedMetricDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-container-metrics", autoStartup = "false")
	public void listenSimulatedContainerMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ContainerSimulatedMetricDTO> containerSimulatedMetricDTOs) {
		int i = 0;
		for (ContainerSimulatedMetricDTO containerSimulatedMetricDTO : containerSimulatedMetricDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, containerSimulatedMetricDTO.toString());
			ContainerSimulatedMetric containerSimulatedMetric = ContainerSimulatedMetricMapper.MAPPER.toContainerSimulatedMetric(containerSimulatedMetricDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = containerSimulatedMetric.getId();
					containerSimulatedMetricsService.deleteContainerSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(containerSimulatedMetricDTO.getField(), context));
					for (ContainerDTO containerDTO : containerSimulatedMetricDTO.getContainers()) {
						pt.unl.fct.miei.usmanagement.manager.containers.Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
						container = containersService.addIfNotPresent(container);
						container.addContainerSimulatedMetric(containerSimulatedMetric);
					}
					containerSimulatedMetricsService.addOrUpdateSimulatedMetric(containerSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-container-metrics with message {}: {}", containerSimulatedMetricDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "host-rules", autoStartup = "false")
	public void listenHostRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<HostRuleDTO> hostRuleDTOs) {
		int i = 0;
		for (HostRuleDTO hostRuleDTO : hostRuleDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, hostRuleDTO.toString());
			HostRule hostRule = HostRuleMapper.MAPPER.toHostRule(hostRuleDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = hostRule.getId();
					hostRulesService.deleteRule(id);
				}
				else {
					decisionsService.addIfNotPresent(DecisionMapper.MAPPER.toDecision(hostRuleDTO.getDecision(), context));
					for (CloudHostDTO cloudHostDTO : hostRuleDTO.getCloudHosts()) {
						CloudHost cloudHost = CloudHostMapper.MAPPER.toCloudHost(cloudHostDTO, context);
						cloudHost = cloudHostsService.addIfNotPresent(cloudHost);
						cloudHost.addRule(hostRule);
					}
					for (EdgeHostDTO edgeHostDTO : hostRuleDTO.getEdgeHosts()) {
						EdgeHost edgeHost = EdgeHostMapper.MAPPER.toEdgeHost(edgeHostDTO, context);
						edgeHost = edgeHostsService.addIfNotPresent(edgeHost);
						edgeHost.addRule(hostRule);
					}
					hostRulesService.addOrUpdateRule(hostRule);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic host-rules with message {}: {}", hostRuleDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "app-rules", autoStartup = "false")
	public void listenAppRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<AppRuleDTO> appRuleDTOs) {
		int i = 0;
		for (AppRuleDTO appRuleDTO : appRuleDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, appRuleDTO.toString());
			AppRule appRule = AppRuleMapper.MAPPER.toAppRule(appRuleDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = appRule.getId();
					appRulesService.deleteRule(id);
				}
				else {
					decisionsService.addIfNotPresent(DecisionMapper.MAPPER.toDecision(appRuleDTO.getDecision(), context));
					for (AppDTO appDTO : appRuleDTO.getApps()) {
						App app = AppMapper.MAPPER.toApp(appDTO, context);
						app = appsService.addIfNotPresent(app);
						app.addRule(appRule);
					}
					appRulesService.addOrUpdateRule(appRule);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic app-rules with message {}: {}", appRuleDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "service-rules", autoStartup = "false")
	public void listenServiceRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ServiceRuleDTO> serviceRuleDTOs) {
		int i = 0;
		for (ServiceRuleDTO serviceRuleDTO : serviceRuleDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, serviceRuleDTO.toString());
			ServiceRule serviceRule = ServiceRuleMapper.MAPPER.toServiceRule(serviceRuleDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = serviceRule.getId();
					serviceRulesService.deleteRule(id);
				}
				else {
					decisionsService.addIfNotPresent(DecisionMapper.MAPPER.toDecision(serviceRuleDTO.getDecision(), context));
					for (ServiceDTO serviceDTO : serviceRuleDTO.getServices()) {
						pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
						service = servicesService.addIfNotPresent(service);
						service.addRule(serviceRule);
					}
					serviceRulesService.addOrUpdateRule(serviceRule);
				}
			}
			catch (Exception e) {
				log.error("Error from topic service-rules while saving {}: {}", serviceRuleDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "container-rules", autoStartup = "false")
	public void listenContainerRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ContainerRuleDTO> containerRuleDTOs) {
		int i = 0;
		for (ContainerRuleDTO containerRuleDTO : containerRuleDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, containerRuleDTO.toString());
			ContainerRule containerRule = ContainerRuleMapper.MAPPER.toContainerRule(containerRuleDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = containerRule.getId();
					containerRulesService.deleteRule(id);
				}
				else {
					decisionsService.addIfNotPresent(DecisionMapper.MAPPER.toDecision(containerRuleDTO.getDecision(), context));
					for (ContainerDTO containerDTO : containerRuleDTO.getContainers()) {
						Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
						container = containersService.addIfNotPresent(container);
						container.addRule(containerRule);
					}
					containerRulesService.addOrUpdateRule(containerRule);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic container-rules with message {}: {}", containerRuleDTO, e.getMessage());
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "value-modes", autoStartup = "false")
	public void listenValueModes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, List<ValueModeDTO> valueModeDTOs) {
		int i = 0;
		for (ValueModeDTO valueModeDTO : valueModeDTOs) {
			String key = keys.get(i++);
			log.debug("Received key={} message={}", key, valueModeDTO.toString());
			ValueMode valueMode = ValueModeMapper.MAPPER.toValueMode(valueModeDTO, context);
			try {
				if (Objects.equal(key, "DELETE")) {
					Long id = valueMode.getId();
					valueModesService.deleteValueMode(id);
				}
				else {
				/*valueModeDTO.getConditions().forEach(condition -> {
					operatorsService.addOrUpdateOperator(OperatorMapper.MAPPER.toOperator(condition.getOperator(), context));
					fieldsService.addOrUpdateField(FieldMapper.MAPPER.toField(condition.getField(), context));
					conditionsService.addOrUpdateCondition(ConditionMapper.MAPPER.toCondition(condition, context));
				});*/
					valueModesService.addOrUpdateValueMode(valueMode);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic value-modes with message {}: {}", valueModeDTO.toString(), e.getMessage());
			}
		}
	}

}
