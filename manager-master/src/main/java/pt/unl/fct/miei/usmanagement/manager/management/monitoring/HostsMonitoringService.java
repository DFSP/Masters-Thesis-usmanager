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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEventEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.MasterManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;

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

@Slf4j
@Service
public class HostsMonitoringService {

	private static final double PERCENTAGE = 0.01;
	private static final int HOST_MINIMUM_LOGS_COUNT = 1;
	private static final int DELAY_STOP_HOST = 60 * 1000;

	private final HostMonitoringRepository hostsMonitoring;
	private final HostMonitoringLogsRepository hostMonitoringLogs;

	private final NodesService nodesService;
	private final ContainersService containersService;
	private final HostRulesService hostRulesService;
	private final HostsService hostsService;
	private final HostMetricsService hostMetricsService;
	private final ServicesService servicesService;
	private final HostsEventsService hostsEventsService;
	private final DecisionsService decisionsService;
	private final CloudHostsService cloudHostsService;

	private final long monitorInterval;
	private final int resolveOverworkedHostOnEventsCount;
	private final int resolveUnderworkedHostOnEventsCount;
	private final int maximumHosts;
	private final int minimumHosts;
	private final boolean isTestEnable;
	private Timer hostMonitoringTimer;

	public HostsMonitoringService(HostMonitoringRepository hostsMonitoring,
								  HostMonitoringLogsRepository hostMonitoringLogs, NodesService nodesService,
								  ContainersService containersService, HostRulesService hostRulesService,
								  HostsService hostsService, HostMetricsService hostMetricsService,
								  ServicesService servicesService, HostsEventsService hostsEventsService,
								  DecisionsService decisionsService, CloudHostsService cloudHostsService, HostProperties hostProperties,
								  MasterManagerProperties masterManagerProperties) {
		this.hostsMonitoring = hostsMonitoring;
		this.hostMonitoringLogs = hostMonitoringLogs;
		this.nodesService = nodesService;
		this.containersService = containersService;
		this.hostRulesService = hostRulesService;
		this.hostsService = hostsService;
		this.hostMetricsService = hostMetricsService;
		this.servicesService = servicesService;
		this.hostsEventsService = hostsEventsService;
		this.decisionsService = decisionsService;
		this.cloudHostsService = cloudHostsService;
		this.monitorInterval = hostProperties.getMonitorPeriod();
		this.resolveOverworkedHostOnEventsCount = hostProperties.getResolveOverworkedHostOnEventsCount();
		this.resolveUnderworkedHostOnEventsCount = hostProperties.getResolveUnderworkedHostOnEventsCount();
		this.maximumHosts = hostProperties.getMaximumHosts();
		this.minimumHosts = hostProperties.getMinimumHosts();
		this.isTestEnable = masterManagerProperties.getTests().isEnabled();
	}

	public List<HostMonitoringEntity> getHostsMonitoring() {
		return hostsMonitoring.findAll();
	}

	public List<HostMonitoringEntity> getHostMonitoring(HostAddress hostAddress) {
		return hostsMonitoring.getByHostAddress(hostAddress);
	}

	public HostMonitoringEntity getHostMonitoring(HostAddress hostAddress, String field) {
		return hostsMonitoring.getByHostAddressAndFieldIgnoreCase(hostAddress, field);
	}

	public void saveHostMonitoring(HostAddress hostAddress, String field, double value) {
		HostMonitoringEntity hostMonitoring = getHostMonitoring(hostAddress, field);
		Timestamp updateTime = Timestamp.from(Instant.now());
		if (hostMonitoring == null) {
			hostMonitoring = HostMonitoringEntity.builder()
				.hostAddress(hostAddress)
				.field(field)
				.minValue(value).maxValue(value).sumValue(value).lastValue(value)
				.count(1)
				.lastUpdate(updateTime)
				.build();
		}
		else {
			hostMonitoring.logValue(value, updateTime);
		}
		hostsMonitoring.save(hostMonitoring);
		if (isTestEnable) {
			saveHostMonitoringLog(hostAddress, field, value);
		}
	}

	public List<HostFieldAvg> getHostFieldsAvg(HostAddress hostAddress) {
		return hostsMonitoring.getHostFieldsAvg(hostAddress);
	}

	public HostFieldAvg getHostFieldAvg(HostAddress hostAddress, String field) {
		return hostsMonitoring.getHostFieldAvg(hostAddress, field);
	}

	public void saveHostMonitoringLog(HostAddress hostAddress, String field, double effectiveValue) {
		HostMonitoringLogEntity hostMonitoringLogEntity = HostMonitoringLogEntity.builder()
			.hostAddress(hostAddress)
			.field(field)
			.timestamp(LocalDateTime.now())
			.effectiveValue(effectiveValue)
			.build();
		hostMonitoringLogs.save(hostMonitoringLogEntity);
	}

	public List<HostMonitoringLogEntity> getHostMonitoringLogs() {
		return hostMonitoringLogs.findAll();
	}

	public List<HostMonitoringLogEntity> getHostMonitoringLogsByAddress(HostAddress hostAddress) {
		return hostMonitoringLogs.findByHostAddress(hostAddress);
	}

	public void initHostMonitorTimer() {
		hostMonitoringTimer = new Timer(true);
		hostMonitoringTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					monitorHostsTask();
				}
				catch (ManagerException e) {
					log.error(e.getMessage());
				}
			}
		}, monitorInterval, monitorInterval);
	}

	private void monitorHostsTask() {
		cloudHostsService.syncDatabaseCloudHosts();
		List<HostDecisionResult> hostDecisions = new LinkedList<>();
		List<SimpleNode> nodes = nodesService.getReadyNodes();
		for (SimpleNode node : nodes) {
			HostAddress hostAddress = node.getHostAddress();
			Map<String, Double> stats = hostMetricsService.getHostStats(hostAddress);
			stats.forEach((stat, value) -> saveHostMonitoring(hostAddress, stat, value));
			HostDecisionResult hostDecisionResult = runHostRules(hostAddress, stats);
			hostDecisions.add(hostDecisionResult);
		}
		if (!hostDecisions.isEmpty()) {
			processHostDecisions(hostDecisions, nodes);
		}
		else {
			log.info("No host decisions to process");
		}
	}

	private HostDecisionResult runHostRules(HostAddress hostAddress, Map<String, Double> newFields) {
		HostEvent hostEvent = new HostEvent(hostAddress);
		Map<String, Double> hostEventFields = hostEvent.getFields();
		getHostMonitoring(hostAddress)
			.stream()
			.filter(loggedField -> loggedField.getCount() >= HOST_MINIMUM_LOGS_COUNT
				&& newFields.get(loggedField.getField()) != null)
			.forEach(loggedField -> {
				long count = loggedField.getCount();
				String field = loggedField.getField();
				//TODO conta com este newValue?
				double sumValue = loggedField.getSumValue();
				double lastValue = loggedField.getLastValue();
				double newValue = newFields.get(field);
				hostEventFields.put(field + "-effective-val", newValue);
				double average = sumValue / (count * 1.0);
				hostEventFields.put(field + "-avg-val", average);
				double deviationFromAvgValue = ((newValue - average) / average) / PERCENTAGE;
				hostEventFields.put(field + "-deviation-%-on-avg-val", deviationFromAvgValue);
				double deviationFromLastValue = ((newValue - lastValue) / lastValue) / PERCENTAGE;
				hostEventFields.put(field + "-deviation-%-on-last-val", deviationFromLastValue);
			});
		return hostEventFields.isEmpty()
			? new HostDecisionResult(hostAddress)
			: hostRulesService.processHostEvent(hostAddress, hostEvent);
	}

	private void processHostDecisions(List<HostDecisionResult> hostDecisions, List<SimpleNode> nodes) {
		List<HostDecisionResult> decisions = new LinkedList<>();
		for (HostDecisionResult decision : hostDecisions) {
			HostAddress hostAddress = decision.getHostAddress();
			RuleDecision ruleDecision = decision.getDecision();
			HostEventEntity hostEvent = hostsEventsService.saveHostEvent(hostAddress, ruleDecision.toString());
			int hostEventCount = hostEvent.getCount();
			if ((ruleDecision == RuleDecision.OVERWORK && hostEventCount >= resolveOverworkedHostOnEventsCount)
				|| (ruleDecision == RuleDecision.UNDERWORK && hostEventCount >= resolveUnderworkedHostOnEventsCount)) {
				decisions.add(decision);
				HostDecisionEntity hostDecisionEntity = decisionsService.addHostDecision(hostAddress, ruleDecision.name(),
					decision.getRuleId());
				decisionsService.addHostDecisionValueFromFields(hostDecisionEntity, decision.getFields());
				log.info("Host {} had decision {} as event #{}. Triggering action {} node", hostAddress, ruleDecision, hostEventCount, ruleDecision);
			}
			else {
				log.info("Host {} had decision {} as event #{}. Nothing to do", hostAddress, ruleDecision, hostEventCount);
			}
		}
		if (!decisions.isEmpty()) {
			executeDecisions(decisions, nodes.size());
		}
	}

	private void executeDecisions(List<HostDecisionResult> decisions, int nodesCount) {
		Collections.sort(decisions);
		HostDecisionResult topPriorityAction = decisions.get(0);
		RuleDecision decision = topPriorityAction.getDecision();
		HostAddress hostAddress = topPriorityAction.getHostAddress();
		if (decision == RuleDecision.OVERWORK) {
			if (maximumHosts <= 0 || nodesCount < maximumHosts) {
				executeOverworkedHostDecision(hostAddress);
			}
		}
		else if (decision == RuleDecision.UNDERWORK) {
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
			String containerId = container.getContainerId();
			String serviceName = container.getServiceName();
			ServiceEntity serviceConfig = servicesService.getService(serviceName);
			double expectedMemoryConsumption = serviceConfig.getExpectedMemoryConsumption();
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
		List<ContainerEntity> containers = containersService.getAppContainers(hostAddress);
		// TODO parallel?
		containers.forEach(container -> {
			String containerId = container.getContainerId();
			String serviceName = container.getServiceName();
			ServiceEntity serviceConfig = servicesService.getService(serviceName);
			double expectedMemoryConsumption = serviceConfig.getExpectedMemoryConsumption();
			// TODO does the memory consumption of containers initializing is taken into account?
			HostAddress toHostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
			containersService.migrateContainer(containerId, toHostAddress);
		});
		hostsService.removeHost(hostAddress);
		log.info("Resolved underworked host: Stopped {} and migrated containers {} new hosts", hostAddress, containers);
	}


	private Optional<ContainerEntity> getRandomContainerToMigrateFrom(HostAddress hostAddress) {
		List<ContainerEntity> containers = containersService.getAppContainers(hostAddress);
		return containers.isEmpty() ? Optional.empty() : Optional.of(containers.get(new Random().nextInt(containers.size())));
	}

	public void stopHostMonitoring() {
		if (hostMonitoringTimer != null) {
			hostMonitoringTimer.cancel();
			log.info("Stopped host monitoring");
		}
	}
}
