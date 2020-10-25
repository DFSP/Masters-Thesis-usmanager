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

package pt.unl.fct.miei.usmanagement.manager.monitoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.ServiceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceSimulatedMetricsRepository extends JpaRepository<ServiceSimulatedMetricEntity, Long> {

	Optional<ServiceSimulatedMetricEntity> findByNameIgnoreCase(@Param("name") String name);

	@Query("select m "
		+ "from ServiceSimulatedMetricEntity m join m.services s "
		+ "where s.serviceName = :serviceName")
	List<ServiceSimulatedMetricEntity> findByService(@Param("serviceName") String serviceName);

	@Query("select m "
		+ "from ServiceSimulatedMetricEntity m join m.services s "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(m.field.name) = lower(:field)")
	Optional<ServiceSimulatedMetricEntity> findByServiceAndField(@Param("serviceName") String serviceName,
																 @Param("field") String field);

	@Query("select m "
		+ "from ServiceSimulatedMetricEntity m "
		+ "where m.generic = true and lower(m.field.name) = lower(:field)")
	Optional<ServiceSimulatedMetricEntity> findGenericByField(@Param("field") String field);

	@Query("select m "
		+ "from ServiceSimulatedMetricEntity m "
		+ "where m.generic = true")
	List<ServiceSimulatedMetricEntity> findGenericServiceSimulatedMetrics();

	@Query("select m "
		+ "from ServiceSimulatedMetricEntity m "
		+ "where m.generic = true and lower(m.name) = lower(:simulatedMetricName)")
	Optional<ServiceSimulatedMetricEntity> findGenericServiceSimulatedMetric(@Param("simulatedMetricName")
																				 String simulatedMetricName);

	@Query("select case when count(m) > 0 then true else false end "
		+ "from ServiceSimulatedMetricEntity m "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	boolean hasServiceSimulatedMetric(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select s "
		+ "from ServiceSimulatedMetricEntity m join m.services s "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	List<ServiceEntity> getServices(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select s "
		+ "from ServiceSimulatedMetricEntity m join m.services s "
		+ "where lower(m.name) = lower(:simulatedMetricName) and s.serviceName = :serviceName")
	Optional<ServiceEntity> getService(@Param("simulatedMetricName") String simulatedMetricName,
									   @Param("serviceName") String serviceName);

}
