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

package pt.unl.fct.miei.usmanagement.manager.services.management.workermanagers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerRepository;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.services.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.edge.EdgeHostsService;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WorkerManagersService {

	private final WorkerManagerRepository workerManagers;
	private final CloudHostsService cloudHostsService;
	private final EdgeHostsService edgeHostsService;
	private final ContainersService containersService;
	private final HostsService hostsService;

	public WorkerManagersService(WorkerManagerRepository workerManagers, CloudHostsService cloudHostsService,
								 EdgeHostsService edgeHostsService, @Lazy ContainersService containersService,
								 HostsService hostsService) {
		this.workerManagers = workerManagers;
		this.cloudHostsService = cloudHostsService;
		this.edgeHostsService = edgeHostsService;
		this.containersService = containersService;
		this.hostsService = hostsService;
	}

	public List<WorkerManagerEntity> getWorkerManagers() {
		return workerManagers.findAll();
	}

	public WorkerManagerEntity getWorkerManager(String id) {
		return workerManagers.findById(id).orElseThrow(() ->
			new EntityNotFoundException(WorkerManagerEntity.class, "id", id));
	}

	public WorkerManagerEntity getWorkerManager(ContainerEntity containerEntity) {
		return workerManagers.getByContainer(containerEntity).orElseThrow(() ->
			new EntityNotFoundException(WorkerManagerEntity.class, "containerEntity", containerEntity.getContainerId()));
	}

	public WorkerManagerEntity saveWorkerManager(ContainerEntity container) {
		return workerManagers.save(WorkerManagerEntity.builder().container(container).build());
	}

	public WorkerManagerEntity launchWorkerManager(HostAddress hostAddress) {
		log.info("Launching worker manager at {}", hostAddress);
		String id = UUID.randomUUID().toString();
		ContainerEntity container = launchWorkerManager(hostAddress, id);
		WorkerManagerEntity workerManagerEntity = WorkerManagerEntity.builder().id(id).container(container).build();
		return workerManagers.save(workerManagerEntity);
	}

	private ContainerEntity launchWorkerManager(HostAddress hostAddress, String id) {
		List<String> environment = new LinkedList<>(List.of(
			ContainerConstants.Environment.ID + "=" + id,
			ContainerConstants.Environment.MASTER + "=" + hostsService.getHostAddress().getPublicIpAddress()));
		return containersService.launchContainer(hostAddress, WorkerManagerProperties.WORKER_MANAGER, environment);
	}

	public void deleteWorkerManager(String workerManagerId) {
		WorkerManagerEntity workerManager = getWorkerManager(workerManagerId);
		containersService.stopContainer(workerManager.getContainer().getContainerId());
	}

	public void deleteWorkerManagerByContainer(ContainerEntity container) {
		WorkerManagerEntity workerManager = getWorkerManager(container);
		workerManagers.delete(workerManager);
	}

	public List<String> getAssignedHosts(String workerManagerId) {
		checkWorkerManagerExists(workerManagerId);
		List<String> cloudHosts = workerManagers.getCloudHosts(workerManagerId).stream()
			.map(CloudHostEntity::getPublicIpAddress).collect(Collectors.toList());
		List<String> edgeHosts = workerManagers.getEdgeHosts(workerManagerId).stream()
			.map(EdgeHostEntity::getPublicIpAddress).collect(Collectors.toList());
		return Stream.concat(cloudHosts.stream(), edgeHosts.stream()).collect(Collectors.toList());
	}

	// host is instanceId for cloud hosts, dns/publicIpAddress for edge hosts
	public void assignHost(String workerManagerId, String hostname) {
		WorkerManagerEntity workerManagerEntity = getWorkerManager(workerManagerId);
		try {
			cloudHostsService.assignWorkerManager(workerManagerEntity, hostname);
		}
		catch (EntityNotFoundException ignored) {
			edgeHostsService.assignWorkerManager(workerManagerEntity, hostname);
		}
	}

	public void assignHosts(String workerManagerId, List<String> hosts) {
		hosts.forEach(host -> assignHost(workerManagerId, host));
	}

	public void unassignHost(String workerManagerId, String host) {
		unassignHosts(workerManagerId, List.of(host));
	}

	public void unassignHosts(String workerManagerId, List<String> hosts) {
		WorkerManagerEntity workerManager = getWorkerManager(workerManagerId);
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
			throw new EntityNotFoundException(WorkerManagerEntity.class, "id", id);
		}
	}

}
