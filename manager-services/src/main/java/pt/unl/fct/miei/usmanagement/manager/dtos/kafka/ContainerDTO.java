package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
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
	private String managerId;
	private Coordinates coordinates;
	private Set<ContainerRuleDTO> containerRules;
	private Set<ContainerSimulatedMetricDTO> simulatedContainerMetrics;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ContainerDTO(String id) {
		this.id = id;
	}

	public ContainerDTO(Container container) {
		this.id = container.getId();
		this.type = container.getType();
		this.created = container.getCreated();
		this.name = container.getName();
		this.image = container.getImage();
		this.command = container.getCommand();
		this.network = container.getNetwork();
		this.publicIpAddress = container.getPublicIpAddress();
		this.privateIpAddress = container.getPrivateIpAddress();
		this.mounts = container.getMounts();
		this.ports = container.getPorts();
		this.labels = container.getLabels();
		this.region = container.getRegion();
		this.managerId = container.getManagerId();
		this.coordinates = container.getCoordinates();
		this.containerRules = container.getContainerRules().stream().map(ContainerRuleDTO::new).collect(Collectors.toSet());
		this.simulatedContainerMetrics = container.getSimulatedContainerMetrics().stream().map(ContainerSimulatedMetricDTO::new).collect(Collectors.toSet());
		/*this.isNew = container.isNew();*/
	}

	@JsonIgnore
	public Container toEntity() {
		Container container = Container.builder()
			.id(id)
			.type(type)
			.created(created)
			.name(name)
			.image(image)
			.command(command)
			.network(network)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.mounts(mounts)
			.ports(ports)
			.labels(labels)
			.region(region)
			.managerId(managerId)
			.coordinates(coordinates)
			.containerRules(containerRules != null ? containerRules.stream().map(ContainerRuleDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.simulatedContainerMetrics(simulatedContainerMetrics != null ? simulatedContainerMetrics.stream().map(ContainerSimulatedMetricDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*container.setNew(isNew);*/
		return container;
	}
}
