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

package pt.unl.fct.microservicemanagement.managermaster.manager.monitoring;

import pt.unl.fct.microservicemanagement.managermaster.ManagerMasterProperties;
import pt.unl.fct.microservicemanagement.managermaster.exceptions.MasterManagerException;
import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainerConstants;
import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainerEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.containers.ContainersService;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MasterManagerMonitoringService {

  private final ContainersService containersService;
  private final ContainersMonitoringService containersMonitoringService;

  private final long monitorPeriod;
  private final boolean isTestLogsEnable;

  public MasterManagerMonitoringService(ContainersService containersService,
                                        ContainersMonitoringService containersMonitoringService,
                                        ManagerMasterProperties managerMasterProperties) {
    this.containersService = containersService;
    this.containersMonitoringService = containersMonitoringService;
    this.monitorPeriod = managerMasterProperties.getMonitorPeriod();
    this.isTestLogsEnable = managerMasterProperties.getTests().isTestLogsEnable();
  }

  public void initMasterManagerMonitorTimer() {
    if (isTestLogsEnable) {
      new Timer("MasterManagerMonitorTimer", true).schedule(new TimerTask() {
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
          long currentTime = System.currentTimeMillis();
          //TODO replace diffSeconds with calculation from previous database save
          int secondsFromLastRun = (int) ((currentTime - lastTime) / 1000);
          lastTime = currentTime;
          try {
            masterManagerMonitorTask(secondsFromLastRun);
          } catch (MasterManagerException e) {
            log.error(e.getMessage());
          }
        }
      }, monitorPeriod, monitorPeriod);
    }
  }

  private void masterManagerMonitorTask(int secondsFromLastRun) {
    containersService
        .getContainersWithLabels(Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME,
            ManagerMasterProperties.MANAGER_MASTER)))
        .stream()
        .findFirst()
        .ifPresent(c -> saveMasterManagerContainerFields(c, secondsFromLastRun));
  }

  private void saveMasterManagerContainerFields(ContainerEntity container, int secondsFromLastRun) {
    Map<String, Double> newFields = containersMonitoringService.getContainerStats(container, secondsFromLastRun);
    newFields.forEach((field, value) ->
        containersMonitoringService.saveMonitoringServiceLog(container.getContainerId(),
            ManagerMasterProperties.MANAGER_MASTER, field, value)
    );
  }

}
