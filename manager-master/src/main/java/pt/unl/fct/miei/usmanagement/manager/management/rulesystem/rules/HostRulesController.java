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
import pt.unl.fct.miei.usmanagement.manager.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rules/hosts")
public class HostRulesController {

	private final HostRulesService hostRulesService;

	public HostRulesController(HostRulesService hostRulesService) {
		this.hostRulesService = hostRulesService;
	}

	@GetMapping
	public List<HostRule> getHostRules() {
		return hostRulesService.getRules();
	}

	@GetMapping("/{ruleName}")
	public HostRule getHostRule(@PathVariable String ruleName) {
		return hostRulesService.getRule(ruleName);
	}

	@PostMapping
	public HostRule addRule(@RequestBody HostRule rule) {
		ComponentTypeEnum decisionComponentTypeEnum = rule.getDecision().getComponentType().getType();
		if (decisionComponentTypeEnum != ComponentTypeEnum.HOST) {
			throw new BadRequestException("Expected decision type %s, instead got %s",
				ComponentTypeEnum.HOST.name(), decisionComponentTypeEnum.name());
		}
		Validation.validatePostRequest(rule.getId());
		return hostRulesService.addRule(rule);
	}

	@PutMapping("/{ruleName}")
	public HostRule updateRule(@PathVariable String ruleName, @RequestBody HostRule rule) {
		Validation.validatePutRequest(rule.getId());
		return hostRulesService.updateRule(ruleName, rule);
	}

	@DeleteMapping("/{ruleName}")
	public void deleteRule(@PathVariable String ruleName) {
		hostRulesService.deleteRule(ruleName);
	}

	@GetMapping("/{ruleName}/conditions")
	public List<Condition> getRuleConditions(@PathVariable String ruleName) {
		return hostRulesService.getConditions(ruleName);
	}

	@PostMapping("/{ruleName}/conditions")
	public void addRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditions) {
		hostRulesService.addConditions(ruleName, conditions);
	}

	@DeleteMapping("/{ruleName}/conditions")
	public void removeRuleConditions(@PathVariable String ruleName, @RequestBody List<String> conditionNames) {
		hostRulesService.removeConditions(ruleName, conditionNames);
	}

	@DeleteMapping("/{ruleName}/conditions/{conditionName}")
	public void removeRuleCondition(@PathVariable String ruleName, @PathVariable String conditionName) {
		hostRulesService.removeCondition(ruleName, conditionName);
	}

	@GetMapping("/{ruleName}/cloud-hosts")
	public List<CloudHost> getRuleCloudHosts(@PathVariable String ruleName) {
		return hostRulesService.getCloudHosts(ruleName);
	}

	@PostMapping("/{ruleName}/cloud-hosts")
	public void addRuleCloudHosts(@PathVariable String ruleName, @RequestBody List<String> cloudHosts) {
		hostRulesService.addCloudHosts(ruleName, cloudHosts);
	}

	@DeleteMapping("/{ruleName}/cloud-hosts")
	public void removeRuleCloudHosts(@PathVariable String ruleName, @RequestBody List<String> cloudHosts) {
		hostRulesService.removeCloudHosts(ruleName, cloudHosts);
	}

	@DeleteMapping("/{ruleName}/cloud-hosts/{instanceId}")
	public void removeRuleCloudHost(@PathVariable String ruleName, @PathVariable String instanceId) {
		hostRulesService.removeCloudHost(ruleName, instanceId);
	}

	@GetMapping("/{ruleName}/edge-hosts")
	public List<EdgeHost> getRuleEdgeHosts(@PathVariable String ruleName) {
		return hostRulesService.getEdgeHosts(ruleName);
	}

	@PostMapping("/{ruleName}/edge-hosts")
	public void addRuleEdgeHosts(@PathVariable String ruleName, @RequestBody List<String> edgeHosts) {
		// TODO parameter List of HostAddress instead
		List<HostAddress> hostAddresses = edgeHosts.stream().map(edgeHost -> {
			String[] addresses = edgeHost.split("-");
			return new HostAddress(addresses[0], addresses[1]);
		}).collect(Collectors.toList());
		hostRulesService.addEdgeHosts(ruleName, hostAddresses);
	}

	@DeleteMapping("/{ruleName}/edge-hosts")
	public void removeRuleEdgeHosts(@PathVariable String ruleName, @RequestBody List<String> edgeHosts) {
		// TODO parameter List of HostAddress instead
		List<HostAddress> hostAddresses = edgeHosts.stream().map(edgeHost -> {
			String[] addresses = edgeHost.split("-");
			return new HostAddress(addresses[0].trim(), addresses[1].trim());
		}).collect(Collectors.toList());
		hostRulesService.removeEdgeHosts(ruleName, hostAddresses);
	}

	@DeleteMapping("/{ruleName}/edge-hosts/{edgeHost}")
	public void removeRuleEdgeHosts(@PathVariable String ruleName, @PathVariable String edgeHost) {
		// TODO 2 path variables publicIpAddress and privateIpAddress
		String publicIpAddress = edgeHost.split("-")[0];
		String privateIpAddress = edgeHost.split("-")[1];
		hostRulesService.removeEdgeHost(ruleName, new HostAddress(publicIpAddress, privateIpAddress));
	}

}
