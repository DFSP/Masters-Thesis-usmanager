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

package pt.unl.fct.microservicemanagement.manager.master.manager.monitoring;

import pt.unl.fct.microservicemanagement.manager.master.ManagerMasterProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TestLogsService {

  //TODO rename class

  private final TestsMonitoringRepository testsMonitoringRepository;
  private final boolean isTestsLogsEnable;

  public TestLogsService(TestsMonitoringRepository testsMonitoringRepository,
                         ManagerMasterProperties managerMasterProperties) {
    this.testsMonitoringRepository = testsMonitoringRepository;
    this.isTestsLogsEnable = managerMasterProperties.getTests().isTestLogsEnable();
  }

  public void saveMonitoringServiceLogTests(String containerId, String serviceName, String field,
                                            double effectiveValue) {
    if (isTestsLogsEnable) {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String lastUpdate = dtf.format(LocalDateTime.now());
      MonitoringServiceLogTests monitoringServiceLogTests = MonitoringServiceLogTests.builder()
          .containerId(containerId).serviceName(serviceName).field(field).lastUpdate(lastUpdate)
          .effectiveValue(effectiveValue).build();
      testsMonitoringRepository.save(monitoringServiceLogTests);
    }
  }

  public List<MonitoringServiceLogTests> getMonitoringServiceLogTests() {
    return testsMonitoringRepository.findAll();
  }

  public List<MonitoringServiceLogTests> getMonitoringServiceLogTestsByServiceName(String serviceName) {
    return testsMonitoringRepository.findByServiceName(serviceName);
  }

  public List<MonitoringServiceLogTests> getMonitoringServiceLogTestsByContainerId(String containerId) {
    return testsMonitoringRepository.findByContainerId(containerId);
  }

}
