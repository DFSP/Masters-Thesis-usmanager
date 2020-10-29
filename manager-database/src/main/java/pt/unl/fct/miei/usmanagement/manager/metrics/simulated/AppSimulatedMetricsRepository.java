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
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppSimulatedMetricsRepository extends JpaRepository<AppSimulatedMetricEntity, Long> {

	Optional<AppSimulatedMetricEntity> findByNameIgnoreCase(@Param("name") String name);

	@Query("select m "
		+ "from AppSimulatedMetricEntity m left join m.apps s "
		+ "where s.name = :name")
	List<AppSimulatedMetricEntity> findByApp(@Param("name") String name);

	@Query("select m "
		+ "from AppSimulatedMetricEntity m join m.apps s "
		+ "where lower(s.name) = lower(:name) and lower(m.field.name) = lower(:field)")
	Optional<AppSimulatedMetricEntity> findByAppAndField(@Param("name") String name,
														 @Param("field") String field);

	@Query("select case when count(m) > 0 then true else false end "
		+ "from AppSimulatedMetricEntity m "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	boolean hasAppSimulatedMetric(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select s "
		+ "from AppSimulatedMetricEntity m join m.apps s "
		+ "where lower(m.name) = lower(:simulatedMetricName)")
	List<AppEntity> getApps(@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select s "
		+ "from AppSimulatedMetricEntity m join m.apps s "
		+ "where lower(m.name) = lower(:simulatedMetricName) and s.name = :name")
	Optional<AppEntity> getApp(@Param("simulatedMetricName") String simulatedMetricName, @Param("name") String name);

}
