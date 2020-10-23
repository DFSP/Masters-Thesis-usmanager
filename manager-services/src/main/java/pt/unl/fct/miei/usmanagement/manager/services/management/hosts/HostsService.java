/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.services.management.hosts;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.Mode;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.MethodNotAllowedException;
import pt.unl.fct.miei.usmanagement.manager.services.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.services.management.containers.ContainerType;
import pt.unl.fct.miei.usmanagement.manager.services.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws.AwsProperties;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.location.LocationRequestService;
import pt.unl.fct.miei.usmanagement.manager.services.management.location.RegionsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.services.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.services.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.services.management.bash.BashCommandResult;
import pt.unl.fct.miei.usmanagement.manager.services.management.bash.BashService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostsService {

	private final NodesService nodesService;
	private final ContainersService containersService;
	private final DockerSwarmService dockerSwarmService;
	private final EdgeHostsService edgeHostsService;
	private final CloudHostsService cloudHostsService;
	private final SshService sshService;
	private final BashService bashService;
	private final HostMetricsService hostMetricsService;
	private final PrometheusService prometheusService;
	private final LocationRequestService locationRequestService;
	private final RegionsService regionsService;
	private final String localMachineDns;
	private final int maxWorkers;
	private final int maxInstances;
	private final Mode mode;
	private HostAddress hostAddress;

	public HostsService(@Lazy NodesService nodesService, @Lazy ContainersService containersService,
						DockerSwarmService dockerSwarmService, EdgeHostsService edgeHostsService,
						CloudHostsService cloudHostsService, SshService sshService, BashService bashService,
						HostMetricsService hostMetricsService, PrometheusService prometheusService,
						@Lazy LocationRequestService locationRequestService, RegionsService regionsService,
						DockerProperties dockerProperties, AwsProperties awsProperties,
						ManagerProperties managerProperties, HostProperties hostProperties) {
		this.nodesService = nodesService;
		this.containersService = containersService;
		this.dockerSwarmService = dockerSwarmService;
		this.edgeHostsService = edgeHostsService;
		this.cloudHostsService = cloudHostsService;
		this.sshService = sshService;
		this.bashService = bashService;
		this.hostMetricsService = hostMetricsService;
		this.prometheusService = prometheusService;
		this.locationRequestService = locationRequestService;
		this.regionsService = regionsService;
		this.maxWorkers = dockerProperties.getSwarm().getInitialMaxWorkers();
		this.maxInstances = awsProperties.getInitialMaxInstances();
		this.localMachineDns = hostProperties.getLocalMachineDns();
		this.mode = managerProperties.getMode();
	}

	public HostAddress setHostAddress() {
		String username = bashService.getUsername();
		String publicIp = bashService.getPublicIp();
		String privateIp = bashService.getPrivateIp();
		if ((mode == null || mode == Mode.LOCAL) && !edgeHostsService.hasEdgeHost(localMachineDns)) {
			Coordinates coordinates = new Coordinates("Portugal", 39.575097, -8.909794);
			edgeHostsService.addEdgeHost(EdgeHostEntity.builder()
				.username(username)
				.publicIpAddress(publicIp)
				.privateIpAddress(privateIp)
				.publicDnsName(localMachineDns)
				.region(regionsService.getRegion("eu-central"))
				.country("pt")
				.city("lisbon")
				.coordinates(coordinates)
				.build());
			this.hostAddress = new HostAddress(username, localMachineDns, publicIp, privateIp, coordinates);
		}
		else {
			this.hostAddress = new HostAddress(username, publicIp, privateIp);
		}
		log.info("Setting local address: {}", hostAddress.toString());
		return hostAddress;
	}

	public HostAddress getHostAddress() {
		if (hostAddress == null) {
			throw new MethodNotAllowedException("manager initialization did not finish");
		}
		return hostAddress;
	}

	public void clusterHosts() {
		setupHost(hostAddress, NodeRole.MANAGER);
		List<HostAddress> workerHosts = new LinkedList<>();
		if (mode == null || mode == Mode.LOCAL) {
			log.info("Clustering edge worker hosts into the swarm");
			workerHosts.addAll(getLocalWorkerNodes().stream().map(EdgeHostEntity::getAddress).collect(Collectors.toList()));
			if (getLocalWorkerNodes().size() < 1) {
				log.info("No edge worker hosts found");
			}
		}
		if (mode == null || mode == Mode.GLOBAL) {
			log.info("Clustering cloud hosts into the swarm");
			workerHosts.addAll(getCloudWorkerNodes().stream().map(CloudHostEntity::getAddress).collect(Collectors.toList()));
			if (getCloudWorkerNodes().size() < 1) {
				log.info("No cloud worker hosts found");
			}
		}
		workerHosts.parallelStream().forEach(host -> setupHost(host, NodeRole.WORKER));
	}

	private List<CloudHostEntity> getCloudWorkerNodes() {
		int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
		int maxInstances = Math.min(this.maxInstances, maxWorkers);
		List<CloudHostEntity> cloudHosts = new ArrayList<>(maxInstances);
		for (int i = 0; i < maxInstances; i++) {
			cloudHosts.add(chooseCloudHost());
		}
		return cloudHosts;
	}

	private List<EdgeHostEntity> getLocalWorkerNodes() {
		int maxWorkers = this.maxWorkers - nodesService.getReadyWorkers().size();
		return edgeHostsService.getEdgeHosts().stream()
			.filter(edgeHost -> Objects.equals(edgeHost.getPublicIpAddress(), this.hostAddress.getPublicIpAddress()))
			.filter(edgeHost -> !Objects.equals(edgeHost.getPrivateIpAddress(), this.hostAddress.getPrivateIpAddress()))
			.filter(this::isEdgeHostRunning)
			.limit(maxWorkers)
			.collect(Collectors.toList());
	}

	public boolean isLocalhost(HostAddress hostAddress) {
		String machinePublicIp = this.hostAddress.getPublicIpAddress();
		String machinePrivateIp = this.hostAddress.getPrivateIpAddress();
		return Objects.equals(machinePublicIp, hostAddress.getPublicIpAddress())
			&& Objects.equals(machinePrivateIp, hostAddress.getPrivateIpAddress());
	}

	private SimpleNode setupHost(HostAddress hostAddress, NodeRole role) {
		log.info("Setting up {} with role {}", hostAddress, role);
		String dockerApiProxyContainerId = containersService.launchDockerApiProxy(hostAddress, false);
		SimpleNode node;
		switch (role) {
			case MANAGER:
				node = setupSwarmManager(hostAddress);
				break;
			case WORKER:
				node = setupSwarmWorker(hostAddress);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		containersService.addContainer(dockerApiProxyContainerId);
		containersService.launchContainer(hostAddress, LocationRequestService.REQUEST_LOCATION_MONITOR, ContainerType.SINGLETON);
		containersService.launchContainer(hostAddress, PrometheusService.PROMETHEUS, ContainerType.SINGLETON);
		return node;
	}

	private SimpleNode setupSwarmManager(HostAddress hostAddress) {
		SimpleNode node;
		if (isLocalhost(hostAddress)) {
			log.info("Setting up docker swarm leader");
			dockerSwarmService.leaveSwarm(hostAddress);
			node = dockerSwarmService.initSwarm();
		}
		else {
			node = joinSwarm(hostAddress, NodeRole.MANAGER);
		}
		return node;
	}

	private SimpleNode setupSwarmWorker(HostAddress hostAddress) {
		return joinSwarm(hostAddress, NodeRole.WORKER);
	}

	private SimpleNode joinSwarm(HostAddress hostAddress, NodeRole role) {
		try {
			return nodesService.getHostNode(hostAddress);
		}
		catch (EntityNotFoundException ignored) {
			return dockerSwarmService.joinSwarm(hostAddress, role);
		}
	}

	public List<HostAddress> getAvailableHostsOnRegions(double expectedMemoryConsumption, List<String> regions) {
		return regions.stream()
			.map(regionsService::getRegion)
			.map(regionEntity -> new HostLocation(null, null, regionEntity.getName(), null))
			.map(location -> getAvailableHost(expectedMemoryConsumption, location))
			.collect(Collectors.toList());
	}

	public HostAddress getClosestInactiveHost(Coordinates coordinates) {
		List<EdgeHostEntity> edgeHosts = edgeHostsService.getInactiveEdgeHosts();
		edgeHosts.sort((oneEdgeHost, anotherEdgeHost) -> {
			double oneDistance = oneEdgeHost.getCoordinates().distanceTo(coordinates);
			double anotherDistance = anotherEdgeHost.getCoordinates().distanceTo(coordinates);
			return Double.compare(oneDistance, anotherDistance);
		});

		List<CloudHostEntity> cloudHosts = cloudHostsService.getInactiveCloudHosts();
		cloudHosts.sort((oneCloudHost, anotherCloudHost) -> {
			double oneDistance = oneCloudHost.getRegion().getCoordinates().distanceTo(coordinates);
			double anotherDistance = anotherCloudHost.getRegion().getCoordinates().distanceTo(coordinates);
			return Double.compare(oneDistance, anotherDistance);
		});

		final HostAddress hostAddress;
		if (!edgeHosts.isEmpty() && !cloudHosts.isEmpty()) {
			EdgeHostEntity edgeHost = edgeHosts.get(0);
			double distanceToEdgeHost = edgeHost.getCoordinates().distanceTo(coordinates);
			CloudHostEntity cloudHost = cloudHosts.get(0);
			double distanceToCloudHost = cloudHost.getRegion().getCoordinates().distanceTo(coordinates);
			hostAddress = distanceToEdgeHost <= distanceToCloudHost ? edgeHost.getAddress() : cloudHost.getAddress();
		} else if (!edgeHosts.isEmpty()) {
			hostAddress = edgeHosts.get(0).getAddress();
		} else if (!cloudHosts.isEmpty()) {
			hostAddress = cloudHosts.get(0).getAddress();
		} else {
			hostAddress = cloudHostsService.launchInstance(coordinates).getAddress();
		}

		return hostAddress;
	}

	public HostAddress getAvailableHost(double expectedMemoryConsumption, Coordinates coordinates) {
		// TODO implement algorithm to get the closest machine based on coordinates
		return edgeHostsService.getEdgeHosts().get(0).getAddress();
	}

	//FIXME
	@Deprecated
	public HostAddress getAvailableHost(double expectedMemoryConsumption, HostLocation hostLocation) {
		//TODO try to improve method
		String region = hostLocation.getRegion();
		String country = hostLocation.getCountry();
		String city = hostLocation.getCity();
		log.info("Looking for available nodes to host container with at least {} memory at region {}, country {}, "
			+ "city {}", expectedMemoryConsumption, region, country, city);
		List<HostAddress> otherRegionsHosts = new LinkedList<>();
		List<HostAddress> sameRegionHosts = new LinkedList<>();
		List<HostAddress> sameCountryHosts = new LinkedList<>();
		List<HostAddress> sameCityHosts = new LinkedList<>();
		nodesService.getActiveNodes().stream()
			.map(SimpleNode::getHostAddress)
			.filter(hostAddress -> hostMetricsService.hostHasAvailableResources(hostAddress, expectedMemoryConsumption))
			.forEach(hostAddress -> {
				HostLocation nodeLocation = getHostLocation(hostAddress);
				String nodeRegion = nodeLocation.getRegion();
				String nodeCountry = nodeLocation.getCountry();
				String nodeCity = nodeLocation.getCity();
				if (Objects.equals(nodeRegion, region)) {
					sameRegionHosts.add(hostAddress);
					if (!Strings.isNullOrEmpty(country) && nodeCountry.equalsIgnoreCase(country)) {
						sameCountryHosts.add(hostAddress);
					}
					if (!Strings.isNullOrEmpty(city) && nodeCity.equalsIgnoreCase(city)) {
						sameCityHosts.add(hostAddress);
					}
				}
				else {
					otherRegionsHosts.add(hostAddress);
				}
			});
		//TODO https://developers.google.com/maps/documentation/geocoding/start?csw=1
		log.info("Found hosts {} on same region", sameRegionHosts.toString());
		log.info("Found hosts {} on same country", sameCountryHosts.toString());
		log.info("Found hosts {} on same city", sameCityHosts.toString());
		log.info("Found hosts {} on other regions", otherRegionsHosts.toString());
		Random random = new Random();
		if (!sameCityHosts.isEmpty()) {
			return sameCityHosts.get(random.nextInt(sameCityHosts.size()));
		}
		else if (!sameCountryHosts.isEmpty()) {
			return sameCountryHosts.get(random.nextInt(sameCountryHosts.size()));
		}
		else if (!sameRegionHosts.isEmpty()) {
			return sameRegionHosts.get(random.nextInt(sameRegionHosts.size()));
		}
		else if (!otherRegionsHosts.isEmpty() && !"us-east-1".equals(region)) {
			//TODO porquê excluir a região us-east-1?
			// TODO: review otherHostRegion and region us-east-1
			return otherRegionsHosts.get(random.nextInt(otherRegionsHosts.size()));
		}
		else {
			log.info("Didn't find any available node");
			return addHost(regionsService.getRegion(region), country, city, NodeRole.WORKER).getHostAddress();
		}
	}

	public HostDetails getHostDetails(HostAddress hostAddress) {
		HostLocation hostLocation;
		try {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByAddress(hostAddress);
			hostAddress = edgeHost.getAddress();
			hostLocation = edgeHost.getLocation();
		}
		catch (EntityNotFoundException e) {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByAddress(hostAddress);
			hostAddress = cloudHost.getAddress();
			hostLocation = cloudHost.getLocation();
		}
		return new HostDetails(hostAddress, hostLocation);
	}

	public HostDetails getHostDetails(String hostname) {
		HostAddress hostAddress;
		HostLocation hostLocation;
		try {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(hostname);
			hostAddress = edgeHost.getAddress();
			hostLocation = edgeHost.getLocation();
		}
		catch (EntityNotFoundException e) {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIp(hostname);
			hostAddress = cloudHost.getAddress();
			hostLocation = cloudHost.getLocation();
		}
		return new HostDetails(hostAddress, hostLocation);
	}

	public HostLocation getHostLocation(HostAddress address) {
		HostLocation hostLocation;
		try {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByAddress(address);
			hostLocation = edgeHost.getLocation();
		}
		catch (EntityNotFoundException e) {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByAddress(address);
			hostLocation = cloudHost.getLocation();
		}
		return hostLocation;
	}

	// TODO add coordinates instead
	public SimpleNode addHost(RegionEntity region, String country, String city, NodeRole role) {
		final HostAddress hostAddress;
		Optional<EdgeHostEntity> edgeHost = chooseEdgeHost(region, country, city);
		if (edgeHost.isPresent()) {
			hostAddress = edgeHost.get().getAddress();
		}
		else {
			CloudHostEntity cloudHost = chooseCloudHost();
			hostAddress = cloudHost.getAddress();
		}
		log.info("Found host {} to join swarm as {}", hostAddress, role.name());
		return setupHost(hostAddress, role);
	}

	public SimpleNode addHost(String host, NodeRole role) {
		HostAddress hostAddress;
		try {
			CloudHostEntity cloudHost = cloudHostsService.getCloudHostByIdOrIp(host);
			if (cloudHost.getState().getCode() != AwsInstanceState.RUNNING.getCode()) {
				cloudHost = cloudHostsService.startInstance(host, false);
			}
			hostAddress = cloudHost.getAddress();
		}
		catch (EntityNotFoundException e) {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByDnsOrIp(host);
			hostAddress = edgeHost.getAddress();
		}
		return setupHost(hostAddress, role);
	}

	public SimpleNode addHost(Coordinates coordinates, NodeRole role) {
		HostAddress hostAddress = getClosestInactiveHost(coordinates);
		return setupHost(hostAddress, role);
	}

	public void removeHost(HostAddress hostAddress) {
		containersService.getSystemContainers(hostAddress).stream()
			.filter(c -> !Objects.equals(c.getLabels().get(ContainerConstants.Label.SERVICE_NAME),
				DockerApiProxyService.DOCKER_API_PROXY))
			.forEach(c -> containersService.stopContainer(c.getContainerId()));
		dockerSwarmService.leaveSwarm(hostAddress);
	}

	private boolean isEdgeHostRunning(EdgeHostEntity edgeHost) {
		return sshService.hasConnection(edgeHost.getAddress());
	}

	private Optional<EdgeHostEntity> chooseEdgeHost(RegionEntity region, String country, String city) {
		List<EdgeHostEntity> edgeHosts = List.of();
		if (!Strings.isNullOrEmpty(city)) {
			edgeHosts = edgeHostsService.getHostsByCity(city);
		}
		else if (!Strings.isNullOrEmpty(country)) {
			edgeHosts = edgeHostsService.getHostsByCountry(country);
		}
		else if (region != null) {
			edgeHosts = edgeHostsService.getHostsByRegion(region);
		}
		return edgeHosts.stream()
			.filter(edgeHost -> !nodesService.isPartOfSwarm(edgeHost.getAddress()))
			.filter(this::isEdgeHostRunning)
			.findFirst();
	}

	private CloudHostEntity chooseCloudHost() {
		for (CloudHostEntity cloudHost : cloudHostsService.getCloudHosts()) {
			int stateCode = cloudHost.getState().getCode();
			if (stateCode == AwsInstanceState.RUNNING.getCode()) {
				HostAddress hostAddress = cloudHost.getAddress();
				if (!nodesService.isPartOfSwarm(hostAddress)) {
					return cloudHost;
				}
			}
			else if (stateCode == AwsInstanceState.STOPPED.getCode()) {
				return cloudHostsService.startInstance(cloudHost, false);
			}
		}
		Coordinates myCoordinates = hostAddress.getCoordinates();
		return cloudHostsService.launchInstance(myCoordinates);
	}

	public List<String> executeCommand(String command, HostAddress hostAddress) {
		List<String> result = null;
		String error = null;
		if (Objects.equals(this.hostAddress, hostAddress)) {
			// execute local command
			BashCommandResult bashCommandResult = bashService.executeCommand(command);
			if (!bashCommandResult.isSuccessful()) {
				error = String.join("\n", bashCommandResult.getError());
			}
			else {
				result = bashCommandResult.getOutput();
			}
		}
		else {
			// execute remote command
			SshCommandResult sshCommandResult = sshService.executeCommand(hostAddress, command);
			if (!sshCommandResult.isSuccessful()) {
				error = String.join("\n", sshCommandResult.getError());
			}
			else {
				result = sshCommandResult.getOutput();
			}
		}
		if (error != null) {
			throw new ManagerException("%s", error);
		}
		return result;
	}

	public String findAvailableExternalPort(String startExternalPort) {
		return findAvailableExternalPort(hostAddress, startExternalPort);
	}

	public String findAvailableExternalPort(HostAddress hostAddress, String startExternalPort) {
		String command = "sudo lsof -i -P -n | grep LISTEN | awk '{print $9}' | cut -d: -f2";
		try {
			List<Integer> usedExternalPorts = executeCommand(command, hostAddress).stream()
				.filter(v -> Pattern.compile("-?\\d+(\\.\\d+)?").matcher(v).matches())
				.map(Integer::parseInt)
				.collect(Collectors.toList());
			for (int i = Integer.parseInt(startExternalPort); ; i++) {
				if (!usedExternalPorts.contains(i)) {
					return String.valueOf(i);
				}
			}
		}
		catch (ManagerException e) {
			throw new ManagerException("Unable to find currently used external ports at %s: %s ", hostAddress,
				e.getMessage());
		}
	}

	public boolean isValidIPAddress(String ip) {
		// Regex for digit from 0 to 255.
		String zeroTo255
			= "(\\d{1,2}|(0|1)\\"
			+ "d{2}|2[0-4]\\d|25[0-5])";

		// Regex for a digit from 0 to 255 and
		// followed by a dot, repeat 4 times.
		// this is the regex to validate an IP address.
		String regex
			= zeroTo255 + "\\."
			+ zeroTo255 + "\\."
			+ zeroTo255 + "\\."
			+ zeroTo255;

		// Compile the ReGex
		Pattern p = Pattern.compile(regex);

		// If the IP address is empty
		// return false
		if (ip == null) {
			return false;
		}

		// Pattern class contains matcher() method
		// to find matching between given IP address
		// and regular expression.
		Matcher m = p.matcher(ip);

		// Return if the IP address
		// matched the ReGex
		return m.matches();
	}

	public String getPublicIpAddressFromHost(String host) {
		try {
			return cloudHostsService.getCloudHostByIdOrDns(host).getPublicIpAddress();
		}
		catch (EntityNotFoundException ignored) {
			return edgeHostsService.getEdgeHostByDns(host).getPublicIpAddress();
		}
	}

}
