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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring.metrics.simulated.hosts;

import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.edge.EdgeHostEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulatedHostMetricsRepository extends JpaRepository<SimulatedHostMetricEntity, Long> {

  Optional<SimulatedHostMetricEntity> findByNameIgnoreCase(@Param("name") String name);

  @Query("select m "
      + "from SimulatedHostMetricEntity m join m.cloudHosts c join m.edgeHosts e "
      + "where c.publicIpAddress = :hostname or e.publicDnsName = :hostname")
  List<SimulatedHostMetricEntity> findByHost(@Param("hostname") String hostname);

  @Query("select m "
      + "from SimulatedHostMetricEntity m join m.cloudHosts c join m.edgeHosts e "
      + "where (c.publicIpAddress = :hostname or e.publicDnsName = :hostname or e.publicIpAddress = :hostname) "
      + "and lower(m.field.name) = lower(:field)")
  Optional<SimulatedHostMetricEntity> findByHostAndField(@Param("hostname") String hostname,
                                                         @Param("field") String field);

  @Query("select m "
      + "from SimulatedHostMetricEntity m join m.cloudHosts h "
      + "where h.publicIpAddress = :hostname")
  List<SimulatedHostMetricEntity> findByCloudHost(@Param("hostname") String hostname);

  @Query("select m "
      + "from SimulatedHostMetricEntity m join m.cloudHosts h "
      + "where h.publicIpAddress = :hostname and lower(m.field.name) = lower(:field)")
  Optional<SimulatedHostMetricEntity> findByCloudHostAndField(@Param("field") String field,
                                                              @Param("hostname") String hostname);

  @Query("select m "
      + "from SimulatedHostMetricEntity m join m.edgeHosts h "
      + "where h.publicDnsName = :hostname or h.publicIpAddress = :hostname")
  List<SimulatedHostMetricEntity> findByEdgeHost(@Param("hostname") String hostname);

  @Query("select m "
      + "from SimulatedHostMetricEntity m join m.edgeHosts h "
      + "where h.publicDnsName = :publicDnsName and lower(m.field.name) = lower(:field)")
  Optional<SimulatedHostMetricEntity> findByEdgeHostAndField(@Param("field") String field,
                                                             @Param("publicDnsName") String publicDnsName);

  @Query("select m "
      + "from SimulatedHostMetricEntity m "
      + "where m.generic = true and lower(m.field.name) = lower(:field)")
  Optional<SimulatedHostMetricEntity> findGenericByField(@Param("field") String field);

  @Query("select m "
      + "from SimulatedHostMetricEntity m "
      + "where m.generic = true")
  List<SimulatedHostMetricEntity> findGenericSimulatedHostMetrics();

  @Query("select m "
      + "from SimulatedHostMetricEntity m "
      + "where m.generic = true and lower(m.name) = lower(:simulatedMetricName)")
  Optional<SimulatedHostMetricEntity> findGenericSimulatedHostMetric(@Param("simulatedMetricName")
                                                                         String simulatedMetricName);

  @Query("select case when count(m) > 0 then true else false end "
      + "from SimulatedHostMetricEntity m "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  boolean hasSimulatedHostMetric(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select h "
      + "from SimulatedHostMetricEntity m join m.cloudHosts h "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  List<CloudHostEntity> getCloudHosts(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select h "
      + "from SimulatedHostMetricEntity m join m.cloudHosts h "
      + "where lower(m.name) = lower(:simulatedMetricName) and h.instanceId = :instanceId")
  Optional<CloudHostEntity> getCloudHost(@Param("simulatedMetricName") String simulatedMetricName,
                                         @Param("instanceId") String instanceId);

  @Query("select h "
      + "from SimulatedHostMetricEntity m join m.edgeHosts h "
      + "where lower(m.name) = lower(:simulatedMetricName)")
  List<EdgeHostEntity> getEdgeHosts(@Param("simulatedMetricName") String simulatedMetricName);

  @Query("select h "
      + "from SimulatedHostMetricEntity m join m.edgeHosts h "
      + "where lower(m.name) = lower(:simulatedMetricName) "
      + "and (h.publicDnsName = :hostname or h.publicIpAddress = :hostname)")
  Optional<EdgeHostEntity> getEdgeHost(@Param("simulatedMetricName") String simulatedMetricName,
                                       @Param("hostname") String hostname);

}
