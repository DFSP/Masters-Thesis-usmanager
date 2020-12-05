package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerPortMapping;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ContainerMessage {

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
	private Set<ContainerRule> containerRules;
	private Set<ContainerSimulatedMetric> simulatedContainerMetrics;

	public ContainerMessage(Container container) {
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
		this.containerRules = container.getContainerRules();
		this.simulatedContainerMetrics = container.getSimulatedContainerMetrics();
	}

	public Container get() {
		return Container.builder()
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
			.containerRules(containerRules)
			.simulatedContainerMetrics(simulatedContainerMetrics)
			.build();
	}
}
