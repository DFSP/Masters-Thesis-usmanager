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

package pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.metrics.simulated.services;

import pt.unl.fct.microservicemanagement.managermaster.manager.services.ServiceEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulatedServiceMetricsRepository extends JpaRepository<SimulatedServiceMetricEntity, Long> {

  Optional<SimulatedServiceMetricEntity> findByNameIgnoreCase(@Param("name") String name);

  @Query("select m "
      + "from SimulatedServiceMetricEntity m join m.services s "
      + "where lower(s.serviceName) = lower(:serviceName) and lower(m.field.name) = lower(:field)")
  Optional<SimulatedServiceMetricEntity> findByServiceAndField(@Param("field") String field,
                                                               @Param("serviceName") String serviceName);

  @Query("select m "
      + "from SimulatedServiceMetricEntity m "
      + "where m.generic = true and lower(m.field.name) = lower(:field)")
  Optional<SimulatedServiceMetricEntity> findGenericByField(@Param("field") String field);

  @Query("select m "
      + "from SimulatedServiceMetricEntity m "
      + "where m.generic = true")
  List<SimulatedServiceMetricEntity> findGenericSimulatedServiceMetrics();

  @Query("select m "
      + "from SimulatedServiceMetricEntity m "
      + "where m.generic = true and lower(m.name) = lower(:simulatedMetricName)")
  Optional<SimulatedServiceMetricEntity> findGenericSimulatedServiceMetric(@Param("simulatedMetricName")
                                                                               String simulatedMetricName);

  @Query("select case when count(m) > 0 then true else false end "
      + "from SimulatedServiceMetricEntity m "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  boolean hasSimulatedServiceMetric(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select s "
      + "from SimulatedServiceMetricEntity m join m.services s "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  List<ServiceEntity> getServices(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select s "
      + "from SimulatedServiceMetricEntity m join m.services s "
      + "where lower(m.name) = lower(:simulatedMetricName) and s.serviceName = :serviceName")
  Optional<ServiceEntity> getService(@Param("simulatedMetricName") String simulatedMetricName,
                                     @Param("serviceName") String serviceName);

}
