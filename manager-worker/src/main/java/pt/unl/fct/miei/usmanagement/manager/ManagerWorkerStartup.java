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

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.heartbeats.HeartbeatService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ManagerWorkerStartup implements ApplicationListener<ApplicationReadyEvent> {

	private final HostsService hostsService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final HostsMonitoringService hostsMonitoringService;
	private final SyncService syncService;
	private final KafkaService kafkaService;
	private final HeartbeatService heartbeatService;
	private final Environment environment;

	public ManagerWorkerStartup(HostsService hostsService, ServicesMonitoringService servicesMonitoringService,
								HostsMonitoringService hostsMonitoringService, SyncService syncService,
								KafkaService kafkaService, HeartbeatService heartbeatService, Environment environment) {
		this.hostsService = hostsService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.syncService = syncService;
		this.kafkaService = kafkaService;
		this.heartbeatService = heartbeatService;
		this.environment = environment;
	}

	@SneakyThrows
	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		requireEnvVars();
		String hostAddressJson = environment.getProperty(ContainerConstants.Environment.Manager.HOST_ADDRESS);
		HostAddress hostAddress = new Gson().fromJson(hostAddressJson, HostAddress.class);
		hostsService.setManagerHostAddress(hostAddress);
		//heartbeatService.startHeartbeat();
		kafkaService.start();
		hostsService.setupWorkerManagerHost(hostAddress, NodeRole.MANAGER);
		servicesMonitoringService.initServiceMonitorTimer();
		hostsMonitoringService.initHostMonitorTimer();
		syncService.startContainersDatabaseSynchronization();
		syncService.startNodesDatabaseSynchronization();
	}

	private void requireEnvVars() {
		Map<String, String> vars = new HashMap<>(3);
		vars.put(ContainerConstants.Environment.Manager.HOST_ADDRESS,
			environment.getProperty(ContainerConstants.Environment.Manager.HOST_ADDRESS));
		vars.put(ContainerConstants.Environment.Manager.ID,
			environment.getProperty(ContainerConstants.Environment.Manager.ID));
		vars.put(ContainerConstants.Environment.Manager.KAFKA_BOOTSTRAP_SERVERS,
			environment.getProperty(ContainerConstants.Environment.Manager.KAFKA_BOOTSTRAP_SERVERS));
		log.info("Environment: {}", vars);
		Set<String> requiredVars = vars.entrySet().stream().filter(e -> e.getValue() == null).map(Map.Entry::getKey).collect(Collectors.toSet());
		if (!requiredVars.isEmpty()) {
			log.error("Usage: {} is required and {} is missing", vars.keySet(), requiredVars);
			System.exit(1);
		}
	}

}
