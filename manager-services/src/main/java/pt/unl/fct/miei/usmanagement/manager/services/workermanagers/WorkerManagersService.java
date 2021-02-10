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

package pt.unl.fct.miei.usmanagement.manager.services.workermanagers;

import com.amazonaws.services.ec2.model.InstanceType;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.Mode;
import pt.unl.fct.miei.usmanagement.manager.config.ManagerServicesConfiguration;
import pt.unl.fct.miei.usmanagement.manager.config.ParallelismProperties;
import pt.unl.fct.miei.usmanagement.manager.config.WorkerManagerRequestInterceptor;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.heartbeats.Heartbeat;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.registrationservers.RegistrationServer;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.LaunchContainerRequest;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.AddNode;
import pt.unl.fct.miei.usmanagement.manager.services.heartbeats.HeartbeatService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManagers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkerManagersService {

	private final WorkerManagers workerManagers;
	private final ContainersService containersService;
	private final HostsService hostsService;
	private final ServicesService servicesService;
	private final KafkaService kafkaService;
	private final CloudHostsService cloudHostsService;
	private final HeartbeatService heartbeatService;
	private final ManagerServicesConfiguration managerServicesConfiguration;

	private final RestTemplate restTemplate;
	private final int threads;

	public WorkerManagersService(WorkerManagers workerManagers, @Lazy ContainersService containersService,
								 HostsService hostsService, ServicesService servicesService, KafkaService kafkaService,
								 CloudHostsService cloudHostsService, @Lazy HeartbeatService heartbeatService,
								 ParallelismProperties parallelismProperties, WorkerManagerRequestInterceptor requestInterceptor,
								 ManagerServicesConfiguration managerServicesConfiguration) {
		this.workerManagers = workerManagers;
		this.containersService = containersService;
		this.hostsService = hostsService;
		this.servicesService = servicesService;
		this.kafkaService = kafkaService;
		this.cloudHostsService = cloudHostsService;
		this.heartbeatService = heartbeatService;
		this.managerServicesConfiguration = managerServicesConfiguration;
		this.restTemplate = new RestTemplate();
		this.restTemplate.setInterceptors(List.of(requestInterceptor));
		this.threads = parallelismProperties.getThreads();
	}

	public List<WorkerManager> getWorkerManagers() {
		return workerManagers.findAll();
	}

	public WorkerManager getWorkerManager(String id) {
		return workerManagers.findById(id).orElseThrow(() ->
			new EntityNotFoundException(WorkerManager.class, "id", id));
	}

	public WorkerManager getWorkerManager(Container container) {
		return workerManagers.getByContainerId(container.getId()).orElseThrow(() ->
			new EntityNotFoundException(WorkerManager.class, "container", container.getId()));
	}

	public List<WorkerManager> getWorkerManagers(RegionEnum region) {
		return workerManagers.getByRegion(region);
	}

	public List<WorkerManager> getReadyWorkerManagers() {
		return workerManagers.getByState("ready");
	}

	public List<WorkerManager> getReadyWorkerManagers(RegionEnum region) {
		return workerManagers.getByRegionAndState(region, "ready");
	}

	public WorkerManager getRegionWorkerManager(RegionEnum region) {
		List<WorkerManager> workerManagers = getWorkerManagers(region);
		log.info("Looking for worker managers at region {}, found {}", region.name(), workerManagers.toString());
		if (workerManagers.isEmpty()) {
			workerManagers.addAll(launchWorkerManagers(List.of(region)));
		}
		return workerManagers.get(0);
	}

	public WorkerManager saveWorkerManager(Container container) {
		String managerId = container.getLabels().get(ContainerConstants.Label.MANAGER_ID);
		WorkerManager workerManager = WorkerManager.builder()
			.id(managerId).containerId(container.getId())
			.publicIpAddress(container.getPublicIpAddress())
			.port(container.getPorts().stream().findFirst().get().getPublicPort()).region(container.getRegion()).build();
		return saveWorkerManager(workerManager);
	}

	public WorkerManager saveWorkerManager(WorkerManager workerManager) {
		log.info("Saving worker manager {}", ToStringBuilder.reflectionToString(workerManager));
		return workerManagers.save(workerManager);
	}

	public WorkerManager launchWorkerManager(HostAddress hostAddress) {
		HostAddress address = hostAddress.isComplete() ? hostAddress : hostsService.completeHostAddress(hostAddress);

		kafkaService.launchKafkaBroker(address.getRegion());

		log.info("Launching worker manager at {}", address);
		String id = UUID.randomUUID().toString();
		String containerId = launchWorkerManager(address, id);
		return workerManagers.getByContainerId(containerId).orElseGet(() -> {
			int port = servicesService.getService(ServiceConstants.Name.WORKER_MANAGER).getDefaultExternalPort();
			WorkerManager workerManager = WorkerManager.builder()
				.id(id).containerId(containerId).publicIpAddress(hostAddress.getPublicIpAddress()).port(port).region(address.getRegion()).build();
			heartbeatService.saveHeartbeat(Heartbeat.builder().id(id).build());
			return workerManagers.save(workerManager);
		});
	}

	public List<WorkerManager> launchWorkerManagers(List<RegionEnum> regions) {
		log.info("Launching worker managers at regions {}", regions);

		/*double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(ServiceConstants.Name.WORKER_MANAGER);*/

		try {
			return new ForkJoinPool(threads).submit(() ->
				regions.parallelStream().map(region -> {
					List<WorkerManager> regionWorkerManagers = getWorkerManagers(region);
					if (regionWorkerManagers.size() > 0) {
						return regionWorkerManagers.get(0);
					}
					else {
						/*Predicate<Node> filter = node -> !node.getHostAddress().equals(hostsService.getManagerHostAddress())
							&& !elasticIpsService.hasElasticIpByPublicIp(node.getHostAddress().getPublicIpAddress());
						HostAddress hostAddress = hostsService.getCapableHost(expectedMemoryConsumption, region, filter);*/
						AwsRegion awsRegion = AwsRegion.getRegionsToAwsRegions().get(region);
						HostAddress hostAddress = managerServicesConfiguration.getMode() == Mode.LOCAL
							? hostsService.getManagerHostAddress()
							: cloudHostsService.launchInstance(awsRegion, InstanceType.T2Medium, false).getAddress();
						return launchWorkerManager(hostAddress);
					}
				}).collect(Collectors.toList())).get();
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			throw new ManagerException("Unable to launch worker managers: %s", e.getMessage());
		}
	}

	private String launchWorkerManager(HostAddress hostAddress, String id) {
		final int retries = 5;
		int tries = 0;
		String containerId;
		do {
			log.info("Launching worker manager container on host {}, attempt {}/{}", hostAddress.toSimpleString(), tries + 1, retries);
			pt.unl.fct.miei.usmanagement.manager.services.Service workerManager = servicesService.getService(ServiceConstants.Name.WORKER_MANAGER);
			String serviceName = workerManager.getServiceName();
			ServiceTypeEnum serviceType = workerManager.getServiceType();
			int externalPort = workerManager.getDefaultExternalPort();
			int internalPort = workerManager.getDefaultInternalPort();
			String dockerRepository = workerManager.getDockerRepository();
			Gson gson = new Gson();
			String command = String.format("WORKER_MANAGER=$(docker ps -q -f 'name=%s') && "
					+ "if [ $WORKER_MANAGER ]; then echo $WORKER_MANAGER; "
					+ "else "
					+ "docker pull %s && "
					+ "docker run -itd --name=%s -p %d:%d --hostname %s -v %s:%s --rm "
					+ "-e %s=%s -e %s='%s' -e %s=%s "
					+ "-l %s=%b -l %s=%s -l %s=%s -l %s=%s -l %s='%s' -l %s=%s -l %s=%s %s; fi",
				serviceName, dockerRepository, serviceName, externalPort, internalPort, serviceName, "/var/run/docker.sock", "/var/run/docker.sock",
				ContainerConstants.Environment.Manager.ID, id,
				ContainerConstants.Environment.Manager.HOST_ADDRESS, gson.toJson(hostAddress),
				ContainerConstants.Environment.Manager.KAFKA_BOOTSTRAP_SERVERS, kafkaService.getKafkaBrokersHosts(),
				ContainerConstants.Label.US_MANAGER, true,
				ContainerConstants.Label.CONTAINER_TYPE, ContainerTypeEnum.BY_REQUEST,
				ContainerConstants.Label.SERVICE_NAME, serviceName,
				ContainerConstants.Label.SERVICE_TYPE, serviceType,
				ContainerConstants.Label.COORDINATES, gson.toJson(hostAddress.getCoordinates()),
				ContainerConstants.Label.REGION, hostAddress.getRegion().name(),
				ContainerConstants.Label.MANAGER_ID, id,
				dockerRepository);
			List<String> output = hostsService.executeCommandSync(command, hostAddress);
			containerId = output.size() > 0 ? output.get(output.size() - 1) : "";
			Timing.sleep(tries + 1, TimeUnit.SECONDS); // waits 1 seconds, then 2 seconds, then 3 seconds, etc
		} while (containerId.isEmpty() && ++tries < retries);
		if (containerId.isEmpty()) {
			throw new ManagerException("Failed to worker manager at %s", hostAddress.toSimpleString());
		}
		return containerId;
	}

	public void stopWorkerManager(String workerManagerId) {
		WorkerManager workerManager = getWorkerManager(workerManagerId);
		String containerId = workerManager.getContainerId();
		workerManagers.delete(workerManager);
		try {
			containersService.stopContainer(containerId);
		}
		catch (EntityNotFoundException e) {
			log.error("Failed to stop container {} associated with worker manager {}", containerId, workerManager.getId());
		}
		heartbeatService.deleteHeartbeat(workerManager.getId());
	}

	public void deleteWorkerManagerByContainer(Container container) {
		WorkerManager workerManager = getWorkerManager(container);
		workerManagers.delete(workerManager);
	}

	public boolean hasWorkerManager(String id) {
		return workerManagers.hasWorkerManager(id);
	}

	private void checkWorkerManagerExists(String id) {
		if (!hasWorkerManager(id)) {
			throw new EntityNotFoundException(WorkerManager.class, "id", id);
		}
	}

	public List<Container> getContainers(WorkerManager workerManager, boolean sync) {
		List<Container> containers = new ArrayList<>();
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers%s", publicIpAddress, port, sync ? "/sync" : "");
		try {
			Container[] response = sync ? restTemplate.postForObject(url, null, Container[].class) : restTemplate.getForObject(url, Container[].class);
			if (response != null) {
				containers.addAll(Arrays.asList(response));
			}
			return containers;
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public List<Container> getContainers() {
		return getContainers(false);
	}

	public List<Container> getContainers(boolean sync) {
		List<Container> containers = new LinkedList<>();

		List<WorkerManager> workerManagers = getWorkerManagers();

		CompletableFuture<?>[] requests = new CompletableFuture[workerManagers.size()];
		int count = 0;
		for (WorkerManager workerManager : workerManagers) {
			requests[count++] = CompletableFuture.supplyAsync(() -> getContainers(workerManager, sync))
				.thenAccept(containers::addAll);
		}
		CompletableFuture.allOf(requests).join();

		return containers;
	}

	public List<Container> synchronizeDatabaseContainers() {
		return getContainers(true);
	}

	public List<Container> launchContainer(LaunchContainerRequest launchContainerRequest, WorkerManager workerManager) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers", publicIpAddress, port);
		final int retries = 10;
		String errorMessage = "";
		for (int i = 0; i < retries; i++) {
			String managerId = workerManager.getId();
			if (heartbeatService.lastHeartbeat(managerId).isEmpty()) {
				log.info("Waiting for worker manager {} to send the first heartbeat. {}/{}", managerId, i + 1, retries);
				Timing.sleep(i + 1, TimeUnit.SECONDS);
				continue;
			}
			try {
				log.info("Sending request {} {} to worker manager {}", url, launchContainerRequest, managerId);
				Container[] response = restTemplate.postForObject(url, launchContainerRequest, Container[].class);
				List<Container> containers = new ArrayList<>();
				if (response != null) {
					containers.addAll(Arrays.asList(response));
				}
				return containers;
			}
			catch (Exception e) {
				errorMessage = e.getMessage();
				log.info("Failed to start container on worker manager {}: {}. Retrying {}/{}", managerId, errorMessage, i + 1, retries);
				Timing.sleep(i + 1, TimeUnit.SECONDS);
			}
		}
		throw new ManagerException("Failed to start container on worker manager %s: %s", workerManager.getId(), errorMessage);
	}

	public List<Container> launchContainers(LaunchContainerRequest launchContainerRequest) {
		HostAddress hostAddress = launchContainerRequest.getHostAddress();
		List<Coordinates> coordinates = launchContainerRequest.getCoordinates();
		List<Container> containers = new ArrayList<>();
		if (hostAddress != null) {
			if (!hostAddress.isComplete()) {
				hostAddress = hostsService.completeHostAddress(hostAddress);
			}
			RegionEnum region = hostAddress.getRegion();
			WorkerManager workerManager = getRegionWorkerManager(region);

			List<Container> workerContainers = launchContainer(launchContainerRequest, workerManager);
			containers.addAll(workerContainers);
		}
		else if (coordinates != null) {
			List<WorkerManager> regionWorkerManagers = coordinates.stream().map(c -> {
				RegionEnum region = RegionEnum.getClosestRegion(c);
				return getRegionWorkerManager(region);
			}).collect(Collectors.toList());

			CompletableFuture<?>[] requests = new CompletableFuture[regionWorkerManagers.size()];
			int count = 0;
			for (WorkerManager workerManager : regionWorkerManagers) {
				requests[count++] = CompletableFuture
					.supplyAsync(() -> launchContainer(launchContainerRequest, workerManager))
					.thenAccept(containers::addAll);
			}
			CompletableFuture.allOf(requests).join();
		}

		return containers;
	}

	public List<Node> getNodes(WorkerManager workerManager, boolean sync) {
		List<Node> nodes = new ArrayList<>();
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/nodes%s", publicIpAddress, port, sync ? "/sync" : "");
		try {
			Node[] response = sync ? restTemplate.postForObject(url, null, Node[].class) : restTemplate.getForObject(url, Node[].class);
			if (response != null) {
				nodes.addAll(Arrays.asList(response));
			}
			return nodes;
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public List<Node> getNodes() {
		return getNodes(false);
	}

	public List<Node> syncNodes() {
		return getNodes(true);
	}

	public List<Node> getNodes(boolean sync) {
		List<Node> nodes = new LinkedList<>();

		List<WorkerManager> workerManagers = getWorkerManagers();

		CompletableFuture<?>[] requests = new CompletableFuture[workerManagers.size()];
		int count = 0;
		for (WorkerManager workerManager : workerManagers) {
			requests[count++] = CompletableFuture
				.supplyAsync(() -> getNodes(workerManager, sync))
				.thenAccept(nodes::addAll);
		}
		CompletableFuture.allOf(requests).join();

		return nodes;
	}

	public List<Node> synchronizeNodesDatabase() {
		return getNodes(true);
	}

	public String getContainerLogs(WorkerManager workerManager) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/logs", publicIpAddress, port);
		try {
			return restTemplate.getForObject(url, String.class);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public Container replicateContainer(String managerId, String containerId, HostAddress toHostAddress) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/%s/replicate", publicIpAddress, port, containerId);
		try {
			return restTemplate.postForObject(url, toHostAddress, Container.class);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public Container migrateContainer(String managerId, String containerId) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/%s/migrate", publicIpAddress, port, containerId);
		try {
			return restTemplate.postForObject(url, null, Container.class);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public void reset() {
		workerManagers.deleteAll();
	}

	public void stopContainer(WorkerManager workerManager, String containerId) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/%s", publicIpAddress, port, containerId);
		try {
			log.info("Sending request {} to worker manager {}", url, workerManager.getId());
			restTemplate.delete(url);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public boolean hasWorkerManager(Container container) {
		return workerManagers.hasWorkerManagerByContainer(container.getId());
	}

	public WorkerManager addIfNotPresent(WorkerManager workerManager) {
		Optional<WorkerManager> workerManagerOptional = workerManagers.findById(workerManager.getId());
		return workerManagerOptional.orElseGet(() -> saveWorkerManager(workerManager));
	}

	public Map<String, List<Container>> launchApp(WorkerManager workerManager, String appName, Coordinates coordinates) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/apps/%s/launch", publicIpAddress, port, appName);
		try {
			log.info("Sending request {} {} to worker manager {}", url, coordinates, workerManager.getId());
			return restTemplate.postForObject(url, coordinates, Map.class);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public Map<String, List<Container>> launchApp(String appName, Coordinates coordinates) {
		RegionEnum region = RegionEnum.getClosestRegion(coordinates);
		WorkerManager workerManager = getRegionWorkerManager(region);
		return launchApp(workerManager, appName, coordinates);
	}

	public void setWorkerManagerDown(String id) {
		try {
			WorkerManager workerManager = getWorkerManager(id);
			workerManager = workerManager.toBuilder().state("down").build();
			saveWorkerManager(workerManager);
		}
		catch (EntityNotFoundException e) {
			log.error("Failed to set worker manager to state down: {}", e.getMessage());
		}
	}

	public List<Node> addNodes(AddNode addNode) {
		String hostname = addNode.getHostname();
		List<Coordinates> coordinates = addNode.getCoordinates();
		Map<RegionEnum, List<AddNode>> regionsRequests = new HashMap<>();
		if (hostname != null) {
			HostAddress hostAddress = hostsService.completeHostAddress(new HostAddress(addNode.getHostname()));
			RegionEnum region = hostAddress.getRegion();
			List<AddNode> requests = List.of(addNode);
			regionsRequests.put(region, requests);
		}
		else if (coordinates != null) {
			for (Coordinates c : coordinates) {
				RegionEnum region = RegionEnum.getClosestRegion(c);
				List<AddNode> requests = regionsRequests.get(region);
				if (requests == null) {
					requests = new LinkedList<>();
				}
				requests.add(addNode);
				regionsRequests.put(region, requests);
			}
		}

		List<List<Node>> regionNodes = new ForkJoinPool(threads).submit(() ->
			regionsRequests.entrySet().parallelStream().map(regionRequests -> {
				RegionEnum region = regionRequests.getKey();
				WorkerManager workerManager = getRegionWorkerManager(region);
				String publicIpAddress = workerManager.getPublicIpAddress();
				int port = workerManager.getPort();
				String url = String.format("http://%s:%d/api/nodes", publicIpAddress, port);
				final int retries = 10;
				String errorMessage = "";
				for (int i = 0; i < retries; i++) {
					String managerId = workerManager.getId();
					if (heartbeatService.lastHeartbeat(managerId).isEmpty()) {
						log.info("Waiting for worker manager {} on region {} to send the first heartbeat. {}/{}", managerId, region.getRegion(), i + 1, retries);
						Timing.sleep(i + 1, TimeUnit.SECONDS);
						continue;
					}
					try {
						log.info("Sending request {} {} to worker manager {}", url, addNode, managerId);
						return (List<Node>) restTemplate.postForObject(url, addNode, List.class);
					}
					catch (Exception e) {
						errorMessage = e.getMessage();
						log.info("Failed to start nodes on region {}: {}. Retrying {}/{}", region.getRegion(), errorMessage, i + 1, retries);
						Timing.sleep(i + 1, TimeUnit.SECONDS);
					}
				}
				throw new ManagerException("Failed to start nodes on region %s: %s", region.getRegion(), errorMessage);
			}).collect(Collectors.toList())
		).join();

		List<Node> nodes = new ArrayList<>();
		regionNodes.forEach(nodes::addAll);
		return nodes;
	}
}
