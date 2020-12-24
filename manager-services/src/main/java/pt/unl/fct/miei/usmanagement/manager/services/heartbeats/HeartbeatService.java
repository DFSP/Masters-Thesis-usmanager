package pt.unl.fct.miei.usmanagement.manager.services.heartbeats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeat;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeats;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Service
public class HeartbeatService {

	private final Heartbeats heartbeats;
	private final KafkaService kafkaService;
	private final WorkerManagersService workerManagersService;
	private final Environment environment;
	private final int heartbeatInterval;

	public HeartbeatService(Heartbeats heartbeats, KafkaService kafkaService, WorkerManagersService workerManagersService,
							Environment environment, WorkerManagerProperties workerManagerProperties) {
		this.heartbeats = heartbeats;
		this.kafkaService = kafkaService;
		this.workerManagersService = workerManagersService;
		this.environment = environment;
		this.heartbeatInterval = workerManagerProperties.getHeartbeatInterval();
	}

	public Optional<Heartbeat> lastHeartbeat(String id) {
		return heartbeats.findById(id);
	}

	public List<Heartbeat> lastHeartbeats() {
		return heartbeats.findAll();
	}

	public Heartbeat saveHeartbeat(Heartbeat heartbeat) {
		return heartbeats.save(heartbeat);
	}

	public Heartbeat saveWorkerHeartbeat(Heartbeat heartbeat) {
		String id = heartbeat.getId();
		log.info("Received heartbeat from worker {}", id);
		WorkerManager workerManager = workerManagersService.getWorkerManager(id);
		workerManager = workerManager.toBuilder().state("ready").build();
		workerManagersService.saveWorkerManager(workerManager);
		return heartbeats.save(heartbeat);
	}

	public void startHeartbeat() {
		new Timer("heartbeat").schedule(new TimerTask() {
			@Override
			public void run() {
				String id = environment.getProperty(ContainerConstants.Environment.Manager.ID);
				kafkaService.sendHeartbeat(Heartbeat.builder().id(id).build());
			}
		}, heartbeatInterval, heartbeatInterval);
	}

	public void deleteHeartbeat(String id) {
		try {
			heartbeats.deleteById(id);
		} catch (EntityNotFoundException ignored) { }
	}

	public void reset() {
		heartbeats.deleteAll();
	}
}
