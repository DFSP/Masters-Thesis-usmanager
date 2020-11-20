package pt.unl.fct.miei.usmanagement.manager.sync;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HeartbeatService {

	private final Heartbeats heartbeats;

	public HeartbeatService(Heartbeats heartbeats) {
		this.heartbeats = heartbeats;
	}

	public Optional<Heartbeat> lastHeartbeat(String nodeId) {
		return heartbeats.getHeartbeatByNodeId(nodeId);
	}

	public List<Heartbeat> lastHeartbeats() {
		return heartbeats.findAll();
	}

}
