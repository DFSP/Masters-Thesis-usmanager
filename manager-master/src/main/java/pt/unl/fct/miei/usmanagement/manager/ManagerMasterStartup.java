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
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.services.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.services.regions.RegionsService;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

@Slf4j
@Component
public class ManagerMasterStartup implements ApplicationListener<ApplicationReadyEvent> {

	private final HostsService hostsService;
	private final SyncService syncService;
	private final ElasticIpsService elasticIpsService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final HostsMonitoringService hostsMonitoringService;
	private final RegionsService regionsService;

	private final Environment environment;

	public ManagerMasterStartup(@Lazy HostsService hostsService,
								@Lazy SyncService syncService,
								@Lazy ElasticIpsService elasticIpsService,
								@Lazy ServicesMonitoringService servicesMonitoringService,
								@Lazy HostsMonitoringService hostsMonitoringService,
								RegionsService regionsService, Environment environment) {
		this.hostsService = hostsService;
		this.syncService = syncService;
		this.elasticIpsService = elasticIpsService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.regionsService = regionsService;
		this.environment = environment;
	}

	@SneakyThrows
	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		String hostAddressJson = environment.getProperty(EnvironmentConstants.HOST_ADDRESS);
		HostAddress hostAddress = hostAddressJson == null
			? hostsService.setManagerHostAddress()
			: hostsService.setManagerHostAddress(new Gson().fromJson(hostAddressJson, HostAddress.class));
		elasticIpsService.allocateElasticIpAddresses();
		hostsService.setupHost(hostAddress, NodeRole.MANAGER);
		hostsService.clusterHosts();
		servicesMonitoringService.initServiceMonitorTimer();
		hostsMonitoringService.initHostMonitorTimer();
		syncService.startCloudHostsDatabaseSynchronization();
		syncService.startContainersDatabaseSynchronization();
		syncService.startNodesDatabaseSynchronization();
	}

}
