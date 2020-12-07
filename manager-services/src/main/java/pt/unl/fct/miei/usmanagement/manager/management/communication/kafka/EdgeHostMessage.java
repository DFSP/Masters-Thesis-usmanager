package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class EdgeHostMessage {

	private Long id;
	private String username;
	private String publicIpAddress;
	private String privateIpAddress;
	private String publicDnsName;
	private RegionEnum region;
	private Coordinates coordinates;
	private WorkerManager managedByWorker;
	private Set<HostRule> hostRules;
	private Set<HostSimulatedMetric> simulatedHostMetrics;

	public EdgeHostMessage(Long id) {
		this.id = id;
	}

	public EdgeHostMessage(EdgeHost edgeHost) {
		this.id = edgeHost.getId();
		this.username = edgeHost.getUsername();
		this.publicIpAddress = edgeHost.getPublicIpAddress();
		this.privateIpAddress = edgeHost.getPrivateIpAddress();
		this.publicDnsName = edgeHost.getPublicDnsName();
		this.region = edgeHost.getRegion();
		this.coordinates = edgeHost.getCoordinates();
		this.managedByWorker = edgeHost.getManagedByWorker();
		this.hostRules = edgeHost.getHostRules();
		this.simulatedHostMetrics = edgeHost.getSimulatedHostMetrics();
	}

	public EdgeHost get() {
		return EdgeHost.builder()
			.id(id)
			.username(username)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.publicDnsName(publicDnsName)
			.region(region)
			.coordinates(coordinates)
			.managedByWorker(managedByWorker)
			.hostRules(hostRules)
			.simulatedHostMetrics(simulatedHostMetrics)
			.build();
	}

}
