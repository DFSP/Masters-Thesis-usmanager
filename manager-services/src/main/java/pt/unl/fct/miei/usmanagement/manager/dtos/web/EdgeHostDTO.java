package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class EdgeHostDTO {

	private Long id;
	private String username;
	private String publicIpAddress;
	private String privateIpAddress;
	private String publicDnsName;
	private RegionEnum region;
	private Coordinates coordinates;
	private WorkerManagerDTO managedByWorker;

}
