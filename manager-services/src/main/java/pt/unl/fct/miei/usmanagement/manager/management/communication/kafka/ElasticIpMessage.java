package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

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
}
