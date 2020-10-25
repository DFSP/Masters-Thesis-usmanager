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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ServiceDecisionResult extends DecisionResult {

	private final String containerId;
	private final String serviceName;

	public ServiceDecisionResult(HostAddress hostAddress, String containerId, String serviceName) {
		this(hostAddress, containerId, serviceName, RuleDecision.NONE, 0, new HashMap<>(), 0);
	}

	public ServiceDecisionResult(HostAddress hostAddress, String containerId, String serviceName, RuleDecision decision,
								 long ruleId, Map<String, Double> fields, int priority) {
		super(hostAddress, decision, ruleId, fields, priority, fields.values().stream().mapToDouble(Double::doubleValue).sum());
		this.containerId = containerId;
		this.serviceName = serviceName;
	}

}
