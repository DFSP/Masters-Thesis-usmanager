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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring;

import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostFieldAverage;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoring;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLogs;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitorings;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.stereotype.Service
public class HostsMonitoringService {

	private static final double PERCENTAGE = 0.01;
	private static final int HOST_MINIMUM_LOGS_COUNT = 1;
	private static final int DELAY_STOP_HOST = 60 * 1000;

	private final HostMonitorings hostsMonitoring;
	private final HostMonitoringLogs hostMonitoringLogs;

	private final DockerSwarmService dockerSwarmService;
	private final ContainersService containersService;
	private final HostRulesService hostRulesService;
	private final HostsService hostsService;
	private final HostMetricsService hostMetricsService;
	private final ServicesService servicesService;
	private final HostsEventsService hostsEventsService;
	private final DecisionsService decisionsService;
	private final HostSimulatedMetricsService hostSimulatedMetricsService;
	private final NodesService nodesService;

	private final long monitorPeriod;
	private final int resolveOverworkedHostOnEventsCount;
	private final int resolveUnderworkedHostOnEventsCount;
	private final int maximumHosts;
	private final int minimumHosts;
	private final boolean isTestEnable;
	private Timer hostMonitoringTimer;

	public HostsMonitoringService(HostMonitorings hostsMonitoring,
								  HostMonitoringLogs hostMonitoringLogs, DockerSwarmService dockerSwarmService,
								  ContainersService containersService, HostRulesService hostRulesService,
								  HostsService hostsService, HostMetricsService hostMetricsService,
								  ServicesService servicesService, HostsEventsService hostsEventsService,
								  DecisionsService decisionsService, HostSimulatedMetricsService hostSimulatedMetricsService,
								  NodesService nodesService, HostProperties hostProperties, WorkerManagerProperties workerManagerProperties) {
		this.hostsMonitoring = hostsMonitoring;
		this.hostMonitoringLogs = hostMonitoringLogs;
		this.dockerSwarmService = dockerSwarmService;
		this.containersService = containersService;
		this.hostRulesService = hostRulesService;
		this.hostsService = hostsService;
		this.hostMetricsService = hostMetricsService;
		this.servicesService = servicesService;
		this.hostsEventsService = hostsEventsService;
		this.decisionsService = decisionsService;
		this.hostSimulatedMetricsService = hostSimulatedMetricsService;
		this.nodesService = nodesService;
		this.monitorPeriod = hostProperties.getMonitorPeriod();
		this.resolveOverworkedHostOnEventsCount = hostProperties.getResolveOverworkedHostOnEventsCount();
		this.resolveUnderworkedHostOnEventsCount = hostProperties.getResolveUnderworkedHostOnEventsCount();
		this.maximumHosts = hostProperties.getMaximumHosts();
		this.minimumHosts = hostProperties.getMinimumHosts();
		this.isTestEnable = workerManagerProperties.getTests().isEnabled();
	}

	public List<HostMonitoring> getHostsMonitoring() {
		return hostsMonitoring.findAll();
	}

	public List<HostMonitoring> getHostMonitoring(HostAddress hostAddress) {
		return hostsMonitoring.getByPublicIpAddressAndPrivateIpAddress(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress());
	}

	public HostMonitoring getHostMonitoring(HostAddress hostAddress, String field) {
		return hostsMonitoring.getByPublicIpAddressAndPrivateIpAddressAndFieldIgnoreCase(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress(), field);
	}

	public void saveHostMonitoring(HostAddress hostAddress, String field, double value) {
		HostMonitoring hostMonitoring = getHostMonitoring(hostAddress, field);
		Timestamp updateTime = Timestamp.from(Instant.now());
		if (hostMonitoring == null) {
			hostMonitoring = HostMonitoring.builder().publicIpAddress(hostAddress.getPublicIpAddress())
				.privateIpAddress(hostAddress.getPrivateIpAddress()).field(field).minValue(value).maxValue(value)
				.sumValue(value).lastValue(value).count(1).lastUpdate(updateTime).build();
		}
		else {
			hostMonitoring.update(value, updateTime);
		}
		hostsMonitoring.save(hostMonitoring);
		if (isTestEnable) {
			saveHostMonitoringLog(hostAddress, field, value);
		}
	}

	public List<HostFieldAverage> getHostMonitoringFieldsAverage(HostAddress hostAddress) {
		return hostsMonitoring.getHostMonitoringFieldsAverage(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress());
	}

	public HostFieldAverage getHostMonitoringFieldAverage(HostAddress hostAddress, String field) {
		return hostsMonitoring.getHostMonitoringFieldAverage(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress(), field);
	}

	public void saveHostMonitoringLog(HostAddress hostAddress, String field, double effectiveValue) {
		HostMonitoringLog hostMonitoringLog = HostMonitoringLog.builder()
			.publicIpAddress(hostAddress.getPublicIpAddress())
			.privateIpAddress(hostAddress.getPrivateIpAddress())
			.field(field)
			.timestamp(LocalDateTime.now())
			.value(effectiveValue)
			.build();
		hostMonitoringLogs.save(hostMonitoringLog);
	}

	public List<HostMonitoringLog> getHostMonitoringLogs() {
		return hostMonitoringLogs.findAll();
	}

	public List<HostMonitoringLog> getHostMonitoringLogs(HostAddress hostAddress) {
		return hostMonitoringLogs.findByPublicIpAddressAndPrivateIpAddress(hostAddress.getPublicIpAddress(),
			hostAddress.getPrivateIpAddress());
	}

	public void initHostMonitorTimer() {
		hostMonitoringTimer = new Timer("hosts-monitoring", true);
		hostMonitoringTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					monitorHostsTask();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, monitorPeriod, monitorPeriod);
	}

	private void monitorHostsTask() {
		List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodes = nodesService.getReadyNodes(); // TODO only swarm nodes
		List<CompletableFuture<HostDecisionResult>> futureHostDecisions = nodes.stream()
			.map(node -> getHostDecisions(node))
			.collect(Collectors.toList());

		CompletableFuture.allOf(futureHostDecisions.toArray(new CompletableFuture[0])).join();

		List<HostDecisionResult> hostDecisions = new LinkedList<>();
		List<HostAddress> successfulHostAddresses = new LinkedList<>();
		for (CompletableFuture<HostDecisionResult> futureHostDecision : futureHostDecisions) {
			try {
				HostDecisionResult hostDecisionResult = futureHostDecision.get();
				hostDecisions.add(hostDecisionResult);
				successfulHostAddresses.add(hostDecisionResult.getHostAddress());
			}
			catch (InterruptedException | ExecutionException e) {
				log.error("Failed to get decisions from all hosts: {}", e.getMessage());
			}
		}

		// filter out unsuccessful hosts
		nodes = nodes.stream().filter(simpleNode -> successfulHostAddresses.contains(simpleNode.getHostAddress()))
			.collect(Collectors.toList());

		if (!hostDecisions.isEmpty()) {
			processHostDecisions(hostDecisions, nodes);
		}
		else {
			log.info("No host decisions to process");
		}
	}

	@Async
	public CompletableFuture<HostDecisionResult> getHostDecisions(pt.unl.fct.miei.usmanagement.manager.nodes.Node node) {
		HostAddress hostAddress = node.getHostAddress();

		// Metrics from prometheus (node_exporter)
		Map<String, CompletableFuture<Optional<Double>>> futureStats = hostMetricsService.getHostStats(hostAddress);

		CompletableFuture.allOf(futureStats.values().toArray(new CompletableFuture[0])).join();

		Map<String, Optional<Double>> stats = futureStats.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey,
				futureStat -> {
					try {
						return futureStat.getValue().get();
					}
					catch (InterruptedException | ExecutionException interruptedException) {
						log.error("Unable to get value of field {} from host {}", futureStat.getKey(), hostAddress.toSimpleString());
					}
					return Optional.empty();
				}));
		Map<String, Double> validStats = stats.entrySet().stream()
			.filter(stat -> stat.getValue().isPresent())
			.collect(Collectors.toMap(Map.Entry::getKey, s -> s.getValue().get()));

		// Simulated host metrics
		Map<String, Double> hostSimulatedFields = hostSimulatedMetricsService.getHostSimulatedMetricByHost(hostAddress)
			.stream().filter(metric -> metric.isActive() && (!validStats.containsKey(metric.getName()) || metric.isOverride()))
			.collect(Collectors.toMap(metric -> metric.getField().getName(), hostSimulatedMetricsService::randomizeFieldValue));
		validStats.putAll(hostSimulatedFields);

		validStats.forEach((stat, value) -> saveHostMonitoring(hostAddress, stat, value));

		return CompletableFuture.completedFuture(runRules(node, validStats));
	}

	private HostDecisionResult runRules(pt.unl.fct.miei.usmanagement.manager.nodes.Node node, Map<String, Double> newFields) {
		HostAddress hostAddress = node.getHostAddress();

		pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostEvent hostEvent = new pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostEvent(node);
		Map<String, Double> hostEventFields = hostEvent.getFields();

		getHostMonitoring(hostAddress)
			.stream()
			.filter(loggedField -> loggedField.getCount() >= HOST_MINIMUM_LOGS_COUNT && newFields.get(loggedField.getField()) != null)
			.forEach(loggedField -> {
				String field = loggedField.getField();
				Double newValue = newFields.get(field);
				hostEventFields.put(field + "-effective-val", newValue);
				double average = loggedField.getSumValue() / loggedField.getCount();
				hostEventFields.put(field + "-avg-val", average);
				double deviationFromAverageValue = ((newValue - average) / average) / PERCENTAGE;
				hostEventFields.put(field + "-deviation-%-on-avg-val", deviationFromAverageValue);
				double lastValue = loggedField.getLastValue();
				double deviationFromLastValue = ((newValue - lastValue) / lastValue) / PERCENTAGE;
				hostEventFields.put(field + "-deviation-%-on-last-val", deviationFromLastValue);
			});

		return hostRulesService.processHostEvent(node, hostEvent);
	}

	private void processHostDecisions(List<HostDecisionResult> hostDecisions, List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> nodes) {
		List<HostDecisionResult> decisions = new LinkedList<>();
		hostDecisions.forEach(futureDecision -> {
			HostDecisionResult decision;
			decision = futureDecision;
			HostAddress hostAddress = decision.getHostAddress();
			RuleDecisionEnum ruleDecision = decision.getDecision();
			HostEvent hostEvent = hostsEventsService.saveHostEvent(hostAddress, ruleDecision.toString());
			int hostEventCount = hostEvent.getCount();
			if ((ruleDecision == RuleDecisionEnum.OVERWORK && hostEventCount >= resolveOverworkedHostOnEventsCount)
				|| (ruleDecision == RuleDecisionEnum.UNDERWORK && hostEventCount >= resolveUnderworkedHostOnEventsCount)) {
				decisions.add(decision);
				HostDecision hostDecision = decisionsService.addHostDecision(hostAddress, ruleDecision.name(),
					decision.getRuleId());
				decisionsService.addHostDecisionValueFromFields(hostDecision, decision.getFields());
				log.info("Host {} had decision {} as event #{}. Triggering action {}", hostAddress, ruleDecision, hostEventCount, ruleDecision);
			}
			else {
				log.info("Host {} had decision {} as event #{}. Nothing to do", hostAddress, ruleDecision, hostEventCount);
			}
		});
		if (!decisions.isEmpty()) {
			executeDecisions(decisions, nodes.size());
		}
	}

	private void executeDecisions(List<HostDecisionResult> decisions, int nodesCount) {
		Collections.sort(decisions);
		HostDecisionResult topPriorityAction = decisions.get(0);
		RuleDecisionEnum decision = topPriorityAction.getDecision();
		HostAddress hostAddress = topPriorityAction.getHostAddress();
		if (decision == RuleDecisionEnum.OVERWORK) {
			if (maximumHosts <= 0 || nodesCount < maximumHosts) {
				executeOverworkedHostDecision(hostAddress);
			}
		}
		else if (decision == RuleDecisionEnum.UNDERWORK) {
			if (nodesCount > minimumHosts) {
				/*Collections.reverse(decisions);
				decisions.stream()
					.filter(d -> !d.getHostAddress().equals(hostsService.getHostAddress()))
					.findFirst()
					.ifPresent(d -> resolveUnderworkedHost(d.getHostAddress()));*/
				executeUnderworkedHostDecision(hostAddress);
			}
		}
	}

	private void executeOverworkedHostDecision(HostAddress hostAddress) {
		// This action is triggered when the asking host is overflowed with work
		// To resolve that, we migrate one of its containers to another host
		getRandomContainerToMigrateFrom(hostAddress).ifPresent(container -> {
			String containerId = container.getId();
			String serviceName = container.getServiceName();
			double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(serviceName);
			Coordinates coordinates = container.getCoordinates();
			HostAddress toHostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
			containersService.migrateContainer(containerId, toHostAddress);
			log.info("RuleDecision executed: Started host {} and migrated container {} to it", toHostAddress, containerId);
		});
	}

	private void executeUnderworkedHostDecision(HostAddress hostAddress) {
		// This action is triggered when the asking host is underworked
		// To resolve that, we migrate all its containers to another close host, and then stop it
		Coordinates coordinates = hostAddress.getCoordinates();
		List<Container> containers = containersService.getAppContainers(hostAddress);
		// TODO parallel?
		containers.forEach(container -> {
			String containerId = container.getId();
			String serviceName = container.getServiceName();
			double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(serviceName);
			// TODO does the memory consumption of containers initializing is taken into account?
			HostAddress toHostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
			containersService.migrateContainer(containerId, toHostAddress);
		});
		hostsService.removeHost(hostAddress);
		log.info("Resolved underworked host: Stopped {} and migrated containers {} to new hosts", hostAddress, containers);
	}

	private Optional<Container> getRandomContainerToMigrateFrom(HostAddress hostAddress) {
		List<Container> containers = containersService.getAppContainers(hostAddress);
		return containers.isEmpty() ? Optional.empty() : Optional.of(containers.get(new Random().nextInt(containers.size())));
	}

	public void stopHostMonitoring() {
		if (hostMonitoringTimer != null) {
			hostMonitoringTimer.cancel();
			log.info("Stopped host monitoring");
		}
	}

	public void reset() {
		log.info("Clearing all host monitoring");
		hostsMonitoring.deleteAll();
	}
}
