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

package pt.unl.fct.miei.usmanagement.manager.database.monitoring;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;

@Repository
public interface ContainerSimulatedMetricsRepository extends JpaRepository<ContainerSimulatedMetricEntity, Long> {

  Optional<ContainerSimulatedMetricEntity> findByNameIgnoreCase(@Param("name") String name);

  @Query("select m "
      + "from ContainerSimulatedMetricEntity m join m.containers c "
      + "where c.containerId = :containerId")
  List<ContainerSimulatedMetricEntity> findByContainer(@Param("containerId") String containerId);

  @Query("select m "
      + "from ContainerSimulatedMetricEntity m join m.containers c "
      + "where c.containerId = :containerId and lower(m.field.name) = lower(:field)")
  Optional<ContainerSimulatedMetricEntity> findByContainerAndField(@Param("containerId") String containerId,
                                                                   @Param("field") String field);

  @Query("select m "
      + "from ContainerSimulatedMetricEntity m "
      + "where m.generic = true and lower(m.field.name) = lower(:field)")
  Optional<ContainerSimulatedMetricEntity> findGenericByField(@Param("field") String field);

  @Query("select m "
      + "from ContainerSimulatedMetricEntity m "
      + "where m.generic = true")
  List<ContainerSimulatedMetricEntity> findGenericContainerSimulatedMetrics();

  @Query("select m "
      + "from ContainerSimulatedMetricEntity m "
      + "where m.generic = true and lower(m.name) = lower(:simulatedMetricName)")
  Optional<ContainerSimulatedMetricEntity> findGenericContainerSimulatedMetric(@Param("simulatedMetricName")
                                                                                   String simulatedMetricName);

  @Query("select case when count(m) > 0 then true else false end "
      + "from ContainerSimulatedMetricEntity m "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  boolean hasContainerSimulatedMetric(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select c "
      + "from ContainerSimulatedMetricEntity m join m.containers c "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  List<ContainerEntity> getContainers(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select c "
      + "from ContainerSimulatedMetricEntity m join m.containers c "
      + "where lower(m.name) = lower(:simulatedMetricName) and c.containerId = :containerId")
  Optional<ContainerEntity> getContainer(@Param("simulatedMetricName") String simulatedMetricName,
                                         @Param("containerId") String containerId);


}
