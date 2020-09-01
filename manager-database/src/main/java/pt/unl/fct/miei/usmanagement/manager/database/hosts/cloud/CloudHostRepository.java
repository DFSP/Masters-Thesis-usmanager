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

package pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;

@Repository
public interface CloudHostRepository extends JpaRepository<CloudHostEntity, Long> {

  Optional<CloudHostEntity> findByInstanceId(@Param("instanceId") String instanceId);

  Optional<CloudHostEntity> findByPublicIpAddress(@Param("publicIpAddress") String publicIpAddress);

  Optional<CloudHostEntity> findByInstanceIdOrPublicDnsName(@Param("instanceId") String instanceId,
                                                            @Param("publicDnsName") String publicDnsName);

  Optional<CloudHostEntity> findByInstanceIdOrPublicIpAddress(@Param("instanceId") String instanceId,
                                                              @Param("publicIpAddress") String publicIpAddress);

  @Query("select r "
      + "from CloudHostEntity h join h.hostRules r "
      + "where r.generic = false and h.instanceId = :instanceId")
  List<HostRuleEntity> getRules(@Param("instanceId") String instanceId);

  @Query("select r "
      + "from CloudHostEntity h join h.hostRules r "
      + "where r.generic = false and h.instanceId = :instanceId and r.name = :ruleName")
  Optional<HostRuleEntity> getRule(@Param("instanceId") String instanceId, @Param("ruleName") String ruleName);

  @Query("select m "
      + "from CloudHostEntity h join h.simulatedHostMetrics m "
      + "where h.instanceId = :instanceId")
  List<HostSimulatedMetricEntity> getSimulatedMetrics(@Param("instanceId") String instanceId);

  @Query("select m "
      + "from CloudHostEntity h join h.simulatedHostMetrics m "
      + "where h.instanceId = :instanceId and m.name = :simulatedMetricName")
  Optional<HostSimulatedMetricEntity> getSimulatedMetric(@Param("instanceId") String instanceId,
                                                         @Param("simulatedMetricName") String simulatedMetricName);

  @Query("select case when count(h) > 0 then true else false end "
      + "from CloudHostEntity h "
      + "where h.instanceId = :instanceId")
  boolean hasCloudHost(@Param("instanceId") String instanceId);

  @Query("select case when count(h) > 0 then true else false end "
      + "from CloudHostEntity h "
      + "where h.publicIpAddress = :hostname")
  boolean hasCloudHostByHostname(@Param("hostname") String hostname);

}
