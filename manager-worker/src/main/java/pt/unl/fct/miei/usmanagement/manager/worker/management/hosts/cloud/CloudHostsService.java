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

package pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud;

import java.util.List;

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
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.aws.AwsService;

@Slf4j
@Service
public class CloudHostsService {

  private final AwsService awsService;
  private final HostsService hostsService;
  private final ContainersService containersService;

  private final CloudHostRepository cloudHosts;
  private final NodesService nodesService;

  public CloudHostsService(@Lazy AwsService awsService,
                           @Lazy HostsService hostsService,
                           @Lazy ContainersService containersService,
                           CloudHostRepository cloudHosts, NodesService nodesService) {
    this.awsService = awsService;
    this.hostsService = hostsService;
    this.containersService = containersService;
    this.cloudHosts = cloudHosts;
    this.nodesService = nodesService;
  }

  public List<CloudHostEntity> getCloudHosts() {
    return cloudHosts.findAll();
  }

  public CloudHostEntity getCloudHostById(Long id) {
    try {
      return cloudHosts.getOne(id);
    } catch (javax.persistence.EntityNotFoundException e) {
      throw new EntityNotFoundException(CloudHostEntity.class, "id", id.toString());
    }
  }

  public CloudHostEntity getCloudHostByInstanceId(String instanceId) {
    return cloudHosts.findByInstanceId(instanceId).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "instanceId", instanceId));
  }

  public CloudHostEntity getCloudHostByInstanceIdOrPublicIpAddress(String value) {
    return cloudHosts.findByInstanceIdOrPublicIpAddress(value, value).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "value", value));
  }

  public CloudHostEntity getCloudHostByPublicIpAddress(String ipAddress) {
    return cloudHosts.findByPublicIpAddress(ipAddress).orElseThrow(() ->
        new EntityNotFoundException(CloudHostEntity.class, "ipAddress", ipAddress));
  }

/*  public void updateCloudHost(String instanceId) {
    CloudHostEntity cloudHost = getCloudHostByInstanceId(instanceId);
    if (nodesService.isPartOfSwarm(cloudHost.getPublicIpAddress())) {

    }
  }*/

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

  public CloudHostEntity startCloudHost() {
    Instance instance = awsService.createInstance();
    containersService.launchDockerApiProxy(instance.getPublicIpAddress());
    return saveCloudHostFromInstance(instance);
  }

  public CloudHostEntity startCloudHost(CloudHostEntity cloudHost, boolean addToSwarm) {
    return startCloudHost(cloudHost.getInstanceId(), addToSwarm);
  }

  public CloudHostEntity startCloudHost(String instanceId, boolean addToSwarm) {
    CloudHostEntity cloudHost = getCloudHostByInstanceIdOrPublicIpAddress(instanceId);
    InstanceState state = new InstanceState()
        .withCode(AwsInstanceState.PENDING.getCode())
        .withName(AwsInstanceState.PENDING.getState());
    cloudHost.setState(state);
    cloudHost = cloudHosts.save(cloudHost);
    Instance instance = awsService.startInstance(instanceId);
    cloudHost = saveCloudHostFromInstance(cloudHost.getId(), instance);
    if (addToSwarm) {
      hostsService.setupHost(cloudHost.getPublicIpAddress(), cloudHost.getPrivateIpAddress(), NodeRole.WORKER);
    }
    return cloudHost;
  }

  public CloudHostEntity stopCloudHost(String instanceId) {
    CloudHostEntity cloudHost = getCloudHostByInstanceIdOrPublicIpAddress(instanceId);
    try {
      hostsService.removeHost(cloudHost.getPublicIpAddress());
    } catch (WorkerManagerException e) {
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
    CloudHostEntity cloudHost = getCloudHostByInstanceIdOrPublicIpAddress(instanceId);
    try {
      hostsService.removeHost(cloudHost.getPublicIpAddress());
    } catch (WorkerManagerException e) {
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
