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
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceSimulatedMetrics extends JpaRepository<ServiceSimulatedMetric, Long> {

	Optional<ServiceSimulatedMetric> findByNameIgnoreCase(@Param("name") String name);

	@Query("select m "
		+ "from ServiceSimulatedMetric m join m.services s "
		+ "where m.generic = true or s.serviceName = :serviceName")
	List<ServiceSimulatedMetric> findByService(@Param("serviceName") String serviceName);

	@Query("select m "
		+ "from ServiceSimulatedMetric m join m.services s "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(m.field.name) = lower(:field)")
	Optional<ServiceSimulatedMetric> findByServiceAndField(@Param("serviceName") String serviceName,
														   @Param("field") String field);

	@Query("select case when count(m) > 0 then true else false end "
		+ "from ServiceSimulatedMetric m "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	boolean hasServiceSimulatedMetric(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select m "
		+ "from ServiceSimulatedMetric m "
		+ "where m.generic = true and lower(m.field.name) = lower(:field)")
	Optional<ServiceSimulatedMetric> findGenericByField(@Param("field") String field);

	@Query("select m "
		+ "from ServiceSimulatedMetric m "
		+ "where m.generic = true")
	List<ServiceSimulatedMetric> findGenericServiceSimulatedMetrics();

	@Query("select m "
		+ "from ServiceSimulatedMetric m "
		+ "where m.generic = true and lower(m.name) = lower(:simulatedMetricName)")
	Optional<ServiceSimulatedMetric> findGenericServiceSimulatedMetric(@Param("simulatedMetricName") String simulatedMetricName);


	@Query("select s "
		+ "from ServiceSimulatedMetric m join m.services s "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	List<Service> getServices(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select s "
		+ "from ServiceSimulatedMetric m join m.services s "
		+ "where lower(m.name) = lower(:simulatedMetricName) and s.serviceName = :serviceName")
	Optional<Service> getService(@Param("simulatedMetricName") String simulatedMetricName,
								 @Param("serviceName") String serviceName);

}
