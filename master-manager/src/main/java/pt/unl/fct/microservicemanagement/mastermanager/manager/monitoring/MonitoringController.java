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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring;

import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.HostFieldAvg;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

  //TODO split into 2?

  private final HostsMonitoringService hostsMonitoringService;
  private final ContainersMonitoringService containersMonitoringService;
  private final TestLogsService testLogsService;

  public MonitoringController(HostsMonitoringService hostsMonitoringService,
                              ContainersMonitoringService containersMonitoringService,
                              TestLogsService testLogsService) {
    this.hostsMonitoringService = hostsMonitoringService;
    this.containersMonitoringService = containersMonitoringService;
    this.testLogsService = testLogsService;
  }

  @GetMapping("/services")
  public List<ServiceMonitoring> getMonitoringServiceLogs() {
    return containersMonitoringService.getMonitoringServiceLogs();
  }

  @GetMapping("/services/{serviceName}/avg")
  public List<ServiceFieldAvg> getMonitoringServiceLogsByService(@PathVariable String serviceName) {
    return containersMonitoringService.getAvgServiceFields(serviceName);
  }

  @GetMapping("/services/{serviceName}/fields/{field}/avg")
  public ServiceFieldAvg getMonitoringServiceLogsByServiceAndField(@PathVariable String serviceName,
                                                                   @PathVariable String field) {
    return containersMonitoringService.getAvgServiceField(serviceName, field);
  }

  @GetMapping("/hosts")
  public List<HostMonitoringEntity> getMonitoringHostLogs() {
    return hostsMonitoringService.getMonitoringHostLogs();
  }

  @GetMapping("/hosts/{hostname}/avg")
  public List<HostFieldAvg> getMonitoringHostLogsByHost(@PathVariable String hostname) {
    return hostsMonitoringService.getAvgHostFields(hostname);
  }

  @GetMapping("/hosts/{hostname}/fields/{field}/avg")
  public HostFieldAvg getMonitoringHostLogsByHostAndField(@PathVariable String hostname, @PathVariable String field) {
    return hostsMonitoringService.getAvgHostField(hostname, field);
  }

  @GetMapping("/logs")
  public List<MonitoringServiceLogTests> getMonitoringServiceLogTests() {
    return testLogsService.getMonitoringServiceLogTests();
  }

  @GetMapping("/logs/services/{serviceName}")
  public List<MonitoringServiceLogTests> getMonitoringServiceLogTestsByServiceName(@PathVariable String serviceName) {
    return testLogsService.getMonitoringServiceLogTestsByServiceName(serviceName);
  }

  @GetMapping("/logs/containers/{containerId}")
  public List<MonitoringServiceLogTests> getMonitoringServiceLogTestsByContainerId(@PathVariable String containerId) {
    return testLogsService.getMonitoringServiceLogTestsByContainerId(containerId);
  }

}
