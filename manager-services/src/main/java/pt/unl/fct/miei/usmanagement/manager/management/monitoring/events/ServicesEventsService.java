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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEventEntity;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEventRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.DecisionEntity;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ServicesEventsService {

	private final ServiceEventRepository serviceEvents;
	private final DecisionsService decisionsService;

	public ServicesEventsService(ServiceEventRepository serviceEvents, DecisionsService decisionsService) {
		this.serviceEvents = serviceEvents;
		this.decisionsService = decisionsService;
	}

	public List<ServiceEventEntity> getServiceEvents() {
		return serviceEvents.findAll();
	}

	public List<ServiceEventEntity> getServiceEventsByServiceName(String serviceName) {
		return serviceEvents.findByServiceName(serviceName);
	}

	public List<ServiceEventEntity> getServiceEventsByContainerId(String containerId) {
		return serviceEvents.findByContainerIdStartingWith(containerId);
	}

	public ServiceEventEntity saveServiceEvent(String containerId, String serviceName, String decisionName) {
		DecisionEntity decision = decisionsService.getServicePossibleDecision(decisionName);
		ServiceEventEntity event = getServiceEventsByContainerId(containerId).stream().findFirst().orElse(ServiceEventEntity.builder()
			.containerId(containerId).serviceName(serviceName).decision(decision).count(0).build());
		if (!Objects.equals(event.getDecision().getId(), decision.getId())) {
			event.setDecision(decision);
			event.setCount(1);
		}
		else {
			event.setCount(event.getCount() + 1);
		}
		event = serviceEvents.save(event);
		return event;
	}

	public void resetServiceEvent(String serviceName) {
		DecisionEntity decision = decisionsService.getServicePossibleDecision("NONE");
		serviceEvents.findByServiceName(serviceName).forEach(serviceEvent -> {
			serviceEvent.setDecision(decision);
			serviceEvent.setCount(1);
			serviceEvents.save(serviceEvent);
		});
	}

	public void reset() {
		log.info("Clearing all service events");
		serviceEvents.deleteAll();
	}
}
