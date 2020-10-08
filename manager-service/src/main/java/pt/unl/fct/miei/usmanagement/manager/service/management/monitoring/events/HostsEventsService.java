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

package pt.unl.fct.miei.usmanagement.manager.service.management.monitoring.events;

import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostEventRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostDetails;
import pt.unl.fct.miei.usmanagement.manager.service.management.rulesystem.decision.DecisionsService;

import java.util.List;
import java.util.Objects;

@Service
public class HostsEventsService {

	private final HostEventRepository hostEvents;
	private final DecisionsService decisionsService;

	public HostsEventsService(HostEventRepository hostEvents, DecisionsService decisionsService) {
		this.hostEvents = hostEvents;
		this.decisionsService = decisionsService;
	}

	public List<HostEventEntity> getHostEventsByHostAddress(HostAddress hostAddress) {
		return hostEvents.findByHostAddress(hostAddress);
	}

	public HostEventEntity saveHostEvent(HostDetails hostDetails, String decisionName) {
		DecisionEntity decision = decisionsService.getHostPossibleDecision(decisionName);
		HostEventEntity hostEvent = getHostEventsByHostAddress(hostDetails.getAddress()).stream().findFirst()
			.orElse(HostEventEntity.builder().hostDetails(hostDetails).decision(decision).count(0).build());
		if (!Objects.equals(hostEvent.getDecision().getId(), decision.getId())) {
			hostEvent.setDecision(decision);
			hostEvent.setCount(1);
		}
		else {
			hostEvent.setCount(hostEvent.getCount() + 1);
		}
		return this.hostEvents.save(hostEvent);
	}

}
