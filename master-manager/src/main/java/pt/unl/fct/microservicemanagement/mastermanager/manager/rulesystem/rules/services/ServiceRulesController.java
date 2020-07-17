package pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.services;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.BadRequestException;
import pt.unl.fct.microservicemanagement.mastermanager.manager.componenttypes.ComponentType;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServiceEntity;
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
public class ServiceRulesController {
  
  private final ServiceRulesService serviceRulesService;

  public ServiceRulesController(ServiceRulesService serviceRulesService) {
    this.serviceRulesService = serviceRulesService;
  }

  @GetMapping("/services")
  public List<ServiceRuleEntity> getRules() {
    return serviceRulesService.getRules();
  }

  @GetMapping("/services/{ruleName}")
  public ServiceRuleEntity getRule(@PathVariable String ruleName) {
    return serviceRulesService.getRule(ruleName);
  }

  @PostMapping("/services")
  public ServiceRuleEntity addRule(@RequestBody ServiceRuleEntity rule) {
    ComponentType decisionComponentType = rule.getDecision().getComponentType().getType();
    if (decisionComponentType != ComponentType.SERVICE) {
      throw new BadRequestException("Expected decision type %s, instead got %s",
          ComponentType.SERVICE.name(), decisionComponentType.name());
    }
    Validation.validatePostRequest(rule.getId());
    return serviceRulesService.addRule(rule);
  }

  @PutMapping("/services/{ruleName}")
  public ServiceRuleEntity updateRule(@PathVariable String ruleName, @RequestBody ServiceRuleEntity rule) {
    Validation.validatePutRequest(rule.getId());
    return serviceRulesService.updateRule(ruleName, rule);
  }

  @DeleteMapping("/services/{ruleName}")
  public void deleteRule(@PathVariable String ruleName) {
    serviceRulesService.deleteRule(ruleName);
  }

  @GetMapping("/services/{ruleName}/conditions")
  public List<ConditionEntity> getRuleConditions(@PathVariable String ruleName) {
    return serviceRulesService.getConditions(ruleName);
  }

  @PostMapping("/services/{ruleName}/conditions")
  public void addRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditions) {
    serviceRulesService.addConditions(ruleName, conditions);
  }

  @DeleteMapping("/services/{ruleName}/conditions")
  public void removeRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditionNames) {
    serviceRulesService.removeConditions(ruleName, conditionNames);
  }

  @DeleteMapping("/services/{ruleName}/conditions/{conditionName}")
  public void removeRuleCondition(@PathVariable String ruleName, @PathVariable String conditionName) {
    serviceRulesService.removeCondition(ruleName, conditionName);
  }

  @GetMapping("/services/{ruleName}/services")
  public List<ServiceEntity> getRuleServices(@PathVariable String ruleName) {
    return serviceRulesService.getServices(ruleName);
  }

  @PostMapping("/services/{ruleName}/services")
  public void addRuleServices(@PathVariable String ruleName, @RequestBody List<String> services) {
    serviceRulesService.addServices(ruleName, services);
  }

  @DeleteMapping("/services/{ruleName}/services")
  public void removeRuleServices(@PathVariable String ruleName, @RequestBody List<String> services) {
    serviceRulesService.removeServices(ruleName, services);
  }

  @DeleteMapping("/services/{ruleName}/services/{serviceName}")
  public void removeRuleService(@PathVariable String ruleName, @PathVariable String serviceName) {
    serviceRulesService.removeService(ruleName, serviceName);
  }

}
