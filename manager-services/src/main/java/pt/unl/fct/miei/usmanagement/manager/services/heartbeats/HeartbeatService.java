package pt.unl.fct.miei.usmanagement.manager.services.heartbeats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeat;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeats;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagerProperties;

import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Service
public class HeartbeatService {

	private static final int HEARTBEAT_INTERVAL = 15000;

	private final Heartbeats heartbeats;
	private final KafkaService kafkaService;
	private final Environment environment;
	private final int heartbeatInterval;

	public HeartbeatService(Heartbeats heartbeats, KafkaService kafkaService, Environment environment,
							WorkerManagerProperties workerManagerProperties) {
		this.heartbeats = heartbeats;
		this.kafkaService = kafkaService;
		this.environment = environment;
		this.heartbeatInterval = workerManagerProperties.getHeartbeatInterval();
	}

	public Optional<Heartbeat> lastHeartbeat(String nodeId) {
		return heartbeats.findById(nodeId);
	}

	public List<Heartbeat> lastHeartbeats() {
		return heartbeats.findAll();
	}

	public Heartbeat saveHeartbeat(Heartbeat heartbeat) {
		log.info("Saving app {}", heartbeat.getId());
		return heartbeats.save(heartbeat);
	}

	public void startHeartbeat() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				String id = environment.getProperty(ContainerConstants.Environment.Manager.ID);
				kafkaService.sendHeartbeat(Heartbeat.builder().id(id).build());
			}
		}, heartbeatInterval);
	}
}
