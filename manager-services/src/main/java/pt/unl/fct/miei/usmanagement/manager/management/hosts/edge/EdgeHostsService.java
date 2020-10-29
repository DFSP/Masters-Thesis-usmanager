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

package pt.unl.fct.miei.usmanagement.manager.management.hosts.edge;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHostRepository;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManagerEntity;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EdgeHostsService {

	private final HostRulesService hostRulesService;
	private final HostSimulatedMetricsService hostSimulatedMetricsService;
	private final SshService sshService;
	private final BashService bashService;
	private final NodesService nodesService;

	private final EdgeHostRepository edgeHosts;

	private final String edgeKeyFilePath;

	public EdgeHostsService(@Lazy HostRulesService hostRulesService,
							@Lazy HostSimulatedMetricsService hostSimulatedMetricsService,
							@Lazy SshService sshService, BashService bashService,
							@Lazy NodesService nodesService,
							EdgeHostRepository edgeHosts, EdgeHostsProperties edgeHostsProperties) {
		this.hostRulesService = hostRulesService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.sshService = sshService;
		this.bashService = bashService;
		this.nodesService = nodesService;
		this.edgeHosts = edgeHosts;
		this.edgeKeyFilePath = edgeHostsProperties.getAccess().getKeyFilePath();
	}

	public String buildKeyFilePath(EdgeHostEntity edgeHostEntity) {
		String username = edgeHostEntity.getUsername();
		String hostname = edgeHostEntity.getHostname();
		return String.format("%s/%s/%s_%s", System.getProperty("user.dir"), edgeKeyFilePath, username,
			hostname.replace(".", "_"));
	}

	public List<EdgeHostEntity> getEdgeHosts() {
		return edgeHosts.findAll();
	}

	public EdgeHostEntity getEdgeHostByDnsOrIp(String host) {
		return edgeHosts.findByPublicDnsNameOrPublicIpAddress(host, host).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "host", host));
	}

	public EdgeHostEntity getEdgeHostByAddress(HostAddress address) {
		return edgeHosts.findByAddress(address.getPublicIpAddress(), address.getPrivateIpAddress()).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "address", address.toString()));
	}

	public EdgeHostEntity getEdgeHostByDns(String dns) {
		return edgeHosts.findByPublicDnsName(dns).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "dns", dns));
	}

	public EdgeHostEntity addEdgeHost(String username, String password, String publicIpAddress, String privateIpAddress,
									  String publicDnsName, Coordinates coordinates) {
		EdgeHostEntity edgeHost = EdgeHostEntity.builder()
			.username(username)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.publicDnsName(publicDnsName)
			.coordinates(coordinates)
			.region(Region.getClosestRegion(coordinates))
			.build();
		return addEdgeHost(edgeHost, password);
	}

	public EdgeHostEntity addEdgeHost(EdgeHostEntity edgeHostEntity) {
		return addEdgeHost(edgeHostEntity, null);
	}

	public EdgeHostEntity addEdgeHost(EdgeHostEntity edgeHost, String password) {
		checkHostDoesntExist(edgeHost);
		if (password != null) {
			setupEdgeHost(edgeHost, password);
		}
		log.info("Saving edgeHost {}", ToStringBuilder.reflectionToString(edgeHost));
		return edgeHosts.save(edgeHost);
	}

	private void setupEdgeHost(EdgeHostEntity edgeHost, String password) {
		HostAddress hostAddress = edgeHost.getAddress();
		String keyFilePath = buildKeyFilePath(edgeHost);
		log.info("Generating keys for edge host {}", hostAddress);
		String generateKeysCommand = String.format("echo y | ssh-keygen -b 2048 -t rsa -f '%s' -q -N \"\" &&"
			+ " sshpass -p '%s' ssh-copy-id -i '%s' '%s'", keyFilePath, password, keyFilePath, hostAddress.getPublicIpAddress());
		SshCommandResult generateKeysResult = sshService.executeCommand(hostAddress, password, generateKeysCommand);
		if (!generateKeysResult.isSuccessful()) {
			deleteEdgeHostConfig(edgeHost);
			throw new ManagerException("Unable to generate public/private key pair for '%s': %s", hostAddress,
				generateKeysResult.getError().get(0));
		}
	}

	public EdgeHostEntity updateEdgeHost(String hostname, EdgeHostEntity newEdgeHost) {
		EdgeHostEntity edgeHost = getEdgeHostByDnsOrIp(hostname);
		log.info("Updating edgeHost {} with {}",
			ToStringBuilder.reflectionToString(edgeHost),
			ToStringBuilder.reflectionToString(newEdgeHost));
		ObjectUtils.copyValidProperties(newEdgeHost, edgeHost);
		return edgeHosts.save(edgeHost);
	}

	public void deleteEdgeHost(String hostname) {
		EdgeHostEntity edgeHost = getEdgeHostByDnsOrIp(hostname);
		edgeHosts.delete(edgeHost);
		deleteEdgeHostConfig(edgeHost);
	}

	public List<HostRuleEntity> getRules(String hostname) {
		checkHostExists(hostname);
		return edgeHosts.getRules(hostname);
	}

	public HostRuleEntity getRule(String hostname, String ruleName) {
		checkHostExists(hostname);
		return edgeHosts.getRule(hostname, ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName)
		);
	}

	public void addRule(String hostname, String ruleName) {
		checkHostExists(hostname);
		hostRulesService.addEdgeHost(ruleName, hostname);
	}

	public void addRules(String hostname, List<String> ruleNames) {
		checkHostExists(hostname);
		ruleNames.forEach(rule -> hostRulesService.addEdgeHost(rule, hostname));
	}

	public void removeRule(String hostname, String ruleName) {
		checkHostExists(hostname);
		hostRulesService.removeEdgeHost(ruleName, hostname);
	}

	public void removeRules(String hostname, List<String> ruleNames) {
		checkHostExists(hostname);
		ruleNames.forEach(rule -> hostRulesService.removeEdgeHost(rule, hostname));
	}

	public List<HostSimulatedMetricEntity> getSimulatedMetrics(String hostname) {
		checkHostExists(hostname);
		return edgeHosts.getSimulatedMetrics(hostname);
	}

	public HostSimulatedMetricEntity getSimulatedMetric(String hostname, String simulatedMetricName) {
		checkHostExists(hostname);
		return edgeHosts.getSimulatedMetric(hostname, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public void addSimulatedMetric(String hostname, String simulatedMetricName) {
		checkHostExists(hostname);
		hostSimulatedMetricsService.addEdgeHost(simulatedMetricName, hostname);
	}

	public void addSimulatedMetrics(String hostname, List<String> simulatedMetricNames) {
		checkHostExists(hostname);
		simulatedMetricNames.forEach(simulatedMetric ->
			hostSimulatedMetricsService.addEdgeHost(simulatedMetric, hostname));
	}

	public void removeSimulatedMetric(String hostname, String simulatedMetricName) {
		checkHostExists(hostname);
		hostSimulatedMetricsService.addEdgeHost(simulatedMetricName, hostname);
	}

	public void removeSimulatedMetrics(String hostname, List<String> simulatedMetricNames) {
		checkHostExists(hostname);
		simulatedMetricNames.forEach(simulatedMetric ->
			hostSimulatedMetricsService.addEdgeHost(simulatedMetric, hostname));
	}

	public void assignWorkerManager(WorkerManagerEntity workerManagerEntity, String edgeHost) {
		log.info("Assigning worker manager {} to edge host {}", workerManagerEntity.getId(), edgeHost);
		EdgeHostEntity edgeHostEntity = getEdgeHostByDnsOrIp(edgeHost).toBuilder()
			.managedByWorker(workerManagerEntity)
			.build();
		edgeHosts.save(edgeHostEntity);
	}

	public void unassignWorkerManager(String edgeHost) {
		EdgeHostEntity edgeHostEntity = getEdgeHostByDnsOrIp(edgeHost).toBuilder()
			.managedByWorker(null)
			.build();
		edgeHosts.save(edgeHostEntity);
	}

	public boolean hasEdgeHost(String hostname) {
		return edgeHosts.hasEdgeHost(hostname);
	}

	private void checkHostExists(String hostname) {
		if (!hasEdgeHost(hostname)) {
			throw new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname);
		}
	}

	private void checkHostDoesntExist(EdgeHostEntity edgeHost) {
		String hostname = edgeHost.getPublicDnsName() == null ? edgeHost.getPublicIpAddress() : edgeHost.getPublicDnsName();
		if (edgeHosts.hasEdgeHost(hostname)) {
			throw new DataIntegrityViolationException("Edge host '" + hostname + "' already exists");
		}
	}

	private void deleteEdgeHostConfig(EdgeHostEntity edgeHost) {
		String privateKeyFilePath = buildKeyFilePath(edgeHost);
		String publicKeyFilePath = String.format("%s.pub", privateKeyFilePath);
		bashService.cleanup(privateKeyFilePath, publicKeyFilePath);
	}

	public List<EdgeHostEntity> getInactiveEdgeHosts() {
		return getEdgeHosts().stream()
			.filter(host -> !nodesService.isPartOfSwarm(host.getAddress()))
			.collect(Collectors.toList());
	}
}
