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

package pt.unl.fct.microservicemanagement.mastermanager.manager.ruleSystem.rules.hosts;

import pt.unl.fct.microservicemanagement.mastermanager.manager.host.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.host.edge.EdgeHostEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.ruleSystem.condition.ConditionEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HostRuleRepository extends CrudRepository<HostRuleEntity, Long> {

  @Query("select r "
      + "from HostRuleEntity r "
      + "where r.name = :ruleName")
  Optional<HostRuleEntity> findHostRule(@Param("ruleName") String ruleName);

  Optional<HostRuleEntity> findByNameIgnoreCase(@Param("name") String name);

  @Query("select r "
      + "from HostRuleEntity r join r.cloudHosts h "
      + "where h.instanceId = :instanceId")
  List<HostRuleEntity> findByCloudInstanceId(@Param("instanceId") String instanceId);

  @Query("select r "
      + "from HostRuleEntity r join r.edgeHosts h "
      + "where h.hostname = :hostname")
  List<HostRuleEntity> findByEdgeHostname(@Param("hostname") String hostname);

  @Query("select r "
      + "from HostRuleEntity r "
      + "where r.generic = true")
  List<HostRuleEntity> findGenericHostRules();

  @Query("select r "
      + "from HostRuleEntity r "
      + "where r.generic = true and r.name = :ruleName")
  Optional<HostRuleEntity> findGenericHostRule(@Param("ruleName") String ruleName);

  @Query("select case when count(r) > 0 then true else false end "
      + "from HostRuleEntity r "
      + "where r.name = :ruleName")
  boolean hasRule(@Param("ruleName") String ruleName);

  @Query("select rc.hostCondition "
      + "from HostRuleEntity r join r.conditions rc "
      + "where r.name = :ruleName")
  List<ConditionEntity> getConditions(@Param("ruleName") String ruleName);

  @Query("select rc.hostCondition "
      + "from HostRuleEntity r join r.conditions rc "
      + "where r.name = :ruleName and rc.hostCondition.name = :conditionName")
  Optional<ConditionEntity> getCondition(@Param("ruleName") String ruleName,
                                         @Param("conditionName") String conditionName);

  @Query("select h "
      + "from HostRuleEntity r join r.cloudHosts h "
      + "where r.name = :ruleName")
  List<CloudHostEntity> getCloudHosts(@Param("ruleName") String ruleName);

  @Query("select h "
      + "from HostRuleEntity r join r.cloudHosts h "
      + "where r.name = :ruleName and h.instanceId = :instanceId")
  Optional<CloudHostEntity> getCloudHost(@Param("ruleName") String ruleName, @Param("instanceId") String instanceId);

  @Query("select h "
      + "from HostRuleEntity r join r.edgeHosts h "
      + "where r.name = :ruleName")
  List<EdgeHostEntity> getEdgeHosts(@Param("ruleName") String ruleName);

  @Query("select h "
      + "from HostRuleEntity r join r.edgeHosts h "
      + "where r.name = :ruleName and h.hostname = :hostname")
  Optional<EdgeHostEntity> getEdgeHost(@Param("ruleName") String ruleName, @Param("hostname") String hostname);

}
