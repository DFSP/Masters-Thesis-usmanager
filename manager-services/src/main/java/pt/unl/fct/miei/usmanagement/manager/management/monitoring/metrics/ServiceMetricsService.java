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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics;

import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.CpuStats;
import com.spotify.docker.client.messages.MemoryStats;
import com.spotify.docker.client.messages.NetworkStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated.ServiceSimulatedMetricsService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Service
public class ServiceMetricsService {

	private final ContainersService containersService;

	public ServiceMetricsService(ContainersService containersService) {
		this.containersService = containersService;
	}

	public Map<String, Double> getContainerStats(HostAddress hostAddress, String containerId) {
		Map<String, Double> stats = new HashMap<>();
		Optional<ContainerStats> containerStats = containersService.getContainerStats(containerId, hostAddress);
		containerStats.ifPresent(s -> {
			CpuStats cpuStats = s.cpuStats();
			CpuStats preCpuStats = s.precpuStats();
			double cpu = cpuStats.cpuUsage().totalUsage().doubleValue();
			stats.put("cpu", cpu);
			double cpuPercent = getContainerCpuPercent(preCpuStats, cpuStats);
			stats.put("cpu-%", cpuPercent);
			MemoryStats memoryStats = s.memoryStats();
			double ram = memoryStats.usage().doubleValue();
			stats.put("ram", ram);
			double ramPercent = getContainerRamPercent(memoryStats);
			stats.put("ram-%", ramPercent);
			double rxBytes = 0;
			double txBytes = 0;
			for (NetworkStats networkStats : s.networks().values()) {
				rxBytes += networkStats.rxBytes().doubleValue();
				txBytes += networkStats.txBytes().doubleValue();
			}
			stats.put("rx-bytes", rxBytes);
			stats.put("tx-bytes", txBytes);
		});
		return stats;
	}

	private double getContainerCpuPercent(CpuStats preCpuStats, CpuStats cpuStats) {
		double systemDelta = cpuStats.systemCpuUsage().doubleValue() - preCpuStats.systemCpuUsage().doubleValue();
		double cpuDelta = cpuStats.cpuUsage().totalUsage().doubleValue() - preCpuStats.cpuUsage().totalUsage().doubleValue();
		double cpuPercent = 0.0;
		if (systemDelta > 0.0 && cpuDelta > 0.0) {
			double onlineCpus = cpuStats.cpuUsage().percpuUsage().stream().filter(cpuUsage -> cpuUsage >= 1).count();
			cpuPercent = (cpuDelta / systemDelta) * onlineCpus * 100.0;
		}
		return cpuPercent;
	}
private double getContainerRamPercent(MemoryStats memStats) {
		return memStats.limit() < 1 ? 0.0 : (memStats.usage().doubleValue() / memStats.limit().doubleValue()) * 100.0;
	}

}
