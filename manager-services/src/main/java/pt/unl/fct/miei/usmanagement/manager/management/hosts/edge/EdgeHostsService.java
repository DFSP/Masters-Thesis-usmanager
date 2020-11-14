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
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHosts;
import pt.unl.fct.miei.usmanagement.manager.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

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

	private final EdgeHosts edgeHosts;

	private final String edgeKeyFilePath;

	public EdgeHostsService(@Lazy HostRulesService hostRulesService,
							@Lazy HostSimulatedMetricsService hostSimulatedMetricsService,
							@Lazy SshService sshService, BashService bashService,
							@Lazy NodesService nodesService,
							EdgeHosts edgeHosts, EdgeHostsProperties edgeHostsProperties) {
		this.hostRulesService = hostRulesService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.sshService = sshService;
		this.bashService = bashService;
		this.nodesService = nodesService;
		this.edgeHosts = edgeHosts;
		this.edgeKeyFilePath = edgeHostsProperties.getAccess().getKeyFilePath();
	}

	public String getPrivateKeyFilePath(EdgeHost edgeHost) {
		String username = edgeHost.getUsername();
		String hostname = edgeHost.getHostname();
		return String.format("%s/%s/%s_%s", System.getProperty("user.dir"), edgeKeyFilePath, username,
			hostname.replace(".", "_"));
	}

	public List<EdgeHost> getEdgeHosts() {
		return edgeHosts.findAll();
	}

	public EdgeHost getEdgeHostByHostname(String host) {
		return edgeHosts.findByPublicDnsNameOrPublicIpAddress(host, host).orElseThrow(() ->
			new EntityNotFoundException(EdgeHost.class, "host", host));
	}

	public EdgeHost getEdgeHostByAddress(HostAddress address) {
		return edgeHosts.findByAddress(address.getPublicIpAddress(), address.getPrivateIpAddress()).orElseThrow(() ->
			new EntityNotFoundException(EdgeHost.class, "address", address.toString()));
	}

	public EdgeHost getEdgeHostByDns(String dns) {
		return edgeHosts.findByPublicDnsName(dns).orElseThrow(() ->
			new EntityNotFoundException(EdgeHost.class, "dns", dns));
	}

	public EdgeHost addEdgeHost(String username, char[] password, String publicIpAddress, String privateIpAddress,
								String publicDnsName, Coordinates coordinates) {
		EdgeHost edgeHost = EdgeHost.builder()
			.username(username)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.publicDnsName(publicDnsName)
			.coordinates(coordinates)
			.region(RegionEnum.getClosestRegion(coordinates))
			.build();
		return addEdgeHost(edgeHost, password);
	}

	public EdgeHost addEdgeHost(EdgeHost edgeHost) {
		return addEdgeHost(edgeHost, null);
	}

	public EdgeHost addManualEdgeHost(EdgeHost edgeHost) {
		checkHostDoesntExist(edgeHost);
		log.info("Saving edgeHost {}", ToStringBuilder.reflectionToString(edgeHost));
		EdgeHost edgeHostEntity = edgeHosts.save(edgeHost);

		boolean setup = false;
		for (int i = 0; i < 5; i++) {
			char[] password = System.console().readPassword("%s", "> Enter edge host password: ");
			try {
				setupEdgeHost(edgeHost, password);
				setup = true;
				break;
			}
			catch (Exception e) {
				log.error("Unable to setup new edge host: {}", e.getMessage());
				log.info("Password is most likely wrong, try again");
			}
			java.util.Arrays.fill(password, ' ');
		}
		if (!setup) {
			edgeHosts.delete(edgeHostEntity);
			throw new ManagerException("Unable to setup new edge host");
		}

		return edgeHostEntity;
	}

	public EdgeHost addEdgeHost(EdgeHost edgeHost, char[] password) {
		checkHostDoesntExist(edgeHost);
		log.info("Saving edgeHost {}", ToStringBuilder.reflectionToString(edgeHost));
		EdgeHost edgeHostEntity = edgeHosts.save(edgeHost);
		if (password != null) {
			try {
				setupEdgeHost(edgeHost, password);
			}
			catch (Exception e) {
				edgeHosts.delete(edgeHostEntity);
				throw new ManagerException("Unable to setup new edge host: %s", e.getMessage());
			}
			finally {
				java.util.Arrays.fill(password, ' ');
			}
		}
		return edgeHostEntity;
	}

	private void setupEdgeHost(EdgeHost edgeHost, char[] password) {
		HostAddress hostAddress = edgeHost.getAddress();
		String keyFilePath = getPrivateKeyFilePath(edgeHost);
		log.info("Generating keys for edge host {}", hostAddress);

		String generateKeysCommand = String.format("echo yes | ssh-keygen -m PEM -t rsa -b 4096 -f '%s' -q -N \"\" &&"
			+ " sshpass -p '%s' ssh-copy-id -i '%s' '%s'", keyFilePath, String.valueOf(password), keyFilePath, hostAddress.getPublicIpAddress());
		SshCommandResult generateKeysResult = sshService.executeCommandSync(generateKeysCommand, hostAddress, password);
		int exitStatus = generateKeysResult.getExitStatus();
		if (exitStatus != 0 && exitStatus != 6) {
			String error = generateKeysResult.getError().get(0);
			log.error("Unable to generate public/private key pair for {}: {}", hostAddress.toSimpleString(), error);
			deleteEdgeHostConfig(edgeHost);
			throw new ManagerException("Unable to generate public/private key pair for %s: %s", hostAddress.toSimpleString(), error);
		}

		/*log.info("Protecting private key {} with chmod 400", keyFilePath);
		String protectPrivateKeyCommand = String.format("chmod 400 %s", keyFilePath);
		SshCommandResult protectPrivateKeyResult = sshService.executeCommandSync(protectPrivateKeyCommand, hostAddress);
		if (!protectPrivateKeyResult.isSuccessful()) {
			String error = protectPrivateKeyResult.getError().get(0);
			log.error("Failed to protect private key {} on host {}: {}", keyFilePath, hostAddress.toSimpleString(), error);
			deleteEdgeHostConfig(edgeHost);
			throw new ManagerException("Failed to protect private key on host %s: %s", hostAddress.toSimpleString(), error);
		}*/
	}

	public EdgeHost updateEdgeHost(String hostname, EdgeHost newEdgeHost) {
		EdgeHost edgeHost = getEdgeHostByHostname(hostname);
		log.info("Updating edgeHost {} with {}",
			ToStringBuilder.reflectionToString(edgeHost),
			ToStringBuilder.reflectionToString(newEdgeHost));
		ObjectUtils.copyValidProperties(newEdgeHost, edgeHost);
		return edgeHosts.save(edgeHost);
	}

	public void deleteEdgeHost(String hostname) {
		EdgeHost edgeHost = getEdgeHostByHostname(hostname);
		edgeHosts.delete(edgeHost);
		deleteEdgeHostConfig(edgeHost);
	}

	public List<HostRule> getRules(String hostname) {
		checkHostExists(hostname);
		return edgeHosts.getRules(hostname);
	}

	public HostRule getRule(String hostname, String ruleName) {
		checkHostExists(hostname);
		return edgeHosts.getRule(hostname, ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRule.class, "ruleName", ruleName)
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

	public List<HostSimulatedMetric> getSimulatedMetrics(String hostname) {
		checkHostExists(hostname);
		return edgeHosts.getSimulatedMetrics(hostname);
	}

	public HostSimulatedMetric getSimulatedMetric(String hostname, String simulatedMetricName) {
		checkHostExists(hostname);
		return edgeHosts.getSimulatedMetric(hostname, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetric.class, "simulatedMetricName", simulatedMetricName)
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

	public void assignWorkerManager(WorkerManager workerManager, String edgeHost) {
		log.info("Assigning worker manager {} to edge host {}", workerManager.getId(), edgeHost);
		EdgeHost edgeHostEntity = getEdgeHostByHostname(edgeHost).toBuilder()
			.managedByWorker(workerManager)
			.build();
		edgeHosts.save(edgeHostEntity);
	}

	public void unassignWorkerManager(String edgeHost) {
		EdgeHost edgeHostEntity = getEdgeHostByHostname(edgeHost).toBuilder()
			.managedByWorker(null)
			.build();
		edgeHosts.save(edgeHostEntity);
	}

	public boolean hasEdgeHost(String hostname) {
		return edgeHosts.hasEdgeHost(hostname);
	}

	private void checkHostExists(String hostname) {
		if (!hasEdgeHost(hostname)) {
			throw new EntityNotFoundException(EdgeHost.class, "hostname", hostname);
		}
	}

	private void checkHostDoesntExist(EdgeHost edgeHost) {
		String hostname = edgeHost.getPublicDnsName() == null ? edgeHost.getPublicIpAddress() : edgeHost.getPublicDnsName();
		if (edgeHosts.hasEdgeHost(hostname)) {
			throw new DataIntegrityViolationException("Edge host '" + hostname + "' already exists");
		}
	}

	private void deleteEdgeHostConfig(EdgeHost edgeHost) {
		String privateKeyFilePath = getPrivateKeyFilePath(edgeHost);
		String publicKeyFilePath = String.format("%s.pub", privateKeyFilePath);
		bashService.cleanup(privateKeyFilePath, publicKeyFilePath);
	}

	public List<EdgeHost> getInactiveEdgeHosts() {
		return getEdgeHosts().stream()
			.filter(host -> !nodesService.isPartOfSwarm(host.getAddress()))
			.collect(Collectors.toList());
	}
}
