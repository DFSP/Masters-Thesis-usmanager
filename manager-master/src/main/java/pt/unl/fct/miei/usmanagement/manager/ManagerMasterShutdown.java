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

package pt.unl.fct.miei.usmanagement.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.management.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.MasterSymService;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
@Component
public class ManagerMasterShutdown implements ApplicationListener<ContextClosedEvent> {

	private final ContainersService containersService;
	private final DockerSwarmService dockerSwarmService;
	private final CloudHostsService cloudHostsService;
	private final ElasticIpsService elasticIpsService;
	private final MasterSymService symService;
	private final HostsMonitoringService hostsMonitoringService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final HostsEventsService hostsEventsService;
	private final ServicesEventsService servicesEventsService;
	private final NodesService nodesService;
	private final SyncService syncService;

	public ManagerMasterShutdown(ContainersService containersService, DockerSwarmService dockerSwarmService,
								 ElasticIpsService elasticIpsService, MasterSymService symService, CloudHostsService cloudHostsService,
								 HostsMonitoringService hostsMonitoringService, ServicesMonitoringService servicesMonitoringService,
								 HostsEventsService hostsEventsService, ServicesEventsService servicesEventsService,
								 NodesService nodesService, SyncService syncService) {
		this.containersService = containersService;
		this.dockerSwarmService = dockerSwarmService;
		this.elasticIpsService = elasticIpsService;
		this.symService = symService;
		this.cloudHostsService = cloudHostsService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.hostsEventsService = hostsEventsService;
		this.servicesEventsService = servicesEventsService;
		this.nodesService = nodesService;
		this.syncService = syncService;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		/*symService.stopSymmetricDSServer();
		hostsMonitoringService.stopHostMonitoring();
		servicesMonitoringService.stopServiceMonitoring();
		syncService.stopCloudHostsDatabaseSynchronization();
		syncService.stopContainersDatabaseSynchronization();
		syncService.stopNodesDatabaseSynchronization();
		try {
			Predicate<DockerContainer> containersPredicate = (dockerContainer) -> {
				String serviceName = dockerContainer.getLabels().getOrDefault(ContainerConstants.Label.SERVICE_NAME, "");
				return !Objects.equals(serviceName, DockerApiProxyService.DOCKER_API_PROXY);
			};
			containersService.stopContainers(containersPredicate);
		}
		catch (Exception e) {
			log.error("Failed to stop all containers: {}", e.getMessage());
		}
		try {
			cloudHostsService.terminateInstances();
		}
		catch (Exception e) {
			log.error("Failed to terminate all cloud instances: {}", e.getMessage());
		}
		try {
			elasticIpsService.releaseElasticIpAddresses();
		}
		catch (Exception e) {
			log.error("Failed to release elastic ip addresses: {}", e.getMessage());
		}
		try {
			dockerSwarmService.destroySwarm();
		}
		catch (Exception e) {
			log.error("Failed to completely destroy swarm: {}", e.getMessage());
		}
		try {
			containersService.stopDockerApiProxies();
		}
		catch (Exception e) {
			log.error("Failed to stop all docker api proxies: {}", e.getMessage());
		}*/
		/*hostsEventsService.reset();
		servicesEventsService.reset();
		hostsMonitoringService.reset();
		servicesMonitoringService.reset();
		nodesService.reset();*/
	}

}
