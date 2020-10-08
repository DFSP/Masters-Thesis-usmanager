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

package pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.containers;

import org.springframework.web.bind.annotation.*;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ContainerRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.rules.ContainerRulesService;

import java.util.List;

@RestController
@RequestMapping("/rules")
public class ContainerRulesController {

	private final ContainerRulesService containerRulesService;

	public ContainerRulesController(ContainerRulesService containerRulesService) {
		this.containerRulesService = containerRulesService;
	}

	@GetMapping("/containers")
	public List<ContainerRuleEntity> getContainerRules() {
		return containerRulesService.getRules();
	}

	@GetMapping("/containers/{ruleName}")
	public ContainerRuleEntity getContainerRule(@PathVariable String ruleName) {
		return containerRulesService.getRule(ruleName);
	}

	@PostMapping("/containers")
	public ContainerRuleEntity addRule(@RequestBody ContainerRuleEntity rule) {
		ComponentType decisionComponentType = rule.getDecision().getComponentType().getType();
		if (decisionComponentType != ComponentType.CONTAINER) {
			throw new BadRequestException("Expected decision type %s, instead got %s",
				ComponentType.CONTAINER.name(), decisionComponentType.name());
		}
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
	public void removeRuleContainers(@PathVariable String ruleName, @RequestBody List<String> containers) {
		containerRulesService.removeContainers(ruleName, containers);
	}

	@DeleteMapping("/containers/{ruleName}/containers/{containerId}")
	public void removeRuleContainer(@PathVariable String ruleName, @PathVariable String containerId) {
		containerRulesService.removeContainer(ruleName, containerId);
	}

}
