package pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.hosts;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.BadRequestException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.componenttypes.ComponentType;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.util.Validation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rules")
public class HostRulesController {
  
  private final HostRulesService hostRulesService;

  public HostRulesController(HostRulesService hostRulesService) {
    this.hostRulesService = hostRulesService;
  }

  @GetMapping("/hosts")
  public List<HostRuleEntity> getHostRules() {
    return hostRulesService.getRules();
  }

  @GetMapping("/hosts/{ruleName}")
  public HostRuleEntity getHostRule(@PathVariable String ruleName) {
    return hostRulesService.getRule(ruleName);
  }

  @PostMapping("/hosts")
  public HostRuleEntity addRule(@RequestBody HostRuleEntity rule) {
    ComponentType decisionComponentType = rule.getDecision().getComponentType().getType();
    if (decisionComponentType != ComponentType.HOST) {
      throw new BadRequestException("Expected decision type %s, instead got %s",
          ComponentType.HOST.name(), decisionComponentType.name());
    }
    Validation.validatePostRequest(rule.getId());
    return hostRulesService.addRule(rule);
  }

  @PutMapping("/hosts/{ruleName}")
  public HostRuleEntity updateRule(@PathVariable String ruleName, @RequestBody HostRuleEntity rule) {
    Validation.validatePutRequest(rule.getId());
    return hostRulesService.updateRule(ruleName, rule);
  }

  @DeleteMapping("/hosts/{ruleName}")
  public void deleteRule(@PathVariable String ruleName) {
    hostRulesService.deleteRule(ruleName);
  }

  @GetMapping("/hosts/{ruleName}/conditions")
  public List<ConditionEntity> getRuleConditions(@PathVariable String ruleName) {
    return hostRulesService.getConditions(ruleName);
  }

  @PostMapping("/hosts/{ruleName}/conditions")
  public void addRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditions) {
    hostRulesService.addConditions(ruleName, conditions);
  }

  @DeleteMapping("/hosts/{ruleName}/conditions")
  public void removeRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditionNames) {
    hostRulesService.removeConditions(ruleName, conditionNames);
  }

  @DeleteMapping("/hosts/{ruleName}/conditions/{conditionName}")
  public void removeRuleCondition(@PathVariable String ruleName, @PathVariable String conditionName) {
    hostRulesService.removeCondition(ruleName, conditionName);
  }

  @GetMapping("/hosts/{ruleName}/cloud-hosts")
  public List<CloudHostEntity> getRuleCloudHosts(@PathVariable String ruleName) {
    return hostRulesService.getCloudHosts(ruleName);
  }

  @PostMapping("/hosts/{ruleName}/cloud-hosts")
  public void addRuleCloudHosts(@PathVariable String ruleName, @RequestBody List<String> cloudHosts) {
    hostRulesService.addCloudHosts(ruleName, cloudHosts);
  }

  @DeleteMapping("/hosts/{ruleName}/cloud-hosts")
  public void removeRuleCloudHosts(@PathVariable String ruleName, @RequestBody List<String> cloudHosts) {
    hostRulesService.removeCloudHosts(ruleName, cloudHosts);
  }

  @DeleteMapping("/hosts/{ruleName}/cloud-hosts/{instanceId}")
  public void removeRuleCloudHost(@PathVariable String ruleName, @PathVariable String instanceId) {
    hostRulesService.removeCloudHost(ruleName, instanceId);
  }

  @GetMapping("/hosts/{ruleName}/edge-hosts")
  public List<EdgeHostEntity> getRuleEdgeHosts(@PathVariable String ruleName) {
    return hostRulesService.getEdgeHosts(ruleName);
  }

  @PostMapping("/hosts/{ruleName}/edge-hosts")
  public void addRuleEdgeHosts(@PathVariable String ruleName, @RequestBody List<String> edgeHosts) {
    hostRulesService.addEdgeHosts(ruleName, edgeHosts);
  }

  @DeleteMapping("/hosts/{ruleName}/edge-hosts")
  public void removeRuleEdgeHosts(@PathVariable String ruleName, @RequestBody List<String> edgeHosts) {
    hostRulesService.removeEdgeHosts(ruleName, edgeHosts);
  }

  @DeleteMapping("/hosts/{ruleName}/edge-hosts/{hostname}")
  public void removeRuleEdgeHosts(@PathVariable String ruleName, @PathVariable String hostname) {
    hostRulesService.removeEdgeHost(ruleName, hostname);
  }

}
