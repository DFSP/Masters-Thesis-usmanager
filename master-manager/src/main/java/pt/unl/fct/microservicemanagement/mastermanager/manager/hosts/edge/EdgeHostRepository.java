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

package pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.edge;

import pt.unl.fct.microservicemanagement.mastermanager.manager.location.RegionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.hosts.SimulatedHostMetricEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.hosts.HostRuleEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EdgeHostRepository extends JpaRepository<EdgeHostEntity, Long> {

  @Query("select h "
      + "from EdgeHostEntity h "
      + "where h.publicDnsName = :hostname or h.publicIpAddress = :hostname")
  Optional<EdgeHostEntity> findEdgeHost(@Param("hostname") String hostname);

  List<EdgeHostEntity> findByRegion(@Param("region") RegionEntity region);

  List<EdgeHostEntity> findByCountry(@Param("country") String country);

  List<EdgeHostEntity> findByCity(@Param("city") String city);

  @Query("select r "
      + "from EdgeHostEntity h join h.hostRules r "
      + "where r.generic = false and (h.publicDnsName = :hostname or h.publicIpAddress = :hostname)")
  List<HostRuleEntity> getRules(@Param("hostname") String hostname);


  @Query("select r "
      + "from EdgeHostEntity h join h.hostRules r "
      + "where r.generic = false "
      + "and (h.publicDnsName = :hostname or h.publicIpAddress = :hostname) "
      + "and r.name = :ruleName")
  Optional<HostRuleEntity> getRule(@Param("hostname") String hostname, @Param("ruleName") String ruleName);

  @Query("select m "
      + "from EdgeHostEntity h join h.simulatedHostMetrics m "
      + "where h.publicDnsName = :hostname or h.publicIpAddress = :hostname")
  List<SimulatedHostMetricEntity> getSimulatedMetrics(@Param("hostname") String hostname);

  @Query("select m "
      + "from EdgeHostEntity h join h.simulatedHostMetrics m "
      + "where (h.publicDnsName = :hostname or h.publicIpAddress = :hostname) "
      + "and m.name = :simulatedMetricName")
  Optional<SimulatedHostMetricEntity> getSimulatedMetric(@Param("hostname") String hostname,
                                                         @Param("simulatedMetricName") String simulatedMetricName);

  @Query("select case when count(h) > 0 then true else false end "
      + "from EdgeHostEntity h "
      + "where (h.publicDnsName = :hostname or h.publicIpAddress = :hostname)")
  boolean hasEdgeHost(@Param("hostname") String hostname);

}
