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

package pt.unl.fct.miei.usmanagement.manager.services.monitoring.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvents;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.DecisionsService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ServicesEventsService {

	private final ServiceEvents serviceEvents;
	private final DecisionsService decisionsService;
	private final HostsService hostsService;

	public ServicesEventsService(ServiceEvents serviceEvents, DecisionsService decisionsService, HostsService hostsService) {
		this.serviceEvents = serviceEvents;
		this.decisionsService = decisionsService;
		this.hostsService = hostsService;
	}

	public List<ServiceEvent> getServiceEvents() {
		return serviceEvents.findAll();
	}

	public List<ServiceEvent> getServiceEventsByServiceName(String serviceName) {
		return serviceEvents.findByServiceName(serviceName);
	}

	public List<ServiceEvent> getServiceEventsByContainerId(String containerId) {
		return serviceEvents.findByContainerIdStartingWith(containerId);
	}

	public ServiceEvent addServiceEvent(ServiceEvent serviceEvent) {
		return serviceEvents.save(serviceEvent);
	}

	public ServiceEvent saveServiceEvent(String containerId, String serviceName, String decisionName) {
		HostAddress managerHostAddress = hostsService.getManagerHostAddress();
		Decision decision = decisionsService.getServicePossibleDecision(decisionName);
		ServiceEvent event = getServiceEventsByContainerId(containerId).stream().findFirst().orElseGet(() ->
			ServiceEvent.builder().containerId(containerId).serviceName(serviceName)
				.managerPublicIpAddress(managerHostAddress.getPublicIpAddress()).managerPrivateIpAddress(managerHostAddress.getPrivateIpAddress())
				.decision(decision).count(0).build());
		if (!Objects.equals(event.getDecision().getId(), decision.getId())) {
			event.setDecision(decision);
			event.setCount(1);
		}
		else {
			event.setCount(event.getCount() + 1);
		}
		return addServiceEvent(event);
	}

	public void resetServiceEvent(String serviceName) {
		Decision decision = decisionsService.getServicePossibleDecision("NONE");
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

	public void reset(String containerId) {
		Optional<ServiceEvent> containerEvent = getServiceEventsByContainerId(containerId).stream().findFirst();
		containerEvent.ifPresent(event -> {
			event.setCount(0);
			serviceEvents.save(event);
		});
	}
}
