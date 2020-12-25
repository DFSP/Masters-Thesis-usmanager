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

package pt.unl.fct.miei.usmanagement.manager.management.containers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.LaunchContainerRequest;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/containers")
public class ContainersController {

	private final ContainersService containersService;
	private final SyncService syncService;

	public ContainersController(ContainersService containersService, SyncService syncService) {
		this.containersService = containersService;
		this.syncService = syncService;
	}

	@GetMapping
	public List<Container> getContainers(@RequestParam(required = false) String serviceName) {
		List<Container> containers;
		if (serviceName != null) {
			containers = containersService.getContainersWithLabels(
				Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, serviceName)));
		}
		else {
			containers = containersService.getContainers();
		}
		return containers;
	}

	@GetMapping("/{id}")
	public Container getContainer(@PathVariable String id) {
		return containersService.getContainer(id);
	}

	@PostMapping
	public List<Container> launchContainer(@RequestBody LaunchContainerRequest launchContainerRequest) {
		String serviceName = launchContainerRequest.getService();
		int internalPort = launchContainerRequest.getInternalPort();
		int externalPort = launchContainerRequest.getExternalPort();
		HostAddress hostAddress = launchContainerRequest.getHostAddress();
		List<Coordinates> coordinates = launchContainerRequest.getCoordinates();
		List<Container> containers = new ArrayList<>();
		if (hostAddress != null) {
			Container container = containersService.launchContainer(hostAddress, serviceName, internalPort, externalPort);
			containers.add(container);
		}
		else if (coordinates != null) {
			for (Coordinates coordinate : coordinates) {
				Container container = containersService.launchContainer(coordinate, serviceName, internalPort, externalPort);
				containers.add(container);
			}
		}
		else {
			throw new BadRequestException("Expected host address or coordinates to start containers");
		}
		return containers;
	}

	@DeleteMapping("/{id}")
	public void deleteContainer(@PathVariable String id) {
		containersService.stopContainer(id);
	}

	@PostMapping("/{id}/replicate")
	public Container replicateContainer(@PathVariable String id, @RequestBody HostAddress hostAddress) {
		return containersService.replicateContainer(id, hostAddress);
	}

	@PostMapping("/{id}/migrate")
	public Container migrateContainer(@PathVariable String id, @RequestBody HostAddress hostAddress) {
		return containersService.migrateContainer(id, hostAddress);
	}

	@PostMapping("/sync")
	public List<Container> syncDatabaseContainers() {
		return syncService.synchronizeContainersDatabase();
	}

	@GetMapping("/{containerId}/logs")
	public String getContainerLogs(@PathVariable String containerId) {
		return containersService.getLogs(containerId);
	}

}
