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

import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.CpuStats;
import com.spotify.docker.client.messages.MemoryStats;
import com.spotify.docker.client.messages.NetworkStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ContainerFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.service.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.service.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.location.LocationRequestService;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.metrics.simulated.containers.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.service.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ServicesMonitoringService {

	// Container minimum logs to start applying rules
	private static final int CONTAINER_MINIMUM_LOGS_COUNT = 1;

	private final ServiceMonitoringRepository servicesMonitoring;
	private final ServiceMonitoringLogsRepository serviceMonitoringLogs;

	private final ContainersService containersService;
	private final ServicesService servicesService;
	private final ServiceRulesService serviceRulesService;
	private final ServicesEventsService servicesEventsService;
	private final HostsService hostsService;
	private final LocationRequestService requestLocationMonitoringService;
	private final DecisionsService decisionsService;
	private final ContainerSimulatedMetricsService containerSimulatedMetricsService;

	private final long monitorPeriod;
	private final int stopContainerOnEventCount;
	private final int replicateContainerOnEventCount;
	private final int migrateContainerOnEventCount;
	private final boolean isTestEnable;

	public ServicesMonitoringService(ServiceMonitoringRepository servicesMonitoring,
									 ServiceMonitoringLogsRepository serviceMonitoringLogs,
									 ContainersService containersService,
									 ServicesService servicesService, ServiceRulesService serviceRulesService,
									 ServicesEventsService servicesEventsService, HostsService hostsService,
									 LocationRequestService requestLocationMonitoringService,
									 DecisionsService decisionsService,
									 ContainerSimulatedMetricsService containerSimulatedMetricsService,
									 ContainerProperties containerProperties,
									 WorkerManagerProperties workerManagerProperties) {
		this.serviceMonitoringLogs = serviceMonitoringLogs;
		this.servicesMonitoring = servicesMonitoring;
		this.containersService = containersService;
		this.servicesService = servicesService;
		this.serviceRulesService = serviceRulesService;
		this.servicesEventsService = servicesEventsService;
		this.hostsService = hostsService;
		this.requestLocationMonitoringService = requestLocationMonitoringService;
		this.decisionsService = decisionsService;
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
		this.monitorPeriod = containerProperties.getMonitorPeriod();
		this.stopContainerOnEventCount = containerProperties.getStopContainerOnEventCount();
		this.replicateContainerOnEventCount = containerProperties.getReplicateContainerOnEventCount();
		this.migrateContainerOnEventCount = containerProperties.getMigrateContainerOnEventCount();
		this.isTestEnable = workerManagerProperties.getTests().isEnabled();
	}

	public List<ServiceMonitoringEntity> getServicesMonitoring() {
		return servicesMonitoring.findAll();
	}

	public List<ServiceMonitoringEntity> getServiceMonitoring(String serviceName) {
		return servicesMonitoring.getByServiceNameIgnoreCase(serviceName);
	}

	public List<ServiceMonitoringEntity> getContainerMonitoring(String containerId) {
		return servicesMonitoring.getByContainerId(containerId);
	}

	public ServiceMonitoringEntity getContainerMonitoring(String containerId, String field) {
		return servicesMonitoring.getByContainerIdAndFieldIgnoreCase(containerId, field);
	}

	public void saveServiceMonitoring(String containerId, String serviceName, String field, double value) {
		ServiceMonitoringEntity serviceMonitoring = getContainerMonitoring(containerId, field);
		Timestamp updateTime = Timestamp.from(Instant.now());
		if (serviceMonitoring == null) {
			serviceMonitoring = ServiceMonitoringEntity.builder()
				.containerId(containerId)
				.serviceName(serviceName)
				.field(field)
				.minValue(value).maxValue(value).sumValue(value).lastValue(value)
				.count(1)
				.lastUpdate(updateTime)
				.build();
		}
		else {
			serviceMonitoring.logValue(value, updateTime);
		}
		servicesMonitoring.save(serviceMonitoring);
		if (isTestEnable) {
			saveServiceMonitoringLog(containerId, serviceName, field, value);
		}
	}

	public List<ServiceFieldAvg> getServiceFieldsAvg(String serviceName) {
		return servicesMonitoring.getServiceFieldsAvg(serviceName);
	}

	public ServiceFieldAvg getServiceFieldAvg(String serviceName, String field) {
		return servicesMonitoring.getServiceFieldAvg(serviceName, field);
	}

	public List<ContainerFieldAvg> getContainerFieldsAvg(String containerId) {
		return servicesMonitoring.getContainerFieldsAvg(containerId);
	}

	public ContainerFieldAvg getContainerFieldAvg(String containerId, String field) {
		return servicesMonitoring.getContainerFieldAvg(containerId, field);
	}

	public List<ServiceMonitoringEntity> getTopContainersByField(List<String> containerIds, String field) {
		return servicesMonitoring.getTopContainersByField(containerIds, field);
	}

	public void saveServiceMonitoringLog(String containerId, String serviceName, String field, double effectiveValue) {
		ServiceMonitoringLogEntity serviceMonitoringLogEntity = ServiceMonitoringLogEntity.builder()
			.containerId(containerId)
			.serviceName(serviceName)
			.field(field)
			.timestamp(LocalDateTime.now())
			.effectiveValue(effectiveValue)
			.build();
		serviceMonitoringLogs.save(serviceMonitoringLogEntity);
	}

	public List<ServiceMonitoringLogEntity> getServiceMonitoringLogs() {
		return serviceMonitoringLogs.findAll();
	}

	public List<ServiceMonitoringLogEntity> getServiceMonitoringLogsByServiceName(String serviceName) {
		return serviceMonitoringLogs.findByServiceName(serviceName);
	}

	public List<ServiceMonitoringLogEntity> getServiceMonitoringLogsByContainerId(String containerId) {
		return serviceMonitoringLogs.findByContainerId(containerId);
	}

	public void initServiceMonitorTimer() {
		new Timer("MonitorServicesTimer", true).schedule(new TimerTask() {
			private long lastRun = System.currentTimeMillis();

			@Override
			public void run() {
				long currRun = System.currentTimeMillis();
				//TODO replace diffSeconds with calculation from previous database save
				int diffSeconds = (int) ((currRun - lastRun) / TimeUnit.SECONDS.toMillis(1));
				lastRun = currRun;
				try {
					monitorServicesTask(diffSeconds);
				}
				catch (WorkerManagerException e) {
					log.error(e.getMessage());
				}
			}
		}, monitorPeriod, monitorPeriod);
	}

	private void monitorServicesTask(int secondsFromLastRun) {
		log.info("Starting service monitoring task...");
		Map<String, List<ServiceDecisionResult>> servicesDecisions = new HashMap<>();
		List<ContainerEntity> containers = containersService.getAppContainers();
		if (isTestEnable) {
			List<ContainerEntity> managerMasterContainers = containersService.getContainersWithLabels(
				Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, WorkerManagerProperties.WORKER_MANAGER)));
			containers.addAll(managerMasterContainers);
		}
		for (ContainerEntity container : containers) {
			log.info("On {}", container);
			String containerId = container.getContainerId();
			String serviceName = container.getLabels().get(ContainerConstants.Label.SERVICE_NAME);
			HostAddress hostAddress = container.getHostAddress();
			Map<String, Double> newFields = getContainerStats(container, secondsFromLastRun);
			newFields.forEach((field, value) -> {
				saveServiceMonitoring(containerId, serviceName, field, value);
			});
			for (AppEntity app : servicesService.getApps(serviceName)) {
				String appName = app.getName();
				ServiceDecisionResult serviceDecisionResult = runAppRules(appName, hostAddress, containerId,
					serviceName, newFields);
				List<ServiceDecisionResult> serviceDecisions = servicesDecisions.get(serviceName);
				if (serviceDecisions != null) {
					serviceDecisions.add(serviceDecisionResult);
				}
				else {
					serviceDecisions = new LinkedList<>(List.of(serviceDecisionResult));
					servicesDecisions.put(serviceName, serviceDecisions);
				}
			}
		}
		processContainerDecisions(servicesDecisions, secondsFromLastRun);
	}

	private ServiceDecisionResult runAppRules(String appName, HostAddress hostAddress, String containerId,
											  String serviceName, Map<String, Double> newFields) {
		List<ServiceMonitoringEntity> loggedFields = getContainerMonitoring(containerId);
		ContainerEvent containerEvent = new ContainerEvent(containerId, serviceName);
		Map<String, Double> containerEventFields = containerEvent.getFields();
		for (ServiceMonitoringEntity loggedField : loggedFields) {
			long count = loggedField.getCount();
			if (count < CONTAINER_MINIMUM_LOGS_COUNT) {
				continue;
			}
			String field = loggedField.getField();
			Double newValue = newFields.get(field);
			if (newValue == null) {
				continue;
			}
			containerEventFields.put(field + "-effective-val", newValue);
			//TODO conta com este newValue?
			double sumValue = loggedField.getSumValue();
			double average = sumValue / (count * 1.0);
			containerEventFields.put(field + "-avg-val", average);
			double deviationFromAvgValue = ((newValue - average) / average) * 100;
			containerEventFields.put(field + "-deviation-%-on-avg-val", deviationFromAvgValue);
			double lastValue = loggedField.getLastValue();
			double deviationFromLastValue = ((newValue - lastValue) / lastValue) * 100;
			containerEventFields.put(field + "-deviation-%-on-last-val", deviationFromLastValue);
		}
		HostDetails hostDetails = hostsService.getHostDetails(hostAddress);
		return containerEventFields.isEmpty()
			? new ServiceDecisionResult(hostDetails, containerId, serviceName)
			: serviceRulesService.processServiceEvent(appName, hostDetails, containerEvent);
	}

	private void processContainerDecisions(Map<String, List<ServiceDecisionResult>> servicesDecisions,
										   int secondsFromLastRun) {
		log.info("Processing container decisions...");
		Map<String, List<ServiceDecisionResult>> relevantServicesDecisions = new HashMap<>();
		for (List<ServiceDecisionResult> serviceDecisions : servicesDecisions.values()) {
			for (ServiceDecisionResult containerDecision : serviceDecisions) {
				String serviceName = containerDecision.getServiceName();
				String containerId = containerDecision.getContainerId();
				RuleDecision decision = containerDecision.getDecision();
				log.info("ServiceName {} on containerId {} had decision {}", serviceName, containerId, decision);
				ServiceEventEntity serviceEvent =
					servicesEventsService.saveServiceEvent(containerId, serviceName, decision.toString());
				int serviceEventCount = serviceEvent.getCount();
				if (decision == RuleDecision.STOP && serviceEventCount >= stopContainerOnEventCount
					|| decision == RuleDecision.REPLICATE && serviceEventCount >= replicateContainerOnEventCount
					|| decision == RuleDecision.MIGRATE && serviceEventCount >= migrateContainerOnEventCount) {
					List<ServiceDecisionResult> relevantServiceDecisions = relevantServicesDecisions.get(serviceName);
					if (relevantServiceDecisions != null) {
						relevantServiceDecisions.add(containerDecision);
					}
					else {
						relevantServiceDecisions = new ArrayList<>(List.of(containerDecision));
						relevantServicesDecisions.put(serviceName, relevantServiceDecisions);
					}
				}
			}
		}
		if (!relevantServicesDecisions.isEmpty()) {
			processRelevantContainerDecisions(relevantServicesDecisions, servicesDecisions, secondsFromLastRun);
		}
	}

	private void processRelevantContainerDecisions(Map<String, List<ServiceDecisionResult>> relevantServicesDecisions,
												   Map<String, List<ServiceDecisionResult>> allServicesDecisions,
												   int secondsFromLastRun) {
		Map<String, HostDetails> servicesHosts =
			requestLocationMonitoringService.findHostsToStartServices(allServicesDecisions, secondsFromLastRun);
		for (Entry<String, List<ServiceDecisionResult>> servicesDecisions : allServicesDecisions.entrySet()) {
			String serviceName = servicesDecisions.getKey();
			List<ServiceDecisionResult> containerDecisions = servicesDecisions.getValue();
			List<ServiceDecisionResult> relevantContainerDecisions =
				relevantServicesDecisions.getOrDefault(serviceName, new ArrayList<>());
			int currentReplicas = containerDecisions.size();
			int minimumReplicas = servicesService.getMinReplicasByServiceName(serviceName);
			int maximumReplicas = servicesService.getMaxReplicasByServiceName(serviceName);
			if (currentReplicas < minimumReplicas) {
				executeStartContainerDecision(containerDecisions, relevantContainerDecisions, servicesHosts);
			}
			else if (!relevantContainerDecisions.isEmpty()) {
				Collections.sort(relevantContainerDecisions);
				ServiceDecisionResult topPriorityDecisionResult = relevantContainerDecisions.get(0);
				RuleDecision topPriorityDecision = topPriorityDecisionResult.getDecision();
				if (topPriorityDecision == RuleDecision.REPLICATE) {
					if (maximumReplicas == 0 || currentReplicas < maximumReplicas) {
						String containerId = topPriorityDecisionResult.getContainerId();
						String service = topPriorityDecisionResult.getServiceName();
						String decision = topPriorityDecisionResult.getDecision().name();
						long ruleId = topPriorityDecisionResult.getRuleId();
						HostAddress hostAddress = topPriorityDecisionResult.getHostDetails().getAddress();
						Map<String, Double> fields = topPriorityDecisionResult.getFields();
						executeStartContainerDecision(containerId, service, decision, ruleId, hostAddress, fields, servicesHosts);
					}
				}
				else if (topPriorityDecision == RuleDecision.STOP) {
					if (currentReplicas > minimumReplicas) {
						ServiceDecisionResult leastPriorityContainer = relevantContainerDecisions.get(relevantContainerDecisions.size() - 1);
						String containerId = leastPriorityContainer.getContainerId();
						String service = leastPriorityContainer.getServiceName();
						String decision = leastPriorityContainer.getDecision().name();
						long ruleId = leastPriorityContainer.getRuleId();
						HostAddress hostAddress = leastPriorityContainer.getHostDetails().getAddress();
						Map<String, Double> fields = leastPriorityContainer.getFields();
						executeStopContainerDecision(containerId, service, decision, ruleId, hostAddress, fields);
					}
				}
			}
		}
	}

	private void executeStartContainerDecision(List<ServiceDecisionResult> allContainersDecisions,
											   List<ServiceDecisionResult> relevantContainersDecisions,
											   Map<String, HostDetails> servicesLocationsRegions) {
		Optional<ServiceDecisionResult> containerDecision = Optional.empty();
		if (!relevantContainersDecisions.isEmpty()) {
			Collections.sort(relevantContainersDecisions);
			ServiceDecisionResult topPriorityContainerDecision = relevantContainersDecisions.get(0);
			if (topPriorityContainerDecision.getDecision() == RuleDecision.REPLICATE) {
				containerDecision = Optional.of(topPriorityContainerDecision);
			}
		}
		if (containerDecision.isEmpty()) {
			Collections.sort(allContainersDecisions);
			containerDecision = allContainersDecisions.stream()
				.filter(d -> d.getDecision() == RuleDecision.REPLICATE)
				.findFirst();
			if (containerDecision.isEmpty()) {
				containerDecision = allContainersDecisions.stream()
					.filter(d -> d.getDecision() == RuleDecision.NONE)
					.findFirst();
			}
		}
		containerDecision.ifPresent(decision ->
			executeStartContainerDecision(decision.getContainerId(), decision.getServiceName(), decision.getDecision().name(),
				decision.getRuleId(), decision.getHostDetails().getAddress(), decision.getFields(), servicesLocationsRegions));
	}

	private void executeStartContainerDecision(String containerId, String serviceName, String decision, long ruleId,
											   HostAddress hostAddress, Map<String, Double> fields,
											   Map<String, HostDetails> servicesHostDetails) {
		final HostLocation startLocation;
		if (servicesHostDetails.containsKey(serviceName)) {
			startLocation = servicesHostDetails.get(serviceName).getLocation();
			log.info("Starting service {} from {} at {} (location from request-location-monitor)", serviceName, hostAddress, startLocation);
		}
		else {
			startLocation = hostsService.getHostLocation(hostAddress);
			log.info("Starting service {} from {} on the same host", serviceName, hostAddress);
		}
		double serviceExpectedMemoryConsumption = servicesService.getService(serviceName).getExpectedMemoryConsumption();
		HostAddress selectedHostAddress = hostsService.getAvailableHost(serviceExpectedMemoryConsumption, startLocation);
		String replicatedContainerId = containersService.replicateContainer(containerId, selectedHostAddress).getContainerId();
		HostDetails selectedHostDetails = hostsService.getHostDetails(selectedHostAddress);
		String result = String.format("replicated container %s of service %s to container %s on %s",
			containerId, serviceName, replicatedContainerId, selectedHostDetails);
		saveServiceDecision(containerId, serviceName, decision, ruleId, fields, result);
	}

	private void executeStopContainerDecision(String containerId, String serviceName, String decision, long ruleId,
											  HostAddress hostAddress, Map<String, Double> fields) {
		containersService.stopContainer(containerId);
		String result = String.format("stopped container %s of service %s on host %s", containerId, serviceName, hostAddress);
		saveServiceDecision(containerId, serviceName, decision, ruleId, fields, result);
	}

	private void saveServiceDecision(String containerId, String serviceName, String decision, long ruleId,
									 Map<String, Double> fields, String result) {
		log.info("Executed decision: {}", result);
		servicesEventsService.resetServiceEvent(serviceName);
		ServiceDecisionEntity serviceDecision = decisionsService.addServiceDecision(containerId, serviceName, decision, ruleId, result);
		decisionsService.addServiceDecisionValueFromFields(serviceDecision, fields);
	}

	Map<String, Double> getContainerStats(ContainerEntity container, double secondsInterval) {
		String containerId = container.getContainerId();
		HostAddress hostAddress = container.getHostAddress();
		ContainerStats containerStats = containersService.getContainerStats(containerId, hostAddress);
		CpuStats cpuStats = containerStats.cpuStats();
		CpuStats preCpuStats = containerStats.precpuStats();
		double cpu = cpuStats.cpuUsage().totalUsage().doubleValue();
		double cpuPercent = getContainerCpuPercent(preCpuStats, cpuStats);
		MemoryStats memoryStats = containerStats.memoryStats();
		double ram = memoryStats.usage().doubleValue();
		double ramPercent = getContainerRamPercent(memoryStats);
		double rxBytes = 0;
		double txBytes = 0;
		for (NetworkStats stats : containerStats.networks().values()) {
			rxBytes += stats.rxBytes().doubleValue();
			txBytes += stats.txBytes().doubleValue();
		}
		// Metrics from docker
		Map<String, Double> fields = new HashMap<>(Map.of(
			"cpu", cpu,
			"ram", ram,
			"cpu-%", cpuPercent,
			"ram-%", ramPercent,
			"rx-bytes", rxBytes,
			"tx-bytes", txBytes));
		// Simulated metrics
		if (container.getLabels().containsKey(ContainerConstants.Label.SERVICE_NAME)) {
			Map<String, Double> simulatedFields = containerSimulatedMetricsService.getSimulatedFieldsValues(containerId);
			fields.putAll(simulatedFields);
		}
		// Calculated metrics
		//TODO use monitoring previous update to calculate interval, instead of passing through argument
		Map.of("rx-bytes", rxBytes, "tx-bytes", txBytes).forEach((field, value) -> {
			ServiceMonitoringEntity monitoring = getContainerMonitoring(containerId, field);
			double lastValue = monitoring == null ? 0 : monitoring.getLastValue();
			double bytesPerSec = Math.max(0, (value - lastValue) / secondsInterval);
			fields.put(field + "-per-sec", bytesPerSec);
		});
		return fields;
	}

	private double getContainerCpuPercent(CpuStats preCpuStats, CpuStats cpuStats) {
		double systemDelta = cpuStats.systemCpuUsage().doubleValue() - preCpuStats.systemCpuUsage().doubleValue();
		double cpuDelta = cpuStats.cpuUsage().totalUsage().doubleValue() - preCpuStats.cpuUsage().totalUsage().doubleValue();
		double cpuPercent = 0.0;
		if (systemDelta > 0.0 && cpuDelta > 0.0) {
			double onlineCpus = cpuStats.cpuUsage().percpuUsage().stream().filter(cpuUsage -> cpuUsage >= 1).count();
			cpuPercent = (cpuDelta / systemDelta) * onlineCpus * 100.0;
		}
		return cpuPercent;
	}


	private double getContainerRamPercent(MemoryStats memStats) {
		return memStats.limit() < 1 ? 0.0 : (memStats.usage().doubleValue() / memStats.limit().doubleValue()) * 100.0;
	}

}
