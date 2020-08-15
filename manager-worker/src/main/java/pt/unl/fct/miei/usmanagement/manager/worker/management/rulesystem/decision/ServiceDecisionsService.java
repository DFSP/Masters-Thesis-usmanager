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
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
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

  private final DecisionRepository decisions;
  private final ServiceDecisionRepository serviceDecisions;
  private final ServiceDecisionValueRepository serviceDecisionValues;

  private final int replicateContainerOnEventCount;
  private final int migrateContainerOnEventCount;
  private final int stopContainerOnEventCount;

  public ServiceDecisionsService(ServiceRulesService serviceRulesService, FieldsService fieldsService,
                                 ServicesEventsService servicesEventsService, ServicesService servicesService,
                                 ContainersService containersService, DecisionRepository decisions,
                                 ServiceDecisionRepository serviceDecisions,
                                 ServiceDecisionValueRepository serviceDecisionValues,
                                 DecisionProperties decisionProperties) {
    this.serviceRulesService = serviceRulesService;
    this.fieldsService = fieldsService;
    this.servicesEventsService = servicesEventsService;
    this.servicesService = servicesService;
    this.containersService = containersService;
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

  public DecisionEntity getDecision(String decisionName) {
    RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
    return decisions.findByDecisionAndComponentTypeType(decision, ComponentType.SERVICE).orElseThrow(() ->
        new EntityNotFoundException(DecisionEntity.class, "decision", decisionName));
  }

  public DecisionEntity getDecision(Long id) {
    return decisions.findById(id).orElseThrow(() ->
        new EntityNotFoundException(DecisionEntity.class, "id", id.toString()));
  }


  public ServiceDecisionEntity addServiceDecision(String containerId, String serviceName,
                                                  String decisionName, long ruleId, String otherInfo) {
    ServiceRuleEntity rule = serviceRulesService.getRule(ruleId);
    DecisionEntity decision = getDecision(decisionName);
    ServiceDecisionEntity serviceDecision = ServiceDecisionEntity.builder().containerId(containerId)
        .serviceName(serviceName).otherInfo(otherInfo).rule(rule).decision(decision).build();
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

  private void saveServiceDecision(String hostname, HostDetails host, ServiceDecisionResult containerDecision) {
    servicesEventsService.resetServiceEvent(containerDecision.getServiceName());
    ServiceDecisionEntity serviceDecision = addServiceDecision(containerDecision.getContainerId(),
        containerDecision.getServiceName(), containerDecision.getDecision().name(), containerDecision.getRuleId(),
        String.format("RuleDecision on host: %s (%s_%s_%s)", hostname, host.getMachineLocation().getRegion(),
            host.getMachineLocation().getCountry(), host.getMachineLocation().getCity()));
    addDecisionForFields(serviceDecision, containerDecision.getFields());
  }

  public void processDecisions(Map<ContainerEntity, Map<String, Double>> servicesMonitoring) {
    var servicesDecisions = new HashMap<String, List<ServiceDecisionResult>>();
    for (Map.Entry<ContainerEntity, Map<String, Double>> serviceFields : servicesMonitoring.entrySet()) {
      ContainerEntity container = serviceFields.getKey();
      String serviceName = container.getLabels().get(ContainerConstants.Label.SERVICE_NAME);
      String containerId = container.getContainerId();
      String hostname = container.getHostname();
      Map<String, Double> fields = serviceFields.getValue();
      ServiceDecisionResult serviceDecisionResult = serviceRulesService.runRules(hostname, containerId, serviceName,
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
      processRelevantServiceDecisions(relevantServicesDecisions);
    }
  }

  private void processRelevantServiceDecisions(Map<String, List<ServiceDecisionResult>> relevantServicesDecisions) {
    for (Map.Entry<String, List<ServiceDecisionResult>> servicesDecisions : relevantServicesDecisions.entrySet()) {

      String serviceName = servicesDecisions.getKey();
      List<ServiceDecisionResult> containerDecisions = servicesDecisions.getValue();
      Collections.sort(containerDecisions);
      List<ServiceDecisionResult> relevantContainerDecisions =
          relevantServicesDecisions.getOrDefault(serviceName, new ArrayList<>());
      int currentReplicas = containerDecisions.size();
      int minimumReplicas = servicesService.getMinReplicasByServiceName(serviceName);
      int maximumReplicas = servicesService.getMaxReplicasByServiceName(serviceName);
      if (currentReplicas < minimumReplicas) {
        containersService.startContainer(containerDecisions, relevantContainerDecisions);
      } else if (!relevantContainerDecisions.isEmpty()) {
        Collections.sort(relevantContainerDecisions);
        ServiceDecisionResult topPriorityDecisionResult = relevantContainerDecisions.get(0);
        RuleDecision topPriorityDecision = topPriorityDecisionResult.getDecision();
        if (topPriorityDecision == RuleDecision.REPLICATE) {
          if (maximumReplicas == 0 || currentReplicas < maximumReplicas) {
            containersService.startContainer(topPriorityDecisionResult);
          }
        } else if (topPriorityDecision == RuleDecision.STOP) {
          if (currentReplicas > minimumReplicas) {
            final var leastPriorityContainer = relevantContainerDecisions.get(relevantContainerDecisions.size() - 1);
            containersService.stopContainer(leastPriorityContainer);
          }
        }
      }
    }
  }

}

