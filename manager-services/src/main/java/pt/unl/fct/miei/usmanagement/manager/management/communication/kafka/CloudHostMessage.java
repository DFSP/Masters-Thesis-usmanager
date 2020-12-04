package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class CloudHostMessage {

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
	private WorkerManager managedByWorker;
	private Set<HostRule> hostRules;
	private Set<HostSimulatedMetric> simulatedHostMetrics;

	public CloudHostMessage(CloudHost cloudHost) {
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
		this.managedByWorker = cloudHost.getManagedByWorker();
		this.hostRules = cloudHost.getHostRules();
		this.simulatedHostMetrics = cloudHost.getSimulatedHostMetrics();
	}

}
