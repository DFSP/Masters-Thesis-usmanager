package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.kafka.KafkaBroker;
import pt.unl.fct.miei.usmanagement.manager.kafka.KafkaBrokers;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.management.communication.zookeeper.ZookeeperService;
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
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLogs;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLogs;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;
import pt.unl.fct.miei.usmanagement.manager.zookeeper.Zookeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaService {

	private static final int PORT = 9092;
	private static final int DELAY_BEFORE_STARTUP = 45000;
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
	private final AtomicLong increment;
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
						ZookeeperService zookeeperService,
						@Lazy ProducerFactory<String, Object> producerFactory,
						KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
						KafkaBrokers kafkaBrokers,
						@Lazy KafkaTemplate<String, Object> kafkaTemplate) {
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
		this.increment = new AtomicLong();
		this.populated = false;
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
			new Timer("populate-kafka", true).schedule(new TimerTask() {
				@Override
				public void run() {
					populateTopics();
				}
			}, DELAY_BEFORE_STARTUP);
		}

		return kafkaBrokers;
	}

	@Async
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
		return elasticIpsService.getElasticIps().stream()
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
			this.stop();
		}
	}

	public void deleteKafkaBrokerByContainer(Container container) {
		KafkaBroker kafkaBroker = getKafkaBrokerByContainer(container);
		kafkaBrokers.delete(kafkaBroker);
		if (!hasKafkaBrokers()) {
			this.stop();
		}
	}

	public void reset() {
		kafkaBrokers.deleteAll();
	}

	public KafkaBroker saveKafkaBroker(Container container) {
		long brokerId = Long.parseLong(container.getLabels().get(ContainerConstants.Label.KAFKA_BROKER_ID));
		return kafkaBrokers.save(KafkaBroker.builder().brokerId(brokerId).container(container).region(container.getRegion()).build());
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
					kafkaTemplate.send(topic, values.get(0));
					/*values.forEach(value -> kafkaTemplate.send(topic, value));*/
				}
				populated = true;
			}
			catch (KafkaException e) {
				String message = e.getMessage();
				/*if (message != null && message.contains("not present in metadata after")) {
					producerFactory.reset();
				}*/
				int randomSleep = random.nextInt(MAX_POPULATE_SLEEP - MIN_POPULATE_SLEEP) + MIN_POPULATE_SLEEP;
				log.error("Failed to populate kafka: {}. Retrying in {} ms", message, randomSleep);
				Timing.sleep(randomSleep, TimeUnit.MILLISECONDS);
			}
		} while (!populated);
	}

	private Map<String, Supplier<?>> topicsValues() {
		Map<String, Supplier<?>> topicsValues = new HashMap<>();

		topicsValues.put("apps", appsService::getApps);
		/*topicsValues.put("cloud-hosts", cloudHostsService::getCloudHosts);
		topicsValues.put("component-types", componentTypesService::getComponentTypes);
		topicsValues.put("conditions", conditionsService::getConditions);
		topicsValues.put("containers", containersService::getContainers);
		topicsValues.put("decisions", decisionsService::getDecisions);
		topicsValues.put("edge-hosts", edgeHostsService::getEdgeHosts);
		topicsValues.put("eips", elasticIpsService::getElasticIps);
		topicsValues.put("fields", fieldsService::getFields);
		topicsValues.put("nodes", nodesService::getNodes);
		topicsValues.put("operators", operatorsService::getOperators);
		topicsValues.put("services", servicesService::getServices);
		topicsValues.put("simulated-host-metrics", hostSimulatedMetricsService::getHostSimulatedMetrics);
		topicsValues.put("simulated-app-metrics", appSimulatedMetricsService::getAppSimulatedMetrics);
		topicsValues.put("simulated-service-metrics", serviceSimulatedMetricsService::getServiceSimulatedMetrics);
		topicsValues.put("simulated-container-metrics", containerSimulatedMetricsService::getContainerSimulatedMetrics);
		topicsValues.put("host-rules", hostRulesService::getRules);
		topicsValues.put("app-rules", appRulesService::getRules);
		topicsValues.put("service-rules", serviceRulesService::getRules);
		topicsValues.put("container-rules", containerRulesService::getRules);
		topicsValues.put("value-modes", servicesService::getServices);*/

		return topicsValues;
	}

	private String topics() {
		return "apps:1:1,cloud-hosts:1:1,component-types:1:1,conditions:1:1,containers:1:1,decisions:1:1,"
			+ "edge-hosts:1:1,eips:1:1,fields:1:1,nodes:1:1,operators:1:1,services:1:1,simulated-host-metrics:1:1,"
			+ "simulated-app-metrics:1:1,simulated-service-metrics:1:1,simulated-container-metrics:1:1,host-rules:1:1,"
			+ "app-rules:1:1,service-rules:1:1,container-rules:1:1,value-modes:1:1";
	}

	@Async
	public void sendApp(App app) {
		if (hasKafkaBrokers() && populated) {
			AppMessage appMessage = new AppMessage(app);
			log.info("Sending app message to kafka: {}", appMessage.toString());
			kafkaTemplate.send("apps", appMessage);
		}
		else {
			log.warn("Not sending app {} to kafka: hasKafkaBrokers={} populated={}", app.getName(), hasKafkaBrokers(), populated);
		}
	}

	@Async
	public void deleteApp(App app) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("apps", "DELETE", app.getId());
		}
	}

	@Async
	public void sendService(pt.unl.fct.miei.usmanagement.manager.services.Service service) {
		if (hasKafkaBrokers() && populated) {
			ServiceMessage serviceMessage = new ServiceMessage(service);
			log.info("Sending service message to kafka: {}", serviceMessage.toString());
			kafkaTemplate.send("services", serviceMessage);
		}
		else {
			log.warn("Not sending service {} to kafka: hasKafkaBrokers={} populated={}", service.getServiceName(), hasKafkaBrokers(), populated);
		}
	}

	@Async
	public void deleteService(pt.unl.fct.miei.usmanagement.manager.services.Service service) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("services", "DELETE", service.getId());
		}
	}

	@Async
	public void sendContainer(Container container) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("containers", container);
		}
	}

	@Async
	public void deleteContainer(Container container) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("containers", "DELETE", container.getId());
		}
	}

	@Async
	public void sendNode(Node node) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("nodes", node);
		}
	}

	@Async
	public void deleteNode(Node node) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("nodes", "DELETE", node.getId());
		}
	}

	@Async
	public void sendHostEvent(HostEvent hostEvent) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("host-events", hostEvent);
		}
	}

	@Async
	public void sendServiceEvent(ServiceEvent serviceEvent) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("service-events", serviceEvent);
		}
	}

	@Async
	public void sendHostMonitoringLogs(HostMonitoringLogs hostMonitoringLogs) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("host-monitoring-logs", hostMonitoringLogs);
		}
	}

	@Async
	public void sendServiceMonitoringLogs(ServiceMonitoringLogs serviceMonitoringLogs) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("service-monitoring-logs", serviceMonitoringLogs);
		}
	}

	@Async
	public void sendHostDecision(HostDecision hostDecision) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("host-decision", hostDecision);
		}
	}

	@Async
	public void sendServiceDecision(ServiceDecision serviceDecision) {
		if (hasKafkaBrokers() && populated) {
			kafkaTemplate.send("service-decision", serviceDecision);
		}
	}

	@KafkaListener(groupId = "manager", topics = "apps", autoStartup = "false")
	public void listenApps(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload AppMessage appMessage) {
		log.info("key={} message={}", key, appMessage.toString());
	}

	@KafkaListener(groupId = "manager", topics = "services", autoStartup = "false")
	public void listenServices(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
							   @Payload ServiceMessage serviceMessage) {
		log.info("key={} value={}", key, serviceMessage.toString());
	}

	@KafkaListener(groupId = "manager", topics = "containers", autoStartup = "false")
	public void listenContainers(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload Container container) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(container));
	}

	@KafkaListener(groupId = "manager", topics = "nodes", autoStartup = "false")
	public void listenNodes(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload Node node) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(node));
	}

	@KafkaListener(groupId = "manager", topics = "host-events", autoStartup = "false")
	public void listenHostEvents(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload HostEvent hostEvent) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(hostEvent));
	}

	@KafkaListener(groupId = "manager", topics = "service-events", autoStartup = "false")
	public void listenServiceEvents(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
									@Payload ServiceEvent serviceEvent) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(serviceEvent));
	}

	@KafkaListener(groupId = "manager", topics = "host-monitoring-logs", autoStartup = "false")
	public void listenHostMonitoringLogs(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
										 @Payload HostMonitoringLog hostMonitoringLog) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(hostMonitoringLog));
	}

	@KafkaListener(groupId = "manager", topics = "service-monitoring-logs", autoStartup = "false")
	public void listenServiceMonitoringLogs(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
											@Payload ServiceMonitoringLog serviceMonitoringLog) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(serviceMonitoringLog));
	}

	@KafkaListener(groupId = "manager", topics = "host-decisions", autoStartup = "false")
	public void listenHostDecisions(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
									@Payload HostDecision hostDecision) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(hostDecision));
	}

	@KafkaListener(groupId = "manager", topics = "service-decisions", autoStartup = "false")
	public void listenServiceDecisions(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
									   @Payload ServiceDecision serviceDecision) {
		log.info("key={} value={}", key, ToStringBuilder.reflectionToString(serviceDecision));
	}

	public void stop() {
		producerFactory.reset();
		kafkaListenerEndpointRegistry.stop();
	}

}