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

package pt.unl.fct.miei.usmanagement.manager.master;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.master.symmetricds.SymService;
import pt.unl.fct.miei.usmanagement.manager.services.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.CloudHostsService;

@Component
public class ManagerMasterShutdown implements ApplicationListener<ContextClosedEvent> {

	private final ContainersService containersService;
	private final DockerSwarmService dockerSwarmService;
	private final CloudHostsService cloudHostsService;
	private final SymService symService;
	private final HostsMonitoringService hostsMonitoringService;
	private final ServicesMonitoringService servicesMonitoringService;

	public ManagerMasterShutdown(ContainersService containersService, DockerSwarmService dockerSwarmService,
								 SymService symService, CloudHostsService cloudHostsService,
								 HostsMonitoringService hostsMonitoringService,
								 ServicesMonitoringService servicesMonitoringService) {
		this.containersService = containersService;
		this.dockerSwarmService = dockerSwarmService;
		this.symService = symService;
		this.cloudHostsService = cloudHostsService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.servicesMonitoringService = servicesMonitoringService;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		symService.stopSymmetricDSServer();
		hostsMonitoringService.stopHostMonitoring();
		servicesMonitoringService.stopServiceMonitoring();
		containersService.stopContainers();
		dockerSwarmService.destroySwarm();
		cloudHostsService.terminateInstances();
	}

}
