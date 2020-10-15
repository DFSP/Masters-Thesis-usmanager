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

package pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws.AwsInstanceState;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws.AwsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws.AwsSimpleInstance;
import pt.unl.fct.miei.usmanagement.manager.services.management.monitoring.metrics.simulated.hosts.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.management.rulesystem.rules.HostRulesService;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CloudHostsService {

	private final AwsService awsService;
	private final HostRulesService hostRulesService;
	private final HostSimulatedMetricsService hostSimulatedMetricsService;
	private final HostsService hostsService;

	private final CloudHostRepository cloudHosts;

	public CloudHostsService(@Lazy AwsService awsService,
							 @Lazy HostRulesService hostRulesService,
							 @Lazy HostSimulatedMetricsService hostSimulatedMetricsService,
							 @Lazy HostsService hostsService,
							 CloudHostRepository cloudHosts) {
		this.awsService = awsService;
		this.hostRulesService = hostRulesService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.hostsService = hostsService;
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
			new EntityNotFoundException(CloudHostEntity.class, "ipAddress", ipAddress));
	}

	public CloudHostEntity getCloudHostByIdOrIp(String value) {
		return cloudHosts.findByInstanceIdOrPublicIpAddress(value, value).orElseThrow(() ->
			new EntityNotFoundException(CloudHostEntity.class, "value", value));
	}

	public CloudHostEntity getCloudHostByAddress(HostAddress address) {
		return cloudHosts.findByAddress(address.getPublicIpAddress(), address.getPrivateIpAddress()).orElseThrow(() ->
			new EntityNotFoundException(CloudHostEntity.class, "address", address.toString()));
	}

	private CloudHostEntity saveCloudHost(CloudHostEntity cloudHost) {
		log.info("Saving cloudHost {}", ToStringBuilder.reflectionToString(cloudHost));
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
			.coordinates(this.getPlacementCoordinates(instance.getPlacement()))
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
			.coordinates(this.getPlacementCoordinates(simpleInstance.getPlacement()))
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

	public CloudHostEntity startCloudHost(String hostname, boolean addToSwarm) {
		CloudHostEntity cloudHost = getCloudHostByIdOrIp(hostname);
		InstanceState state = new InstanceState()
			.withCode(AwsInstanceState.PENDING.getCode())
			.withName(AwsInstanceState.PENDING.getState());
		cloudHost.setState(state);
		cloudHost = cloudHosts.save(cloudHost);
		Instance instance = awsService.startInstance(cloudHost.getInstanceId());
		cloudHost = saveCloudHostFromInstance(cloudHost.getId(), instance);
		if (addToSwarm) {
			hostsService.addHost(hostname, NodeRole.WORKER);
		}
		return cloudHost;
	}

	public CloudHostEntity stopCloudHost(String hostname) {
		CloudHostEntity cloudHost = getCloudHostByIdOrIp(hostname);
		try {
			hostsService.removeHost(cloudHost.getAddress());
		}
		catch (ManagerException e) {
			log.error(e.getMessage());
		}
		InstanceState state = new InstanceState()
			.withCode(AwsInstanceState.STOPPING.getCode())
			.withName(AwsInstanceState.STOPPING.getState());
		cloudHost.setState(state);
		cloudHost = cloudHosts.save(cloudHost);
		Instance instance = awsService.stopInstance(cloudHost.getInstanceId());
		return saveCloudHostFromInstance(cloudHost.getId(), instance);
	}

	public void terminateCloudHost(String hostname) {
		CloudHostEntity cloudHost = getCloudHostByIdOrIp(hostname);
		try {
			hostsService.removeHost(cloudHost.getAddress());
		}
		catch (ManagerException e) {
			log.error(e.getMessage());
		}
		InstanceState state = new InstanceState()
			.withCode(AwsInstanceState.SHUTTING_DOWN.getCode())
			.withName(AwsInstanceState.SHUTTING_DOWN.getState());
		cloudHost.setState(state);
		cloudHost = cloudHosts.save(cloudHost);
		awsService.terminateInstance(cloudHost.getInstanceId());
		cloudHosts.delete(cloudHost);
	}

	public void terminateCloudHosts() {
		getCloudHosts().parallelStream().forEach(instance -> terminateCloudHost(instance.getInstanceId()));
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
				log.info("Removing invalid cloud host {}", instanceId);
			}
			else {
				Instance instance = awsInstancesIds.get(instanceId);
				InstanceState currentState = instance.getState();
				InstanceState savedState = cloudHost.getState();
				if (currentState != savedState) {
					CloudHostEntity newCloudHost = saveCloudHostFromInstance(cloudHost.getId(), instance);
					log.info("Updating cloud host {} from {} to {}", instanceId, ToStringBuilder.reflectionToString(cloudHost),
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
				log.info("Added missing cloud host {}", instanceId);
			}
		});
		return cloudHosts;
	}

	public List<HostRuleEntity> getRules(String hostname) {
		assertHostExists(hostname);
		return cloudHosts.getRules(hostname);
	}

	public HostRuleEntity getRule(String hostname, String ruleName) {
		assertHostExists(hostname);
		return cloudHosts.getRule(hostname, ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName)
		);
	}

	public void addRule(String hostname, String ruleName) {
		assertHostExists(hostname);
		hostRulesService.addCloudHost(ruleName, hostname);
	}

	public void addRules(String hostname, List<String> ruleNames) {
		assertHostExists(hostname);
		ruleNames.forEach(rule -> hostRulesService.addCloudHost(rule, hostname));
	}

	public void removeRule(String hostname, String ruleName) {
		assertHostExists(hostname);
		hostRulesService.removeCloudHost(ruleName, hostname);
	}

	public void removeRules(String hostname, List<String> ruleNames) {
		assertHostExists(hostname);
		ruleNames.forEach(rule -> hostRulesService.removeCloudHost(rule, hostname));
	}

	public List<HostSimulatedMetricEntity> getSimulatedMetrics(String hostname) {
		assertHostExists(hostname);
		return cloudHosts.getSimulatedMetrics(hostname);
	}

	public HostSimulatedMetricEntity getSimulatedMetric(String hostname, String simulatedMetricName) {
		assertHostExists(hostname);
		return cloudHosts.getSimulatedMetric(hostname, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String hostname, String simulatedMetricName) {
		assertHostExists(hostname);
		hostSimulatedMetricsService.addCloudHost(simulatedMetricName, hostname);
	}

	public void addSimulatedMetrics(String hostname, List<String> simulatedMetricNames) {
		assertHostExists(hostname);
		simulatedMetricNames.forEach(simulatedMetric ->
			hostSimulatedMetricsService.addCloudHost(simulatedMetric, hostname));
	}

	public void removeSimulatedMetric(String hostname, String simulatedMetricName) {
		assertHostExists(hostname);
		hostSimulatedMetricsService.addCloudHost(simulatedMetricName, hostname);
	}

	public void removeSimulatedMetrics(String hostname, List<String> simulatedMetricNames) {
		assertHostExists(hostname);
		simulatedMetricNames.forEach(simulatedMetric ->
			hostSimulatedMetricsService.addCloudHost(simulatedMetric, hostname));
	}

	public void assignWorkerManager(WorkerManagerEntity workerManagerEntity, String hostname) {
		log.info("Assigning worker manager {} to cloud host {}", workerManagerEntity.getId(), hostname);
		CloudHostEntity cloudHostEntity = getCloudHostByIp(hostname).toBuilder()
			.managedByWorker(workerManagerEntity)
			.build();
		cloudHosts.save(cloudHostEntity);
	}

	public void unassignWorkerManager(String hostname) {
		CloudHostEntity cloudHostEntity = getCloudHostByIp(hostname).toBuilder()
			.managedByWorker(null)
			.build();
		cloudHosts.save(cloudHostEntity);
	}

	public boolean hasCloudHost(String hostname) {
		return cloudHosts.hasCloudHost(hostname);
	}

	private void assertHostExists(String hostname) {
		if (!hasCloudHost(hostname)) {
			throw new EntityNotFoundException(CloudHostEntity.class, "hostname", hostname);
		}
	}

	private Coordinates getPlacementCoordinates(Placement placement) {
		String availabilityZone = placement.getAvailabilityZone();
		while (!Character.isDigit(availabilityZone.charAt(availabilityZone.length() - 1))) {
			availabilityZone = availabilityZone.substring(0, availabilityZone.length() - 1);
		}
		AwsRegion awsRegion = AwsRegion.valueOf(availabilityZone.toUpperCase().replace("-", "_"));
		log.info("Instance placement {} is on aws region {}", placement, awsRegion);
		return awsRegion.getCoordinates();
	}
}
