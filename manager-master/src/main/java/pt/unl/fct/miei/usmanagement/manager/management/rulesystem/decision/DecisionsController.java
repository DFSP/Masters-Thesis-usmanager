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

package pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.ServiceDecision;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;

import java.util.List;

@RestController
@RequestMapping("/decisions")
public class DecisionsController {

	private final DecisionsService decisionsService;

	public DecisionsController(DecisionsService decisionsService) {
		this.decisionsService = decisionsService;
	}

	@GetMapping
	public List<Decision> getDecisions() {
		return decisionsService.getDecisions();
	}

	@GetMapping("/services")
	public List<pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision> getServicesPossibleDecisions() {
		return decisionsService.getServicesPossibleDecisions();
	}

	@GetMapping("/services/{serviceName}")
	public List<ServiceDecision> getServiceDecisions(@PathVariable String serviceName) {
		return decisionsService.getServiceDecisions(serviceName);
	}

	@GetMapping("/containers/{containerId}")
	public List<ServiceDecision> getContainerDecisions(@PathVariable String containerId) {
		return decisionsService.getContainerDecisions(containerId);
	}

	@GetMapping("/hosts")
	public List<pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision> getHostsPossibleDecisions() {
		return decisionsService.getHostsPossibleDecisions();
	}

	@GetMapping("/hosts/{publicIpAddress}/{privateIpAddress}")
	public List<HostDecision> getHostDecisions(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		return decisionsService.getHostDecisions(new HostAddress(publicIpAddress, privateIpAddress));
	}

}
