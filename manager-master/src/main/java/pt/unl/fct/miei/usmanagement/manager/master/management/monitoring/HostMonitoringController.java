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

package pt.unl.fct.miei.usmanagement.manager.master.management.monitoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostFieldAvg;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostMonitoringLogEntity;

import java.util.List;

@RestController
@RequestMapping("/monitoring/hosts")
public class HostMonitoringController {

	private final HostsMonitoringService hostsMonitoringService;

	public HostMonitoringController(HostsMonitoringService hostsMonitoringService) {
		this.hostsMonitoringService = hostsMonitoringService;
	}

	@GetMapping
	public List<HostMonitoringEntity> getHostsMonitoring() {
		return hostsMonitoringService.getHostsMonitoring();
	}

	@GetMapping("/{hostname}")
	public List<HostMonitoringEntity> getHostMonitoring(@PathVariable String hostname) {
		return hostsMonitoringService.getHostMonitoring(hostname);
	}

	@GetMapping("/{hostname}/avg")
	public List<HostFieldAvg> getHostMonitoringAvg(@PathVariable String hostname) {
		return hostsMonitoringService.getHostFieldsAvg(hostname);
	}

	@GetMapping("/{hostname}/fields/{field}/avg")
	public HostFieldAvg getMonitoringHostLogsByHostAndField(@PathVariable String hostname, @PathVariable String field) {
		return hostsMonitoringService.getHostFieldAvg(hostname, field);
	}

	@GetMapping("/logs")
	public List<HostMonitoringLogEntity> getHostMonitoringLogs() {
		return hostsMonitoringService.getHostMonitoringLogs();
	}

	@GetMapping("/logs/hosts/{hostname}")
	public List<HostMonitoringLogEntity> getHostMonitoringLogsByHostname(@PathVariable String hostname) {
		return hostsMonitoringService.getHostMonitoringLogsByHostname(hostname);
	}

}
