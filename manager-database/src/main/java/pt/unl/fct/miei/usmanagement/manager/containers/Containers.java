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

package pt.unl.fct.miei.usmanagement.manager.containers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;

import java.util.List;
import java.util.Optional;

public interface Containers extends JpaRepository<Container, String> {

	Optional<Container> findByIdStartingWith(@Param("containerId") String containerId);

	List<Container> findByPublicIpAddressAndPrivateIpAddress(String publicIpAddress, String privateIpAddress);

	@Query("select r "
		+ "from Container c join c.containerRules r "
		+ "where c.id = :containerId")
	List<ContainerRule> getRules(@Param("containerId") String containerId);

	@Query("select r "
		+ "from Container c join c.containerRules r "
		+ "where c.id = :containerId and lower(r.name) = lower(:ruleName)")
	List<ContainerRule> getRule(@Param("containerId") String containerId,
								@Param("ruleName") String ruleName);

	@Query("select m "
		+ "from Container c join c.simulatedContainerMetrics m "
		+ "where c.id = :containerId")
	List<ContainerSimulatedMetric> getSimulatedMetrics(@Param("containerId") String containerId);

	@Query("select m "
		+ "from Container c join c.simulatedContainerMetrics m "
		+ "where c.id = :containerId and lower(m.name) = lower(:simulatedMetricName)")
	Optional<ContainerSimulatedMetric> getSimulatedMetric(@Param("containerId") String containerId,
														  @Param("simulatedMetricName") String simulatedMetricName);

	@Query("select case when count(c) > 0 then true else false end "
		+ "from Container c "
		+ "where c.id = :containerId")
	boolean hasContainer(@Param("containerId") String containerId);

	List<Container> findByManagerId(String managerId);

	@Query("select c "
		+ "from Container c left join fetch c.containerRules left join fetch c.simulatedContainerMetrics")
	List<Container> getContainersAndEntities();
}
