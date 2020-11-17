package pt.unl.fct.miei.usmanagement.manager.sync;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeartbeatService {

	private final Heartbeats heartbeats;

	public HeartbeatService(Heartbeats heartbeats) {
		this.heartbeats = heartbeats;
	}

	public Heartbeat lastHeartbeat(String nodeId) {
		return heartbeats.getOne(nodeId);
	}

	public List<Heartbeat> lastHeartbeats() {
		return heartbeats.findAll();
	}

}
