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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostFieldAverage;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoring;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostMonitoringLog;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/monitoring/hosts")
public class HostMonitoringController {

	private final HostsMonitoringService hostsMonitoringService;

	public HostMonitoringController(HostsMonitoringService hostsMonitoringService) {
		this.hostsMonitoringService = hostsMonitoringService;
	}

	@GetMapping
	public List<HostMonitoring> getHostsMonitoring() {
		return hostsMonitoringService.getHostsMonitoring();
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}")
	public List<HostMonitoring> getHostMonitoring(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		return hostsMonitoringService.getHostMonitoring(new HostAddress(publicIpAddress, privateIpAddress));
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}/fields")
	public List<HostFieldAverage> getHostMonitoringFieldsAverage(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		return hostsMonitoringService.getHostMonitoringFieldsAverage(new HostAddress(publicIpAddress, privateIpAddress));
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}/fields/{field}")
	public HostFieldAverage getHostMonitoringFieldAverage(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress,
														  @PathVariable String field) {
		return hostsMonitoringService.getHostMonitoringFieldAverage(new HostAddress(publicIpAddress, privateIpAddress), field);
	}

	@GetMapping("/logs")
	public List<HostMonitoringLog> getHostMonitoringLogs() {
		return hostsMonitoringService.getHostMonitoringLogs();
	}

	@GetMapping("/{hostname}/logs")
	public List<HostMonitoringLog> getHostMonitoringLogs(@PathVariable String hostname) {
		return hostsMonitoringService.getHostMonitoringLogs(new HostAddress(hostname));
	}

}
