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

package pt.unl.fct.miei.usmanagement.manager.master.management.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.*;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.ManagerMasterProperties;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.master.management.services.ServicesService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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

	private final long monitorInterval;
	private final int startHostOnEventsCount;
	private final int stopHostOnEventsCount;
	private final int maximumHosts;
	private final int minimumHosts;
	private final boolean isTestEnable;

	public HostsMonitoringService(HostMonitoringRepository hostsMonitoring,
								  HostMonitoringLogsRepository hostMonitoringLogs, NodesService nodesService,
								  ContainersService containersService, HostRulesService hostRulesService,
								  HostsService hostsService, HostMetricsService hostMetricsService,
								  ServicesService servicesService, HostsEventsService hostsEventsService,
								  DecisionsService decisionsService, HostProperties hostProperties,
								  ManagerMasterProperties managerMasterProperties) {
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
		this.monitorInterval = hostProperties.getMonitorPeriod();
		this.startHostOnEventsCount = hostProperties.getStartHostOnEventsCount();
		this.stopHostOnEventsCount = hostProperties.getStopHostOnEventsCount();
		this.maximumHosts = hostProperties.getMaximumHosts();
		this.minimumHosts = hostProperties.getMinimumHosts();
		this.isTestEnable = managerMasterProperties.getTests().isEnabled();
	}

	public List<HostMonitoringEntity> getHostsMonitoring() {
		return hostsMonitoring.findAll();
	}

	public List<HostMonitoringEntity> getHostMonitoring(String hostname) {
		return hostsMonitoring.getByHostname(hostname);
	}

	public HostMonitoringEntity getHostMonitoring(String hostname, String field) {
		return hostsMonitoring.getByHostnameAndFieldIgnoreCase(hostname, field);
	}

	public void saveHostMonitoring(String hostname, String field, double value) {
		HostMonitoringEntity hostMonitoring = getHostMonitoring(hostname, field);
		Timestamp updateTime = Timestamp.from(Instant.now());
		if (hostMonitoring == null) {
			hostMonitoring = HostMonitoringEntity.builder()
				.hostname(hostname)
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
			saveHostMonitoringLog(hostname, field, value);
		}
	}

	public List<HostFieldAvg> getHostFieldsAvg(String hostname) {
		return hostsMonitoring.getHostFieldsAvg(hostname);
	}

	public HostFieldAvg getHostFieldAvg(String hostname, String field) {
		return hostsMonitoring.getHostFieldAvg(hostname, field);
	}

	public void saveHostMonitoringLog(String hostname, String field, double effectiveValue) {
		HostMonitoringLogEntity hostMonitoringLogEntity = HostMonitoringLogEntity.builder()
			.hostname(hostname)
			.field(field)
			.timestamp(LocalDateTime.now())
			.effectiveValue(effectiveValue)
			.build();
		hostMonitoringLogs.save(hostMonitoringLogEntity);
	}

	public List<HostMonitoringLogEntity> getHostMonitoringLogs() {
		return hostMonitoringLogs.findAll();
	}

	public List<HostMonitoringLogEntity> getHostMonitoringLogsByHostname(String hostname) {
		return hostMonitoringLogs.findByHostname(hostname);
	}

	public void initHostMonitorTimer() {
		new Timer("MonitorHostTimer", true).schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					monitorHostsTask();
				}
				catch (MasterManagerException e) {
					log.error(e.getMessage());
				}
			}
		}, monitorInterval, monitorInterval);
	}

	private void monitorHostsTask() {
		log.info("Starting host monitoring task...");
		List<HostDecisionResult> hostDecisions = new LinkedList<>();
		List<SimpleNode> nodes = nodesService.getReadyNodes();
		for (SimpleNode node : nodes) {
			String hostname = node.getPublicIpAddress();
			Map<String, Double> newFields = hostMetricsService.getHostStats(hostname);
			newFields.forEach((field, value) -> saveHostMonitoring(hostname, field, value));
			HostDecisionResult hostDecisionResult = runHostRules(hostname, newFields);
			hostDecisions.add(hostDecisionResult);
		}
		if (!hostDecisions.isEmpty()) {
			processHostDecisions(hostDecisions, nodes);
		}
	}

	private HostDecisionResult runHostRules(String hostname, Map<String, Double> newFields) {
		HostEvent hostEvent = new HostEvent(hostname);
		Map<String, Double> hostEventFields = hostEvent.getFields();
		getHostMonitoring(hostname)
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
			? new HostDecisionResult(hostname)
			: hostRulesService.processHostEvent(hostname, hostEvent);
	}

	//TODO move to decisionsService?
	private void processHostDecisions(List<HostDecisionResult> hostDecisions, List<SimpleNode> nodes) {
		List<HostDecisionResult> relevantHostDecisions = new LinkedList<>();
		log.info("Processing host decisions...");
		for (HostDecisionResult hostDecision : hostDecisions) {
			String hostname = hostDecision.getHostname();
			RuleDecision decision = hostDecision.getDecision();
			log.info("Hostname {} had decision {}", hostname, decision);
			HostEventEntity hostEvent = hostsEventsService.saveHostEvent(hostname, decision.toString());
			int hostEventCount = hostEvent.getCount();
			if ((decision == RuleDecision.START && hostEventCount >= startHostOnEventsCount)
				|| (decision == RuleDecision.STOP && hostEventCount >= stopHostOnEventsCount)) {
				relevantHostDecisions.add(hostDecision);
				HostDecisionEntity hostDecisionEntity = decisionsService.addHostDecision(
					hostDecision.getHostname(),
					hostDecision.getDecision().name(),
					hostDecision.getRuleId());
				decisionsService.addHostDecisionValueFromFields(hostDecisionEntity, hostDecision.getFields());
			}
		}
		if (!relevantHostDecisions.isEmpty()) {
			processRelevantHostDecisions(relevantHostDecisions, nodes);
		}
	}

	private void processRelevantHostDecisions(List<HostDecisionResult> relevantHostDecisions,
											  final List<SimpleNode> nodes) {
		Collections.sort(relevantHostDecisions);
		HostDecisionResult topPriorityHostDecision = relevantHostDecisions.get(0);
		RuleDecision decision = topPriorityHostDecision.getDecision();
		if (decision == RuleDecision.START) {
			if (maximumHosts <= 0 || nodes.size() < maximumHosts) {
				startHost(topPriorityHostDecision.getHostname());
			}
		}
		else if (decision == RuleDecision.STOP) {
			if (nodes.size() > minimumHosts) {
				stopHost(relevantHostDecisions, nodes);
			}
		}
	}

	private void startHost(String hostname) {
		HostLocation hostLocation = hostsService.getHostDetails(hostname).getHostLocation();
		Pair<String, String> container = getRandomContainerToMigrate(hostname);
		String serviceName = container.getFirst();
		String containerId = container.getSecond();
		if (!containerId.isEmpty()) {
			ServiceEntity serviceConfig = servicesService.getService(serviceName);
			double serviceAvgMem = serviceConfig.getExpectedMemoryConsumption();
			HostAddress toHostAddress = hostsService.getAvailableHost(serviceAvgMem, hostLocation);
			// TODO porquê migrar logo um container?
			containersService.migrateContainer(containerId, toHostAddress);
			log.info("RuleDecision executed: Started host {} and migrated container {} to it", toHostAddress, containerId);
		}
	}

	private void stopHost(List<HostDecisionResult> relevantHostDecisions, List<SimpleNode> nodes) {
		//TODO : review stop host
		String stopHostname = "";
		ListIterator<HostDecisionResult> decisionIterator =
			relevantHostDecisions.listIterator(relevantHostDecisions.size());
		while (decisionIterator.hasPrevious()) {
			String hostname = decisionIterator.previous().getHostname();
			if (nodes.stream().anyMatch(n -> n.getPublicIpAddress().equals(hostname) && n.getRole() != NodeRole.MANAGER)) {
				// Node with least priority that is not a manager
				stopHostname = hostname;
				break;
			}
		}
		String migrateToHostname = getHostToMigrate(stopHostname, nodes);
		List<ContainerEntity> containers = containersService.migrateAppContainers(stopHostname, migrateToHostname);
		String hostnameToRemove = stopHostname;
		//TODO os containers não migram em paralelo?
		new Timer("RemoveHostFromSwarmTimer").schedule(new TimerTask() {
			@Override
			public void run() {
				hostsService.removeHost(hostnameToRemove);
			}
		}, containers.size() * DELAY_STOP_HOST);
		//TODO garantir que o host é removido dinamicamente só depois de serem migrados todos os containers
		log.info("RuleDecision executed: Stopped host {} and migrated containers to host {}",
			stopHostname, migrateToHostname);
	}

	private String getHostToMigrate(String hostToRemove, List<SimpleNode> nodes) {
		//TODO e se não existir mais nenhum node na mesma zona?
		return nodes.stream()
			.filter(n -> !n.getPublicIpAddress().equals(hostToRemove)
				&& Objects.equals(hostsService.getHostDetails(hostToRemove).getHostLocation().getRegion(),
				hostsService.getHostDetails(n.getPublicIpAddress()).getHostLocation().getRegion()))
			.map(SimpleNode::getPublicIpAddress)
			.findFirst()
			.orElseThrow(() -> new MasterManagerException("Can't find new host to migrate containers to"));
	}

	private Pair<String, String> getRandomContainerToMigrate(String hostname) {
		// TODO: Improve container choice
		List<ContainerEntity> hostContainers = containersService.getAppContainers(hostname);
		if (hostContainers.isEmpty()) {
			return Pair.of("", "");
		}
		ContainerEntity container = hostContainers.get(0);
		return Pair.of(container.getLabels().get(ContainerConstants.Label.SERVICE_NAME), container.getContainerId());
	}

}
