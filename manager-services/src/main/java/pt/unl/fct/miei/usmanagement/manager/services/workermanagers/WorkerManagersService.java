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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.services.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.LaunchContainerRequest;
import pt.unl.fct.miei.usmanagement.manager.services.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.services.heartbeats.HeartbeatService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkerManagersService {

	private static final int DELAY_BEFORE_SWARM_SETUP = 2000;

	private final WorkerManagers workerManagers;
	private final ContainersService containersService;
	private final HostsService hostsService;
	private final ServicesService servicesService;
	private final DockerSwarmService dockerSwarmService;
	private final BashService bashService;
	private final KafkaService kafkaService;
	private final CloudHostsService cloudHostsService;
	private final HeartbeatService heartbeatService;

	private final RestTemplate restTemplate;
	private final int threads;

	public WorkerManagersService(WorkerManagers workerManagers, @Lazy ContainersService containersService,
								 HostsService hostsService, ServicesService servicesService, DockerProperties dockerProperties,
								 DockerSwarmService dockerSwarmService, BashService bashService, KafkaService kafkaService,
								 CloudHostsService cloudHostsService,
								 @Lazy HeartbeatService heartbeatService, ParallelismProperties parallelismProperties,
								 WorkerManagerRequestInterceptor workerManagerRequestInterceptor) {
		this.workerManagers = workerManagers;
		this.containersService = containersService;
		this.hostsService = hostsService;
		this.servicesService = servicesService;
		this.dockerSwarmService = dockerSwarmService;
		this.bashService = bashService;
		this.kafkaService = kafkaService;
		this.cloudHostsService = cloudHostsService;
		this.heartbeatService = heartbeatService;
		this.restTemplate = new RestTemplate();
		this.restTemplate.setInterceptors(List.of(workerManagerRequestInterceptor));
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
						HostAddress hostAddress = cloudHostsService.launchInstance(awsRegion, InstanceType.T2Medium, false).getAddress();
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

	@Async
	public CompletableFuture<List<Container>> getContainers(WorkerManager workerManager, boolean sync) {
		List<Container> containers = new ArrayList<>();
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers%s", publicIpAddress, port, sync ? "/sync" : "");
		try {
			Container[] response = sync ? restTemplate.postForObject(url, null, Container[].class) : restTemplate.getForObject(url, Container[].class);
			if (response != null) {
				containers.addAll(Arrays.asList(response));
			}
			return CompletableFuture.completedFuture(containers);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public List<Container> getContainers() {
		return getContainers(false);
	}

	public List<Container> getContainers(boolean sync) {
		List<WorkerManager> workerManagers = getWorkerManagers();
		List<CompletableFuture<List<Container>>> futureContainers =
			workerManagers.stream().map(workerManager -> getContainers(workerManager, sync)).collect(Collectors.toList());

		CompletableFuture.allOf(futureContainers.toArray(new CompletableFuture[0])).join();

		List<Container> containers = new ArrayList<>();
		for (CompletableFuture<List<Container>> futureContainer : futureContainers) {
			try {
				containers.addAll(futureContainer.get());
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		return containers;
	}

	public List<Container> synchronizeDatabaseContainers() {
		return getContainers(true);
	}

	@Async
	public CompletableFuture<List<Container>> launchContainer(LaunchContainerRequest launchContainerRequest,
															  WorkerManager workerManager) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers", publicIpAddress, port);
		try {
			ResponseEntity<Container[]> response = restTemplate.postForEntity(url, launchContainerRequest, Container[].class);
			Container[] responseBody = response.getBody();
			List<Container> containers = new ArrayList<>();
			if (responseBody != null) {
				containers.addAll(Arrays.asList(responseBody));
			}
			return CompletableFuture.completedFuture(containers);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public List<Container> launchContainers(LaunchContainerRequest launchContainerRequest) {
		HostAddress hostAddress = launchContainerRequest.getHostAddress();
		List<Coordinates> coordinates = launchContainerRequest.getCoordinates();
		List<Container> containers = new ArrayList<>();
		final int retries = 5;
		int tries = 0;
		do {
			try {
				if (hostAddress != null) {
					if (!hostAddress.isComplete()) {
						hostAddress = hostsService.completeHostAddress(hostAddress);
					}
					RegionEnum region = hostAddress.getRegion();
					WorkerManager workerManager = getRegionWorkerManager(region);
					// TODO when new workerManager, wait for it to start until requesting the action
					List<Container> workerContainers = launchContainer(launchContainerRequest, workerManager).get();
					containers.addAll(workerContainers);
				}
				else if (coordinates != null) {
					List<WorkerManager> regionWorkerManagers = coordinates.stream().map(c -> {
						RegionEnum region = RegionEnum.getClosestRegion(c);
						return getRegionWorkerManager(region);
					}).collect(Collectors.toList());
					Map<WorkerManager, CompletableFuture<List<Container>>> futureContainers = regionWorkerManagers.stream().collect(Collectors.toMap(
						workerManager -> workerManager,
						workerManager -> launchContainer(launchContainerRequest, workerManager)
					));

					CompletableFuture.allOf(futureContainers.values().toArray(new CompletableFuture[0])).join();

					for (Map.Entry<WorkerManager, CompletableFuture<List<Container>>> futureWorkerContainers : futureContainers.entrySet()) {
						WorkerManager workerManager = futureWorkerContainers.getKey();
						try {
							List<Container> workerContainers = futureWorkerContainers.getValue().get();
							containers.addAll(workerContainers);
						}
						catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
							throw new ManagerException("Failed to launch containers on worker from region %s", workerManager.getRegion());
						}
					}
				}
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				log.error("Failed to launch containers: {}... retrying ({}/{})", e.getMessage(), tries + 1, retries);
				Timing.sleep(tries * 2, TimeUnit.SECONDS);
			}
		} while (containers.isEmpty() && ++tries < retries);

		if (containers.isEmpty()) {
			throw new ManagerException("Failed to launch containers");
		}

		return containers;
	}

	@Async
	public CompletableFuture<List<Node>> getNodes(WorkerManager workerManager, boolean sync) {
		List<Node> nodes = new ArrayList<>();
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/nodes%s", publicIpAddress, port, sync ? "/sync" : "");
		try {
			Node[] response = sync ? restTemplate.postForObject(url, null, Node[].class) : restTemplate.getForObject(url, Node[].class);
			if (response != null) {
				nodes.addAll(Arrays.asList(response));
			}
			return CompletableFuture.completedFuture(nodes);
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
		List<WorkerManager> workerManagers = getWorkerManagers();
		List<CompletableFuture<List<Node>>> futureNodes =
			workerManagers.stream().map(workerManager -> getNodes(workerManager, sync)).collect(Collectors.toList());

		CompletableFuture.allOf(futureNodes.toArray(new CompletableFuture[0])).join();

		List<Node> nodes = new ArrayList<>();
		for (CompletableFuture<List<Node>> futureNode : futureNodes) {
			try {
				nodes.addAll(futureNode.get());
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		return nodes;
	}

	public List<Node> synchronizeNodesDatabase() {
		return getNodes(true);
	}

	@Async
	public CompletableFuture<String> getContainerLogs(WorkerManager workerManager) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/logs", publicIpAddress, port);
		try {
			String response = restTemplate.getForObject(url, String.class);
			return CompletableFuture.completedFuture(response);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	@Async
	public CompletableFuture<Container> replicateContainer(String managerId, String containerId, HostAddress toHostAddress) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/%s/replicate", publicIpAddress, port, containerId);
		try {
			Container container = restTemplate.postForObject(url, toHostAddress, Container.class);
			return CompletableFuture.completedFuture(container);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	@Async
	public CompletableFuture<Container> migrateContainer(String managerId, String containerId, HostAddress hostAddress) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/%s/migrate", publicIpAddress, port, containerId);
		try {
			Container container = restTemplate.postForObject(url, null, Container.class);
			return CompletableFuture.completedFuture(container);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public void reset() {
		workerManagers.deleteAll();
	}

	@Async
	public void stopContainer(WorkerManager workerManager, String containerId) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/containers/%s", publicIpAddress, port, containerId);
		try {
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

	@Async
	public CompletableFuture<Map<String, List<Container>>> launchApp(String appName, Coordinates coordinates, WorkerManager workerManager) {
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/apps/%s/launch", publicIpAddress, port, appName);
		try {
			Map<String, List<Container>> response = restTemplate.postForObject(url, coordinates, Map.class);
			log.info("Got reply from {}: {}", url, response);
			return CompletableFuture.completedFuture(response);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public Map<String, List<Container>> launchApp(String appName, Coordinates coordinates) {
		RegionEnum region = RegionEnum.getClosestRegion(coordinates);
		String errorMessage;
		final int retries = 5;
		int tries = 0;
		do {
			try {
				WorkerManager workerManager = getRegionWorkerManager(region);
				CompletableFuture<Map<String, List<Container>>> futureApp = launchApp(appName, coordinates, workerManager);
				return futureApp.get();
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				errorMessage = e.getMessage();
				log.error("Failed to launch app {}: {}... retrying ({}/{})", appName, e.getMessage(), tries + 1, retries);
				Timing.sleep(tries * 2, TimeUnit.SECONDS);
			}
		} while (++tries < retries);
		throw new ManagerException("Failed to launch app {}: {}", appName, errorMessage);
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

	public List<Node> leaveHost(String managerId, HostAddress hostAddress) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/nodes/%s/%s/leave", publicIpAddress, port, hostAddress.getPublicIpAddress(), hostAddress.getPrivateIpAddress());
		try {
			return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(new HttpHeaders()), List.class).getBody();
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public Node rejoinSwarm(String managerId, String nodeId) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/nodes/%s/join", publicIpAddress, port, nodeId);
		try {
			return restTemplate.postForObject(url, null, Node.class);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}

	public void removeNode(String managerId, String nodeId) {
		WorkerManager workerManager = getWorkerManager(managerId);
		String publicIpAddress = workerManager.getPublicIpAddress();
		int port = workerManager.getPort();
		String url = String.format("http://%s:%d/api/nodes/%s", publicIpAddress, port, nodeId);
		try {
			restTemplate.delete(url);
		}
		catch (HttpClientErrorException e) {
			throw new ManagerException(e.getMessage());
		}
	}
}
