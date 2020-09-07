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

package pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloudHostRepository extends JpaRepository<CloudHostEntity, Long> {

	@Query("select h "
		+ "from CloudHostEntity h "
		+ "where h.id = :id")
	Optional<CloudHostEntity> getCloudHost(@Param("id") Long id);

	Optional<CloudHostEntity> findByInstanceId(@Param("instanceId") String instanceId);

	Optional<CloudHostEntity> findByPublicIpAddress(@Param("publicIpAddress") String publicIpAddress);

	Optional<CloudHostEntity> findByInstanceIdOrPublicDnsName(@Param("instanceId") String instanceId,
															  @Param("publicDnsName") String publicDnsName);

	Optional<CloudHostEntity> findByInstanceIdOrPublicIpAddress(@Param("instanceId") String instanceId,
																@Param("publicIpAddress") String publicIpAddress);

	@Query("select r "
		+ "from CloudHostEntity h join h.hostRules r "
		+ "where r.generic = false and (h.publicIpAddress = :hostname or h.instanceId = :hostname)")
	List<HostRuleEntity> getRules(@Param("hostname") String hostname);

	@Query("select r "
		+ "from CloudHostEntity h join h.hostRules r "
		+ "where r.generic = false and (h.publicIpAddress = :hostname or h.instanceId = :hostname) "
		+ "and r.name = :ruleName")
	Optional<HostRuleEntity> getRule(@Param("hostname") String hostname, @Param("ruleName") String ruleName);

	@Query("select m "
		+ "from CloudHostEntity h join h.simulatedHostMetrics m "
		+ "where h.publicIpAddress = :hostname or h.instanceId = :hostname")
	List<HostSimulatedMetricEntity> getSimulatedMetrics(@Param("hostname") String hostname);

	@Query("select m "
		+ "from CloudHostEntity h join h.simulatedHostMetrics m "
		+ "where (h.publicIpAddress = :hostname or h.instanceId = :hostname) and m.name = :simulatedMetricName")
	Optional<HostSimulatedMetricEntity> getSimulatedMetric(@Param("hostname") String hostname,
														   @Param("simulatedMetricName") String simulatedMetricName);

	@Query("select case when count(h) > 0 then true else false end "
		+ "from CloudHostEntity h "
		+ "where h.publicIpAddress = :hostname or h.instanceId = :hostname")
	boolean hasCloudHost(@Param("hostname") String hostname);

}
