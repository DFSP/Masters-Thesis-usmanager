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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;

@RestController
@RequestMapping("/worker-managers")
public class WorkerManagersController {

  private final WorkerManagersMachine workerManagersMachine;

  public WorkerManagersController(WorkerManagersMachine workerManagersMachine) {
    this.workerManagersMachine = workerManagersMachine;
  }

  @GetMapping
  public List<WorkerManagerEntity> getWorkerManagers() {
    return workerManagersMachine.getWorkerManagers();
  }

  @GetMapping("/{workerManagerId}")
  public WorkerManagerEntity getWorkerManager(@PathVariable String workerManagerId) {
    return workerManagersMachine.getWorkerManager(workerManagerId);
  }

  @PostMapping
  public WorkerManagerEntity addWorkerManager(@RequestBody AddWorkerManager addWorkerManager) {
    return workerManagersMachine.addWorkerManager(addWorkerManager.getHost());
  }

  @DeleteMapping("/{workerManagerId}")
  public void deleteWorkerManager(@PathVariable String workerManagerId) {
    workerManagersMachine.deleteWorkerManager(workerManagerId);
  }

  @GetMapping("/{workerManagerId}/machines")
  public List<String> getMachines(@PathVariable String workerManagerId) {
    return workerManagersMachine.getMachines(workerManagerId);
  }

  @PostMapping("/{workerManagerId}/machines")
  public void addMachines(@PathVariable String workerManagerId, @RequestBody String[] machines) {
    workerManagersMachine.addMachines(workerManagerId, machines);
  }

  @DeleteMapping("/{workerManagerId}/machines")
  public void removeMachines(@PathVariable String workerManagerId, @RequestBody String[] machines) {
    workerManagersMachine.removeMachines(workerManagerId, Arrays.asList(machines));
  }

  @DeleteMapping("/{workerManagerId}/machines/{hostname}")
  public void removeMachine(@PathVariable String workerManagerId, @PathVariable String hostname) {
    workerManagersMachine.removeMachine(workerManagerId, hostname);
  }

}
