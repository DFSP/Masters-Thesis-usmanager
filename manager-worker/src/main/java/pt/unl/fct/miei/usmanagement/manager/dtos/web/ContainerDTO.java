package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerDTO {

	private String id;
	private ContainerTypeEnum type;
	private long created;
	private String name;
	private String image;
	private String command;
	private String network;
	private String publicIpAddress;
	private String privateIpAddress;
	private Set<String> mounts;
	private Set<ContainerPortMapping> ports;
	private Map<String, String> labels;
	private RegionEnum region;
	private Coordinates coordinates;
	private String state;

}
