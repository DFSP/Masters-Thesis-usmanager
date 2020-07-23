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

package pt.unl.fct.microservicemanagement.managermaster.manager.containers;

import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.metrics.simulated.containers.SimulatedContainerMetricEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.rulesystem.rules.containers.ContainerRuleEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerEntity, Long> {

  Optional<ContainerEntity> findByContainerId(@Param("containerId") String containerId);

  List<ContainerEntity> findByHostname(String hostname);

  @Query("select r "
      + "from ContainerEntity c join c.containerRules r "
      + "where r.generic = false and c.containerId = :containerId")
  List<ContainerRuleEntity> getRules(@Param("containerId") String containerId);


  @Query("select r "
      + "from ContainerEntity c join c.containerRules r "
      + "where r.generic = false and c.containerId = :containerId and lower(r.name) = lower(:ruleName)")
  List<ContainerRuleEntity> getRule(@Param("containerId") String containerId,
                                    @Param("ruleName") String ruleName);

  @Query("select m "
      + "from ContainerEntity c join c.simulatedContainerMetrics m "
      + "where c.containerId = :containerId")
  List<SimulatedContainerMetricEntity> getSimulatedMetrics(@Param("containerId") String containerId);

  @Query("select m "
      + "from ContainerEntity c join c.simulatedContainerMetrics m "
      + "where c.containerId = :containerId and lower(m.name) = lower(:simulatedMetricName)")
  Optional<SimulatedContainerMetricEntity> getSimulatedMetric(@Param("containerId") String containerId,
                                                              @Param("simulatedMetricName") String simulatedMetricName);


  @Query("select case when count(c) > 0 then true else false end "
      + "from ContainerEntity c "
      + "where c.containerId = :containerId")
  boolean hasContainer(@Param("containerId") String containerId);

}
