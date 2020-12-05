package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ElasticIpMessage {

	private Long id;
	private RegionEnum region;
	private String allocationId;
	private String publicIp;
	private String associationId;
	private String instanceId;

	public ElasticIpMessage(ElasticIp elasticIp) {
		this.id = elasticIp.getId();
		this.region = elasticIp.getRegion();
		this.allocationId = elasticIp.getAllocationId();
		this.publicIp = elasticIp.getPublicIp();
		this.associationId = elasticIp.getAssociationId();
		this.instanceId = elasticIp.getInstanceId();
	}

	public ElasticIp get() {
		return ElasticIp.builder()
			.id(id)
			.region(region)
			.allocationId(allocationId)
			.publicIp(publicIp)
			.associationId(associationId)
			.instanceId(instanceId)
			.build();
	}
}
