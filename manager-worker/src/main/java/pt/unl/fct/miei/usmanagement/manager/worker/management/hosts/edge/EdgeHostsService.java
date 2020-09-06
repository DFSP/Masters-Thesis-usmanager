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

package pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.edge;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;

@Slf4j
@Service
public class EdgeHostsService {

	private final EdgeHostRepository edgeHosts;

	private final String edgeKeyFilePath;

	public EdgeHostsService(EdgeHostRepository edgeHosts, EdgeHostsProperties edgeHostsProperties) {
		this.edgeHosts = edgeHosts;
		this.edgeKeyFilePath = edgeHostsProperties.getAccess().getKeyFilePath();
	}

	public String getKeyFilePath(EdgeHostEntity edgeHostEntity) {
		String username = edgeHostEntity.getUsername();
		String hostname = edgeHostEntity.getHostname();
		return String.format("%s/%s/%s_%s", System.getProperty("user.dir"), edgeKeyFilePath, username,
			hostname.replace(".", "_"));
	}

	public List<EdgeHostEntity> getEdgeHosts() {
		return edgeHosts.findAll();
	}

	public EdgeHostEntity getEdgeHostById(Long id) {
		try {
			return edgeHosts.getOne(id);
		}
		catch (javax.persistence.EntityNotFoundException e) {
			throw new EntityNotFoundException(EdgeHostEntity.class, "id", id.toString());
		}
	}

	public EdgeHostEntity getEdgeHostByDnsOrIp(String host) {
		return edgeHosts.findByPublicDnsNameOrPublicIpAddress(host, host).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "host", host));
	}

	public EdgeHostEntity getEdgeHostByDns(String dns) {
		return edgeHosts.findByPublicDnsName(dns).orElseThrow(() ->
			new EntityNotFoundException(EdgeHostEntity.class, "dns", dns));
	}


	public List<EdgeHostEntity> getHostsByRegion(RegionEntity region) {
		return edgeHosts.findByRegion(region);
	}

	public List<EdgeHostEntity> getHostsByCountry(String country) {
		return edgeHosts.findByCountry(country);
	}

	public List<EdgeHostEntity> getHostsByCity(String city) {
		return edgeHosts.findByCity(city);
	}

	public List<HostRuleEntity> getRules(String hostname) {
		assertHostExists(hostname);
		return edgeHosts.getRules(hostname);
	}

	public HostRuleEntity getRule(String hostname, String ruleName) {
		assertHostExists(hostname);
		return edgeHosts.getRule(hostname, ruleName).orElseThrow(() ->
			new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName)
		);
	}

	public List<HostSimulatedMetricEntity> getSimulatedMetrics(String hostname) {
		assertHostExists(hostname);
		return edgeHosts.getSimulatedMetrics(hostname);
	}

	public HostSimulatedMetricEntity getSimulatedMetric(String hostname, String simulatedMetricName) {
		assertHostExists(hostname);
		return edgeHosts.getSimulatedMetric(hostname, simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
		);
	}

	public boolean hasEdgeHost(String hostname) {
		return edgeHosts.hasEdgeHost(hostname);
	}

	private void assertHostExists(String hostname) {
		if (!hasEdgeHost(hostname)) {
			throw new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname);
		}
	}

}
