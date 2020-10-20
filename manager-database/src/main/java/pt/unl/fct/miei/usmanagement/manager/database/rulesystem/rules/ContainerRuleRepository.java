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

package pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContainerRuleRepository extends JpaRepository<ContainerRuleEntity, Long> {

	Optional<ContainerRuleEntity> findByNameIgnoreCase(@Param("name") String name);

	@Query("select r "
		+ "from ContainerRuleEntity r join r.containers c "
		+ "where c.containerId = :containerId")
	List<ContainerRuleEntity> findByContainerId(@Param("containerId") String containerId);

	@Query("select r "
		+ "from ContainerRuleEntity r "
		+ "where r.generic = true")
	List<ContainerRuleEntity> findGenericContainerRules();

	@Query("select r "
		+ "from ContainerRuleEntity r "
		+ "where r.generic = true and r.name = :ruleName")
	Optional<ContainerRuleEntity> findGenericContainerRule(@Param("ruleName") String ruleName);

	@Query("select case when count(r) > 0 then true else false end "
		+ "from ContainerRuleEntity r "
		+ "where lower(r.name) = lower(:ruleName)")
	boolean hasRule(@Param("ruleName") String ruleName);

	@Query("select rc.containerCondition "
		+ "from ContainerRuleEntity r join r.conditions rc "
		+ "where r.name = :ruleName")
	List<ConditionEntity> getConditions(@Param("ruleName") String ruleName);

	@Query("select rc.containerCondition "
		+ "from ContainerRuleEntity r join r.conditions rc "
		+ "where r.name = :ruleName and rc.containerCondition.name = :conditionName")
	Optional<ConditionEntity> getCondition(@Param("ruleName") String ruleName,
										   @Param("conditionName") String conditionName);

	@Query("select c "
		+ "from ContainerRuleEntity r join r.containers c "
		+ "where r.name = :ruleName")
	List<ContainerEntity> getContainers(@Param("ruleName") String ruleName);

	@Query("select c "
		+ "from ContainerRuleEntity r join r.containers c "
		+ "where r.name = :ruleName and c.containerId = :containerId")
	Optional<ContainerEntity> getContainer(@Param("ruleName") String ruleName,
										   @Param("containerId") String containerId);

}
