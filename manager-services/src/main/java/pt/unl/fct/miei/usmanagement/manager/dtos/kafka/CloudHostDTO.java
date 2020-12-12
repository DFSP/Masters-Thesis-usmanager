package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CloudHostDTO {

	private Long id;
	private String instanceId;
	private String instanceType;
	private InstanceState state;
	private String imageId;
	private String publicDnsName;
	private String publicIpAddress;
	private String privateIpAddress;
	private Placement placement;
	private AwsRegion awsRegion;
	private WorkerManagerDTO managedByWorker;
	private Set<HostRuleDTO> hostRules;
	private Set<HostSimulatedMetricDTO> simulatedHostMetrics;
	/*@JsonProperty("isNew")
	private boolean isNew;*/

	public CloudHostDTO(Long id) {
		this.id = id;
	}

	public CloudHostDTO(CloudHost cloudHost) {
		this.id = cloudHost.getId();
		this.instanceId = cloudHost.getInstanceId();
		this.instanceType = cloudHost.getInstanceType();
		this.state = cloudHost.getState();
		this.imageId = cloudHost.getImageId();
		this.publicDnsName = cloudHost.getPublicDnsName();
		this.publicIpAddress = cloudHost.getPublicIpAddress();
		this.privateIpAddress = cloudHost.getPrivateIpAddress();
		this.placement = cloudHost.getPlacement();
		this.awsRegion = cloudHost.getAwsRegion();
		this.managedByWorker = cloudHost.getManagedByWorker() == null ? null : new WorkerManagerDTO(cloudHost.getManagedByWorker());
		this.hostRules = cloudHost.getHostRules().stream().map(HostRuleDTO::new).collect(Collectors.toSet());
		this.simulatedHostMetrics = cloudHost.getSimulatedHostMetrics().stream().map(HostSimulatedMetricDTO::new).collect(Collectors.toSet());
		/*this.isNew = cloudHost.isNew();*/
	}

	@JsonIgnore
	public CloudHost toEntity() {
		CloudHost cloudHost = CloudHost.builder()
			.id(id)
			.instanceId(instanceId)
			.instanceType(instanceType)
			.state(state)
			.imageId(imageId)
			.publicDnsName(publicDnsName)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.placement(placement)
			.awsRegion(awsRegion)
			.managedByWorker(managedByWorker == null ? null : managedByWorker.toEntity())
			.hostRules(hostRules != null ? hostRules.stream().map(HostRuleDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.simulatedHostMetrics(simulatedHostMetrics != null ? simulatedHostMetrics.stream().map(HostSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*cloudHost.setNew(isNew);*/
		return cloudHost;
	}

}
