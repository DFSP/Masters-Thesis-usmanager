package pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.containers;

import pt.unl.fct.microservicemanagement.mastermanager.manager.docker.container.ContainerEntity;
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
public class ContainerRulesController {
  
  private final ContainerRulesService containerRulesService;

  public ContainerRulesController(ContainerRulesService containerRulesService) {
    this.containerRulesService = containerRulesService;
  }

  @GetMapping("/containers")
  public Iterable<ContainerRuleEntity> getContainerRules() {
    return containerRulesService.getRules();
  }

  @GetMapping("/containers/{ruleName}")
  public ContainerRuleEntity getContainerRule(@PathVariable String ruleName) {
    return containerRulesService.getRule(ruleName);
  }

  @PostMapping("/containers")
  public ContainerRuleEntity addRule(@RequestBody ContainerRuleEntity rule) {
    Validation.validatePostRequest(rule.getId());
    return containerRulesService.addRule(rule);
  }

  @PutMapping("/containers/{ruleName}")
  public ContainerRuleEntity updateRule(@PathVariable String ruleName, @RequestBody ContainerRuleEntity rule) {
    Validation.validatePutRequest(rule.getId());
    return containerRulesService.updateRule(ruleName, rule);
  }

  @DeleteMapping("/containers/{ruleName}")
  public void deleteRule(@PathVariable String ruleName) {
    containerRulesService.deleteRule(ruleName);
  }

  @GetMapping("/containers/{ruleName}/conditions")
  public List<ConditionEntity> getRuleConditions(@PathVariable String ruleName) {
    return containerRulesService.getConditions(ruleName);
  }

  @PostMapping("/containers/{ruleName}/conditions")
  public void addRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditions) {
    containerRulesService.addConditions(ruleName, conditions);
  }

  @DeleteMapping("/containers/{ruleName}/conditions")
  public void removeRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditionNames) {
    containerRulesService.removeConditions(ruleName, conditionNames);
  }

  @DeleteMapping("/containers/{ruleName}/conditions/{conditionName}")
  public void removeRuleCondition(@PathVariable String ruleName, @PathVariable String conditionName) {
    containerRulesService.removeCondition(ruleName, conditionName);
  }

  @GetMapping("/containers/{ruleName}/containers")
  public List<ContainerEntity> getRuleContainers(@PathVariable String ruleName) {
    return containerRulesService.getContainers(ruleName);
  }

  @PostMapping("/containers/{ruleName}/containers")
  public void addRuleContainers(@PathVariable String ruleName, @RequestBody List<String> containers) {
    containerRulesService.addContainers(ruleName, containers);
  }

  @DeleteMapping("/containers/{ruleName}/containers")
  public void removeRuleCloudHosts(@PathVariable String ruleName, @RequestBody List<String> containers) {
    containerRulesService.removeContainers(ruleName, containers);
  }

  @DeleteMapping("/hosts/{ruleName}/containers/{containerId}")
  public void removeRuleContainer(@PathVariable String ruleName, @PathVariable String containerId) {
    containerRulesService.removeContainer(ruleName, containerId);
  }

}
