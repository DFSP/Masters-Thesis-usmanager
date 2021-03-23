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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.services.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
@Component
public class ManagerWorkerShutdown implements ApplicationListener<ContextClosedEvent> {

	private final ContainersService containersService;
	private final DockerSwarmService dockerSwarmService;
	private final HostsMonitoringService hostsMonitoringService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final SyncService syncService;
	private final KafkaService kafkaService;

	public ManagerWorkerShutdown(ContainersService containersService, ServicesMonitoringService servicesMonitoringService,
								 SyncService syncService, DockerSwarmService dockerSwarmService,
								 HostsMonitoringService hostsMonitoringService, KafkaService kafkaService) {
		this.containersService = containersService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.syncService = syncService;
		this.dockerSwarmService = dockerSwarmService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.kafkaService = kafkaService;
	}

	@Override
	public void onApplicationEvent(@NonNull ContextClosedEvent event) {
		hostsMonitoringService.stopHostMonitoring();
		servicesMonitoringService.stopServiceMonitoring();
		syncService.stopCloudHostsDatabaseSynchronization();
		syncService.stopContainersDatabaseSynchronization();
		syncService.stopNodesDatabaseSynchronization();
		kafkaService.stop();
		try {
			Predicate<DockerContainer> containersPredicate = (dockerContainer) -> {
				String serviceName = dockerContainer.getLabels().getOrDefault(ContainerConstants.Label.SERVICE_NAME, "");
				return !Objects.equals(serviceName, ServiceConstants.Name.DOCKER_API_PROXY);
			};
			containersService.stopContainers(containersPredicate);
		}
		catch (Exception e) {
			log.error("Failed to stop all containers: {}", e.getMessage());
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
		}
	}

}
