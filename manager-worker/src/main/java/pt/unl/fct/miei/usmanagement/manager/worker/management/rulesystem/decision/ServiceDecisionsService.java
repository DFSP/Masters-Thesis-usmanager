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

package pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.decision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionValueEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionValueRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.worker.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.location.LocationRequestService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.services.ServicesService;

@Slf4j
@Service
public class ServiceDecisionsService {

  private final ServiceRulesService serviceRulesService;
  private final FieldsService fieldsService;
  private final ServicesEventsService servicesEventsService;
  private final ServicesService servicesService;
  private final ContainersService containersService;
  private final LocationRequestService requestLocationMonitoringService;
  private final HostsService hostsService;

  private final DecisionRepository decisions;
  private final ServiceDecisionRepository serviceDecisions;
  private final ServiceDecisionValueRepository serviceDecisionValues;

  private final int replicateContainerOnEventCount;
  private final int migrateContainerOnEventCount;
  private final int stopContainerOnEventCount;

  public ServiceDecisionsService(ServiceRulesService serviceRulesService, FieldsService fieldsService,
                                 ServicesEventsService servicesEventsService, ServicesService servicesService,
                                 ContainersService containersService,
                                 LocationRequestService requestLocationMonitoringService, HostsService hostsService,
                                 DecisionRepository decisions, ServiceDecisionRepository serviceDecisions,
                                 ServiceDecisionValueRepository serviceDecisionValues,
                                 DecisionProperties decisionProperties) {
    this.serviceRulesService = serviceRulesService;
    this.fieldsService = fieldsService;
    this.servicesEventsService = servicesEventsService;
    this.servicesService = servicesService;
    this.containersService = containersService;
    this.requestLocationMonitoringService = requestLocationMonitoringService;
    this.hostsService = hostsService;
    this.decisions = decisions;
    this.serviceDecisions = serviceDecisions;
    this.serviceDecisionValues = serviceDecisionValues;
    this.replicateContainerOnEventCount = decisionProperties.getReplicateContainerOnEventCount();
    this.migrateContainerOnEventCount = decisionProperties.getMigrateContainerOnEventCount();
    this.stopContainerOnEventCount = decisionProperties.getStopContainerOnEventCount();
  }

  public List<DecisionEntity> getDecisions() {
    return decisions.findByComponentTypeType(ComponentType.SERVICE);
  }

  public DecisionEntity getDecision(RuleDecision decision) {
    return decisions.findByRuleDecisionAndComponentTypeType(decision, ComponentType.SERVICE).orElseThrow(() ->
        new EntityNotFoundException(DecisionEntity.class, "decision", decision.name()));
  }

  public DecisionEntity getDecision(String decisionName) {
    RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
    return this.getDecision(decision);
  }

  public DecisionEntity getDecision(Long id) {
    return decisions.findById(id).orElseThrow(() ->
        new EntityNotFoundException(DecisionEntity.class, "id", id.toString()));
  }


  public ServiceDecisionEntity addServiceDecision(String containerId, String serviceName,
                                                  String decisionName, long ruleId, String result) {
    ServiceRuleEntity rule = serviceRulesService.getRule(ruleId);
    DecisionEntity decision = getDecision(decisionName);
    ServiceDecisionEntity serviceDecision = ServiceDecisionEntity.builder().containerId(containerId)
        .serviceName(serviceName).result(result).rule(rule).decision(decision).build();
    return serviceDecisions.save(serviceDecision);
  }

  public void addDecisionForFields(ServiceDecisionEntity serviceDecision, Map<String, Double> fields) {
    serviceDecisionValues.saveAll(
        fields.entrySet().stream()
            .filter(field -> field.getKey().contains("effective-val"))
            .map(field ->
                ServiceDecisionValueEntity.builder().serviceDecision(serviceDecision)
                    .field(fieldsService.getField(field.getKey().split("-effective-val")[0]))
                    .value(field.getValue()).build())
            .collect(Collectors.toList()));
  }

  public List<ServiceDecisionEntity> getDecisionsByService(String serviceName) {
    return serviceDecisions.findByServiceName(serviceName);
  }

  private void saveServiceDecision(ServiceDecisionResult serviceDecision, String result) {
    String containerId = serviceDecision.getContainerId();
    String serviceName = serviceDecision.getServiceName();
    String decision = serviceDecision.getDecision().name();
    long ruleId = serviceDecision.getRuleId();
    servicesEventsService.resetServiceEvent(serviceName);
    ServiceDecisionEntity serviceDecisionEntity =
        this.addServiceDecision(containerId, serviceName, decision, ruleId, result);
    addDecisionForFields(serviceDecisionEntity, serviceDecision.getFields());
  }

  public void processDecisions(Map<ContainerEntity, Map<String, Double>> servicesMonitoring, int secondsFromLastRun) {
    var servicesDecisions = new HashMap<String, List<ServiceDecisionResult>>();
    for (Map.Entry<ContainerEntity, Map<String, Double>> serviceFields : servicesMonitoring.entrySet()) {
      ContainerEntity container = serviceFields.getKey();
      String serviceName = container.getLabels().get(ContainerConstants.Label.SERVICE_NAME);
      String containerId = container.getContainerId();
      String hostname = container.getHostname();
      Map<String, Double> fields = serviceFields.getValue();
      ServiceDecisionResult serviceDecisionResult = serviceRulesService.executeRules(hostname, containerId, serviceName,
          fields);
      List<ServiceDecisionResult> serviceDecisions = servicesDecisions.get(serviceName);
      if (serviceDecisions != null) {
        serviceDecisions.add(serviceDecisionResult);
      } else {
        serviceDecisions = new LinkedList<>(List.of(serviceDecisionResult));
        servicesDecisions.put(serviceName, serviceDecisions);
      }
    }
    log.info("Processing container decisions...");
    var relevantServicesDecisions = new HashMap<String, List<ServiceDecisionResult>>();
    for (List<ServiceDecisionResult> containerDecisions : servicesDecisions.values()) {
      for (ServiceDecisionResult containerDecision : containerDecisions) {
        String serviceName = containerDecision.getServiceName();
        String containerId = containerDecision.getContainerId();
        RuleDecision decision = containerDecision.getDecision();
        log.info("Service '{}' on container '{}' had decision '{}'", serviceName, containerId, decision);
        ServiceEventEntity serviceEvent =
            servicesEventsService.saveServiceEvent(containerId, serviceName, this.getDecision(decision.toString()));
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
      processRelevantServiceDecisions(servicesDecisions, relevantServicesDecisions, secondsFromLastRun);
    }
  }

  private void processRelevantServiceDecisions(Map<String, List<ServiceDecisionResult>> serviceDecisions,
                                               Map<String, List<ServiceDecisionResult>> relevantServicesDecisions,
                                               int secondsFromLastRun) {
    for (Map.Entry<String, List<ServiceDecisionResult>> servicesDecisions : serviceDecisions.entrySet()) {
      String serviceName = servicesDecisions.getKey();
      List<ServiceDecisionResult> containerDecisions = servicesDecisions.getValue();
      List<ServiceDecisionResult> relevantContainerDecisions =
          relevantServicesDecisions.getOrDefault(serviceName, new ArrayList<>());
      int currentReplicas = containerDecisions.size();
      int minimumReplicas = servicesService.getMinReplicasByServiceName(serviceName);
      int maximumReplicas = servicesService.getMaxReplicasByServiceName(serviceName);
      if (currentReplicas < minimumReplicas) {
        Optional<ServiceDecisionResult> containerDecision = Optional.empty();
        if (!relevantContainerDecisions.isEmpty()) {
          Collections.sort(relevantContainerDecisions);
          ServiceDecisionResult topPriorityContainerDecision = relevantContainerDecisions.get(0);
          if (topPriorityContainerDecision.getDecision() == RuleDecision.REPLICATE) {
            containerDecision = Optional.of(topPriorityContainerDecision);
          }
        }
        if (containerDecision.isEmpty()) {
          Collections.sort(containerDecisions);
          containerDecision = containerDecisions.stream().filter(d -> d.getDecision() == RuleDecision.REPLICATE)
              .findFirst()
              .or(() -> containerDecisions.stream().filter(d -> d.getDecision() == RuleDecision.NONE).findFirst());
        }
        containerDecision.ifPresent(decision -> {
          Map<String, HostDetails> servicesLocationsRegions =
              requestLocationMonitoringService.getBestLocationToStartServices(serviceDecisions, secondsFromLastRun);
          this.startContainerFromDecision(decision, servicesLocationsRegions);
        });
      } else if (!relevantContainerDecisions.isEmpty()) {
        Collections.sort(relevantContainerDecisions);
        ServiceDecisionResult topPriorityContainerDecision = relevantContainerDecisions.get(0);
        RuleDecision topPriorityDecision = topPriorityContainerDecision.getDecision();
        if (topPriorityDecision == RuleDecision.REPLICATE) {
          if (maximumReplicas == 0 || currentReplicas < maximumReplicas) {
            Map<String, HostDetails> servicesLocationsRegions =
                requestLocationMonitoringService.getBestLocationToStartServices(serviceDecisions, secondsFromLastRun);
            this.startContainerFromDecision(topPriorityContainerDecision, servicesLocationsRegions);
          }
        } else if (topPriorityDecision == RuleDecision.STOP) {
          // FIXME choose better container to stop
          if (currentReplicas > minimumReplicas) {
            ServiceDecisionResult leastPriorityContainerDecision =
                relevantContainerDecisions.get(relevantContainerDecisions.size() - 1);
            this.stopContainerFromDecision(leastPriorityContainerDecision);
          }
        }
      }
    }
  }

  private void startContainerFromDecision(ServiceDecisionResult serviceDecision,
                                          Map<String, HostDetails> servicesLocations) {
    String containerId = serviceDecision.getContainerId();
    String serviceName = serviceDecision.getServiceName();
    String hostname = serviceDecision.getHostname();
    HostLocation hostLocation;
    if (servicesLocations.containsKey(serviceName)) {
      hostLocation = servicesLocations.get(serviceName).getHostLocation();
      log.info("Starting container for service '{}'. Location from request-location-monitor: '{}' ({})", serviceName,
          hostname, hostLocation.getRegion());
    } else {
      hostLocation = hostsService.getHostDetails(hostname).getHostLocation();
      log.info("Starting container for service '{}'. Location: '{}' ({})", serviceName, hostname,
          hostLocation.getRegion());
    }
    double expectedMemoryConsumption = servicesService.getService(serviceName).getExpectedMemoryConsumption();
    HostDetails host = hostsService.getAvailableHost(expectedMemoryConsumption, hostLocation);
    String toHostname = host.getHostAddress().getPublicIpAddress();
    HostLocation selectedHost = host.getHostLocation();
    String replicatedContainerId = containersService.replicateContainer(containerId, toHostname).getContainerId();
    String result = String.format("Execution of rule decision for container %s (%s) on host %s: Replicated to "
            + "container %s at %s (%s_%s_%s)", containerId, serviceName, hostname, replicatedContainerId, toHostname,
        selectedHost.getRegion(), selectedHost.getCountry(), selectedHost.getCity());
    log.info(result);
    this.saveServiceDecision(serviceDecision, result);
  }

  private void stopContainerFromDecision(ServiceDecisionResult serviceDecision) {
    String containerId = serviceDecision.getContainerId();
    String hostname = serviceDecision.getHostname();
    String serviceName = serviceDecision.getServiceName();
    containersService.stopContainer(containerId);
    HostLocation selectedHost = hostsService.getHostDetails(hostname).getHostLocation();
    String result = String.format("Execution of rule decision for container %s (%s) on host %s (%s_%s_%s): Stopped "
            + "container ", containerId, serviceName, hostname, selectedHost.getRegion(), selectedHost.getCountry(),
        selectedHost.getCity());
    this.saveServiceDecision(serviceDecision, result);
  }

}

