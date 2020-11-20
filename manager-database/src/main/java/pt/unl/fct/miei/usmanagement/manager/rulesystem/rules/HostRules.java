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
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;

import java.util.List;
import java.util.Optional;

public interface HostRules extends JpaRepository<HostRule, Long> {

	Optional<HostRule> findByNameIgnoreCase(@Param("name") String name);

	@Query("select r "
		+ "from HostRule r left join r.cloudHosts c left join r.edgeHosts e "
		+ "where r.generic = true or "
		+ "(c.publicIpAddress = :publicIpAddress and c.privateIpAddress = :privateIpAddress) or "
		+ "(e.publicIpAddress = :publicIpAddress and e.privateIpAddress = :privateIpAddress)")
	List<HostRule> findByHostAddress(@Param("publicIpAddress") String publicIpAddress,
									 @Param("privateIpAddress") String privateIpAddress);

	@Query("select r "
		+ "from HostRule r "
		+ "where r.generic = true")
	List<HostRule> findGenericHostRules();

	@Query("select r "
		+ "from HostRule r "
		+ "where r.generic = true and lower(r.name) = lower(:ruleName)")
	Optional<HostRule> findGenericHostRule(@Param("ruleName") String ruleName);

	@Query("select case when count(r) > 0 then true else false end "
		+ "from HostRule r "
		+ "where lower(r.name) = lower(:ruleName)")
	boolean hasRule(@Param("ruleName") String ruleName);

	@Query("select rc.hostCondition "
		+ "from HostRule r left join r.conditions rc "
		+ "where lower(r.name) = lower(:ruleName)")
	List<Condition> getConditions(@Param("ruleName") String ruleName);

	@Query("select rc.hostCondition "
		+ "from HostRule r left join r.conditions rc "
		+ "where lower(r.name) = lower(:ruleName) and lower(rc.hostCondition.name) = lower(:conditionName)")
	Optional<Condition> getCondition(@Param("ruleName") String ruleName,
									 @Param("conditionName") String conditionName);

	@Query("select h "
		+ "from HostRule r left join r.cloudHosts h "
		+ "where lower(r.name) = lower(:ruleName)")
	List<CloudHost> getCloudHosts(@Param("ruleName") String ruleName);

	@Query("select h "
		+ "from HostRule r left join r.cloudHosts h "
		+ "where lower(r.name) = lower(:ruleName) and h.instanceId = :instanceId")
	Optional<CloudHost> getCloudHost(@Param("ruleName") String ruleName, @Param("instanceId") String instanceId);

	@Query("select h "
		+ "from HostRule r left join r.edgeHosts h "
		+ "where lower(r.name) = lower(:ruleName)")
	List<EdgeHost> getEdgeHosts(@Param("ruleName") String ruleName);

	@Query("select h "
		+ "from HostRule r left join r.edgeHosts h "
		+ "where lower(r.name) = lower(:ruleName) and (h.publicDnsName = :hostname or h.publicIpAddress = :hostname)")
	Optional<EdgeHost> getEdgeHost(@Param("ruleName") String ruleName, @Param("hostname") String hostname);

}
