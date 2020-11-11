package pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws;

import lombok.Data;

@Data
public class AllocatedElasticIp {

	private final String allocationId;
	private final String publicIp;

}
