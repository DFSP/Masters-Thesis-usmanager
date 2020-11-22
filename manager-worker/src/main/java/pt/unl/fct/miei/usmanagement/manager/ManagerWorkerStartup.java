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
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.WorkerSymService;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

@Slf4j
@Component
public class ManagerWorkerStartup implements ApplicationListener<ApplicationReadyEvent> {

	private final HostsService hostsService;
	private final WorkerSymService symService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final HostsMonitoringService hostsMonitoringService;
	private final SyncService syncService;
	private final Environment environment;

	public ManagerWorkerStartup(WorkerSymService symService, HostsService hostsService,
								ServicesMonitoringService servicesMonitoringService,
								HostsMonitoringService hostsMonitoringService, SyncService syncService,
								Environment environment) {
		this.symService = symService;
		this.hostsService = hostsService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.syncService = syncService;
		this.environment = environment;
	}

	@SneakyThrows
	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		if (environment.getProperty(ContainerConstants.Environment.Manager.HOST_ADDRESS) == null
			|| environment.getProperty(ContainerConstants.Environment.Manager.EXTERNAL_ID) == null
			|| environment.getProperty(ContainerConstants.Environment.Manager.REGISTRATION_URL) == null) {
			System.out.println("usage: host_address, external_id and registration_url must be set");
			System.exit(1);
		}
		String hostAddressJson = environment.getProperty(ContainerConstants.Environment.Manager.HOST_ADDRESS);
		HostAddress hostAddress = new Gson().fromJson(hostAddressJson, HostAddress.class);
		hostsService.setManagerHostAddress(hostAddress);
		String externalId = environment.getProperty(ContainerConstants.Environment.Manager.EXTERNAL_ID);
		String registrationUrl = environment.getProperty(ContainerConstants.Environment.Manager.REGISTRATION_URL);
		symService.startSymmetricDsService(externalId, registrationUrl, hostAddress);
		servicesMonitoringService.initServiceMonitorTimer();
		hostsMonitoringService.initHostMonitorTimer();
		syncService.startContainersDatabaseSynchronization();
		syncService.startNodesDatabaseSynchronization();
	}

}
