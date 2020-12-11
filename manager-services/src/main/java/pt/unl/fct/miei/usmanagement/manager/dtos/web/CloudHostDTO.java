package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

}
