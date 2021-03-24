package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ElasticIpDTO {

	private Long id;
	private RegionEnum region;
	private String allocationId;
	private String publicIp;
	private String associationId;
	private String instanceId;

}
