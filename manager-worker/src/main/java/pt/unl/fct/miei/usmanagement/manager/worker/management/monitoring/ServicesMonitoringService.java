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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ContainerFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.worker.ManagerWorkerProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainerProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics.ContainerMetricsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.decision.ServiceDecisionsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.services.ServicesService;

@Service
@Slf4j
public class ServicesMonitoringService {

  private final ServiceMonitoringRepository servicesMonitoring;
  private final ServiceMonitoringLogsRepository serviceMonitoringLogs;

  private final ContainersService containersService;
  private final ServiceDecisionsService serviceDecisionsService;
  private final ContainerMetricsService containerMetricsService;

  private final long monitorPeriod;
  private final boolean isTestEnable;

  public ServicesMonitoringService(ServiceMonitoringRepository servicesMonitoring,
                                   ServiceMonitoringLogsRepository serviceMonitoringLogs,
                                   ContainersService containersService,
                                   ServiceDecisionsService serviceDecisionsService,
                                   ContainerMetricsService containerMetricsService,
                                   ContainerProperties containerProperties,
                                   ManagerWorkerProperties managerWorkerProperties) {
    this.serviceMonitoringLogs = serviceMonitoringLogs;
    this.servicesMonitoring = servicesMonitoring;
    this.containersService = containersService;
    this.serviceDecisionsService = serviceDecisionsService;
    this.containerMetricsService = containerMetricsService;
    this.monitorPeriod = containerProperties.getMonitorPeriod();
    this.isTestEnable = managerWorkerProperties.getTests().isEnabled();
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

  public void saveServiceMonitoring(String containerId, String serviceName, String hostname, String field,
                                    double value) {
    ServiceMonitoringEntity serviceMonitoring = getContainerMonitoring(containerId, field);
    Timestamp updateTime = Timestamp.from(Instant.now());
    if (serviceMonitoring == null) {
      serviceMonitoring = ServiceMonitoringEntity.builder().containerId(containerId).serviceName(serviceName)
          .hostname(hostname).field(field).minValue(value).maxValue(value).sumValue(value).lastValue(value).count(1)
          .lastUpdate(updateTime).build();
    } else {
      serviceMonitoring.logValue(value, updateTime);
    }
    servicesMonitoring.save(serviceMonitoring);
    if (isTestEnable) {
      ServiceMonitoringLogEntity serviceMonitoringLogEntity = ServiceMonitoringLogEntity.builder()
          .containerId(containerId).serviceName(serviceName).field(field).effectiveValue(value).build();
      serviceMonitoringLogs.save(serviceMonitoringLogEntity);
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
        int diffSeconds = (int) ((currRun - lastRun) / TimeUnit.SECONDS.toMillis(1));
        lastRun = currRun;
        try {
          monitorServicesTask(diffSeconds);
        } catch (WorkerManagerException e) {
          log.error(e.getMessage());
        }
      }
    }, monitorPeriod, monitorPeriod);
  }

  private void monitorServicesTask(int secondsFromLastRun) {
    log.info("Starting service monitoring task...");
    List<ContainerEntity> containers = containersService.getAppContainers();
    Map<ContainerEntity, Map<String, Double>> servicesMonitoring = new HashMap<>();
    for (ContainerEntity container : containers) {
      String containerId = container.getContainerId();
      String serviceName = container.getLabels().get(ContainerConstants.Label.SERVICE_NAME);
      String hostname = container.getHostname();
      Map<String, Double> fields = containerMetricsService.getContainerStats(container, secondsFromLastRun);
      fields.forEach((field, value) -> saveServiceMonitoring(containerId, serviceName, hostname, field, value));
      servicesMonitoring.put(container, fields);
    }
    serviceDecisionsService.processDecisions(servicesMonitoring, secondsFromLastRun);
  }

}
