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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.symmetricds.SymService;
import pt.unl.fct.miei.usmanagement.manager.services.management.hosts.HostsService;

@Slf4j
@Component
public class ManagerMasterStartup implements ApplicationListener<ApplicationReadyEvent> {

	private final HostsService hostsService;
	private final ServicesMonitoringService servicesMonitoringService;
	private final HostsMonitoringService hostsMonitoringService;
	private final SymService symService;

	public ManagerMasterStartup(@Lazy HostsService hostsService,
								@Lazy ServicesMonitoringService servicesMonitoringService,
								@Lazy HostsMonitoringService hostsMonitoringService,
								SymService symService) {
		this.hostsService = hostsService;
		this.servicesMonitoringService = servicesMonitoringService;
		this.hostsMonitoringService = hostsMonitoringService;
		this.symService = symService;
	}

	@SneakyThrows
	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		HostAddress hostAddress = hostsService.setHostAddress();
		try {
			hostsService.clusterHosts();
		}
		catch (ManagerException e) {
			e.printStackTrace();
		}
		servicesMonitoringService.initServiceMonitorTimer();
		hostsMonitoringService.initHostMonitorTimer();
		symService.startSymmetricDSServer(hostAddress);
	}

}
