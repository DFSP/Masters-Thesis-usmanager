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

package pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.List;

@RestController
@RequestMapping("/rules/containers")
public class ContainerRulesController {

	private final ContainerRulesService containerRulesService;

	public ContainerRulesController(ContainerRulesService containerRulesService) {
		this.containerRulesService = containerRulesService;
	}

	@GetMapping
	public List<ContainerRule> getContainerRules() {
		return containerRulesService.getRules();
	}

	@GetMapping("/{ruleName}")
	public ContainerRule getContainerRule(@PathVariable String ruleName) {
		return containerRulesService.getRule(ruleName);
	}

	@PostMapping
	public ContainerRule addRule(@RequestBody ContainerRule rule) {
		ComponentTypeEnum decisionComponentTypeEnum = rule.getDecision().getComponentType().getType();
		if (decisionComponentTypeEnum != ComponentTypeEnum.SERVICE) {
			throw new BadRequestException("Expected decision type %s, instead got %s",
				ComponentTypeEnum.SERVICE.name(), decisionComponentTypeEnum.name());
		}
		Validation.validatePostRequest(rule.getId());
		return containerRulesService.addRule(rule);
	}

	@PutMapping("/{ruleName}")
	public ContainerRule updateRule(@PathVariable String ruleName, @RequestBody ContainerRule rule) {
		Validation.validatePutRequest(rule.getId());
		return containerRulesService.updateRule(ruleName, rule);
	}

	@DeleteMapping("/{ruleName}")
	public void deleteRule(@PathVariable String ruleName) {
		containerRulesService.deleteRule(ruleName);
	}

	@GetMapping("/{ruleName}/conditions")
	public List<Condition> getRuleConditions(@PathVariable String ruleName) {
		return containerRulesService.getConditions(ruleName);
	}

	@PostMapping("/{ruleName}/conditions")
	public void addRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditions) {
		containerRulesService.addConditions(ruleName, conditions);
	}

	@DeleteMapping("/{ruleName}/conditions")
	public void removeRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditionNames) {
		containerRulesService.removeConditions(ruleName, conditionNames);
	}

	@DeleteMapping("/{ruleName}/conditions/{conditionName}")
	public void removeRuleCondition(@PathVariable String ruleName, @PathVariable String conditionName) {
		containerRulesService.removeCondition(ruleName, conditionName);
	}

	@GetMapping("/{ruleName}/containers")
	public List<Container> getRuleContainers(@PathVariable String ruleName) {
		return containerRulesService.getContainers(ruleName);
	}

	@PostMapping("/{ruleName}/containers")
	public void addRuleContainers(@PathVariable String ruleName, @RequestBody List<String> containers) {
		containerRulesService.addContainers(ruleName, containers);
	}

	@DeleteMapping("/{ruleName}/containers")
	public void removeRuleContainers(@PathVariable String ruleName, @RequestBody List<String> containers) {
		containerRulesService.removeContainers(ruleName, containers);
	}

	@DeleteMapping("/{ruleName}/containers/{containerId}")
	public void removeRuleContainer(@PathVariable String ruleName, @PathVariable String containerId) {
		containerRulesService.removeContainer(ruleName, containerId);
	}

}
