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

package pt.unl.fct.miei.usmanagement.manager.metrics.simulated;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;

import java.util.List;
import java.util.Optional;

public interface ContainerSimulatedMetrics extends JpaRepository<ContainerSimulatedMetric, Long> {

	Optional<ContainerSimulatedMetric> findByNameIgnoreCase(@Param("name") String name);

	@Query("select m "
		+ "from ContainerSimulatedMetric m left join m.containers c "
		+ "where c.id = :containerId")
	List<ContainerSimulatedMetric> findByContainer(@Param("containerId") String containerId);

	@Query("select m "
		+ "from ContainerSimulatedMetric m left join m.containers c "
		+ "where c.id = :containerId and lower(m.field.name) = lower(:field)")
	Optional<ContainerSimulatedMetric> findByContainerAndField(@Param("containerId") String containerId,
															   @Param("field") String field);

	@Query("select case when count(m) > 0 then true else false end "
		+ "from ContainerSimulatedMetric m "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	boolean hasContainerSimulatedMetric(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select c "
		+ "from ContainerSimulatedMetric m join m.containers c "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	List<Container> getContainers(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select c "
		+ "from ContainerSimulatedMetric m join m.containers c "
		+ "where lower(m.name) = lower(:simulatedMetricName) and c.id = :containerId")
	Optional<Container> getContainer(@Param("simulatedMetricName") String simulatedMetricName,
									 @Param("containerId") String containerId);


}
