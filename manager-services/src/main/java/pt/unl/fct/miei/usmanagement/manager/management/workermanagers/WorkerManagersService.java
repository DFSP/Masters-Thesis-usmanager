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

package pt.unl.fct.miei.usmanagement.manager.management.workermanagers;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WorkerManagersService {

	private final WorkerManagers workerManagers;
	private final CloudHostsService cloudHostsService;
	private final EdgeHostsService edgeHostsService;
	private final ContainersService containersService;
	private final HostsService hostsService;
	private final ServicesService servicesService;
	private final Environment environment;

	private final HttpHeaders headers;
	private final RestTemplate restTemplate;
	private final int port;

	public WorkerManagersService(WorkerManagers workerManagers, CloudHostsService cloudHostsService,
								 EdgeHostsService edgeHostsService, @Lazy ContainersService containersService,
								 HostsService hostsService, ServicesService servicesService, DockerProperties dockerProperties,
								 Environment environment, WorkerManagerProperties workerManagerProperties) {
		this.workerManagers = workerManagers;
		this.cloudHostsService = cloudHostsService;
		this.edgeHostsService = edgeHostsService;
		this.containersService = containersService;
		this.hostsService = hostsService;
		this.servicesService = servicesService;
		this.environment = environment;
		String username = dockerProperties.getApiProxy().getUsername();
		String password = dockerProperties.getApiProxy().getPassword();
		byte[] auth = String.format("%s:%s", username, password).getBytes();
		String basicAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
		this.headers = new HttpHeaders();
		this.headers.add("Authorization", basicAuthorization);
		this.restTemplate = new RestTemplate();
		this.port = workerManagerProperties.getPort();
	}

	public List<WorkerManager> getWorkerManagers() {
		return workerManagers.findAll();
	}

	public WorkerManager getWorkerManager(String id) {
		return workerManagers.findById(id).orElseThrow(() ->
			new EntityNotFoundException(WorkerManager.class, "id", id));
	}

	public WorkerManager getWorkerManager(Container container) {
		return workerManagers.getByContainer(container).orElseThrow(() ->
			new EntityNotFoundException(WorkerManager.class, "containerEntity", container.getContainerId()));
	}

	public List<WorkerManager> getWorkerManagers(RegionEnum region) {
		return workerManagers.getByContainer_Region(region);
	}

	public WorkerManager saveWorkerManager(Container container) {
		return workerManagers.save(WorkerManager.builder().container(container).build());
	}

	public WorkerManager launchWorkerManager(HostAddress hostAddress) {
		log.info("Launching worker manager at {}", hostAddress);
		String id = UUID.randomUUID().toString();
		Container container = launchWorkerManager(hostAddress, id);
		WorkerManager workerManager = WorkerManager.builder().id(id).container(container).build();
		return workerManagers.save(workerManager);
	}

	public List<WorkerManager> launchWorkerManagers(List<RegionEnum> regions) {
		log.info("Launching worker managers at regions {}", regions);

		double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(WorkerManagerProperties.WORKER_MANAGER);

		return regions.parallelStream()
			.map(region -> hostsService.getCapableNode(expectedMemoryConsumption, region))
			.distinct()
			.map(hostAddress -> {
				// avoid launching another worker manager on the same region
				List<WorkerManager> workerManagers = getWorkerManagers(hostAddress.getRegion());
				return !workerManagers.isEmpty() ?
					workerManagers.get(0)
					: launchWorkerManager(hostAddress);
			}).collect(Collectors.toList());
	}

	private Container launchWorkerManager(HostAddress hostAddress, String id) {
		List<String> environment = new LinkedList<>(List.of(
			ContainerConstants.Environment.WorkerManager.EXTERNAL_ID + "=" + id,
			ContainerConstants.Environment.WorkerManager.REGISTRATION_URL + "=" + getRegistrationUrl(),
			ContainerConstants.Environment.WorkerManager.SYNC_URL + "=" + getSyncUrl(),
			ContainerConstants.Environment.WorkerManager.HOST_ADDRESS + "=" + new Gson().toJson(hostAddress)
		));
		return containersService.launchContainer(hostAddress, WorkerManagerProperties.WORKER_MANAGER, environment);
	}

	public void deleteWorkerManager(String workerManagerId) {
		WorkerManager workerManager = getWorkerManager(workerManagerId);
		containersService.stopContainer(workerManager.getContainer().getContainerId());
	}

	public void deleteWorkerManagerByContainer(Container container) {
		WorkerManager workerManager = getWorkerManager(container);
		workerManagers.delete(workerManager);
	}

	public List<String> getAssignedHosts(String workerManagerId) {
		checkWorkerManagerExists(workerManagerId);
		List<String> cloudHosts = workerManagers.getCloudHosts(workerManagerId).stream()
			.map(CloudHost::getPublicIpAddress).collect(Collectors.toList());
		List<String> edgeHosts = workerManagers.getEdgeHosts(workerManagerId).stream()
			.map(EdgeHost::getPublicIpAddress).collect(Collectors.toList());
		return Stream.concat(cloudHosts.stream(), edgeHosts.stream()).collect(Collectors.toList());
	}

	// host is instanceId for cloud hosts, dns/publicIpAddress for edge hosts
	public void assignHost(String workerManagerId, String hostname) {
		WorkerManager workerManager = getWorkerManager(workerManagerId);
		try {
			cloudHostsService.assignWorkerManager(workerManager, hostname);
		}
		catch (EntityNotFoundException ignored) {
			edgeHostsService.assignWorkerManager(workerManager, hostname);
		}
	}

	public void assignHosts(String workerManagerId, List<String> hosts) {
		hosts.forEach(host -> assignHost(workerManagerId, host));
	}

	public void unassignHost(String workerManagerId, String host) {
		unassignHosts(workerManagerId, List.of(host));
	}

	public void unassignHosts(String workerManagerId, List<String> hosts) {
		WorkerManager workerManager = getWorkerManager(workerManagerId);
		log.info("Removing hosts {}", hosts);
		hosts.forEach(host -> {
			try {
				cloudHostsService.unassignWorkerManager(host);
			}
			catch (EntityNotFoundException ignored) {
				edgeHostsService.unassignWorkerManager(host);
			}
		});
		workerManagers.save(workerManager);
	}

	private void checkWorkerManagerExists(String id) {
		if (!workerManagers.hasWorkerManager(id)) {
			throw new EntityNotFoundException(WorkerManager.class, "id", id);
		}
	}

	@Async
	public CompletableFuture<List<Container>> getContainers(Container workerManager, boolean sync) {
		List<Container> containers = new ArrayList<>();
		String hostname = workerManager.getHostAddress().getPublicIpAddress();
		String url = String.format("%s:%s/api/containers/%s", hostname, port, sync ? "sync" : "");
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<Container[]> response = restTemplate.exchange(url, HttpMethod.GET, request, Container[].class);
		Container[] responseBody = response.getBody();
		if (responseBody != null) {
			containers.addAll(Arrays.asList(responseBody));
		}
		return CompletableFuture.completedFuture(containers);
	}

	public List<Container> getContainers() {
		return getContainers(false);
	}

	public List<Container> getContainers(boolean sync) {
		List<WorkerManager> workerManagers = getWorkerManagers();
		List<CompletableFuture<List<Container>>> futureContainers =
			workerManagers.stream().map(WorkerManager::getContainer).map(c -> getContainers(c, sync)).collect(Collectors.toList());

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
	public CompletableFuture<List<Node>> getNodes(Container workerManager, boolean sync) {
		List<Node> nodes = new ArrayList<>();
		String hostname = workerManager.getHostAddress().getPublicIpAddress();
		String url = String.format("%s:%s/api/nodes/%s", hostname, port, sync ? "sync" : "");
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<Node[]> response = restTemplate.exchange(url, HttpMethod.GET, request, Node[].class);
		Node[] responseBody = response.getBody();
		if (responseBody != null) {
			nodes.addAll(Arrays.asList(responseBody));
		}
		return CompletableFuture.completedFuture(nodes);
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
			workerManagers.stream().map(WorkerManager::getContainer).map(c -> getNodes(c, sync)).collect(Collectors.toList());

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

	public List<Node> synchronizeDatabaseNodes() {
		return getNodes(true);
	}

	private String getRegistrationUrl() {
		String hostname = hostsService.getManagerHostAddress().getPublicIpAddress();
		String port = environment.getProperty("local.server.port");
		return String.format("http://%s:%s/api/worker-managers/registration", hostname, port);
	}

	private String getSyncUrl() {
		String hostname = hostsService.getManagerHostAddress().getPublicIpAddress();
		String port = environment.getProperty("local.server.port");
		return String.format("http://%s:%s/api/worker-managers/sync", hostname, port);
	}



}
