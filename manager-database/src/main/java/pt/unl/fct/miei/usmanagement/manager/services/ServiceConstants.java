package pt.unl.fct.miei.usmanagement.manager.services;

import java.util.List;

public class ServiceConstants {

	private static final List<String> systemServices;

	private ServiceConstants() { }

	static {
		systemServices = List.of(
			Name.MASTER_MANAGER,
			Name.WORKER_MANAGER,
			Name.REGISTRATION_SERVER,
			Name.PROMETHEUS,
			Name.DOCKER_API_PROXY,
			Name.NODE_EXPORTER,
			Name.LOAD_BALANCER,
			Name.KAFKA,
			Name.ZOOKEEPER);
	}

	public static List<String> getSystemServices() {
		return systemServices;
	}

	public static final class Name {
		public static final String MASTER_MANAGER = "master-manager";
		public static final String WORKER_MANAGER = "worker-manager";
		public static final String REGISTRATION_SERVER = "registration-server";
		public static final String PROMETHEUS = "prometheus";
		public static final String DOCKER_API_PROXY = "nginx-basic-auth-proxy";
		public static final String NODE_EXPORTER = "node_exporter";
		public static final String LOAD_BALANCER = "load-balancer";
		public static final String KAFKA = "kafka";
		public static final String ZOOKEEPER = "zookeeper";
		public static final String REQUEST_LOCATION_MONITOR = "request-location-monitor";
	}
}
