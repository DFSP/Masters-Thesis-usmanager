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
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.CloudHostDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ComponentTypeDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ContainerSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.DecisionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.EdgeHostDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ElasticIpDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.FieldDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostRuleConditionDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.HostSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.OperatorDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceDependencyDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ServiceRuleConditionDTO;
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
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaTopicKey;
import pt.unl.fct.miei.usmanagement.manager.services.componenttypes.ComponentTypesService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.services.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.operators.OperatorsService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	private final WorkerManagersService workerManagersService;
	private final HostsService hostsService;

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
							  HostRulesService hostRulesService, AppRulesService appRulesService,
							  ServiceRulesService serviceRulesService, ContainerRulesService containerRulesService,
							  ValueModesService valueModesService, WorkerManagersService workerManagersService, HostsService hostsService) {
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
		this.workerManagersService = workerManagersService;
		this.hostsService = hostsService;
		this.context = new CycleAvoidingMappingContext();
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "apps", autoStartup = "false")
	public void listenApps(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<AppDTO> appDTOs) {
		int i = 0;
		for (AppDTO appDTO : appDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, appDTO.toString());
			try {
				App app = AppMapper.MAPPER.toApp(appDTO, context);
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = app.getId();
					appsService.deleteApp(id);
				}
				else {
					Set<AppServiceDTO> appServices = appDTO.getAppServices();
					if (appServices != null) {
						appServices.forEach(appService -> {
							pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(appService.getService(), context);
							servicesService.addIfNotPresent(service);
						});
					}
					appsService.addOrUpdateApp(app);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic apps with key={} and message={}: {}", key, appDTO.toString(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "cloud-hosts", autoStartup = "false")
	public void listenCloudHosts(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<CloudHostDTO> cloudHostDTOs) {
		int i = 0;
		for (CloudHostDTO cloudHostDTO : cloudHostDTOs) {
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

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "component-types", autoStartup = "false")
	public void listenComponentTypes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ComponentTypeDTO> componentTypeDTOs) {
		int i = 0;
		for (ComponentTypeDTO componentTypeDTO : componentTypeDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, componentTypeDTO);
			ComponentType componentType = ComponentTypeMapper.MAPPER.toComponentType(componentTypeDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = componentType.getId();
					componentTypesService.deleteComponentType(id);
				}
				else {
					componentTypesService.addIfNotPresent(componentType);
					Set<DecisionDTO> decisions = componentTypeDTO.getDecisions();
					if (decisions != null) {
						decisions.forEach(decisionDTO -> {
							Decision decision = DecisionMapper.MAPPER.toDecision(decisionDTO, context);
							decisionsService.addIfNotPresent(decision);
						});
					}
					componentTypesService.addOrUpdateComponentType(componentType);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic component-types with key={} and message={}: {}", key, componentTypeDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "conditions", autoStartup = "false")
	public void listenConditions(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ConditionDTO> conditionDTOs) {
		int i = 0;
		for (ConditionDTO conditionDTO : conditionDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, conditionDTO);
			Condition condition = ConditionMapper.MAPPER.toCondition(conditionDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = condition.getId();
					conditionsService.deleteCondition(id);
				}
				else {
					operatorsService.addIfNotPresent(OperatorMapper.MAPPER.toOperator(conditionDTO.getOperator(), context));
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(conditionDTO.getField(), context));
					valueModesService.addIfNotPresent(ValueModeMapper.MAPPER.toValueMode(conditionDTO.getValueMode(), context));

					Set<HostRuleConditionDTO> hostRuleConditions = conditionDTO.getHostConditions();
					if (hostRuleConditions != null && hostRuleConditions.size() > 0) {
						Set<HostRuleDTO> hostRules = hostRuleConditions.stream().map(HostRuleConditionDTO::getRule).collect(Collectors.toSet());
						listenHostRules(new ArrayList<>(Collections.nCopies(hostRuleConditions.size(), null)), hostRules);
					}

					Set<AppRuleConditionDTO> appRuleConditions = conditionDTO.getAppConditions();
					if (appRuleConditions != null && appRuleConditions.size() > 0) {
						Set<AppRuleDTO> appRules = appRuleConditions.stream().map(AppRuleConditionDTO::getRule).collect(Collectors.toSet());
						listenAppRules(new ArrayList<>(Collections.nCopies(appRuleConditions.size(), null)), appRules);
					}

					Set<ServiceRuleConditionDTO> serviceRuleConditions = conditionDTO.getServiceConditions();
					if (serviceRuleConditions != null && serviceRuleConditions.size() > 0) {
						Set<ServiceRuleDTO> serviceRules = serviceRuleConditions.stream().map(ServiceRuleConditionDTO::getRule).collect(Collectors.toSet());
						listenServiceRules(new ArrayList<>(Collections.nCopies(serviceRuleConditions.size(), null)), serviceRules);
					}

					Set<ContainerRuleConditionDTO> containerRuleConditions = conditionDTO.getContainerConditions();
					if (containerRuleConditions != null && containerRuleConditions.size() > 0) {
						Set<ContainerRuleDTO> containerRules = containerRuleConditions.stream().map(ContainerRuleConditionDTO::getRule).collect(Collectors.toSet());
						listenContainerRules(new ArrayList<>(Collections.nCopies(containerRuleConditions.size(), null)), containerRules);
					}

					conditionsService.addOrUpdateCondition(condition);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic conditions with key={} and message={}: {}", key, conditionDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "containers", autoStartup = "false")
	public void listenContainers(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ContainerDTO> containerDTOs) {
		int i = 0;
		for (ContainerDTO containerDTO : containerDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, containerDTO);
			if (key != null && key.getManagerId() != null && !key.getManagerId().equalsIgnoreCase("manager-master")) {
				continue;
			}
			Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					String id = container.getId();
					if (containersService.hasContainer(id)) {
						containersService.deleteContainer(id);
					}
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
	@KafkaListener(topics = "decisions", autoStartup = "false")
	public void listenDecisions(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<DecisionDTO> decisionDTOs) {
		int i = 0;
		for (DecisionDTO decisionDTO : decisionDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, decisionDTO.toString());
			Decision decision = DecisionMapper.MAPPER.toDecision(decisionDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = decision.getId();
					decisionsService.deleteDecision(id);
				}
				else {
					componentTypesService.addIfNotPresent(ComponentTypeMapper.MAPPER.toComponentType(decisionDTO.getComponentType(), context));
					decisionsService.addOrUpdateDecision(decision);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic decisions with key={} and message={}: {}", key, decisionDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "edge-hosts", autoStartup = "false")
	public void listenEdgeHosts(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<EdgeHostDTO> edgeHostDTOs) {
		int i = 0;
		for (EdgeHostDTO edgeHostDTO : edgeHostDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, edgeHostDTO.toString());
			EdgeHost edgeHost = EdgeHostMapper.MAPPER.toEdgeHost(edgeHostDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = edgeHost.getId();
					edgeHostsService.deleteEdgeHost(id);
				}
				else {
					if (edgeHostDTO.getManagedByWorker() != null) {
						workerManagersService.addIfNotPresent(WorkerManagerMapper.MAPPER.toWorkerManager(edgeHostDTO.getManagedByWorker(), context));
					}
					edgeHostsService.addOrUpdateEdgeHost(edgeHost);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic edge-hosts with key={} and message={}: {}", key, edgeHostDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "eips", autoStartup = "false")
	public void listenElasticIps(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ElasticIpDTO> elasticIpDTOs) {
		int i = 0;
		for (ElasticIpDTO elasticIpDTO : elasticIpDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, elasticIpDTO.toString());
			ElasticIp elasticIp = ElasticIpMapper.MAPPER.toElasticIp(elasticIpDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = elasticIp.getId();
					elasticIpsService.deleteElasticIp(id);
				}
				else {
					elasticIpsService.addOrUpdateElasticIp(elasticIp);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic eips with key={} and message={}: {}", key, elasticIpDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "fields", autoStartup = "false")
	public void listenFields(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<FieldDTO> fieldDTOs) {
		int i = 0;
		for (FieldDTO fieldDTO : fieldDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, fieldDTO.toString());
			Field field = FieldMapper.MAPPER.toField(fieldDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = field.getId();
					fieldsService.deleteField(id);
				}
				else {
					Set<ConditionDTO> conditions = fieldDTO.getConditions();
					if (conditions != null && conditions.size() > 0) {
						listenConditions(new ArrayList<>(Collections.nCopies(conditions.size(), null)), conditions);
					}
					fieldsService.addOrUpdateField(field);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic fields with key={} and message={}: {}", key, fieldDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/*@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "nodes", autoStartup = "false")
	public void listenNodes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<NodeDTO> nodeDTOs) {
		int i = 0;
		for (NodeDTO nodeDTO : nodeDTOs) {
			KafkaTopicKey key = keys.get(i++);
			if (key == null && nodeDTO.getRegion() != hostsService.getManagerHostAddress().getRegion()) {
				continue;
			}
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
	}*/

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "operators", autoStartup = "false")
	public void listenOperators(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<OperatorDTO> operatorDTOs) {
		int i = 0;
		for (OperatorDTO operatorDTO : operatorDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, operatorDTO.toString());
			Operator operator = OperatorMapper.MAPPER.toOperator(operatorDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = operator.getId();
					operatorsService.deleteOperator(id);
				}
				else {
					Set<ConditionDTO> conditions = operatorDTO.getConditions();
					if (conditions != null && conditions.size() > 0) {
						listenConditions(new ArrayList<>(Collections.nCopies(conditions.size(), null)), conditions);
					}
					operatorsService.addOrUpdateOperator(operator);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic operators with key={} and message={}: {}", key, operatorDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "services", autoStartup = "false")
	public void listenService(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ServiceDTO> serviceDTOs) {
		int i = 0;
		for (ServiceDTO serviceDTO : serviceDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, serviceDTO.toString());
			try {
				pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					String serviceName = service.getServiceName();
					servicesService.deleteServiceByName(serviceName);
				}
				else {
					Set<AppServiceDTO> appServices = serviceDTO.getAppServices();
					if (appServices != null) {
						for (AppServiceDTO appService : appServices) {
							App app = AppMapper.MAPPER.toApp(appService.getApp(), context);
							appsService.addIfNotPresent(app);
						}
					}
					Set<ServiceDependencyDTO> serviceDependencies = serviceDTO.getDependencies();
					if (serviceDependencies != null) {
						for (ServiceDependencyDTO dependencyDTO : serviceDependencies) {
							pt.unl.fct.miei.usmanagement.manager.services.Service dependencyService = ServiceMapper.MAPPER.toService(dependencyDTO.getDependency(), context);
							servicesService.addIfNotPresent(dependencyService);
						}
					}
					Set<ServiceDependencyDTO> serviceDependents = serviceDTO.getDependents();
					if (serviceDependents != null) {
						for (ServiceDependencyDTO dependentDTO : serviceDependents) {
							pt.unl.fct.miei.usmanagement.manager.services.Service dependentService = ServiceMapper.MAPPER.toService(dependentDTO.getService(), context);
							servicesService.addIfNotPresent(dependentService);
						}
					}
					servicesService.addOrUpdateService(service);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic services with key={} and message={}: {}", key, serviceDTO.toString(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-host-metrics", autoStartup = "false")
	public void listenSimulatedHostMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys,
										   List<HostSimulatedMetricDTO> hostSimulatedMetricDTOs) {
		int i = 0;
		for (HostSimulatedMetricDTO hostSimulatedMetricDTO : hostSimulatedMetricDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, hostSimulatedMetricDTO.toString());
			HostSimulatedMetric hostSimulatedMetric = HostSimulatedMetricMapper.MAPPER.toHostSimulatedMetric(hostSimulatedMetricDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = hostSimulatedMetric.getId();
					hostSimulatedMetricsService.deleteHostSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(hostSimulatedMetricDTO.getField(), context));
					Set<CloudHostDTO> cloudHosts = hostSimulatedMetricDTO.getCloudHosts();
					if (cloudHosts != null) {
						for (CloudHostDTO cloudHostDTO : hostSimulatedMetricDTO.getCloudHosts()) {
							CloudHost cloudHost = CloudHostMapper.MAPPER.toCloudHost(cloudHostDTO, context);
							cloudHost = cloudHostsService.addIfNotPresent(cloudHost);
							cloudHost.addHostSimulatedMetric(hostSimulatedMetric);
						}
					}
					Set<EdgeHostDTO> edgeHosts = hostSimulatedMetricDTO.getEdgeHosts();
					if (edgeHosts != null) {
						for (EdgeHostDTO edgeHostDTO : edgeHosts) {
							EdgeHost edgeHost = EdgeHostMapper.MAPPER.toEdgeHost(edgeHostDTO, context);
							edgeHost = edgeHostsService.addIfNotPresent(edgeHost);
							edgeHost.addHostSimulatedMetric(hostSimulatedMetric);
						}
					}
					hostSimulatedMetricsService.addOrUpdateSimulatedMetric(hostSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-host-metrics with key={} and message={}: {}", key, hostSimulatedMetricDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-app-metrics", autoStartup = "false")
	public void listenSimulatedAppMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys,
										  List<AppSimulatedMetricDTO> appSimulatedMetricDTOs) {
		int i = 0;
		for (AppSimulatedMetricDTO appSimulatedMetricDTO : appSimulatedMetricDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, appSimulatedMetricDTO.toString());
			AppSimulatedMetric appSimulatedMetric = AppSimulatedMetricMapper.MAPPER.toAppSimulatedMetric(appSimulatedMetricDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = appSimulatedMetric.getId();
					appSimulatedMetricsService.deleteAppSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(appSimulatedMetricDTO.getField(), context));
					Set<AppDTO> apps = appSimulatedMetricDTO.getApps();
					if (apps != null) {
						for (AppDTO appDTO : apps) {
							pt.unl.fct.miei.usmanagement.manager.apps.App app = AppMapper.MAPPER.toApp(appDTO, context);
							app = appsService.addIfNotPresent(app);
							app.addAppSimulatedMetric(appSimulatedMetric);
						}
					}
					appSimulatedMetricsService.addOrUpdateSimulatedMetric(appSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-app-metrics with key={} and message={}: {}", key, appSimulatedMetricDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-service-metrics", autoStartup = "false")
	public void listenSimulatedServiceMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys,
											  List<ServiceSimulatedMetricDTO> serviceSimulatedMetricDTOs) {
		int i = 0;
		for (ServiceSimulatedMetricDTO serviceSimulatedMetricDTO : serviceSimulatedMetricDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, serviceSimulatedMetricDTO.toString());
			ServiceSimulatedMetric serviceSimulatedMetric = ServiceSimulatedMetricMapper.MAPPER.toServiceSimulatedMetric(serviceSimulatedMetricDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = serviceSimulatedMetric.getId();
					serviceSimulatedMetricsService.deleteServiceSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(serviceSimulatedMetricDTO.getField(), context));
					Set<ServiceDTO> services = serviceSimulatedMetricDTO.getServices();
					if (services != null) {
						for (ServiceDTO serviceDTO : services) {
							pt.unl.fct.miei.usmanagement.manager.services.Service service = ServiceMapper.MAPPER.toService(serviceDTO, context);
							service = servicesService.addIfNotPresent(service);
							service.addServiceSimulatedMetric(serviceSimulatedMetric);
						}
					}
					serviceSimulatedMetricsService.addOrUpdateSimulatedMetric(serviceSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-service-metrics with key={} and message={}: {}", key, serviceSimulatedMetricDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "simulated-container-metrics", autoStartup = "false")
	public void listenSimulatedContainerMetrics(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ContainerSimulatedMetricDTO> containerSimulatedMetricDTOs) {
		int i = 0;
		for (ContainerSimulatedMetricDTO containerSimulatedMetricDTO : containerSimulatedMetricDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, containerSimulatedMetricDTO.toString());
			ContainerSimulatedMetric containerSimulatedMetric = ContainerSimulatedMetricMapper.MAPPER.toContainerSimulatedMetric(containerSimulatedMetricDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = containerSimulatedMetric.getId();
					containerSimulatedMetricsService.deleteContainerSimulatedMetric(id);
				}
				else {
					fieldsService.addIfNotPresent(FieldMapper.MAPPER.toField(containerSimulatedMetricDTO.getField(), context));
					Set<ContainerDTO> containers = containerSimulatedMetricDTO.getContainers();
					if (containers != null) {
						for (ContainerDTO containerDTO : containers) {
							pt.unl.fct.miei.usmanagement.manager.containers.Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
							container = containersService.addIfNotPresent(container);
							container.addContainerSimulatedMetric(containerSimulatedMetric);
						}
					}
					containerSimulatedMetricsService.addOrUpdateSimulatedMetric(containerSimulatedMetric);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic simulated-container-metrics with key={} and message={}: {}", key, containerSimulatedMetricDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "host-rules", autoStartup = "false")
	public void listenHostRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<HostRuleDTO> hostRuleDTOs) {
		int i = 0;
		for (HostRuleDTO hostRuleDTO : hostRuleDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, hostRuleDTO.toString());
			HostRule hostRule = HostRuleMapper.MAPPER.toHostRule(hostRuleDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = hostRule.getId();
					hostRulesService.deleteRule(id);
				}
				else {
					listenDecisions(new ArrayList<>(Collections.nCopies(1, null)), Set.of(hostRuleDTO.getDecision()));
					Set<HostRuleConditionDTO> ruleConditions = hostRuleDTO.getConditions();
					if (ruleConditions != null && ruleConditions.size() > 0) {
						ruleConditions.stream().map(HostRuleConditionDTO::getCondition)
							.map(condition -> ConditionMapper.MAPPER.toCondition(condition, context))
							.collect(Collectors.toSet())
							.forEach(condition -> {
								operatorsService.addIfNotPresent(condition.getOperator());
								fieldsService.addIfNotPresent(condition.getField());
								valueModesService.addIfNotPresent(condition.getValueMode());
								conditionsService.addIfNotPresent(condition);
							});
						ruleConditions.stream().map(HostRuleConditionDTO::getRule)
							.map(rule -> HostRuleMapper.MAPPER.toHostRule(rule, context))
							.collect(Collectors.toSet())
							.forEach(hostRulesService::addIfNotPresent);
					}
					Set<CloudHostDTO> cloudHosts = hostRuleDTO.getCloudHosts();
					if (cloudHosts != null) {
						for (CloudHostDTO cloudHostDTO : cloudHosts) {
							CloudHost cloudHost = CloudHostMapper.MAPPER.toCloudHost(cloudHostDTO, context);
							cloudHost = cloudHostsService.addIfNotPresent(cloudHost);
							cloudHost.addRule(hostRule);
						}
					}
					Set<EdgeHostDTO> edgeHosts = hostRuleDTO.getEdgeHosts();
					if (edgeHosts != null) {
						for (EdgeHostDTO edgeHostDTO : edgeHosts) {
							EdgeHost edgeHost = EdgeHostMapper.MAPPER.toEdgeHost(edgeHostDTO, context);
							edgeHost = edgeHostsService.addIfNotPresent(edgeHost);
							edgeHost.addRule(hostRule);
						}
					}
					hostRulesService.addOrUpdateRule(hostRule);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic host-rules with key={} and message={}: {}", key, hostRuleDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "app-rules", autoStartup = "false")
	public void listenAppRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<AppRuleDTO> appRuleDTOs) {
		int i = 0;
		for (AppRuleDTO appRuleDTO : appRuleDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, appRuleDTO.toString());
			AppRule appRule = AppRuleMapper.MAPPER.toAppRule(appRuleDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = appRule.getId();
					appRulesService.deleteRule(id);
				}
				else {
					componentTypesService.addIfNotPresent(ComponentTypeMapper.MAPPER.toComponentType(appRuleDTO.getDecision().getComponentType(), context));
					decisionsService.addIfNotPresent(DecisionMapper.MAPPER.toDecision(appRuleDTO.getDecision(), context));
					Set<AppRuleConditionDTO> ruleConditions = appRuleDTO.getConditions();
					if (ruleConditions != null && ruleConditions.size() > 0) {
						ruleConditions.stream().map(AppRuleConditionDTO::getCondition)
							.map(condition -> ConditionMapper.MAPPER.toCondition(condition, context))
							.collect(Collectors.toSet())
							.forEach(condition -> {
								operatorsService.addIfNotPresent(condition.getOperator());
								fieldsService.addIfNotPresent(condition.getField());
								valueModesService.addIfNotPresent(condition.getValueMode());
								conditionsService.addIfNotPresent(condition);
							});
						ruleConditions.stream().map(AppRuleConditionDTO::getRule)
							.map(rule -> AppRuleMapper.MAPPER.toAppRule(rule, context))
							.collect(Collectors.toSet())
							.forEach(appRulesService::addIfNotPresent);
					}
					Set<AppDTO> apps = appRuleDTO.getApps();
					if (apps != null) {
						for (AppDTO appDTO : appRuleDTO.getApps()) {
							App app = AppMapper.MAPPER.toApp(appDTO, context);
							app = appsService.addIfNotPresent(app);
							app.addRule(appRule);
						}
					}
					appRulesService.addOrUpdateRule(appRule);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic app-rules with key={} and message={}: {}", key, appRuleDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "service-rules", autoStartup = "false")
	public void listenServiceRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ServiceRuleDTO> serviceRuleDTOs) {
		int i = 0;
		for (ServiceRuleDTO serviceRuleDTO : serviceRuleDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, serviceRuleDTO.toString());
			ServiceRule serviceRule = ServiceRuleMapper.MAPPER.toServiceRule(serviceRuleDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = serviceRule.getId();
					serviceRulesService.deleteRule(id);
				}
				else {
					listenDecisions(new ArrayList<>(Collections.nCopies(1, null)), Set.of(serviceRuleDTO.getDecision()));
					Set<ServiceRuleConditionDTO> ruleConditions = serviceRuleDTO.getConditions();
					if (ruleConditions != null && ruleConditions.size() > 0) {
						ruleConditions.stream().map(ServiceRuleConditionDTO::getCondition)
							.map(condition -> ConditionMapper.MAPPER.toCondition(condition, context))
							.collect(Collectors.toSet())
							.forEach(condition -> {
								operatorsService.addIfNotPresent(condition.getOperator());
								fieldsService.addIfNotPresent(condition.getField());
								valueModesService.addIfNotPresent(condition.getValueMode());
								conditionsService.addIfNotPresent(condition);
							});
						ruleConditions.stream().map(ServiceRuleConditionDTO::getRule)
							.map(rule -> ServiceRuleMapper.MAPPER.toServiceRule(rule, context))
							.collect(Collectors.toSet())
							.forEach(serviceRulesService::addIfNotPresent);
					}
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
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "container-rules", autoStartup = "false")
	public void listenContainerRules(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ContainerRuleDTO> containerRuleDTOs) {
		int i = 0;
		for (ContainerRuleDTO containerRuleDTO : containerRuleDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, containerRuleDTO.toString());
			ContainerRule containerRule = ContainerRuleMapper.MAPPER.toContainerRule(containerRuleDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = containerRule.getId();
					containerRulesService.deleteRule(id);
				}
				else {
					listenDecisions(new ArrayList<>(Collections.nCopies(1, null)), Set.of(containerRuleDTO.getDecision()));
					Set<ContainerRuleConditionDTO> ruleConditions = containerRuleDTO.getConditions();
					if (ruleConditions != null && ruleConditions.size() > 0) {
						ruleConditions.stream().map(ContainerRuleConditionDTO::getCondition)
							.map(condition -> ConditionMapper.MAPPER.toCondition(condition, context))
							.collect(Collectors.toSet())
							.forEach(condition -> {
								operatorsService.addIfNotPresent(condition.getOperator());
								fieldsService.addIfNotPresent(condition.getField());
								valueModesService.addIfNotPresent(condition.getValueMode());
								conditionsService.addIfNotPresent(condition);
							});
						ruleConditions.stream().map(ContainerRuleConditionDTO::getRule)
							.map(rule -> ContainerRuleMapper.MAPPER.toContainerRule(rule, context))
							.collect(Collectors.toSet())
							.forEach(containerRulesService::addIfNotPresent);
					}
					for (ContainerDTO containerDTO : containerRuleDTO.getContainers()) {
						Container container = ContainerMapper.MAPPER.toContainer(containerDTO, context);
						container = containersService.addIfNotPresent(container);
						container.addRule(containerRule);
					}
					containerRulesService.addOrUpdateRule(containerRule);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic container-rules with key={} and message={}: {}", key, containerRuleDTO, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Transactional(noRollbackFor = ConstraintViolationException.class)
	@KafkaListener(topics = "value-modes", autoStartup = "false")
	public void listenValueModes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<KafkaTopicKey> keys, Set<ValueModeDTO> valueModeDTOs) {
		int i = 0;
		for (ValueModeDTO valueModeDTO : valueModeDTOs) {
			KafkaTopicKey key = keys.get(i++);
			log.debug("Received key={} message={}", key, valueModeDTO.toString());
			ValueMode valueMode = ValueModeMapper.MAPPER.toValueMode(valueModeDTO, context);
			try {
				if (key != null && Objects.equal(key.getOperation(), "DELETE")) {
					Long id = valueMode.getId();
					valueModesService.deleteValueMode(id);
				}
				else {
					Set<ConditionDTO> conditions = valueModeDTO.getConditions();
					if (conditions != null && conditions.size() > 0) {
						listenConditions(new ArrayList<>(Collections.nCopies(conditions.size(), null)), conditions);
					}
					valueModesService.addOrUpdateValueMode(valueMode);
				}
			}
			catch (Exception e) {
				log.error("Error while processing topic value-modes with key={} and message={}: {}", key, valueModeDTO.toString(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
