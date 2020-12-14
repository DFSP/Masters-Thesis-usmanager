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

package pt.unl.fct.miei.usmanagement.manager.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;

import java.util.List;
import java.util.Optional;

public interface Services extends JpaRepository<Service, Long> {

	@Query("select case when count(s) > 0 then true else false end "
		+ "from Service s "
		+ "where s.id = :serviceId")
	boolean hasService(@Param("serviceId") long serviceId);

	@Query("select case when count(s) > 0 then true else false end "
		+ "from Service s "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	boolean hasService(@Param("serviceName") String serviceName);

	Optional<Service> findByServiceNameIgnoreCase(@Param("serviceName") String serviceName);

	List<Service> findByDockerRepositoryIgnoreCase(@Param("dockerRepository") String dockerRepository);

	@Query("select s.minimumReplicas "
		+ "from Service s "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	int getMinimumReplicas(@Param("serviceName") String serviceName);

	@Query("select s.maximumReplicas "
		+ "from Service s "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	int getMaximumReplicas(@Param("serviceName") String serviceName);

	@Query("select a.app "
		+ "from Service s join s.appServices a "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	List<App> getApps(@Param("serviceName") String serviceName);

	@Query("select a.app "
		+ "from Service s join s.appServices a "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(a.app.name) = lower(:appName)")
	Optional<App> getApp(@Param("serviceName") String serviceName, String appName);

	@Query("select d.dependency "
		+ "from Service s join s.dependencies d "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	List<Service> getDependenciesServices(@Param("serviceName") String serviceName);

	@Query("select d.dependency "
		+ "from Service s join s.dependencies d "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(d.dependency.serviceName) = lower(:dependencyName)")
	Optional<Service> getDependency(@Param("serviceName") String serviceName,
									@Param("dependencyName") String dependencyName);

	@Query("select case when count(d) > 0 then true else false end "
		+ "from Service s join s.dependencies d "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(d.dependency.serviceName) = lower(:dependencyName)")
	boolean dependsOn(@Param("serviceName") String serviceName, @Param("dependencyName") String dependencyName);

	@Query("select d.dependency "
		+ "from Service s join s.dependencies d "
		+ "where lower(s.serviceName) = lower(:serviceName) and d.dependency.serviceType = :serviceType")
	List<Service> getDependenciesByType(@Param("serviceName") String serviceName,
										@Param("serviceType") ServiceTypeEnum serviceType);

	@Query("select d.service "
		+ "from Service s join s.dependents d "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	List<Service> getDependents(@Param("serviceName") String serviceName);

	//TODO confirm correctness
	@Query("select d.service "
		+ "from Service s join s.dependents d "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(d.service.serviceName) = lower(:dependentName)")
	Optional<Service> getDependent(@Param("serviceName") String serviceName,
								   @Param("dependentName") String dependentName);

	@Query("select r "
		+ "from Service s join s.serviceRules r "
		+ "where r.generic = false and lower(s.serviceName) = lower(:serviceName)")
	List<ServiceRule> getRules(@Param("serviceName") String serviceName);

	@Query("select r "
		+ "from Service s join s.serviceRules r "
		+ "where r.generic = false and lower(s.serviceName) = lower(:serviceName) and lower(r.name) = lower(:ruleName)")
	Optional<ServiceRule> getRule(@Param("serviceName") String serviceName, @Param("ruleName") String ruleName);

	@Query("select p "
		+ "from Service s join s.eventPredictions p "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	List<ServiceEventPrediction> getPredictions(@Param("serviceName") String serviceName);

	@Query("select p "
		+ "from Service s join s.eventPredictions p "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(p.name) = lower(:eventPredictionName)")
	Optional<ServiceEventPrediction> getPrediction(@Param("serviceName") String serviceName,
												   @Param("eventPredictionName") String eventPredictionName);

	@Query("select m "
		+ "from Service s join s.simulatedServiceMetrics m "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	List<ServiceSimulatedMetric> getSimulatedMetrics(@Param("serviceName") String serviceName);

	@Query("select m "
		+ "from Service s join s.simulatedServiceMetrics m "
		+ "where lower(s.serviceName) = lower(:serviceName) and lower(m.name) = lower(:simulatedMetricName)")
	Optional<ServiceSimulatedMetric> getSimulatedMetric(@Param("serviceName") String serviceName,
														@Param("simulatedMetricName") String simulatedMetricName);

	@Query("select s "
		+ "from Service s "
		+ "left join fetch s.appServices "
		+ "left join fetch s.dependencies "
		+ "left join fetch s.dependents "
		+ "left join fetch s.eventPredictions "
		+ "left join fetch s.serviceRules "
		+ "left join fetch s.simulatedServiceMetrics "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	Optional<Service> findByServiceNameIgnoreCaseWithEntities(String serviceName);

	@Query("select d "
		+ "from Service s join s.dependencies d "
		+ "where lower(s.serviceName) = lower(:serviceName)")
	List<ServiceDependency> getDependencies(String serviceName);
}
