/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.event;

import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.HostsEventsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.ServicesEventsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.event.HostEventEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.event.ServiceEventEntity;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventsController {

  //TODO split into hosts and services?
  //TODO create a LogsController?

  private final HostsEventsService hostsEventsService;
  private final ServicesEventsService servicesEventsService;

  public EventsController(HostsEventsService hostsEventsService, ServicesEventsService servicesEventsService) {
    this.hostsEventsService = hostsEventsService;
    this.servicesEventsService = servicesEventsService;
  }

  @GetMapping("/hosts/{hostname}")
  public List<HostEventEntity> getHostEventLogsByHostname(@PathVariable String hostname)
      {
    return hostsEventsService.getHostEventsByHostname(hostname);
  }

  @GetMapping("/services/{serviceName}")
  public List<ServiceEventEntity> getServiceEventLogsByServiceName(@PathVariable String serviceName)
      {
    return servicesEventsService.getServiceEventsByServiceName(serviceName);
  }

  @GetMapping("/containers/{containerId}")
  public List<ServiceEventEntity> getServiceEventLogsByContainerId(@PathVariable String containerId)
      {
    return servicesEventsService.getServiceEventsByContainerId(containerId);
  }

}
