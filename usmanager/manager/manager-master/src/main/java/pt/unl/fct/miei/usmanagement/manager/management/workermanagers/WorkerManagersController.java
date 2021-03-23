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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.Mode;
import pt.unl.fct.miei.usmanagement.manager.config.ManagerServicesConfiguration;
import pt.unl.fct.miei.usmanagement.manager.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/worker-managers")
public class WorkerManagersController {

	private final WorkerManagersService workerManagersService;
	private final ManagerServicesConfiguration managerServicesConfiguration;

<<<<<<< HEAD:usmanager/manager/manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/management/workermanagers/WorkerManagersController.java
	public WorkerManagersController(WorkerManagersService workerManagersService,
									ManagerServicesConfiguration managerServicesConfiguration) {
=======
	public WorkerManagersController(WorkerManagersService workerManagersService, ManagerServicesConfiguration managerServicesConfiguration) {
>>>>>>> 02b9fb7e15ad1655a7a18f29507ba6f0ca23dee7:manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/management/workermanagers/WorkerManagersController.java
		this.workerManagersService = workerManagersService;
		this.managerServicesConfiguration = managerServicesConfiguration;
	}

	@GetMapping
	public List<WorkerManager> getWorkerManagers() {
		return workerManagersService.getWorkerManagers();
	}

	@GetMapping("/{workerManagerId}")
	public WorkerManager getWorkerManager(@PathVariable String workerManagerId) {
		return workerManagersService.getWorkerManager(workerManagerId);
	}

	@PostMapping
	public List<WorkerManager> launchWorkerManagers(@RequestBody LaunchWorkerManager launchWorkerManager) {
		if (managerServicesConfiguration.getMode() == Mode.LOCAL) {
<<<<<<< HEAD:usmanager/manager/manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/management/workermanagers/WorkerManagersController.java
			throw new BadRequestException("local managers can't be launched on local mode configuration");
=======
			throw new BadRequestException("worker managers are not supported when using local execution mode");
>>>>>>> 02b9fb7e15ad1655a7a18f29507ba6f0ca23dee7:manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/management/workermanagers/WorkerManagersController.java
		}
		HostAddress hostAddress = launchWorkerManager.getHostAddress();
		List<String> regions = launchWorkerManager.getRegions();
		if (hostAddress != null) {
			return List.of(workerManagersService.launchWorkerManager(hostAddress));
		}
		else if (regions != null) {
			List<RegionEnum> regionsList = Arrays.stream(regions.toArray(new String[0])).map(RegionEnum::getRegion).collect(Collectors.toList());
			return workerManagersService.launchWorkerManagers(regionsList);
		}
		else {
			throw new BadRequestException("expected host address or regions to start registration server");
		}
	}

	@DeleteMapping("/{workerManagerId}")
	public void stopWorkerManager(@PathVariable String workerManagerId) {
		workerManagersService.stopWorkerManager(workerManagerId);
	}

}
