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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionValueEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.HostDecisionValueRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.rulesystem.rules.HostRulesService;

@Slf4j
@Service
public class HostDecisionsService {

  private final HostRulesService hostRulesService;
  private final HostsEventsService hostsEventsService;
  private final HostsService hostsService;
  private final FieldsService fieldsService;

  private final DecisionRepository decisions;
  private final HostDecisionRepository hostDecisions;
  private final HostDecisionValueRepository hostDecisionValues;

  private final int startHostOnEventsCount;
  private final int stopHostOnEventsCount;
  private final int maximumHosts;
  private final int minimumHosts;

  public HostDecisionsService(HostRulesService hostRulesService, HostsEventsService hostsEventsService,
                              HostsService hostsService, FieldsService fieldsService, DecisionRepository decisions,
                              HostDecisionRepository hostDecisions, HostDecisionValueRepository hostDecisionValues,
                              DecisionProperties decisionProperties, HostProperties hostProperties) {
    this.hostRulesService = hostRulesService;
    this.hostsEventsService = hostsEventsService;
    this.hostsService = hostsService;
    this.fieldsService = fieldsService;
    this.decisions = decisions;
    this.hostDecisions = hostDecisions;
    this.hostDecisionValues = hostDecisionValues;
    this.startHostOnEventsCount = decisionProperties.getStartHostOnEventsCount();
    this.stopHostOnEventsCount = decisionProperties.getStopHostOnEventsCount();
    this.maximumHosts = hostProperties.getMaximumHosts();
    this.minimumHosts = hostProperties.getMinimumHosts();
  }


  public List<DecisionEntity> getDecisions() {
    return decisions.findByComponentTypeType(ComponentType.HOST);
  }

  public DecisionEntity getDecision(Long id) {
    return decisions.findById(id).orElseThrow(() ->
        new EntityNotFoundException(DecisionEntity.class, "id", id.toString()));
  }

  public DecisionEntity getDecision(String decisionName) {
    RuleDecision decision = RuleDecision.valueOf(decisionName.toUpperCase());
    return decisions.findByDecisionAndComponentTypeType(decision, ComponentType.HOST).orElseThrow(() ->
        new EntityNotFoundException(DecisionEntity.class, "decisionName", decisionName));
  }

  public HostDecisionEntity addDecision(String hostname, String decisionName, long ruleId) {
    HostRuleEntity rule = hostRulesService.getRule(ruleId);
    DecisionEntity decision = getDecision(decisionName);
    HostDecisionEntity hostDecision = HostDecisionEntity.builder().hostname(hostname).rule(rule).decision(decision)
        .build();
    return hostDecisions.save(hostDecision);
  }

  public void addDecisionForFields(HostDecisionEntity hostDecision, Map<String, Double> fields) {
    hostDecisionValues.saveAll(
        fields.entrySet().stream()
            .filter(field -> field.getKey().contains("effective-val"))
            .map(field ->
                HostDecisionValueEntity.builder().hostDecision(hostDecision)
                    .field(fieldsService.getField(field.getKey().split("-effective-val")[0]))
                    .value(field.getValue()).build())
            .collect(Collectors.toList()));
  }


  public List<HostDecisionEntity> getDecisionsByHost(String hostname) {
    return hostDecisions.findByHostname(hostname);
  }

  public void processDecisions(Map<String, Map<String, Double>> hostsMonitoring) {
    var hostsDecisions = new LinkedList<HostDecisionResult>();
    for (Map.Entry<String, Map<String, Double>> hostFields : hostsMonitoring.entrySet()) {
      String hostname = hostFields.getKey();
      Map<String, Double> fields = hostFields.getValue();
      HostDecisionResult hostDecisionResult = hostRulesService.runHostRules(hostname, fields);
      hostsDecisions.add(hostDecisionResult);
    }
    log.info("Processing host decisions...");
    var relevantHostDecisions = new LinkedList<HostDecisionResult>();
    for (HostDecisionResult hostDecision : hostsDecisions) {
      String hostname = hostDecision.getHostname();
      RuleDecision decision = hostDecision.getDecision();
      log.info("Hostname '{}' had decision '{}'", hostname, decision);
      HostEventEntity hostEvent = hostsEventsService.saveHostEvent(hostname, decision.toString());
      int hostEventCount = hostEvent.getCount();
      if ((decision == RuleDecision.START && hostEventCount >= startHostOnEventsCount)
          || (decision == RuleDecision.STOP && hostEventCount >= stopHostOnEventsCount)) {
        relevantHostDecisions.add(hostDecision);
        HostDecisionEntity hostDecisionEntity = addDecision(hostDecision.getHostname(),
            hostDecision.getDecision().name(), hostDecision.getRuleId());
        this.addDecisionForFields(hostDecisionEntity, hostDecision.getFields());
      }
    }
    if (!relevantHostDecisions.isEmpty()) {
      processRelevantDecisions(relevantHostDecisions, hostsMonitoring.size());
    }
  }

  private void processRelevantDecisions(List<HostDecisionResult> relevantHostDecisions, int hostsNumber) {
    Collections.sort(relevantHostDecisions);
    HostDecisionResult topPriorityHostDecision = relevantHostDecisions.get(0);
    RuleDecision decision = topPriorityHostDecision.getDecision();
    if (decision == RuleDecision.START) {
      if (maximumHosts <= 0 || hostsNumber < maximumHosts) {
        String hostname = topPriorityHostDecision.getHostname();
        hostsService.startHostCloseTo(hostname);
      }
    } else if (decision == RuleDecision.STOP) {
      if (hostsNumber > minimumHosts) {
        // reduce skips all elements but the last, so we get the host with least priority
        relevantHostDecisions.stream()
            .filter(d -> !hostsService.isLocalhost(d.getHostname())).reduce((first, second) -> second)
            .map(DecisionResult::getHostname).ifPresent(hostsService::stopHost);
      }
    }
  }

}

