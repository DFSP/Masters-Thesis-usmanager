package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.kafka.KafkaBroker;
import pt.unl.fct.miei.usmanagement.manager.kafka.KafkaBrokers;
import pt.unl.fct.miei.usmanagement.manager.management.communication.zookeeper.ZookeeperService;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.zookeeper.Zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaService {

	private final ContainersService containersService;
	private final ElasticIpsService elasticIpsService;
	private final ServicesService servicesService;
	private final ZookeeperService zookeeperService;

	private final KafkaBrokers kafkaBrokers;

	private final AtomicLong idCounter;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public KafkaService(@Lazy ContainersService containersService, ElasticIpsService elasticIpsService,
						ServicesService servicesService, ZookeeperService zookeeperService, KafkaBrokers kafkaBrokers,
						KafkaTemplate<String, Object> kafkaTemplate) {
		this.containersService = containersService;
		this.elasticIpsService = elasticIpsService;
		this.servicesService = servicesService;
		this.zookeeperService = zookeeperService;
		this.kafkaBrokers = kafkaBrokers;
		this.idCounter = new AtomicLong();
		this.kafkaTemplate = kafkaTemplate;
	}

	public KafkaBroker launchKafkaBroker(RegionEnum region) {
		return launchKafkaBrokers(List.of(region)).get(0);
	}

	public List<KafkaBroker> launchKafkaBrokers(List<RegionEnum> regions) {
		log.info("Launching kafka brokers at regions {}", regions);

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
			this.populate();
			kafkaBrokers.add(kafkaBroker);
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
		/*String kafkaBrokerHosts = getKafkaBrokers();*/
		long brokerId = idCounter.getAndIncrement();
		String zookeeperConnect = String.format("%s:%d", zookeepers.get(0).getContainer().getPrivateIpAddress(),
			servicesService.getService(ServiceConstants.Name.ZOOKEEPER).getDefaultExternalPort());
		List<String> environment = List.of(
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_BROKER_ID, brokerId),
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_ADVERTISED_HOST_NAME, hostAddress.getPublicIpAddress()),
			/*String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_CREATE_TOPICS, topics),*/
			String.format("%s=%s", ContainerConstants.Environment.Kafka.KAFKA_ZOOKEEPER_CONNECT, zookeeperConnect)
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
		return getKafkaBrokers().stream().map(KafkaBroker::getContainer)
			.map(Container::getHostAddress).map(HostAddress::getPublicIpAddress).collect(Collectors.joining(","));
	}

	private List<KafkaBroker> getKafkaBroker(RegionEnum region) {
		return kafkaBrokers.getByRegion(region);
	}

	public KafkaBroker getKafkaBroker(Long id) {
		return kafkaBrokers.findById(id).orElseThrow(() ->
			new EntityNotFoundException(KafkaBroker.class, "id", String.valueOf(id)));
	}

	public KafkaBroker getKafkaBrokerByContainer(Container container) {
		return kafkaBrokers.getByContainer(container).orElseThrow(() ->
			new EntityNotFoundException(KafkaBroker.class, "container", container.getId()));
	}

	private void populate() {
		for (Map.Entry<String, Supplier<?>> topicKeyValue : topics().entrySet()) {
			String topic = topicKeyValue.getKey();
			List<?> values = (List<?>) topicKeyValue.getValue().get();
			values.forEach(value -> this.kafkaTemplate.send(topic, value));
		}
	}

	public Map<String, Supplier<?>> topics() {
		return Map.of("services", servicesService::getServices);
	}

	public void stopKafkaBroker(Long id) {
		KafkaBroker kafkaBroker = getKafkaBroker(id);
		String containerId = kafkaBroker.getContainer().getId();
		kafkaBrokers.delete(kafkaBroker);
		containersService.stopContainer(containerId);
	}

	public void deleteKafkaBrokerByContainer(Container container) {
		KafkaBroker kafkaBroker = getKafkaBrokerByContainer(container);
		kafkaBrokers.delete(kafkaBroker);
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
}