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

package pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.services;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.rules.ServiceRulesService;

import java.util.List;

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
