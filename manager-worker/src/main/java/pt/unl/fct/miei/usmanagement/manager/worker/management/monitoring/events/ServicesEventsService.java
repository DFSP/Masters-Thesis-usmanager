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

package pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.events;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceEventEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceEventRepository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionEntity;

@Service
public class ServicesEventsService {

  private final ServiceEventRepository serviceEvents;

  public ServicesEventsService(ServiceEventRepository serviceEvents) {
    this.serviceEvents = serviceEvents;
  }

  public List<ServiceEventEntity> getServiceEventsByContainerId(String containerId) {
    return serviceEvents.findByContainerId(containerId);
  }

  public ServiceEventEntity saveServiceEvent(String containerId, String serviceName, DecisionEntity decision) {
    ServiceEventEntity event =
        getServiceEventsByContainerId(containerId).stream().findFirst().orElse(
            ServiceEventEntity.builder().containerId(containerId).serviceName(serviceName).decision(decision).count(0)
                .build());
    if (event.getDecision() == null || !Objects.equals(event.getDecision().getId(), decision.getId())) {
      event.setDecision(decision);
      event.setCount(1);
    } else {
      event.setCount(event.getCount() + 1);
    }
    event = serviceEvents.save(event);
    return event;
  }

  //TODO ?
  public void resetServiceEvent(String serviceName) {
    serviceEvents.findByServiceName(serviceName).forEach(serviceEvent -> {
      serviceEvent.setDecision(null);
      serviceEvent.setCount(1);
      serviceEvents.save(serviceEvent);
    });
  }

}
