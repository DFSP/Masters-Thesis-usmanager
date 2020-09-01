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
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringLogEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringLogsRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringRepository;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.ManagerWorkerProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.metrics.HostMetricsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.decision.HostDecisionsService;

@Slf4j
@Service
public class HostsMonitoringService {

  private final HostMonitoringRepository hostsMonitoring;
  private final HostMonitoringLogsRepository hostMonitoringLogs;

  private final NodesService nodesService;
  private final HostMetricsService hostMetricsService;
  private final HostDecisionsService hostDecisionsService;

  private final long monitorInterval;
  private final boolean isTestEnable;

  public HostsMonitoringService(HostMonitoringRepository hostsMonitoring,
                                HostMonitoringLogsRepository hostMonitoringLogs, NodesService nodesService,
                                HostMetricsService hostMetricsService, HostDecisionsService hostDecisionsService,
                                HostProperties hostProperties, ManagerWorkerProperties managerWorkerProperties) {
    this.hostsMonitoring = hostsMonitoring;
    this.hostMonitoringLogs = hostMonitoringLogs;
    this.nodesService = nodesService;
    this.hostMetricsService = hostMetricsService;
    this.hostDecisionsService = hostDecisionsService;
    this.monitorInterval = hostProperties.getMonitorPeriod();
    this.isTestEnable = managerWorkerProperties.getTests().isEnabled();
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

  public HostMonitoringEntity saveHostMonitoring(String hostname, String field, double value) {
    HostMonitoringEntity hostMonitoring = getHostMonitoring(hostname, field);
    Timestamp updateTime = Timestamp.from(Instant.now());
    if (hostMonitoring == null) {
      hostMonitoring = HostMonitoringEntity.builder().hostname(hostname).field(field).minValue(value).maxValue(value)
          .sumValue(value).lastValue(value).count(1).lastUpdate(updateTime).build();
    } else {
      hostMonitoring.logValue(value, updateTime);
    }
    hostMonitoring = hostsMonitoring.save(hostMonitoring);
    if (isTestEnable) {
      HostMonitoringLogEntity hostMonitoringLogEntity = HostMonitoringLogEntity.builder().hostname(hostname)
          .field(field).effectiveValue(value).build();
      hostMonitoringLogs.save(hostMonitoringLogEntity);
    }
    return hostMonitoring;
  }

  public List<HostFieldAvg> getHostFieldsAvg(String hostname) {
    return hostsMonitoring.getHostFieldsAvg(hostname);
  }

  public HostFieldAvg getHostFieldAvg(String hostname, String field) {
    return hostsMonitoring.getHostFieldAvg(hostname, field);
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
        } catch (WorkerManagerException e) {
          log.error(e.getMessage());
        }
      }
    }, monitorInterval, monitorInterval);
  }

  private void monitorHostsTask() {
    log.info("Starting host monitoring task...");
    nodesService.updateNodes();
    List<SimpleNode> nodes = nodesService.getReadyNodes();
    Map<String, Map<String, Double>> hostsMonitoring = new HashMap<>();
    for (SimpleNode node : nodes) {
      String hostname = node.getHostname();
      Map<String, Double> fields = hostMetricsService.getHostStats(hostname);
      fields.forEach((field, value) -> saveHostMonitoring(hostname, field, value));
      hostsMonitoring.put(hostname, fields);
    }
    hostDecisionsService.processDecisions(hostsMonitoring);
  }

}
