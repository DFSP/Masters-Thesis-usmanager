/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package pt.unl.fct.microservicemanagement.mastermanager.manager.services;

import pt.unl.fct.microservicemanagement.mastermanager.manager.apps.AppEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.services.SimulatedServiceMetricEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.prediction.ServiceEventPredictionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.services.ServiceRuleEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

  @Query("select case when count(s) > 0 then true else false end "
      + "from ServiceEntity s "
      + "where s.id = :serviceId")
  boolean hasService(@Param("serviceId") long serviceId);

  @Query("select case when count(s) > 0 then true else false end "
      + "from ServiceEntity s "
      + "where lower(s.serviceName) = lower(:serviceName)")
  boolean hasService(@Param("serviceName") String serviceName);

  Optional<ServiceEntity> findByServiceNameIgnoreCase(@Param("serviceName") String serviceName);

  List<ServiceEntity> findByDockerRepositoryIgnoreCase(@Param("dockerRepository") String dockerRepository);

  @Query("select s.minReplicas "
      + "from ServiceEntity s "
      + "where lower(s.serviceName) = lower(:serviceName)")
  int getMinReplicas(@Param("serviceName") String serviceName);

  @Query("select s.maxReplicas "
      + "from ServiceEntity s "
      + "where lower(s.serviceName) = lower(:serviceName)")
  int getMaxReplicas(@Param("serviceName") String serviceName);

  @Query("select a.app "
      + "from ServiceEntity s join s.appServices a "
      + "where lower(s.serviceName) = lower(:serviceName)")
  List<AppEntity> getApps(@Param("serviceName") String serviceName);

  @Query("select a.app "
      + "from ServiceEntity s join s.appServices a "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(a.app.name) = lower(:appName)")
  Optional<AppEntity> getApp(@Param("serviceName") String serviceName, String appName);

  @Query("select d.dependency "
      + "from ServiceEntity s join s.dependencies d "
      + "where lower(s.serviceName) = lower(:serviceName)")
  List<ServiceEntity> getDependencies(@Param("serviceName") String serviceName);

  @Query("select d.dependency "
      + "from ServiceEntity s join s.dependencies d "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(d.dependency.serviceName) = lower(:dependencyName)")
  Optional<ServiceEntity> getDependency(@Param("serviceName") String serviceName,
                                        @Param("dependencyName") String dependencyName);

  @Query("select case when count(d) > 0 then true else false end "
      + "from ServiceEntity s join s.dependencies d "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(d.dependency.serviceName) = lower(:dependencyName)")
  boolean dependsOn(@Param("serviceName") String serviceName, @Param("dependencyName") String dependencyName);

  @Query("select d.dependency "
      + "from ServiceEntity s join s.dependencies d "
      + "where lower(s.serviceName) = lower(:serviceName) and d.dependency.serviceType = :serviceType")
  List<ServiceEntity> getDependenciesByType(@Param("serviceName") String serviceName,
                                            @Param("serviceType") ServiceType serviceType);

  @Query("select d.service "
      + "from ServiceEntity s join s.dependents d "
      + "where lower(s.serviceName) = lower(:serviceName)")
  List<ServiceEntity> getDependents(@Param("serviceName") String serviceName);

  //TODO confirm correctness
  @Query("select d.service "
      + "from ServiceEntity s join s.dependents d "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(d.service.serviceName) = lower(:dependentName)")
  Optional<ServiceEntity> getDependent(@Param("serviceName") String serviceName,
                                       @Param("dependentName") String dependentName);

  @Query("select r "
      + "from ServiceEntity s join s.serviceRules r "
      + "where r.generic = false and lower(s.serviceName) = lower(:serviceName)")
  List<ServiceRuleEntity> getRules(@Param("serviceName") String serviceName);

  @Query("select r "
      + "from ServiceEntity s join s.serviceRules r "
      + "where r.generic = false and lower(s.serviceName) = lower(:serviceName) and lower(r.name) = lower(:ruleName)")
  Optional<ServiceRuleEntity> getRule(@Param("serviceName") String serviceName, @Param("ruleName") String ruleName);

  @Query("select p "
      + "from ServiceEntity s join s.eventPredictions p "
      + "where lower(s.serviceName) = lower(:serviceName)")
  List<ServiceEventPredictionEntity> getPredictions(@Param("serviceName") String serviceName);

  @Query("select p "
      + "from ServiceEntity s join s.eventPredictions p "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(p.name) = lower(:eventPredictionName)")
  Optional<ServiceEventPredictionEntity> getPrediction(@Param("serviceName") String serviceName,
                                                       @Param("eventPredictionName") String eventPredictionName);

  @Query("select m "
      + "from ServiceEntity s join s.simulatedServiceMetrics m "
      + "where lower(s.serviceName) = lower(:serviceName)")
  List<SimulatedServiceMetricEntity> getSimulatedMetrics(@Param("serviceName") String serviceName);

  @Query("select m "
      + "from ServiceEntity s join s.simulatedServiceMetrics m "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(m.name) = lower(:simulatedMetricName)")
  Optional<SimulatedServiceMetricEntity> getSimulatedMetric(@Param("serviceName") String serviceName,
                                                            @Param("simulatedMetricName") String simulatedMetricName);

}
