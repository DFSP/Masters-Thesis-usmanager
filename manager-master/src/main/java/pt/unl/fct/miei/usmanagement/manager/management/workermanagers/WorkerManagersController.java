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
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/worker-managers")
public class WorkerManagersController {

	private final WorkerManagersService workerManagersService;

	public WorkerManagersController(WorkerManagersService workerManagersService) {
		this.workerManagersService = workerManagersService;
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
			throw new BadRequestException("Expected host address or regions to start registration server");
		}
	}

	@DeleteMapping("/{workerManagerId}")
	public void stopWorkerManager(@PathVariable String workerManagerId) {
		workerManagersService.stopWorkerManager(workerManagerId);
	}

}
