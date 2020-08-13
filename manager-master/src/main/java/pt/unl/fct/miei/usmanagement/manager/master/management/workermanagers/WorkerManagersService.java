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

package pt.unl.fct.miei.usmanagement.manager.master.management.workermanagers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerRepository;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.edge.EdgeHostsService;

@Slf4j
@Service
public class WorkerManagersService {

  private final WorkerManagerRepository workerManagers;
  private final CloudHostsService cloudHostsService;
  private final EdgeHostsService edgeHostsService;
  private final ContainersService containersService;

  public WorkerManagersService(WorkerManagerRepository workerManagers,
                               CloudHostsService cloudHostsService,
                               EdgeHostsService edgeHostsService,
                               ContainersService containersService) {
    this.workerManagers = workerManagers;
    this.cloudHostsService = cloudHostsService;
    this.edgeHostsService = edgeHostsService;
    this.containersService = containersService;
  }

  public List<WorkerManagerEntity> getWorkerManagers() {
    return workerManagers.findAll();
  }

  public WorkerManagerEntity getWorkerManager(String id) {
    return workerManagers.findById(id).orElseThrow(() ->
        new EntityNotFoundException(WorkerManagerEntity.class, "id", id));
  }

  public WorkerManagerEntity addWorkerManager(String hostname) {
    log.debug("Launching worker manager at {}", hostname);
    WorkerManagerEntity workerManagerEntity;
    try {
      CloudHostEntity cloudHostEntity = cloudHostsService.getCloudHostByHostname(hostname);
      workerManagerEntity = workerManagers.save(WorkerManagerEntity.builder().cloudHost(cloudHostEntity).build());
    } catch (EntityNotFoundException ignored) {
      EdgeHostEntity edgeHostEntity = edgeHostsService.getEdgeHost(hostname);
      hostname = edgeHostEntity.getPublicIpAddress();
      workerManagerEntity = workerManagers.save(WorkerManagerEntity.builder().edgeHost(edgeHostEntity).build());
    }
    this.launchWorkerManager(hostname);
    return workerManagerEntity;
  }

  private ContainerEntity launchWorkerManager(String hostname) {
    List<String> environment = List.of("");
    return containersService.launchContainer(hostname, WorkerManagerProperties.WORKER_MANAGER, environment);
  }

  public void deleteWorkerManager(String workerManagerId) {
    var workerManager = getWorkerManager(workerManagerId);
    workerManagers.delete(workerManager);
  }

  public List<String> getAssignedMachines(String workerManagerId) {
    assertWorkerManagerExists(workerManagerId);
    List<String> cloudHosts = workerManagers.getCloudHosts(workerManagerId).stream()
        .map(CloudHostEntity::getPublicIpAddress).collect(Collectors.toList());
    List<String> edgeHosts = workerManagers.getEdgeHosts(workerManagerId).stream()
        .map(EdgeHostEntity::getHostname).collect(Collectors.toList());
    return Stream.concat(cloudHosts.stream(), edgeHosts.stream()).collect(Collectors.toList());
  }

  public void assignMachine(String workerManagerId, String machine) {
    WorkerManagerEntity workerManagerEntity = this.getWorkerManager(workerManagerId);
    try {
      cloudHostsService.assignWorkerManager(workerManagerEntity, machine);
    } catch (EntityNotFoundException ignored) {
      edgeHostsService.assignWorkerManager(workerManagerEntity, machine);
    }
  }

  public void assignMachines(String workerManagerId, List<String> machines) {
    machines.forEach(machine -> assignMachine(workerManagerId, machine));
  }

  public void unassignMachine(String workerManagerId, String machine) {
    unassignMachines(workerManagerId, List.of(machine));
  }

  public void unassignMachines(String workerManagerId, List<String> machines) {
    var workerManager = getWorkerManager(workerManagerId);
    log.info("Removing machines {}", machines);
    machines.forEach(machine -> {
      cloudHostsService.unassignWorkerManager(machine);
      edgeHostsService.unassignWorkerManager(machine);
    });
    /*workerManager.getAssignedCloudHosts().removeIf(cloudHost -> machines.contains(cloudHost.getPublicIpAddress()));
    workerManager.getAssignedEdgeHosts().removeIf(edgeHost -> machines.contains(edgeHost.getHostname()));*/
    workerManagers.save(workerManager);
  }

  private void assertWorkerManagerExists(String id) {
    if (!workerManagers.hasWorkerManager(id)) {
      throw new EntityNotFoundException(WorkerManagerEntity.class, "id", id);
    }
  }

}
