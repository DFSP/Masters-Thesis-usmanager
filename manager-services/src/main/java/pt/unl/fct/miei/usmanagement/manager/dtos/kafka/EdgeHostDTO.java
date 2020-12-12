package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class EdgeHostDTO {

	private Long id;
	private String username;
	private String publicIpAddress;
	private String privateIpAddress;
	private String publicDnsName;
	private RegionEnum region;
	private Coordinates coordinates;
	private WorkerManagerDTO managedByWorker;
	private Set<HostRuleDTO> hostRules;
	private Set<HostSimulatedMetricDTO> simulatedHostMetrics;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public EdgeHostDTO(Long id) {
		this.id = id;
	}

	public EdgeHostDTO(EdgeHost edgeHost) {
		this.id = edgeHost.getId();
		this.username = edgeHost.getUsername();
		this.publicIpAddress = edgeHost.getPublicIpAddress();
		this.privateIpAddress = edgeHost.getPrivateIpAddress();
		this.publicDnsName = edgeHost.getPublicDnsName();
		this.region = edgeHost.getRegion();
		this.coordinates = edgeHost.getCoordinates();
		this.managedByWorker = edgeHost.getManagedByWorker() == null ? null : new WorkerManagerDTO(edgeHost.getManagedByWorker());
		this.hostRules = edgeHost.getHostRules().stream().map(HostRuleDTO::new).collect(Collectors.toSet());;
		this.simulatedHostMetrics = edgeHost.getSimulatedHostMetrics().stream().map(HostSimulatedMetricDTO::new).collect(Collectors.toSet());;
		/*this.isNew = edgeHost.isNew();*/
	}

	@JsonIgnore
	public EdgeHost toEntity() {
		EdgeHost edgeHost = EdgeHost.builder()
			.id(id)
			.username(username)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.publicDnsName(publicDnsName)
			.region(region)
			.coordinates(coordinates)
			.managedByWorker(managedByWorker == null ? null : managedByWorker.toEntity())
			.hostRules(hostRules != null ? hostRules.stream().map(HostRuleDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.simulatedHostMetrics(simulatedHostMetrics != null ? simulatedHostMetrics.stream().map(HostSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*edgeHost.setNew(isNew);*/
		return edgeHost;
	}

}
