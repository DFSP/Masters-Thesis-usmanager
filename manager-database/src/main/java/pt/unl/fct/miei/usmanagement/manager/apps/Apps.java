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

package pt.unl.fct.miei.usmanagement.manager.apps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceOrder;

import java.util.List;
import java.util.Optional;

public interface Apps extends JpaRepository<App, Long> {

	@Query("select a "
		+ "from App a left join fetch a.appServices left join fetch a.appRules left join fetch a.simulatedAppMetrics "
		+ "where lower(a.name) = lower(:appName)")
	Optional<App> findByNameWithEntities(String appName);

	@Query("select case when count(a) > 0 then true else false end "
		+ "from App a "
		+ "where lower(a.name) = lower(:appName)")
	boolean hasApp(@Param("appName") String appName);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.services"
		+ ".ServiceOrder(s.service, s.launchOrder) "
		+ "from App a left join a.appServices s "
		+ "where lower(a.name) = lower(:appName) order by s.launchOrder")
	List<ServiceOrder> getServicesOrder(@Param("appName") String appName);

	Optional<App> findByNameIgnoreCase(@Param("name") String name);

	@Query("select s "
		+ "from App a left join a.appServices s "
		+ "where lower(a.name) = lower(:appName)")
	List<AppService> getServices(@Param("appName") String appName);

	@Query("select r "
		+ "from App c left join c.appRules r "
		+ "where c.name = :appName")
	List<AppRule> getRules(@Param("appName") String appName);

	@Query("select r "
		+ "from App c left join c.appRules r "
		+ "where c.name = :appName and lower(r.name) = lower(:ruleName)")
	List<AppRule> getRule(@Param("appName") String appName,
						  @Param("ruleName") String ruleName);

	@Query("select m "
		+ "from App c left join c.simulatedAppMetrics m "
		+ "where c.name = :appName")
	List<AppSimulatedMetric> getSimulatedMetrics(@Param("appName") String appName);

	@Query("select m "
		+ "from App c left join c.simulatedAppMetrics m "
		+ "where c.name = :appName and lower(m.name) = lower(:simulatedMetricName)")
	Optional<AppSimulatedMetric> getSimulatedMetric(@Param("appName") String appName,
													@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select a "
		+ "from App a left join fetch a.simulatedAppMetrics join fetch a.appRules join fetch a.appServices")
	List<App> getAppsAndRelations();
}


