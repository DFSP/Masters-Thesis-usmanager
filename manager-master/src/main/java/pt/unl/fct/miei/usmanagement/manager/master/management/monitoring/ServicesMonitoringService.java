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

import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.CpuStats;
import com.spotify.docker.client.messages.MemoryStats;
import com.spotify.docker.client.messages.NetworkStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ContainerFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.master.ManagerMasterProperties;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.master.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.location.LocationRequestService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.containers.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.decision.ServiceDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.master.management.services.ServicesService;

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
                                   ManagerMasterProperties managerMasterProperties) {
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
    this.isTestEnable = managerMasterProperties.getTests().isEnabled();
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
    } else {
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
        } catch (MasterManagerException e) {
          log.error(e.getMessage());
        }
      }
    }, monitorPeriod, monitorPeriod);
  }

  private void monitorServicesTask(int secondsFromLastRun) {
    log.info("Starting service monitoring task...");
    var servicesDecisions = new HashMap<String, List<ServiceDecisionResult>>();
    List<ContainerEntity> containers = containersService.getAppContainers();
    if (isTestEnable) {
      List<ContainerEntity> managerMasterContainers = containersService.getContainersWithLabels(
          Set.of(Pair.of(ContainerConstants.Label.SERVICE_NAME, ManagerMasterProperties.MANAGER_MASTER)));
      // TODO include MANAGER_WORKERS too
      containers.addAll(managerMasterContainers);
    }
    for (ContainerEntity container : containers) {
      log.info("On {}", container);
      String containerId = container.getContainerId();
      String serviceName = container.getLabels().get(ContainerConstants.Label.SERVICE_NAME);
      String serviceHostname = container.getHostname();
      Map<String, Double> newFields = getContainerStats(container, secondsFromLastRun);
      newFields.forEach((field, value) -> {
        saveServiceMonitoring(containerId, serviceName, field, value);
      });
      for (AppEntity app : servicesService.getApps(serviceName)) {
        String appName = app.getName();
        ServiceDecisionResult serviceDecisionResult = runAppRules(appName, serviceHostname, containerId,
            serviceName, newFields);
        var serviceDecisions = servicesDecisions.get(serviceName);
        if (serviceDecisions != null) {
          serviceDecisions.add(serviceDecisionResult);
        } else {
          serviceDecisions = new LinkedList<>(List.of(serviceDecisionResult));
          servicesDecisions.put(serviceName, serviceDecisions);
        }
      }
    }
    processContainerDecisions(servicesDecisions, secondsFromLastRun);
  }

  private ServiceDecisionResult runAppRules(String appName, String serviceHostname, String containerId,
                                            String serviceName, Map<String, Double> newFields) {
    List<ServiceMonitoringEntity> loggedFields = getContainerMonitoring(containerId);
    var containerEvent = new ContainerEvent(containerId, serviceName);
    Map<String, Double> containerEventFields = containerEvent.getFields();
    for (var loggedField : loggedFields) {
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
    return containerEventFields.isEmpty()
        ? new ServiceDecisionResult(serviceHostname, containerId, serviceName)
        : serviceRulesService.processServiceEvent(appName, serviceHostname, containerEvent);
  }

  private void processContainerDecisions(Map<String, List<ServiceDecisionResult>> servicesDecisions,
                                         int secondsFromLastRun) {
    log.info("Processing container decisions...");
    var relevantServicesDecisions = new HashMap<String, List<ServiceDecisionResult>>();
    for (List<ServiceDecisionResult> serviceDecisions : servicesDecisions.values()) {
      for (ServiceDecisionResult containerDecision : serviceDecisions) {
        String serviceName = containerDecision.getServiceName();
        String containerId = containerDecision.getContainerId();
        RuleDecision decision = containerDecision.getDecision();
        log.info("ServiceName '{}' on containerId '{}' had decision '{}'", serviceName, containerId, decision);
        ServiceEventEntity serviceEvent =
            servicesEventsService.saveServiceEvent(containerId, serviceName, decision.toString());
        int serviceEventCount = serviceEvent.getCount();
        if (decision == RuleDecision.STOP && serviceEventCount >= stopContainerOnEventCount
            || decision == RuleDecision.REPLICATE && serviceEventCount >= replicateContainerOnEventCount
            || decision == RuleDecision.MIGRATE && serviceEventCount >= migrateContainerOnEventCount) {
          List<ServiceDecisionResult> relevantServiceDecisions = relevantServicesDecisions.get(serviceName);
          if (relevantServiceDecisions != null) {
            relevantServiceDecisions.add(containerDecision);
          } else {
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
    Map<String, HostDetails> servicesLocationsRegions =
        requestLocationMonitoringService.getBestLocationToStartServices(allServicesDecisions, secondsFromLastRun);
    for (Entry<String, List<ServiceDecisionResult>> servicesDecisions : allServicesDecisions.entrySet()) {
      String serviceName = servicesDecisions.getKey();
      List<ServiceDecisionResult> containerDecisions = servicesDecisions.getValue();
      List<ServiceDecisionResult> relevantContainerDecisions =
          relevantServicesDecisions.getOrDefault(serviceName, new ArrayList<>());
      int currentReplicas = containerDecisions.size();
      int minimumReplicas = servicesService.getMinReplicasByServiceName(serviceName);
      int maximumReplicas = servicesService.getMaxReplicasByServiceName(serviceName);
      if (currentReplicas < minimumReplicas) {
        startContainer(containerDecisions, relevantContainerDecisions, servicesLocationsRegions);
      } else if (!relevantContainerDecisions.isEmpty()) {
        Collections.sort(relevantContainerDecisions);
        ServiceDecisionResult topPriorityDecisionResult = relevantContainerDecisions.get(0);
        RuleDecision topPriorityDecision = topPriorityDecisionResult.getDecision();
        if (topPriorityDecision == RuleDecision.REPLICATE) {
          if (maximumReplicas == 0 || currentReplicas < maximumReplicas) {
            startContainer(topPriorityDecisionResult, servicesLocationsRegions);
          }
        } else if (topPriorityDecision == RuleDecision.STOP) {
          if (currentReplicas > minimumReplicas) {
            final var leastPriorityContainer = relevantContainerDecisions.get(relevantContainerDecisions.size() - 1);
            stopContainer(leastPriorityContainer);
          }
        }
      }
    }
  }

  private void startContainer(List<ServiceDecisionResult> allContainersDecisions,
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
    containerDecision.ifPresent(serviceDecisionResult ->
        startContainer(serviceDecisionResult, servicesLocationsRegions));
  }

  private void startContainer(ServiceDecisionResult topPriorityContainerDecision,
                              Map<String, HostDetails> servicesLocationsRegions) {
    String containerId = topPriorityContainerDecision.getContainerId();
    String hostname = topPriorityContainerDecision.getHostname();
    String serviceName = topPriorityContainerDecision.getServiceName();
    final HostDetails startLocation;
    if (servicesLocationsRegions.containsKey(serviceName)) {
      startLocation = servicesLocationsRegions.get(serviceName);
      log.info("Starting service '{}'. Location from request-location-monitor: '{}' ({})",
          serviceName, hostname, startLocation.getMachineLocation().getRegion());
    } else {
      startLocation = hostsService.getHostDetails(hostname);
      log.info("Starting service '{}'. Location: '{}' ({})",
          serviceName, hostname, startLocation.getMachineLocation().getRegion());
    }
    double serviceAvgMem = servicesService.getService(serviceName).getExpectedMemoryConsumption();
    String toHostname = hostsService.getAvailableHost(serviceAvgMem, startLocation);
    ContainerEntity replicatedContainer = containersService.replicateContainer(containerId, toHostname);
    HostDetails selectedHostDetails = hostsService.getHostDetails(toHostname);
    log.info("RuleDecision executed: Replicated container '{}' of service '{}' to container '{}' "
            + "on host '{} ({}_{}_{})'", containerId, serviceName, replicatedContainer.getId(), toHostname,
        selectedHostDetails.getMachineLocation().getRegion(), selectedHostDetails.getMachineLocation().getCountry(),
        selectedHostDetails.getMachineLocation().getCity());
    /*if (selectedHostDetails instanceof EdgeHostDetails) {
      final var edgeHostDetails = (EdgeHostDetails) selectedHostDetails;
      log.info("RuleDecision executed: Replicated container '{}' of service '{}' to container '{}' " +
              "on edge host '{} ({}_{}_{})'",
          containerId, serviceName, replicatedContainerId, toHostname, edgeHostDetails.getRegion(),
          edgeHostDetails.getCountry(), edgeHostDetails.getCity());
    } else if (selectedHostDetails instanceof AwsHostDetails) {
      final var awsHostDetails = (AwsHostDetails) selectedHostDetails;
      log.info("RuleDecision executed: Replicated container '{}' of service '{}' to  container '{}' " +
              "on aws host '{} ({})'",
          containerId, serviceName, replicatedContainerId, toHostname, awsHostDetails.getRegion());
    }*/
    saveServiceDecision(hostname, selectedHostDetails, topPriorityContainerDecision);
  }

  private void stopContainer(ServiceDecisionResult leastPriorityContainerDecision) {
    String containerId = leastPriorityContainerDecision.getContainerId();
    String hostname = leastPriorityContainerDecision.getHostname();
    String serviceName = leastPriorityContainerDecision.getServiceName();
    containersService.stopContainer(containerId);
    HostDetails selectedHostDetails = hostsService.getHostDetails(hostname);
    log.info("RuleDecision executed: Stopped container '{}' of service '{}' on edge host '{} ({}_{}_{})'",
        containerId, serviceName, hostname, selectedHostDetails.getMachineLocation().getRegion(),
        selectedHostDetails.getMachineLocation().getCountry(),
        selectedHostDetails.getMachineLocation().getCity());
    /*if (selectedHostDetails instanceof EdgeHostDetails) {
      final var edgeHostDetails = (EdgeHostDetails) selectedHostDetails;
      log.info("RuleDecision executed: Stopped container '{}' of service '{}' on edge host '{} ({}_{}_{})'",
          containerId, serviceName, hostname, edgeHostDetails.getRegion(), edgeHostDetails.getCountry(),
          edgeHostDetails.getCity());
    } else if (selectedHostDetails instanceof AwsHostDetails) {
      final var awsHostDetails = (AwsHostDetails) selectedHostDetails;
      log.info("RuleDecision executed: Stopped container '{}' of service '{}' on aws host '{} ({})'",
          containerId, serviceName, hostname, awsHostDetails.getRegion());
    }*/
    saveServiceDecision(hostname, selectedHostDetails, leastPriorityContainerDecision);
  }

  private void saveServiceDecision(String hostname, HostDetails host, ServiceDecisionResult containerDecision) {
    servicesEventsService.resetServiceEvent(containerDecision.getServiceName());
    /*if (host instanceof EdgeHostDetails) {
      final var edgeHostDetails = (EdgeHostDetails)host;
      otherInfo = String.format("RuleDecision on Edge host: %s (%s_%s_%s)", hostname,
          edgeHostDetails.getRegion(), edgeHostDetails.getCountry(), edgeHostDetails.getCity());
    } else if (host instanceof AwsHostDetails) {
      final var awsHostDetails = (AwsHostDetails)host;
      otherInfo = String.format("RuleDecision on Aws host: %s (%s)", hostname, awsHostDetails.getRegion());
    }*/
    ServiceDecisionEntity serviceDecision =
        decisionsService.addServiceDecision(containerDecision.getContainerId(), containerDecision.getServiceName(),
            containerDecision.getDecision().name(), containerDecision.getRuleId(),
            String.format("RuleDecision on host: %s (%s_%s_%s)",
                hostname, host.getMachineLocation().getRegion(), host.getMachineLocation().getCountry(),
                host.getMachineLocation().getCity()));
    decisionsService.addServiceDecisionValueFromFields(serviceDecision, containerDecision.getFields());
  }

  Map<String, Double> getContainerStats(ContainerEntity container, double secondsInterval) {
    String containerId = container.getContainerId();
    String containerHostname = container.getHostname();
    String containerName = container.getNames().get(0);
    String serviceName = container.getLabels().getOrDefault(ContainerConstants.Label.SERVICE_NAME, containerName);
    ContainerStats containerStats = containersService.getContainerStats(containerId, containerHostname);
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
    final var fields = new HashMap<>(Map.of(
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
    var systemDelta = cpuStats.systemCpuUsage().doubleValue() - preCpuStats.systemCpuUsage().doubleValue();
    var cpuDelta = cpuStats.cpuUsage().totalUsage().doubleValue() - preCpuStats.cpuUsage().totalUsage().doubleValue();
    double cpuPercent = 0.0;
    if (systemDelta > 0.0 && cpuDelta > 0.0) {
      final double onlineCpus = cpuStats.cpuUsage().percpuUsage().stream().filter(cpuUsage -> cpuUsage >= 1).count();
      assert onlineCpus == getOnlineCpus(cpuStats.cpuUsage().percpuUsage());
      cpuPercent = (cpuDelta / systemDelta) * onlineCpus * 100.0;
    }
    return cpuPercent;
  }

  //TODO apagar
  private int getOnlineCpus(List<Long> perCpuUsage) {
    var count = 0;
    for (Long cpuUsage : perCpuUsage) {
      if (cpuUsage < 1) {
        break;
      }
      count++;
    }
    return count;
  }

  private double getContainerRamPercent(MemoryStats memStats) {
    return memStats.limit() < 1 ? 0.0 : (memStats.usage().doubleValue() / memStats.limit().doubleValue()) * 100.0;
  }

}
