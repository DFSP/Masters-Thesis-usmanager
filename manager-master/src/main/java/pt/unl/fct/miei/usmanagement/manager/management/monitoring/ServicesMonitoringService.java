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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.MasterManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.AppSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerType;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.ServiceMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ServiceSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ContainerFieldAverage;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEventEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceFieldAverage;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServicesMonitoringService {

	// Container minimum logs to start applying rules
	private static final int CONTAINER_MINIMUM_LOGS_COUNT = 1;

	private static final int STOP_CONTAINER_RECOVERY_ON_FAILURES = 3;
	private static final long STOP_CONTAINER_RECOVERY_TIME_FRAME = TimeUnit.MINUTES.toMillis(10);

	private final ServiceMonitoringRepository servicesMonitoring;
	private final ServiceMonitoringLogsRepository serviceMonitoringLogs;

	private final ContainersService containersService;
	private final ServicesService servicesService;
	private final ServiceRulesService serviceRulesService;
	private final ServicesEventsService servicesEventsService;
	private final HostsService hostsService;
	private final LocationRequestsService requestLocationMonitoringService;
	private final DecisionsService decisionsService;
	private final ServiceMetricsService serviceMetricsService;
	private final AppSimulatedMetricsService appSimulatedMetricsService;
	private final ServiceSimulatedMetricsService serviceSimulatedMetricsService;
	private final ContainerSimulatedMetricsService containerSimulatedMetricsService;

	private final long monitorPeriod;
	private final int stopContainerOnEventCount;
	private final int replicateContainerOnEventCount;
	private final int migrateContainerOnEventCount;
	private final boolean isTestEnable;
	private Timer serviceMonitoringTimer;

	public ServicesMonitoringService(ServiceMonitoringRepository servicesMonitoring,
									 ServiceMonitoringLogsRepository serviceMonitoringLogs,
									 ContainersService containersService,
									 ServicesService servicesService, ServiceRulesService serviceRulesService,
									 ServicesEventsService servicesEventsService, HostsService hostsService,
									 LocationRequestsService requestLocationMonitoringService,
									 DecisionsService decisionsService, ServiceMetricsService serviceMetricsService,
									 AppSimulatedMetricsService appSimulatedMetricsService,
									 ServiceSimulatedMetricsService serviceSimulatedMetricsService,
									 ContainerSimulatedMetricsService containerSimulatedMetricsService,
									 ContainerProperties containerProperties, MasterManagerProperties masterManagerProperties) {
		this.serviceMonitoringLogs = serviceMonitoringLogs;
		this.servicesMonitoring = servicesMonitoring;
		this.containersService = containersService;
		this.servicesService = servicesService;
		this.serviceRulesService = serviceRulesService;
		this.servicesEventsService = servicesEventsService;
		this.hostsService = hostsService;
		this.requestLocationMonitoringService = requestLocationMonitoringService;
		this.decisionsService = decisionsService;
		this.serviceMetricsService = serviceMetricsService;
		this.appSimulatedMetricsService = appSimulatedMetricsService;
		this.serviceSimulatedMetricsService = serviceSimulatedMetricsService;
		this.containerSimulatedMetricsService = containerSimulatedMetricsService;
		this.monitorPeriod = containerProperties.getMonitorPeriod();
		this.stopContainerOnEventCount = containerProperties.getStopContainerOnEventCount();
		this.replicateContainerOnEventCount = containerProperties.getReplicateContainerOnEventCount();
		this.migrateContainerOnEventCount = containerProperties.getMigrateContainerOnEventCount();
		this.isTestEnable = masterManagerProperties.getTests().isEnabled();
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
			serviceMonitoring.update(value, updateTime);
		}
		servicesMonitoring.save(serviceMonitoring);
		if (isTestEnable) {
			saveServiceMonitoringLog(containerId, serviceName, field, value);
		}
	}

	public List<ServiceFieldAverage> getServiceFieldsAvg(String serviceName) {
		return servicesMonitoring.getServiceFieldsAvg(serviceName);
	}

	public ServiceFieldAverage getServiceFieldAverage(String serviceName, String field) {
		return servicesMonitoring.getServiceFieldAverage(serviceName, field);
	}

	public List<ContainerFieldAverage> getContainerFieldsAvg(String containerId) {
		return servicesMonitoring.getContainerFieldsAvg(containerId);
	}

	public ContainerFieldAverage getContainerFieldAverage(String containerId, String field) {
		return servicesMonitoring.getContainerFieldAverage(containerId, field);
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
			.value(effectiveValue)
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
		return serviceMonitoringLogs.findByContainerIdStartingWith(containerId);
	}

	public void initServiceMonitorTimer() {
		serviceMonitoringTimer = new Timer("master-manager-services-monitoring", true);
		serviceMonitoringTimer.schedule(new TimerTask() {
			private long previousTime = System.currentTimeMillis();

			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				int interval = (int) (currentTime - previousTime);
				previousTime = currentTime;
				try {
					monitorServicesTask(interval);
				}
				catch (ManagerException e) {
					log.error(e.getMessage());
				}
			}
		}, monitorPeriod, monitorPeriod);
	}

	private void monitorServicesTask(int interval) {
		List<ContainerEntity> monitoringContainers = containersService.getAppContainers();
		if (isTestEnable) {
			List<ContainerEntity> systemContainers = containersService.getContainersWithLabels(
				Set.of(
					Pair.of(ContainerConstants.Label.SERVICE_NAME, MasterManagerProperties.MASTER_MANAGER),
					Pair.of(ContainerConstants.Label.SERVICE_NAME, WorkerManagerProperties.WORKER_MANAGER)
				));
			monitoringContainers.addAll(systemContainers);
		}

		List<ContainerEntity> systemContainers = containersService.getSystemContainers();

		List<ContainerEntity> synchronizedContainers = containersService.synchronizeDatabaseContainers();
		restoreCrashedContainers(monitoringContainers, synchronizedContainers);
		systemContainers.parallelStream()
			.filter(container -> synchronizedContainers.stream().noneMatch(c -> Objects.equals(c.getContainerId(), container.getContainerId())))
			.forEach(this::restartContainer);

		restoreCrashedContainers(systemContainers, synchronizedContainers);

		Map<String, List<ServiceDecisionResult>> containersDecisions = new HashMap<>();

		monitoringContainers.parallelStream().forEach(container -> {
			if (synchronizedContainers.stream().noneMatch(c ->
				Objects.equals(c.getContainerId(), container.getContainerId()) && Objects.equals(c.getHostAddress(), container.getHostAddress()))) {
				containersService.launchContainer(container.getHostAddress(), container.getServiceName(), ContainerType.SINGLETON);
			}
			else {
				HostAddress hostAddress = container.getHostAddress();
				String containerId = container.getContainerId();
				String serviceName = container.getServiceName();

				// Metrics from docker
				Map<String, Double> stats = serviceMetricsService.getContainerStats(hostAddress, containerId);

				// Simulated app metrics
				for (AppEntity app : servicesService.getApps(serviceName)) {
					String appName = app.getName();
					Map<String, Double> appSimulatedFields = appSimulatedMetricsService.getAppSimulatedMetricByApp(appName)
						.stream().filter(metric -> metric.isActive() && (!stats.containsKey(metric.getName()) || metric.isOverride()))
						.collect(Collectors.toMap(metric -> metric.getField().getName(), appSimulatedMetricsService::randomizeFieldValue));
					stats.putAll(appSimulatedFields);
				}
				
				// Simulated service metrics
				Map<String, Double> serviceSimulatedFields = serviceSimulatedMetricsService.getServiceSimulatedMetricByService(serviceName)
					.stream().filter(metric -> metric.isActive() && (!stats.containsKey(metric.getName()) || metric.isOverride()))
					.collect(Collectors.toMap(metric -> metric.getField().getName(), serviceSimulatedMetricsService::randomizeFieldValue));
				stats.putAll(serviceSimulatedFields);

				// Simulated container metrics
				Map<String, Double> containerSimulatedFields = containerSimulatedMetricsService.getServiceSimulatedMetricByContainer(containerId)
					.stream().filter(metric -> metric.isActive() && (!stats.containsKey(metric.getName()) || metric.isOverride()))
					.collect(Collectors.toMap(metric -> metric.getField().getName(), containerSimulatedMetricsService::randomizeFieldValue));
				stats.putAll(containerSimulatedFields);

				// Calculated metrics
				Map<String, Double> calculatedMetrics = new HashMap<>(2);
				if (!serviceSimulatedFields.containsKey("rx-bytes-per-sec")
					&& !containerSimulatedFields.containsKey("rx-bytes-per-sec")) {
					calculatedMetrics.put("rx-bytes", stats.get("rx-bytes"));
				}
				if (!serviceSimulatedFields.containsKey("tx-bytes-per-sec")
					&& !containerSimulatedFields.containsKey("tx-bytes-per-sec")) {
					calculatedMetrics.put("tx-bytes", stats.get("tx-bytes"));
				}
				calculatedMetrics.forEach((field, value) -> {
					ServiceMonitoringEntity monitoring = getContainerMonitoring(containerId, field);
					double lastValue = monitoring == null ? 0 : monitoring.getLastValue();
					double bytesPerSec = Math.max(0, (value - lastValue) / interval);
					stats.put(field + "-per-sec", bytesPerSec);
				});

				stats.forEach((stat, value) -> saveServiceMonitoring(containerId, serviceName, stat, value));

				if (!serviceName.equals(MasterManagerProperties.MASTER_MANAGER)
					&& !serviceName.equals(WorkerManagerProperties.WORKER_MANAGER)) {
					ServiceDecisionResult containerDecisionResult = runRules(hostAddress, containerId, serviceName, stats);
					List<ServiceDecisionResult> containerDecisions = containersDecisions.get(serviceName);
					if (containerDecisions != null) {
						containerDecisions.add(containerDecisionResult);
					}
					else {
						containerDecisions = new LinkedList<>();
						containerDecisions.add(containerDecisionResult);
						containersDecisions.put(serviceName, containerDecisions);
					}
				}
			}
		});

		if (!containersDecisions.isEmpty()) {
			processContainerDecisions(containersDecisions);
		}
		else {
			log.info("No service decisions to process");
		}
	}

	private void restoreCrashedContainers(List<ContainerEntity> monitoringContainers, List<ContainerEntity> synchronizedContainers) {
		monitoringContainers.parallelStream()
			.filter(container -> synchronizedContainers.stream().noneMatch(c -> Objects.equals(c.getContainerId(), container.getContainerId())))
			.forEach(this::restartContainerCloseTo);
	}

	// Restarts the container on a host close to where it used to be running
	private void restartContainerCloseTo(ContainerEntity container) {
		String containerId = container.getContainerId();
		log.info("Recovering crashed container {} = {}", container.getServiceName(), containerId);
		Gson gson = new Gson();
		String previousRecovery = container.getLabels().get(ContainerConstants.Label.RECOVERY);
		List<ContainerRecovery> recoveries = new ArrayList<>();
		if (previousRecovery != null) {
			recoveries.addAll(Arrays.asList(gson.fromJson(previousRecovery, ContainerRecovery[].class)));
		}
		recoveries.add(new ContainerRecovery(containerId, System.currentTimeMillis()));
		if (shouldStopContainerRecovering(recoveries)) {
			log.info("Stopping recovery of crashed container {} {}... crashing too many times", container.getServiceName(), containerId);
			return;
		}
		Coordinates coordinates = container.getCoordinates();
		String serviceName = container.getServiceName();
		ServiceEntity service = servicesService.getService(serviceName);
		double expectedMemoryConsumption = service.getExpectedMemoryConsumption();
		HostAddress hostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
		Map<String, String> labels = Map.of(
			ContainerConstants.Label.RECOVERY, gson.toJson(recoveries)
		);
		containersService.launchContainer(hostAddress, serviceName, Collections.emptyList(), labels);
	}

	private boolean shouldStopContainerRecovering(List<ContainerRecovery> recoveries) {
		int count = 0;
		long currentTimestamp = System.currentTimeMillis();
		for (ContainerRecovery recovery : recoveries) {
			if (recovery.getTimestamp() + STOP_CONTAINER_RECOVERY_TIME_FRAME < currentTimestamp) {
				count++;
			}
		}
		return count >= STOP_CONTAINER_RECOVERY_ON_FAILURES;
	}

	// Restarts the container on the same host
	private void restartContainer(ContainerEntity container) {
		HostAddress hostAddress = container.getHostAddress();
		String serviceName = container.getServiceName();
		containersService.launchContainer(hostAddress, serviceName);
	}

	private ServiceDecisionResult runRules(HostAddress hostAddress, String containerId, String serviceName, Map<String, Double> newFields) {

		ContainerEvent containerEvent = new ContainerEvent(containerId, serviceName);
		Map<String, Double> containerEventFields = containerEvent.getFields();

		getContainerMonitoring(containerId)
			.stream()
			.filter(loggedField -> loggedField.getCount() >= CONTAINER_MINIMUM_LOGS_COUNT && newFields.get(loggedField.getField()) != null)
			.forEach(loggedField -> {
				String field = loggedField.getField();
				Double newValue = newFields.get(field);
				containerEventFields.put(field + "-effective-val", newValue);
				double average = loggedField.getSumValue() / loggedField.getCount();
				containerEventFields.put(field + "-avg-val", average);
				double deviationFromAverageValue = ((newValue - average) / average) * 100;
				containerEventFields.put(field + "-deviation-%-on-avg-val", deviationFromAverageValue);
				double lastValue = loggedField.getLastValue();
				double deviationFromLastValue = ((newValue - lastValue) / lastValue) * 100;
				containerEventFields.put(field + "-deviation-%-on-last-val", deviationFromLastValue);
			});

		return serviceRulesService.processServiceEvent(hostAddress, containerEvent);
	}

	private void processContainerDecisions(Map<String, List<ServiceDecisionResult>> servicesDecisions) {
		log.info("Processing container decisions...");
		Map<String, List<ServiceDecisionResult>> decisions = new HashMap<>();
		for (List<ServiceDecisionResult> serviceDecisions : servicesDecisions.values()) {
			for (ServiceDecisionResult containerDecision : serviceDecisions) {
				String serviceName = containerDecision.getServiceName();
				String containerId = containerDecision.getContainerId();
				RuleDecision decision = containerDecision.getDecision();
				log.info("Service {} on container {} had decision {}", serviceName, containerId, decision);
				ServiceEventEntity serviceEvent =
					servicesEventsService.saveServiceEvent(containerId, serviceName, decision.toString());
				int serviceEventCount = serviceEvent.getCount();
				if (decision == RuleDecision.STOP && serviceEventCount >= stopContainerOnEventCount
					|| decision == RuleDecision.REPLICATE && serviceEventCount >= replicateContainerOnEventCount
					|| decision == RuleDecision.MIGRATE && serviceEventCount >= migrateContainerOnEventCount) {
					List<ServiceDecisionResult> decisionsList = decisions.get(serviceName);
					if (decisionsList != null) {
						decisionsList.add(containerDecision);
					}
					else {
						decisionsList = new ArrayList<>(List.of(containerDecision));
						decisions.put(serviceName, decisionsList);
					}
				}
			}
		}
		if (!decisions.isEmpty()) {
			Map<String, Integer> replicasCount = servicesDecisions.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
			executeDecisions(decisions, replicasCount);
		}
	}

	private void executeDecisions(Map<String, List<ServiceDecisionResult>> decisions, Map<String, Integer> replicasCount) {
		Map<String, Coordinates> serviceWeightedMiddlePoint = requestLocationMonitoringService.getServicesWeightedMiddlePoint();
		for (Entry<String, List<ServiceDecisionResult>> servicesDecisions : decisions.entrySet()) {
			String serviceName = servicesDecisions.getKey();
			List<ServiceDecisionResult> containerDecisions = servicesDecisions.getValue();
			Collections.sort(containerDecisions);
			ServiceDecisionResult topPriorityDecisionResult = containerDecisions.get(0);
			int currentReplicas = replicasCount.get(serviceName);
			int minimumReplicas = servicesService.getMinimumReplicasByServiceName(serviceName);
			int maximumReplicas = servicesService.getMaximumReplicasByServiceName(serviceName);
			if (currentReplicas < minimumReplicas) {
				// start a new container to meet the requirements. The location is based on the data collected from the
				// location-request-monitor component
				Coordinates coordinates = serviceWeightedMiddlePoint.get(serviceName);
				if (coordinates == null) {
					coordinates = topPriorityDecisionResult.getHostAddress().getCoordinates();
				}
				ServiceEntity service = servicesService.getService(serviceName);
				double expectedMemoryConsumption = service.getExpectedMemoryConsumption();
				HostAddress hostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
				log.info("Service {} has too few replicas ({}/{}). Starting another container close to {}",
					serviceName, currentReplicas, minimumReplicas, hostAddress);
				containersService.launchContainer(hostAddress, serviceName);
			}
			else {
				RuleDecision topPriorityDecision = topPriorityDecisionResult.getDecision();
				if (topPriorityDecision == RuleDecision.REPLICATE) {
					if (maximumReplicas == 0 || currentReplicas < maximumReplicas) {
						String containerId = topPriorityDecisionResult.getContainerId();
						String decision = topPriorityDecisionResult.getDecision().name();
						long ruleId = topPriorityDecisionResult.getRuleId();
						Map<String, Double> fields = topPriorityDecisionResult.getFields();
						Coordinates coordinates = serviceWeightedMiddlePoint.get(serviceName);
						if (coordinates == null) {
							coordinates = topPriorityDecisionResult.getHostAddress().getCoordinates();
						}
						double expectedMemoryConsumption = servicesService.getService(serviceName).getExpectedMemoryConsumption();
						HostAddress toHostAddress = hostsService.getClosestCapableHost(expectedMemoryConsumption, coordinates);
						String replicatedContainerId = containersService.replicateContainer(containerId, toHostAddress).getContainerId();
						String result = String.format("replicated container %s of service %s to container %s on %s",
							containerId, serviceName, replicatedContainerId, toHostAddress);
						saveServiceDecision(containerId, serviceName, decision, ruleId, fields, result);
					}
				}
				else if (topPriorityDecision == RuleDecision.STOP) {
					if (currentReplicas > minimumReplicas) {
						ServiceDecisionResult leastPriorityContainer = containerDecisions.get(containerDecisions.size() - 1);
						String containerId = leastPriorityContainer.getContainerId();
						String decision = leastPriorityContainer.getDecision().name();
						long ruleId = leastPriorityContainer.getRuleId();
						HostAddress hostAddress = leastPriorityContainer.getHostAddress();
						Map<String, Double> fields = leastPriorityContainer.getFields();
						containersService.stopContainer(containerId);
						String result = String.format("stopped container %s of service %s on host %s", containerId, serviceName, hostAddress);
						saveServiceDecision(containerId, serviceName, decision, ruleId, fields, result);
					}
				}
			}
		}
	}

	private void saveServiceDecision(String containerId, String serviceName, String decision, long ruleId, Map<String, Double> fields, String result) {
		log.info("Executed decision: {}", result);
		servicesEventsService.resetServiceEvent(serviceName);
		ServiceDecisionEntity serviceDecision = decisionsService.addServiceDecision(containerId, serviceName, decision, ruleId, result);
		decisionsService.addServiceDecisionValueFromFields(serviceDecision, fields);
	}

	public void stopServiceMonitoring() {
		if (serviceMonitoringTimer != null) {
			serviceMonitoringTimer.cancel();
			log.info("Stopped service monitoring");
		}
	}

	public void reset() {
		log.info("Clearing all service monitoring");
		servicesMonitoring.deleteAll();
	}
}
