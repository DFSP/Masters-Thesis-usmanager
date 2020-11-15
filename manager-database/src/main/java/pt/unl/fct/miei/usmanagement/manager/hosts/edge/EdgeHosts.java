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

package pt.unl.fct.miei.usmanagement.manager.hosts.edge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

import java.util.List;
import java.util.Optional;

public interface EdgeHosts extends JpaRepository<EdgeHost, Long> {

	@Query("select h "
		+ "from EdgeHost h join fetch h.managedByWorker "
		+ "where h.id = :id")
	Optional<EdgeHost> getEdgeHostWithWorker(@Param("id") Long id);

	Optional<EdgeHost> findByPublicDnsName(@Param("publicDnsName") String publicDnsName);

	Optional<EdgeHost> findByPublicDnsNameOrPublicIpAddress(@Param("publicDnsName") String publicDnsName,
															@Param("publicIpAddress") String publicIpAddress);

	@Query("select h "
		+ "from EdgeHost h "
		+ "where h.publicIpAddress = :publicIpAddress and h.privateIpAddress = :privateIpAddress")
	Optional<EdgeHost> findByAddress(@Param("publicIpAddress") String publicIpAddress,
									 @Param("privateIpAddress") String privateIpAddress);

	@Query("select r "
		+ "from EdgeHost h join h.hostRules r "
		+ "where r.generic = false and h.publicIpAddress = :publicIpAddress and h.privateIpAddress = :privateIpAddress")
	List<HostRule> getRules(@Param("publicIpAddress") String publicIpAddress, @Param("privateIpAddress") String privateIpAddress);


	@Query("select r "
		+ "from EdgeHost h join h.hostRules r "
		+ "where r.generic = false "
		+ "and h.publicIpAddress = :publicIpAddress and h.privateIpAddress = :privateIpAddress "
		+ "and r.name = :ruleName")
	Optional<HostRule> getRule(@Param("publicIpAddress") String publicIpAddress, @Param("privateIpAddress") String privateIpAddress,
							   @Param("ruleName") String ruleName);

	@Query("select m "
		+ "from EdgeHost h join h.simulatedHostMetrics m "
		+ "where h.publicIpAddress = :publicIpAddress and h.privateIpAddress = :privateIpAddress")
	List<HostSimulatedMetric> getSimulatedMetrics(@Param("publicIpAddress") String publicIpAddress,
												  @Param("privateIpAddress") String privateIpAddress);

	@Query("select m "
		+ "from EdgeHost h join h.simulatedHostMetrics m "
		+ "where h.publicIpAddress = :publicIpAddress and h.privateIpAddress = :privateIpAddress "
		+ "and m.name = :simulatedMetricName")
	Optional<HostSimulatedMetric> getSimulatedMetric(@Param("publicIpAddress") String publicIpAddress,
													 @Param("privateIpAddress") String privateIpAddress,
													 @Param("simulatedMetricName") String simulatedMetricName);

	@Query("select case when count(h) > 0 then true else false end "
		+ "from EdgeHost h "
		+ "where h.publicIpAddress = :publicIpAddress and h.privateIpAddress = :privateIpAddress")
	boolean hasEdgeHost(@Param("publicIpAddress") String publicIpAddress, @Param("privateIpAddress") String privateIpAddress);

}
