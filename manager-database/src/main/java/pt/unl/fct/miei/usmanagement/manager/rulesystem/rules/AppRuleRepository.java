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

package pt.unl.fct.miei.usmanagement.manager.rulesystem.rules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.ConditionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRuleRepository extends JpaRepository<AppRuleEntity, Long> {

	@Query("select r "
		+ "from AppRuleEntity r "
		+ "where r.name = :ruleName")
	Optional<AppRuleEntity> findAppRule(@Param("ruleName") String ruleName);

	Optional<AppRuleEntity> findByNameIgnoreCase(@Param("name") String name);

	@Query("select r "
		+ "from AppRuleEntity r join r.apps s "
		+ "where s.name = :name")
	List<AppRuleEntity> findByAppName(@Param("name") String name);

	@Query("select case when count(r) > 0 then true else false end "
		+ "from AppRuleEntity r "
		+ "where lower(r.name) = lower(:ruleName)")
	boolean hasRule(@Param("ruleName") String ruleName);

	@Query("select rc.appCondition "
		+ "from AppRuleEntity r join r.conditions rc "
		+ "where r.name = :ruleName")
	List<ConditionEntity> getConditions(@Param("ruleName") String ruleName);

	@Query("select rc.appCondition "
		+ "from AppRuleEntity r join r.conditions rc "
		+ "where r.name = :ruleName and rc.appCondition.name = :conditionName")
	Optional<ConditionEntity> getCondition(@Param("ruleName") String ruleName,
										   @Param("conditionName") String conditionName);

	@Query("select s "
		+ "from AppRuleEntity r join r.apps s "
		+ "where r.name = :ruleName")
	List<AppEntity> getApps(@Param("ruleName") String ruleName);

	@Query("select s "
		+ "from AppRuleEntity r join r.apps s "
		+ "where r.name = :ruleName and s.name = :name")
	Optional<AppEntity> getApp(@Param("ruleName") String ruleName, @Param("name") String name);

}
