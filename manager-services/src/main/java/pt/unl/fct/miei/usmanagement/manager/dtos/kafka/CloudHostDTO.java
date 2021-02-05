package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = CloudHostDTO.class)
@AllArgsConstructor
@NoArgsConstructor
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

	public CloudHostDTO(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CloudHostDTO)) {
			return false;
		}
		CloudHostDTO other = (CloudHostDTO) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "CloudHostDTO{" +
			"id=" + id +
			", instanceId='" + instanceId + '\'' +
			", instanceType='" + instanceType + '\'' +
			", state=" + state +
			", imageId='" + imageId + '\'' +
			", publicDnsName='" + publicDnsName + '\'' +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", placement=" + placement +
			", awsRegion=" + awsRegion +
			", managedByWorker=" + managedByWorker +
			", hostRules=" + (hostRules == null ? "null" : hostRules.stream().map(HostRuleDTO::getId).collect(Collectors.toSet())) +
			", simulatedHostMetrics=" + (simulatedHostMetrics == null ? "null" : simulatedHostMetrics.stream().map(HostSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
