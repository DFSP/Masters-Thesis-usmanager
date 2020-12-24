package pt.unl.fct.miei.usmanagement.manager.services.communication.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
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
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HeartbeatMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostDecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostEventMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostMonitoringLogMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostRuleMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.HostSimulatedMetricMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.NodeMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.OperatorMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceDecisionMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceEventMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceMonitoringLogMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceRuleMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ServiceSimulatedMetricMapper;
import pt.unl.fct.miei.usmanagement.manager.dtos.mapper.ValueModeMapper;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeat;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.kafka.KafkaBroker;
import pt.unl.fct.miei.usmanagement.manager.kafka.KafkaBrokers;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.services.communication.zookeeper.ZookeeperService;
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
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.services.valuemodes.ValueModesService;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;
import pt.unl.fct.miei.usmanagement.manager.zookeeper.Zookeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaService {

	private static final int PORT = 9092;
	private static final int MIN_POPULATE_SLEEP = 5000;
	private static final int MAX_POPULATE_SLEEP = 15000;

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
	private final ZookeeperService zookeeperService;
	private final ProducerFactory<String, Object> producerFactory;
	private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	private final KafkaBrokers kafkaBrokers;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String managerId;
	private final String kafkaBootstrapServers;
	private final AtomicLong increment;
	private final CycleAvoidingMappingContext context;
	private boolean needsPopulate;
	private boolean populated;

	public KafkaService(@Lazy ContainersService containersService,
						@Lazy AppsService appsService,
						@Lazy CloudHostsService cloudHostsService,
						@Lazy ComponentTypesService componentTypesService,
						@Lazy ConditionsService conditionsService,
						@Lazy DecisionsService decisionsService,
						@Lazy EdgeHostsService edgeHostsService,
						@Lazy ElasticIpsService elasticIpsService,
						@Lazy FieldsService fieldsService,
						@Lazy NodesService nodesService,
						@Lazy OperatorsService operatorsService,
						@Lazy ServicesService servicesService,
						@Lazy ServiceRulesService serviceRulesService,
						@Lazy HostSimulatedMetricsService hostSimulatedMetricsService,
						@Lazy AppSimulatedMetricsService appSimulatedMetricsService,
						@Lazy ServiceSimulatedMetricsService serviceSimulatedMetricsService,
						@Lazy ContainerSimulatedMetricsService containerSimulatedMetricsService,
						@Lazy HostRulesService hostRulesService,
						@Lazy AppRulesService appRulesService,
						@Lazy ContainerRulesService containerRulesService,
						@Lazy ValueModesService valueModesService,
						@Lazy ZookeeperService zookeeperService,
						@Lazy ProducerFactory<String, Object> producerFactory,
						KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
						KafkaBrokers kafkaBrokers,
						@Lazy KafkaTemplate<String, Object> kafkaTemplate,
						Environment environment) {
		this.appsService = appsService;
		this.cloudHostsService = cloudHostsService;
		this.componentTypesService = componentTypesService;
		this.conditionsService = conditionsService;
		this.decisionsService = decisionsService;
		this.edgeHostsService = edgeHostsService;
		this.fieldsService = fieldsService;
		this.nodesService = nodesService;
		this.operatorsService = operatorsService;
		this.serviceRulesService = serviceRulesService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.appSimulatedMetricsService = appSimulatedMetricsService;
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
		this.hostRulesService = hostRulesService;
		this.appRulesService = appRulesService;
		this.containerRulesService = containerRulesService;
		this.valueModesService = valueModesService;
		this.containersService = containersService;
		this.elasticIpsService = elasticIpsService;
		this.servicesService = servicesService;
		this.zookeeperService = zookeeperService;
		this.producerFactory = producerFactory;
		this.kafkaBrokers = kafkaBrokers;
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
		this.managerId = environment.getProperty(ContainerConstants.Environment.Manager.ID);
		this.kafkaBootstrapServers = environment.getProperty(ContainerConstants.Environment.Manager.KAFKA_BOOTSTRAP_SERVERS);
		this.increment = new AtomicLong();
		String id = environment.getProperty(ContainerConstants.Environment.Manager.ID);
		this.needsPopulate = id == null || id.equalsIgnoreCase("manager-master");
		this.populated = false;
		this.context = new CycleAvoidingMappingContext();
	}

	public KafkaBroker launchKafkaBroker(RegionEnum region) {
		return launchKafkaBrokers(List.of(region)).get(0);
	}

	public List<KafkaBroker> launchKafkaBrokers(List<RegionEnum> regions) {
		log.info("Launching kafka brokers at regions {}", regions);

		int previousKafkaBrokersCount = getKafkaBrokers().size();

		List<CompletableFuture<KafkaBroker>> futureKafkaBrokers = regions.stream().map(region -> {
			List<KafkaBroker> regionKafkaBrokers = getKafkaBroker(region);
			if (regionKafkaBrokers.size() > 0) {
				return CompletableFuture.completedFuture(regionKafkaBrokers.get(0));
			}
			else {
				HostAddress hostAddress = elasticIpsService.getHost(region);
				return launchKafkaBroker(hostAddress);
			}
		}).collect(Collectors.toList());

		CompletableFuture.allOf(futureKafkaBrokers.toArray(new CompletableFuture[0])).join();

		List<KafkaBroker> kafkaBrokers = new ArrayList<>();
		for (CompletableFuture<KafkaBroker> futureKafkaBroker : futureKafkaBrokers) {
			KafkaBroker kafkaBroker = futureKafkaBroker.join();
			kafkaBrokers.add(kafkaBroker);
		}

		if (previousKafkaBrokersCount == 0) {
			startConsumers();
			//populateTopics();
		}

		return kafkaBrokers;
	}

	public CompletableFuture<KafkaBroker> launchKafkaBroker(HostAddress hostAddress) {
		RegionEnum region = hostAddress.getRegion();
		List<Zookeeper> zookeepers = zookeeperService.getZookeepers(region);
		if (zookeepers.size() == 0) {
			zookeepers.add(zookeeperService.launchZookeeper(hostAddress));
		}
		else if (!Objects.equal(zookeepers.get(0).getContainer().getHostAddress(), hostAddress)) {
			zookeeperService.stopZookeeper(zookeepers.get(0).getId());
			zookeepers.add(zookeeperService.launchZookeeper(hostAddress));
		}
		long brokerId = increment.getAndIncrement() + 1;
		String zookeeperConnect = String.format("%s:%d", zookeepers.get(0).getContainer().getPrivateIpAddress(),
			servicesService.getService(ServiceConstants.Name.ZOOKEEPER).getDefaultExternalPort());
		String listeners = String.format("PLAINTEXT://:%d", PORT);
		String advertisedListeners = String.format("PLAINTEXT://%s:%d", hostAddress.getPublicIpAddress(), PORT);
		List<String> environment = List.of(
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_BROKER_ID, brokerId),
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_ZOOKEEPER_CONNECT, zookeeperConnect),
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_LISTENERS, listeners),
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_ADVERTISED_LISTENERS, advertisedListeners),
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_CREATE_TOPICS, topics())
		);
		Map<String, String> labels = Map.of(
			ContainerConstants.Label.KAFKA_BROKER_ID, String.valueOf(brokerId)
		);
		Container container = containersService.launchContainer(hostAddress, ServiceConstants.Name.KAFKA, environment, labels);
		return CompletableFuture.completedFuture(saveKafkaBroker(container));
	}

	public List<KafkaBroker> getKafkaBrokers() {
		return kafkaBrokers.findAll();
	}

	public String getKafkaBrokersHosts() {
		return kafkaBootstrapServers != null
			? kafkaBootstrapServers
			: elasticIpsService.getElasticIps().stream()
			.filter(elasticIp -> elasticIp.getAssociationId() != null)
			.map(elasticIp -> String.format("%s:%d", elasticIp.getPublicIp(), PORT))
			.collect(Collectors.joining(","));
	}

	private List<KafkaBroker> getKafkaBroker(RegionEnum region) {
		return kafkaBrokers.getByRegion(region);
	}

	public KafkaBroker getKafkaBroker(Long id) {
		return kafkaBrokers.findByBrokerId(id).orElseThrow(() ->
			new EntityNotFoundException(KafkaBroker.class, "id", String.valueOf(id)));
	}

	public KafkaBroker getKafkaBrokerByContainer(Container container) {
		return kafkaBrokers.getByContainer(container).orElseThrow(() ->
			new EntityNotFoundException(KafkaBroker.class, "container", container.getId()));
	}

	public void stopKafkaBroker(Long id) {
		KafkaBroker kafkaBroker = getKafkaBroker(id);
		String containerId = kafkaBroker.getContainer().getId();
		kafkaBrokers.delete(kafkaBroker);
		containersService.stopContainer(containerId);
		if (!hasKafkaBrokers()) {
			kafkaListenerEndpointRegistry.stop();
		}
	}

	public void deleteKafkaBrokerByContainer(Container container) {
		KafkaBroker kafkaBroker = getKafkaBrokerByContainer(container);
		kafkaBrokers.delete(kafkaBroker);
		if (!hasKafkaBrokers()) {
			kafkaListenerEndpointRegistry.stop();
		}
	}

	public void reset() {
		kafkaBrokers.deleteAll();
	}

	public KafkaBroker saveKafkaBroker(Container container) {
		long brokerId = Long.parseLong(container.getLabels().get(ContainerConstants.Label.KAFKA_BROKER_ID));
		return kafkaBrokers.findByBrokerId(brokerId).orElseGet(() ->
			kafkaBrokers.save(KafkaBroker.builder().brokerId(brokerId).container(container).region(container.getRegion()).build()));
	}

	public boolean hasKafkaBroker(Container container) {
		return kafkaBrokers.hasKafkaBrokerByContainer(container.getId());
	}

	public boolean hasKafkaBrokers() {
		return kafkaBrokers.hasKafkaBrokers();
	}

	private void startConsumers() {
		kafkaListenerEndpointRegistry.start();
	}

	private void populateTopics() {
		Random random = new Random();
		do {
			try {
				for (Map.Entry<String, Supplier<?>> topicKeyValue : topicsValues().entrySet()) {
					String topic = topicKeyValue.getKey();
					List<?> values = (List<?>) topicKeyValue.getValue().get();
					for (Object value : values) {
						log.info("Sending {} to topic={}", value, topic);
						kafkaTemplate.send(topic, value);
					}
				}
				populated = true;
				log.info("Finished populating kafka topics");
			}
			catch (KafkaException e) {
				String message = e.getMessage();
				int randomSleep = random.nextInt(MAX_POPULATE_SLEEP - MIN_POPULATE_SLEEP) + MIN_POPULATE_SLEEP;
				log.error("Failed to populate kafka: {}... Retrying in {} ms", message, randomSleep);
				Timing.sleep(randomSleep, TimeUnit.MILLISECONDS);
			}
		} while (!populated);
	}

	private Map<String, Supplier<?>> topicsValues() {
		Map<String, Supplier<?>> topicsValues = new HashMap<>();
		topicsValues.put("apps", () ->
			appsService.getApps().stream().map(app -> AppMapper.MAPPER.fromApp(app, context)).collect(Collectors.toList()));
		topicsValues.put("cloud-hosts", () ->
			cloudHostsService.getCloudHosts().stream().map(cloudHost -> CloudHostMapper.MAPPER.fromCloudHost(cloudHost, context)).collect(Collectors.toList()));
		topicsValues.put("component-types", () ->
			componentTypesService.getComponentTypes().stream().map(componentType -> ComponentTypeMapper.MAPPER.fromComponentType(componentType, context)).collect(Collectors.toList()));
		topicsValues.put("conditions", () ->
			conditionsService.getConditions().stream().map(condition -> ConditionMapper.MAPPER.fromCondition(condition, context)).collect(Collectors.toList()));
		topicsValues.put("containers", () ->
			containersService.getContainers().stream().map(container -> ContainerMapper.MAPPER.fromContainer(container, context)).collect(Collectors.toList()));
		// TODO check where kafka is sent on decisions
		topicsValues.put("decisions", () ->
			decisionsService.getDecisions().stream().map(decision -> DecisionMapper.MAPPER.fromDecision(decision, context)).collect(Collectors.toList()));
		topicsValues.put("edge-hosts", () ->
			edgeHostsService.getEdgeHosts().stream().map(edgeHost -> EdgeHostMapper.MAPPER.fromEdgeHost(edgeHost, context)).collect(Collectors.toList()));
		topicsValues.put("eips", () ->
			elasticIpsService.getElasticIps().stream().map(elasticIp -> ElasticIpMapper.MAPPER.fromElasticIp(elasticIp, context)).collect(Collectors.toList()));
		topicsValues.put("fields", () ->
			fieldsService.getFields().stream().map(field -> FieldMapper.MAPPER.fromField(field, context)).collect(Collectors.toList()));
		topicsValues.put("nodes", () ->
			nodesService.getNodes().stream().map(node -> NodeMapper.MAPPER.fromNode(node, context)).collect(Collectors.toList()));
		topicsValues.put("operators", () ->
			operatorsService.getOperators().stream().map(operator -> OperatorMapper.MAPPER.fromOperator(operator, context)).collect(Collectors.toList()));
		topicsValues.put("services", () ->
			servicesService.getServices().stream().map(service -> ServiceMapper.MAPPER.fromService(service, context)).collect(Collectors.toList()));
		topicsValues.put("simulated-host-metrics", () ->
			hostSimulatedMetricsService.getHostSimulatedMetrics().stream().map(metric -> HostSimulatedMetricMapper.MAPPER.fromHostSimulatedMetric(metric, context)).collect(Collectors.toList()));
		topicsValues.put("simulated-app-metrics", () ->
			appSimulatedMetricsService.getAppSimulatedMetrics().stream().map(metric -> AppSimulatedMetricMapper.MAPPER.fromAppSimulatedMetric(metric, context)).collect(Collectors.toList()));
		topicsValues.put("simulated-service-metrics", () ->
			serviceSimulatedMetricsService.getServiceSimulatedMetrics().stream().map(metric -> ServiceSimulatedMetricMapper.MAPPER.fromServiceSimulatedMetric(metric, context)).collect(Collectors.toList()));
		topicsValues.put("simulated-container-metrics", () ->
			containerSimulatedMetricsService.getContainerSimulatedMetrics().stream().map(metric -> ContainerSimulatedMetricMapper.MAPPER.fromContainerSimulatedMetric(metric, context)).collect(Collectors.toList()));
		topicsValues.put("host-rules", () ->
			hostRulesService.getRules().stream().map(rule -> HostRuleMapper.MAPPER.fromHostRule(rule, context)).collect(Collectors.toList()));
		topicsValues.put("app-rules", () ->
			appRulesService.getRules().stream().map(rule -> AppRuleMapper.MAPPER.fromAppRule(rule, context)).collect(Collectors.toList()));
		topicsValues.put("service-rules", () ->
			serviceRulesService.getRules().stream().map(rule -> ServiceRuleMapper.MAPPER.fromServiceRule(rule, context)).collect(Collectors.toList()));
		topicsValues.put("container-rules", () ->
			containerRulesService.getRules().stream().map(rule -> ContainerRuleMapper.MAPPER.fromContainerRule(rule, context)).collect(Collectors.toList()));
		topicsValues.put("value-modes", () ->
			valueModesService.getValueModes().stream().map(valueMode -> ValueModeMapper.MAPPER.fromValueMode(valueMode, context)).collect(Collectors.toList()));
		return topicsValues;
	}

	private String topics() {
		String masterManagerTopics = "apps:1:1,cloud-hosts:1:1,component-types:1:1,conditions:1:1,containers:1:1,decisions:1:1,"
			+ "edge-hosts:1:1,eips:1:1,fields:1:1,nodes:1:1,operators:1:1,services:1:1,simulated-host-metrics:1:1,"
			+ "simulated-app-metrics:1:1,simulated-service-metrics:1:1,simulated-container-metrics:1:1,host-rules:1:1,"
			+ "app-rules:1:1,service-rules:1:1,container-rules:1:1,value-modes:1:1";
		String workerManagerTopics = "host-events:1:1,service-events:1:1,host-monitoring-logs:1:1,service-monitoring-logs:1:1,"
			+ "host-decisions:1:1,service-decisions:1:1";
		return masterManagerTopics + "," + workerManagerTopics;
	}

	public void start() {
		kafkaListenerEndpointRegistry.start();
	}

	public void stop() {
		producerFactory.reset();
		kafkaListenerEndpointRegistry.stop();
	}

	public void sendApp(App app) {
		send("apps", AppMapper.MAPPER.fromApp(app, context), app.getId());
	}

	public void sendDeleteApp(App app) {
		delete("apps", new AppDTO(app.getId()));
	}

	public void sendCloudHost(CloudHost cloudHost) {
		send("cloud-hosts", CloudHostMapper.MAPPER.fromCloudHost(cloudHost, context), cloudHost.getId());
	}

	public void sendDeleteCloudHost(CloudHost cloudHost) {
		delete("cloud-hosts", new CloudHostDTO(cloudHost.getId()));
	}

	public void sendComponentType(ComponentType componentType) {
		send("component-types", ComponentTypeMapper.MAPPER.fromComponentType(componentType, context), componentType.getId());
	}

	public void sendDeleteComponentType(ComponentType componentType) {
		delete("component-types", new ComponentTypeDTO(componentType.getId()));
	}

	public void sendCondition(Condition condition) {
		send("conditions", ConditionMapper.MAPPER.fromCondition(condition, context), condition.getId());
	}

	public void sendDeleteCondition(Condition condition) {
		delete("conditions", new ConditionDTO(condition.getId()));
	}

	public void sendContainer(Container container) {
		send("containers", ContainerMapper.MAPPER.fromContainer(container, context), container.getId());
	}

	public void sendDeleteContainer(Container container) {
		delete("containers", new ContainerDTO(container.getId()));
	}

	public void sendDecision(Decision decision) {
		send("decisions", DecisionMapper.MAPPER.fromDecision(decision, context), decision.getId());
	}

	public void sendDeleteDecision(Decision decision) {
		delete("decisions", new DecisionDTO(decision.getId()));
	}

	public void sendEdgeHost(EdgeHost edgeHost) {
		send("edge-hosts", EdgeHostMapper.MAPPER.fromEdgeHost(edgeHost, context), edgeHost.getId());
	}

	public void sendDeleteEdgeHost(EdgeHost edgeHost) {
		delete("edge-hosts", new EdgeHostDTO(edgeHost.getId()));
	}

	public void sendElasticIp(ElasticIp elasticIp) {
		send("eips", ElasticIpMapper.MAPPER.fromElasticIp(elasticIp, context), elasticIp.getId());
	}

	public void sendDeleteElasticIp(ElasticIp elasticIp) {
		delete("eips", new ElasticIpDTO(elasticIp.getId()));
	}

	public void sendField(Field field) {
		send("fields", FieldMapper.MAPPER.fromField(field, context), field.getId());
	}

	public void sendDeleteField(Field field) {
		delete("fields", new FieldDTO(field.getId()));
	}

	public void sendNode(Node node) {
		send("nodes", NodeMapper.MAPPER.fromNode(node, context), node.getId());
	}

	public void sendDeleteNode(Node node) {
		delete("nodes", new NodeDTO(node.getId()));
	}

	public void sendOperator(Operator operator) {
		send("operators", OperatorMapper.MAPPER.fromOperator(operator, context), operator.getId());
	}

	public void sendDeleteOperator(Operator operator) {
		delete("operators", new OperatorDTO(operator.getId()));
	}

	public void sendService(pt.unl.fct.miei.usmanagement.manager.services.Service service) {
		send("services", ServiceMapper.MAPPER.fromService(service, context), service.getServiceName());
	}

	public void sendDeleteService(pt.unl.fct.miei.usmanagement.manager.services.Service service) {
		delete("services", new ServiceDTO(service.getServiceName()));
	}

	public void sendHostSimulatedMetric(HostSimulatedMetric hostSimulatedMetric) {
		send("simulated-host-metrics", HostSimulatedMetricMapper.MAPPER.fromHostSimulatedMetric(hostSimulatedMetric, context), hostSimulatedMetric.getId());
	}

	public void sendDeleteHostSimulatedMetric(HostSimulatedMetric hostSimulatedMetric) {
		delete("simulated-host-metrics", new HostSimulatedMetricDTO(hostSimulatedMetric.getId()));
	}

	public void sendAppSimulatedMetric(AppSimulatedMetric appSimulatedMetric) {
		send("simulated-app-metrics", AppSimulatedMetricMapper.MAPPER.fromAppSimulatedMetric(appSimulatedMetric, context), appSimulatedMetric.getId());
	}

	public void sendDeleteAppSimulatedMetric(AppSimulatedMetric appSimulatedMetric) {
		delete("simulated-app-metrics", new AppSimulatedMetricDTO(appSimulatedMetric.getId()));
	}

	public void sendServiceSimulatedMetric(ServiceSimulatedMetric serviceSimulatedMetric) {
		send("simulated-service-metrics", ServiceSimulatedMetricMapper.MAPPER.fromServiceSimulatedMetric(serviceSimulatedMetric, context), serviceSimulatedMetric.getId());
	}

	public void sendDeleteServiceSimulatedMetric(ServiceSimulatedMetric serviceSimulatedMetric) {
		delete("simulated-service-metrics", new ServiceSimulatedMetricDTO(serviceSimulatedMetric.getId()));
	}

	public void sendContainerSimulatedMetric(ContainerSimulatedMetric containerSimulatedMetric) {
		send("simulated-container-metrics", ContainerSimulatedMetricMapper.MAPPER.fromContainerSimulatedMetric(containerSimulatedMetric, context), containerSimulatedMetric.getId());
	}

	public void sendDeleteContainerSimulatedMetric(ContainerSimulatedMetric containerSimulatedMetric) {
		delete("simulated-container-metrics", new ContainerSimulatedMetricDTO(containerSimulatedMetric.getId()));
	}

	public void sendHostRule(HostRule hostRule) {
		send("host-rules", HostRuleMapper.MAPPER.fromHostRule(hostRule, context), hostRule.getId());
	}

	public void sendDeleteHostRule(HostRule hostRule) {
		delete("host-rules", new HostRuleDTO(hostRule.getId()));
	}

	public void sendAppRule(AppRule appRule) {
		send("app-rules", AppRuleMapper.MAPPER.fromAppRule(appRule, context), appRule.getId());
	}

	public void sendDeleteAppRule(AppRule appRule) {
		delete("app-rules", new AppRuleDTO(appRule.getId()));
	}

	public void sendServiceRule(ServiceRule serviceRule) {
		send("service-rules", ServiceRuleMapper.MAPPER.fromServiceRule(serviceRule, context), serviceRule.getId());
	}

	public void sendDeleteServiceRule(ServiceRule serviceRule) {
		delete("service-rules", new ServiceRuleDTO(serviceRule.getId()));
	}

	public void sendContainerRule(ContainerRule containerRule) {
		send("container-rules", ContainerRuleMapper.MAPPER.fromContainerRule(containerRule, context), containerRule.getId());
	}

	public void sendDeleteContainerRule(ContainerRule containerRule) {
		delete("container-rules", new ContainerRuleDTO(containerRule.getId()));
	}

	public void sendValueMode(ValueMode valueMode) {
		send("value-modes", ValueModeMapper.MAPPER.fromValueMode(valueMode, context), valueMode.getId());
	}

	public void sendDeleteValueMode(ValueMode valueMode) {
		delete("value-modes", new ValueModeDTO(valueMode.getId()));
	}

	public void sendHostEvent(HostEvent hostEvent) {
		send("host-events", HostEventMapper.MAPPER.fromHostEvent(hostEvent, context), hostEvent.getId());
	}

	public void sendServiceEvent(ServiceEvent serviceEvent) {
		send("service-events", ServiceEventMapper.MAPPER.fromServiceEvent(serviceEvent, context), serviceEvent.getId());
	}

	public void sendHostMonitoringLog(HostMonitoringLog hostMonitoringLog) {
		send("host-monitoring-logs", HostMonitoringLogMapper.MAPPER.fromHostMonitoringLog(hostMonitoringLog, context), hostMonitoringLog.getId());
	}

	public void sendServiceMonitoringLog(ServiceMonitoringLog serviceMonitoringLog) {
		send("service-monitoring-logs", ServiceMonitoringLogMapper.MAPPER.fromServiceMonitoringLog(serviceMonitoringLog, context), serviceMonitoringLog.getId());
	}

	public void sendHostDecision(HostDecision hostDecision) {
		send("host-decisions", HostDecisionMapper.MAPPER.fromHostDecision(hostDecision, context), hostDecision.getId());
	}

	public void sendServiceDecision(ServiceDecision serviceDecision) {
		send("service-decisions", ServiceDecisionMapper.MAPPER.fromServiceDecision(serviceDecision, context), serviceDecision.getId());
	}

	public void sendHeartbeat(Heartbeat heartbeat) {
		send("heartbeats", HeartbeatMapper.MAPPER.fromHeartbeat(heartbeat, context), heartbeat.getId());
	}

	public void send(String topic, Object message) {
		send(topic, message, null);
	}

	public void send(String topic, Object message, Object id) {
		boolean hasKafkaBrokers = hasKafkaBrokers();
		if (hasKafkaBrokers && (!needsPopulate || populated)) {
			log.info("Sending {} to topic={}", ToStringBuilder.reflectionToString(message), topic);
			kafkaTemplate.send(topic, message);
		}
		else {
			log.warn("Not sending message id={} to topic={} because hasKafkaBrokers={} and populated={}",
				id, topic, hasKafkaBrokers, populated);
		}
	}

	public void delete(String topic, Object id) {
		boolean hasKafkaBrokers = hasKafkaBrokers();
		if (hasKafkaBrokers && (!needsPopulate || populated)) {
			log.info("Sending DELETE id={} request to topic={}", id, topic);
			kafkaTemplate.send(topic, "DELETE", id);
		}
		else {
			log.warn("Not sending DELETE id={} request to topic={} because hasKafkaBrokers={} and populated={}",
				id, topic, hasKafkaBrokers, populated);
		}
	}

}