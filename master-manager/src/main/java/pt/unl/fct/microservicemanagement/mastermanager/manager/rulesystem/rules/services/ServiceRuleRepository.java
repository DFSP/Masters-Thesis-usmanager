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

package pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.rules.services;

import pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServiceEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRuleRepository extends JpaRepository<ServiceRuleEntity, Long> {

  @Query("select r "
      + "from ServiceRuleEntity r "
      + "where r.name = :ruleName")
  Optional<ServiceRuleEntity> findServiceRule(@Param("ruleName") String ruleName);

  Optional<ServiceRuleEntity> findByNameIgnoreCase(@Param("name") String name);

  @Query("select r "
      + "from ServiceRuleEntity r join r.services s "
      + "where r.generic = false and s.serviceName = :serviceName")
  List<ServiceRuleEntity> findByServiceName(@Param("serviceName") String serviceName);

  @Query("select r "
      + "from ServiceRuleEntity r "
      + "where r.generic = true")
  List<ServiceRuleEntity> findGenericServiceRules();

  @Query("select case when count(r) > 0 then true else false end "
      + "from ServiceRuleEntity r "
      + "where lower(r.name) = lower(:ruleName)")
  boolean hasRule(@Param("ruleName") String ruleName);

  @Query("select rc.serviceCondition "
      + "from ServiceRuleEntity r join r.conditions rc "
      + "where r.name = :ruleName")
  List<ConditionEntity> getConditions(@Param("ruleName") String ruleName);

  @Query("select rc.serviceCondition "
      + "from ServiceRuleEntity r join r.conditions rc "
      + "where r.name = :ruleName and rc.serviceCondition.name = :conditionName")
  Optional<ConditionEntity> getCondition(@Param("ruleName") String ruleName,
                                         @Param("conditionName") String conditionName);

  @Query("select s "
      + "from ServiceRuleEntity r join r.services s "
      + "where r.name = :ruleName")
  List<ServiceEntity> getServices(@Param("ruleName") String ruleName);

  @Query("select s "
      + "from ServiceRuleEntity r join r.services s "
      + "where r.name = :ruleName and s.serviceName = :serviceName")
  Optional<ServiceEntity> getService(@Param("ruleName") String ruleName, @Param("serviceName") String serviceName);

}
