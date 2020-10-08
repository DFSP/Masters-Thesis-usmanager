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

package pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.service.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.service.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.service.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.service.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
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
								  WorkerManagerProperties workerManagerProperties) {
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
		this.isTestEnable = workerManagerProperties.getTests().isEnabled();
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
		new Timer("MonitorHostTimer", true).schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					monitorHostsTask();
				}
				catch (WorkerManagerException e) {
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
			HostAddress hostAddress = node.getHostAddress();
			HostDetails hostDetails = hostsService.getHostDetails(hostAddress);
			Map<String, Double> newFields = hostMetricsService.getHostStats(hostAddress);
			newFields.forEach((field, value) -> saveHostMonitoring(hostAddress, field, value));
			HostDecisionResult hostDecisionResult = runHostRules(hostDetails, newFields);
			hostDecisions.add(hostDecisionResult);
		}
		if (!hostDecisions.isEmpty()) {
			processHostDecisions(hostDecisions, nodes);
		}
	}

	private HostDecisionResult runHostRules(HostDetails hostDetails, Map<String, Double> newFields) {
		HostEvent hostEvent = new HostEvent(hostDetails);
		Map<String, Double> hostEventFields = hostEvent.getFields();
		getHostMonitoring(hostDetails.getAddress())
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
			? new HostDecisionResult(hostDetails)
			: hostRulesService.processHostEvent(hostDetails, hostEvent);
	}

	//TODO move to decisionsService?
	private void processHostDecisions(List<HostDecisionResult> hostDecisions, List<SimpleNode> nodes) {
		List<HostDecisionResult> relevantHostDecisions = new LinkedList<>();
		log.info("Processing host decisions...");
		for (HostDecisionResult hostDecision : hostDecisions) {
			HostDetails hostDetails = hostDecision.getHostDetails();
			RuleDecision decision = hostDecision.getDecision();
			log.info("Host {} had decision {}", hostDetails, decision);
			HostEventEntity hostEvent = hostsEventsService.saveHostEvent(hostDetails, decision.toString());
			int hostEventCount = hostEvent.getCount();
			if ((decision == RuleDecision.START && hostEventCount >= startHostOnEventsCount)
				|| (decision == RuleDecision.STOP && hostEventCount >= stopHostOnEventsCount)) {
				relevantHostDecisions.add(hostDecision);
				HostDecisionEntity hostDecisionEntity = decisionsService.addHostDecision(hostDetails, decision.name(),
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
				startHost(topPriorityHostDecision.getHostDetails());
			}
		}
		else if (decision == RuleDecision.STOP) {
			if (nodes.size() > minimumHosts) {
				stopHost(relevantHostDecisions, nodes);
			}
		}
	}

	private void startHost(HostDetails hostDetails) {
		HostAddress hostAddress = hostDetails.getAddress();
		HostLocation hostLocation = hostDetails.getLocation();
		Pair<String, String> container = getRandomContainerToMigrate(hostAddress);
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
		HostAddress hostToStop = null;
		ListIterator<HostDecisionResult> decisionIterator =
			relevantHostDecisions.listIterator(relevantHostDecisions.size());
		while (decisionIterator.hasPrevious()) {
			HostAddress hostAddress = decisionIterator.previous().getHostDetails().getAddress();
			if (nodes.stream().anyMatch(n -> n.getHostAddress().equals(hostAddress) && n.getRole() != NodeRole.MANAGER)) {
				// Node with least priority that is not a manager
				hostToStop = hostAddress;
				break;
			}
		}
		if (hostToStop == null) {
			// TODO now what?
			return;
		}
		HostAddress migrateToHost = getHostToMigrate(hostToStop, nodes);
		List<ContainerEntity> containers = containersService.migrateAppContainers(hostToStop, migrateToHost);
		final HostAddress hostToRemove = hostToStop;
		//TODO os containers não migram em paralelo?
		new Timer("RemoveHostFromSwarmTimer").schedule(new TimerTask() {
			@Override
			public void run() {
				hostsService.removeHost(hostToRemove);
			}
		}, containers.size() * DELAY_STOP_HOST);
		//TODO garantir que o host é removido dinamicamente só depois de serem migrados todos os containers
		log.info("RuleDecision executed: Stopped host {} and migrated containers to host {}", hostToRemove, migrateToHost);
	}

	private HostAddress getHostToMigrate(HostAddress hostAddress, List<SimpleNode> nodes) {
		return nodes.stream()
			.filter(n -> {
				HostDetails hostDetails = hostsService.getHostDetails(hostAddress);
				HostDetails nodeHostDetails = hostsService.getHostDetails(n.getHostAddress());
				return !n.getHostAddress().equals(hostAddress)
					// TODO e se não existir mais nenhum node na mesma zona?
					// TODO mudar para coordenadas
					&& Objects.equals(hostDetails.getLocation().getRegion(), nodeHostDetails.getLocation().getRegion());
			})
			.map(SimpleNode::getHostAddress)
			.findFirst()
			.orElseThrow(() -> new WorkerManagerException("Can't find new host to migrate containers to"));
	}

	private Pair<String, String> getRandomContainerToMigrate(HostAddress hostAddress) {
		// TODO: Improve container choice
		List<ContainerEntity> hostContainers = containersService.getAppContainers(hostAddress);
		if (hostContainers.isEmpty()) {
			return Pair.of("", "");
		}
		ContainerEntity container = hostContainers.get(0);
		return Pair.of(container.getLabels().get(ContainerConstants.Label.SERVICE_NAME), container.getContainerId());
	}

}
