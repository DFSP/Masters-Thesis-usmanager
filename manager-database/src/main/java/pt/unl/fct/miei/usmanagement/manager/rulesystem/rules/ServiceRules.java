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
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceRules extends JpaRepository<ServiceRule, Long> {

	@Query("select r "
		+ "from ServiceRule r "
		+ "where r.name = :ruleName")
	Optional<ServiceRule> findServiceRule(@Param("ruleName") String ruleName);

	Optional<ServiceRule> findByNameIgnoreCase(@Param("name") String name);

	@Query("select r "
		+ "from ServiceRule r join r.services s "
		+ "where r.generic = false and s.serviceName = :serviceName")
	List<ServiceRule> findByServiceName(@Param("serviceName") String serviceName);

	@Query("select r "
		+ "from ServiceRule r "
		+ "where r.generic = true")
	List<ServiceRule> findGenericServiceRules();

	@Query("select case when count(r) > 0 then true else false end "
		+ "from ServiceRule r "
		+ "where lower(r.name) = lower(:ruleName)")
	boolean hasRule(@Param("ruleName") String ruleName);

	@Query("select rc.serviceCondition "
		+ "from ServiceRule r join r.conditions rc "
		+ "where r.name = :ruleName")
	List<Condition> getConditions(@Param("ruleName") String ruleName);

	@Query("select rc.serviceCondition "
		+ "from ServiceRule r join r.conditions rc "
		+ "where r.name = :ruleName and rc.serviceCondition.name = :conditionName")
	Optional<Condition> getCondition(@Param("ruleName") String ruleName,
									 @Param("conditionName") String conditionName);

	@Query("select s "
		+ "from ServiceRule r join r.services s "
		+ "where r.name = :ruleName")
	List<Service> getServices(@Param("ruleName") String ruleName);

	@Query("select s "
		+ "from ServiceRule r join r.services s "
		+ "where r.name = :ruleName and s.serviceName = :serviceName")
	Optional<Service> getService(@Param("ruleName") String ruleName, @Param("serviceName") String serviceName);

}
