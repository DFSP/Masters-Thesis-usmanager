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

package pt.unl.fct.miei.usmanagement.manager.master.management.hosts;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.edge.AddEdgeHostRequest;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;

@RestController
@RequestMapping("/hosts")
public class HostsController {

  private final CloudHostsService cloudHostsService;
  private final EdgeHostsService edgeHostsService;

  public HostsController(CloudHostsService cloudHostsService, EdgeHostsService edgeHostsService) {
    this.cloudHostsService = cloudHostsService;
    this.edgeHostsService = edgeHostsService;
  }

  @PostMapping("/cloud")
  public CloudHostEntity startCloudHost() {
    return cloudHostsService.startCloudHost();
  }

  @GetMapping("/cloud")
  public List<CloudHostEntity> getCloudHosts() {
    return cloudHostsService.getCloudHosts();
  }

  @PostMapping("/cloud/sync")
  public List<CloudHostEntity> syncCloudInstances() {
    return cloudHostsService.syncCloudInstances();
  }

  @GetMapping("/cloud/{id}")
  public CloudHostEntity getCloudHost(@PathVariable String id) {
    return cloudHostsService.getCloudHostByIdOrIp(id);
  }

  @PostMapping("/cloud/{instanceId}/state")
  public CloudHostEntity changeCloudHostState(@PathVariable String instanceId, @RequestBody String action) {
    switch (action) {
      case "start":
        return cloudHostsService.startCloudHost(instanceId, true);
      case "stop":
        return cloudHostsService.stopCloudHost(instanceId);
      default:
        throw new BadRequestException("Invalid request body: expected 'start' or 'stop'");
    }
  }

  @DeleteMapping("/cloud/{instanceId}")
  public void terminateCloudInstance(@PathVariable String instanceId) {
    cloudHostsService.terminateCloudHost(instanceId);
  }

  @GetMapping("/cloud/{instanceId}/rules/{ruleName}")
  public HostRuleEntity getCloudHostRule(@PathVariable String instanceId, String ruleName) {
    return cloudHostsService.getRule(instanceId, ruleName);
  }

  @GetMapping("/cloud/{instanceId}/rules")
  public List<HostRuleEntity> getCloudHostRules(@PathVariable String instanceId) {
    return cloudHostsService.getRules(instanceId);
  }

  @PostMapping("/cloud/{instanceId}/rules")
  public void addCloudHostRules(@PathVariable String instanceId, @RequestBody String[] rules) {
    cloudHostsService.addRules(instanceId, Arrays.asList(rules));
  }

  @DeleteMapping("/cloud/{instanceId}/rules")
  public void removeCloudHostRules(@PathVariable String instanceId, @RequestBody String[] rules) {
    cloudHostsService.removeRules(instanceId, Arrays.asList(rules));
  }

  @DeleteMapping("/cloud/{instanceId}/rules/{ruleName}")
  public void removeCloudHostRule(@PathVariable String instanceId, @PathVariable String ruleName) {
    cloudHostsService.removeRule(instanceId, ruleName);
  }

  @GetMapping("/cloud/{instanceId}/simulated-metrics")
  public List<HostSimulatedMetricEntity> getCloudHostSimulatedMetrics(@PathVariable String instanceId) {
    return cloudHostsService.getSimulatedMetrics(instanceId);
  }

  @GetMapping("/cloud/{instanceId}/simulated-metrics/{simulatedMetricName}")
  public HostSimulatedMetricEntity getCloudHostSimulatedMetric(@PathVariable String instanceId,
                                                               @PathVariable String simulatedMetricName) {
    return cloudHostsService.getSimulatedMetric(instanceId, simulatedMetricName);
  }

  @PostMapping("/cloud/{instanceId}/simulated-metrics")
  public void addCloudHostSimulatedMetrics(@PathVariable String instanceId, @RequestBody String[] simulatedMetrics) {
    cloudHostsService.addSimulatedMetrics(instanceId, Arrays.asList(simulatedMetrics));
  }

  @DeleteMapping("/cloud/{instanceId}/simulated-metrics")
  public void removeCloudHostSimulatedMetrics(@PathVariable String instanceId, @RequestBody String[] simulatedMetrics) {
    cloudHostsService.removeSimulatedMetrics(instanceId, Arrays.asList(simulatedMetrics));
  }

  @DeleteMapping("/cloud/{instanceId}/simulated-metrics/{simulatedMetricName}")
  public void removeCloudHostSimulatedMetric(@PathVariable String instanceId,
                                             @PathVariable String simulatedMetricName) {
    cloudHostsService.removeSimulatedMetric(instanceId, simulatedMetricName);
  }

  @GetMapping("/edge")
  public List<EdgeHostEntity> getEdgeHosts() {
    return edgeHostsService.getEdgeHosts();
  }

  @GetMapping("/edge/{hostname}")
  public EdgeHostEntity getEdgeHost(@PathVariable String hostname) {
    return edgeHostsService.getEdgeHostByDnsOrIp(hostname);
  }

  @PostMapping("/edge")
  public EdgeHostEntity addEdgeHost(@RequestBody AddEdgeHostRequest addEdgeHostRequest) {
    return edgeHostsService.addEdgeHost(addEdgeHostRequest);
  }

  @PutMapping("/edge/{hostname}")
  public EdgeHostEntity updateEdgeHost(@PathVariable String hostname, @RequestBody EdgeHostEntity edgeHost) {
    Validation.validatePutRequest(edgeHost.getId());
    return edgeHostsService.updateEdgeHost(hostname, edgeHost);
  }

  @DeleteMapping("/edge/{hostname}")
  public void deleteEdgeHost(@PathVariable String hostname) {
    edgeHostsService.deleteEdgeHost(hostname);
  }

  @GetMapping("/edge/{hostname}/rules")
  public List<HostRuleEntity> getEdgeHostRules(@PathVariable String hostname) {
    return edgeHostsService.getRules(hostname);
  }

  @GetMapping("/edge/{hostname}/rules/{ruleName}")
  public HostRuleEntity getEdgeHostRule(@PathVariable String hostname, @PathVariable String ruleName) {
    return edgeHostsService.getRule(hostname, ruleName);
  }

  @PostMapping("/edge/{hostname}/rules")
  public void addEdgeHostRules(@PathVariable String hostname, @RequestBody String[] rules) {
    edgeHostsService.addRules(hostname, Arrays.asList(rules));
  }

  @DeleteMapping("/edge/{hostname}/rules")
  public void removeEdgeHostRules(@PathVariable String hostname, @RequestBody String[] rules) {
    edgeHostsService.removeRules(hostname, Arrays.asList(rules));
  }

  @DeleteMapping("/edge/{hostname}/rules/{ruleName}")
  public void removeEdgeHostRule(@PathVariable String hostname, @PathVariable String ruleName) {
    edgeHostsService.removeRule(hostname, ruleName);
  }

  @GetMapping("/edge/{hostname}/simulated-metrics")
  public List<HostSimulatedMetricEntity> getEdgeHostSimulatedMetrics(@PathVariable String hostname) {
    return edgeHostsService.getSimulatedMetrics(hostname);
  }

  @GetMapping("/edge/{hostname}/simulated-metrics/{simulatedMetricName}")
  public HostSimulatedMetricEntity getEdgeHostSimulatedMetric(@PathVariable String hostname,
                                                              @PathVariable String simulatedMetricName) {
    return edgeHostsService.getSimulatedMetric(hostname, simulatedMetricName);
  }

  @PostMapping("/edge/{hostname}/simulated-metrics")
  public void addEdgeHostSimulatedMetrics(@PathVariable String hostname, @RequestBody String[] simulatedMetrics) {
    edgeHostsService.addSimulatedMetrics(hostname, Arrays.asList(simulatedMetrics));
  }

  @DeleteMapping("/edge/{hostname}/simulated-metrics")
  public void removeEdgeHostSimulatedMetrics(@PathVariable String hostname, @RequestBody String[] simulatedMetrics) {
    edgeHostsService.removeSimulatedMetrics(hostname, Arrays.asList(simulatedMetrics));
  }

  @DeleteMapping("/edge/{hostname}/simulated-metrics/{simulatedMetricName}")
  public void removeEdgeHostSimulatedMetric(@PathVariable String hostname, @PathVariable String simulatedMetricName) {
    edgeHostsService.removeSimulatedMetric(hostname, simulatedMetricName);
  }

}
