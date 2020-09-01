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

package pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.aws.AwsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.cloud.aws.AwsSimpleInstance;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.hosts.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.HostRulesService;

@Slf4j
@Service
public class CloudHostsService {

  private final AwsService awsService;
  private final HostRulesService hostRulesService;
  private final HostSimulatedMetricsService hostSimulatedMetricsService;
  private final HostsService hostsService;
  private final ContainersService containersService;

  private final CloudHostRepository cloudHosts;

  public CloudHostsService(@Lazy AwsService awsService,
                           @Lazy HostRulesService hostRulesService,
                           @Lazy HostSimulatedMetricsService hostSimulatedMetricsService,
                           @Lazy HostsService hostsService,
                           @Lazy ContainersService containersService,
                           CloudHostRepository cloudHosts) {
    this.awsService = awsService;
    this.hostRulesService = hostRulesService;
    this.hostSimulatedMetricsService = hostSimulatedMetricsService;
    this.hostsService = hostsService;
    this.containersService = containersService;
    this.cloudHosts = cloudHosts;
  }

  public List<CloudHostEntity> getCloudHosts() {
    return cloudHosts.findAll();
  }

  public CloudHostEntity getCloudHostById(String id) {
    return cloudHosts.findByInstanceId(id).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "id", id));
  }

  public CloudHostEntity getCloudHostByIdOrDns(String value) {
    return cloudHosts.findByInstanceIdOrPublicDnsName(value, value).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "value", value));
  }

  public CloudHostEntity getCloudHostByIp(String ipAddress) {
    return cloudHosts.findByPublicIpAddress(ipAddress).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "hostname", ipAddress));
  }

  public CloudHostEntity getCloudHostByIdOrIp(String value) {
    return cloudHosts.findByInstanceIdOrPublicIpAddress(value, value).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "value", value));
  }

  private CloudHostEntity saveCloudHost(CloudHostEntity cloudHost) {
    log.debug("Saving cloudHost {}", ToStringBuilder.reflectionToString(cloudHost));
    return cloudHosts.save(cloudHost);
  }

  private CloudHostEntity saveCloudHostFromInstance(Instance instance) {
    return saveCloudHostFromInstance(0L, instance);
  }

  private CloudHostEntity saveCloudHostFromInstance(Long id, Instance instance) {
    CloudHostEntity cloudHost = CloudHostEntity.builder()
        .id(id)
        .instanceId(instance.getInstanceId())
        .instanceType(instance.getInstanceType())
        .state(instance.getState())
        .imageId(instance.getImageId())
        .publicDnsName(instance.getPublicDnsName())
        .publicIpAddress(instance.getPublicIpAddress())
        .privateIpAddress(instance.getPrivateIpAddress())
        .placement(instance.getPlacement())
        .build();
    return saveCloudHost(cloudHost);
  }

  private CloudHostEntity addCloudHostFromSimpleInstance(AwsSimpleInstance simpleInstance) {
    CloudHostEntity cloudHost = CloudHostEntity.builder()
        .instanceId(simpleInstance.getInstanceId())
        .instanceType(simpleInstance.getInstanceType())
        .state(simpleInstance.getState())
        .imageId(simpleInstance.getImageId())
        .publicDnsName(simpleInstance.getPublicDnsName())
        .publicIpAddress(simpleInstance.getPublicIpAddress())
        .privateIpAddress(simpleInstance.getPrivateIpAddress())
        .placement(simpleInstance.getPlacement())
        .build();
    return saveCloudHost(cloudHost);
  }

  public CloudHostEntity startCloudHost() {
    Instance instance = awsService.createInstance();
    CloudHostEntity cloudHost = saveCloudHostFromInstance(instance);
    hostsService.addHost(instance.getPublicIpAddress(), NodeRole.WORKER);
    return cloudHost;
  }

  public CloudHostEntity startCloudHost(CloudHostEntity cloudHost, boolean addToSwarm) {
    return startCloudHost(cloudHost.getInstanceId(), addToSwarm);
  }

  public CloudHostEntity startCloudHost(String instanceId, boolean addToSwarm) {
    CloudHostEntity cloudHost = getCloudHostByIdOrIp(instanceId);
    InstanceState state = new InstanceState()
        .withCode(AwsInstanceState.PENDING.getCode())
        .withName(AwsInstanceState.PENDING.getState());
    cloudHost.setState(state);
    cloudHost = cloudHosts.save(cloudHost);
    Instance instance = awsService.startInstance(instanceId);
    cloudHost = saveCloudHostFromInstance(cloudHost.getId(), instance);
    if (addToSwarm) {
      hostsService.addHost(instanceId, NodeRole.WORKER);
    }
    return cloudHost;
  }

  public CloudHostEntity stopCloudHost(String instanceId) {
    CloudHostEntity cloudHost = getCloudHostByIdOrIp(instanceId);
    try {
      hostsService.removeHost(cloudHost.getPublicIpAddress());
    } catch (MasterManagerException e) {
      log.error(e.getMessage());
    }
    InstanceState state = new InstanceState()
        .withCode(AwsInstanceState.STOPPING.getCode())
        .withName(AwsInstanceState.STOPPING.getState());
    cloudHost.setState(state);
    cloudHost = cloudHosts.save(cloudHost);
    Instance instance = awsService.stopInstance(instanceId);
    return saveCloudHostFromInstance(cloudHost.getId(), instance);
  }

  public void terminateCloudHost(String instanceId) {
    CloudHostEntity cloudHost = getCloudHostByIdOrIp(instanceId);
    try {
      hostsService.removeHost(cloudHost.getPublicIpAddress());
    } catch (MasterManagerException e) {
      log.error(e.getMessage());
    }
    InstanceState state = new InstanceState()
        .withCode(AwsInstanceState.SHUTTING_DOWN.getCode())
        .withName(AwsInstanceState.SHUTTING_DOWN.getState());
    cloudHost.setState(state);
    cloudHost = cloudHosts.save(cloudHost);
    awsService.terminateInstance(instanceId);
    cloudHosts.delete(cloudHost);
  }

  public List<CloudHostEntity> syncCloudInstances() {
    List<CloudHostEntity> cloudHosts = getCloudHosts();
    List<Instance> awsInstances = awsService.getInstances();
    Map<String, Instance> awsInstancesIds = awsInstances.stream()
        .collect(Collectors.toMap(Instance::getInstanceId, instance -> instance));
    Iterator<CloudHostEntity> cloudHostsIterator = cloudHosts.iterator();
    // Remove invalid and update cloud host entities
    while (cloudHostsIterator.hasNext()) {
      CloudHostEntity cloudHost = cloudHostsIterator.next();
      String instanceId = cloudHost.getInstanceId();
      if (!awsInstancesIds.containsKey(instanceId)) {
        this.cloudHosts.delete(cloudHost);
        cloudHostsIterator.remove();
        log.debug("Removing invalid cloud host {}", instanceId);
      } else {
        Instance instance = awsInstancesIds.get(instanceId);
        InstanceState currentState = instance.getState();
        InstanceState savedState = cloudHost.getState();
        if (currentState != savedState) {
          CloudHostEntity newCloudHost = saveCloudHostFromInstance(cloudHost.getId(), instance);
          log.debug("Updating cloud host {} from {} to {}", instanceId, ToStringBuilder.reflectionToString(cloudHost),
              ToStringBuilder.reflectionToString(newCloudHost));
        }
      }
    }
    // Add missing cloud host entities
    awsInstances.forEach(instance -> {
      String instanceId = instance.getInstanceId();
      if (instance.getState().getCode() != AwsInstanceState.TERMINATED.getCode() && !hasCloudHost(instanceId)) {
        CloudHostEntity cloudHost = addCloudHostFromSimpleInstance(new AwsSimpleInstance(instance));
        cloudHosts.add(cloudHost);
        log.debug("Added missing cloud host {}", instanceId);
      }
    });
    return cloudHosts;
  }

  public List<HostRuleEntity> getRules(String instanceId) {
    assertHostExists(instanceId);
    return cloudHosts.getRules(instanceId);
  }

  public HostRuleEntity getRule(String instanceId, String ruleName) {
    assertHostExists(instanceId);
    return cloudHosts.getRule(instanceId, ruleName).orElseThrow(() ->
        new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName)
    );
  }

  public void addRule(String instanceId, String ruleName) {
    assertHostExists(instanceId);
    hostRulesService.addCloudHost(ruleName, instanceId);
  }

  public void addRules(String instanceId, List<String> ruleNames) {
    assertHostExists(instanceId);
    ruleNames.forEach(rule -> hostRulesService.addCloudHost(rule, instanceId));
  }

  public void removeRule(String instanceId, String ruleName) {
    assertHostExists(instanceId);
    hostRulesService.removeCloudHost(ruleName, instanceId);
  }

  public void removeRules(String instanceId, List<String> ruleNames) {
    assertHostExists(instanceId);
    ruleNames.forEach(rule -> hostRulesService.removeCloudHost(rule, instanceId));
  }

  public List<HostSimulatedMetricEntity> getSimulatedMetrics(String instanceId) {
    assertHostExists(instanceId);
    return cloudHosts.getSimulatedMetrics(instanceId);
  }

  public HostSimulatedMetricEntity getSimulatedMetric(String instanceId, String simulatedMetricName) {
    assertHostExists(instanceId);
    return cloudHosts.getSimulatedMetric(instanceId, simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
    );
  }

  public void addSimulatedMetric(String instanceId, String simulatedMetricName) {
    assertHostExists(instanceId);
    hostSimulatedMetricsService.addCloudHost(simulatedMetricName, instanceId);
  }

  public void addSimulatedMetrics(String instanceId, List<String> simulatedMetricNames) {
    assertHostExists(instanceId);
    simulatedMetricNames.forEach(simulatedMetric ->
        hostSimulatedMetricsService.addCloudHost(simulatedMetric, instanceId));
  }

  public void removeSimulatedMetric(String instanceId, String simulatedMetricName) {
    assertHostExists(instanceId);
    hostSimulatedMetricsService.addCloudHost(simulatedMetricName, instanceId);
  }

  public void removeSimulatedMetrics(String instanceId, List<String> simulatedMetricNames) {
    assertHostExists(instanceId);
    simulatedMetricNames.forEach(simulatedMetric ->
        hostSimulatedMetricsService.addCloudHost(simulatedMetric, instanceId));
  }

  public void assignWorkerManager(WorkerManagerEntity workerManagerEntity, String instanceId) {
    log.debug("Assigning worker manager {} to cloud host {}", workerManagerEntity.getId(), instanceId);
    CloudHostEntity cloudHostEntity = getCloudHostById(instanceId).toBuilder()
        .managedByWorker(workerManagerEntity)
        .build();
    cloudHosts.save(cloudHostEntity);
  }

  public void unassignWorkerManager(String cloudHost) {
    CloudHostEntity cloudHostEntity = getCloudHostByIdOrIp(cloudHost).toBuilder()
        .managedByWorker(null)
        .build();
    cloudHosts.save(cloudHostEntity);
  }

  public boolean hasCloudHost(String instanceId) {
    return cloudHosts.hasCloudHost(instanceId);
  }

  public boolean hasCloudHostByHostname(String hostname) {
    return cloudHosts.hasCloudHostByHostname(hostname);
  }

  private void assertHostExists(String instanceId) {
    if (!hasCloudHost(instanceId)) {
      throw new EntityNotFoundException(CloudHostEntity.class, "instanceId", instanceId);
    }
  }
}
