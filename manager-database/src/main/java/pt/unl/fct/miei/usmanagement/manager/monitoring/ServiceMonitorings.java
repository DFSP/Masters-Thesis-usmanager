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

package pt.unl.fct.miei.usmanagement.manager.monitoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceMonitorings extends JpaRepository<ServiceMonitoring, Long> {

	List<ServiceMonitoring> getByServiceNameIgnoreCase(@Param("serviceName") String serviceName);

	List<ServiceMonitoring> getByContainerId(@Param("containerId") String containerId);

	ServiceMonitoring getByContainerIdAndFieldIgnoreCase(@Param("containerId") String containerId,
														 @Param("field") String field);

	long countByContainerIdAndFieldIgnoreCase(@Param("containerId") String containerId, @Param("field") String field);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.monitoring"
		+ ".ServiceFieldAverage(m.serviceName, m.field, m.sumValue / m.count, m.count) "
		+ "from ServiceMonitoring m "
		+ "where m.serviceName = :serviceName")
	List<ServiceFieldAverage> getServiceFieldsAvg(@Param("serviceName") String serviceName);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.monitoring"
		+ ".ServiceFieldAverage(m.serviceName, m.field, m.sumValue / m.count, m.count) "
		+ "from ServiceMonitoring m "
		+ "where m.serviceName = :serviceName and m.field = :field")
	ServiceFieldAverage getServiceFieldAverage(@Param("serviceName") String serviceName, @Param("field") String field);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.monitoring"
		+ ".ContainerFieldAverage(m.containerId, m.field, m.sumValue / m.count, m.count) "
		+ "from ServiceMonitoring m "
		+ "where m.containerId = :containerId")
	List<ContainerFieldAverage> getContainerFieldsAvg(@Param("containerId") String containerId);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.monitoring"
		+ ".ContainerFieldAverage(m.containerId, m.field, m.sumValue / m.count, m.count) "
		+ "from ServiceMonitoring m "
		+ "where m.containerId = :containerId and m.field = :field")
	ContainerFieldAverage getContainerFieldAverage(@Param("containerId") String containerId, @Param("field") String field);

	@Query("select m "
		+ "from ServiceMonitoring m "
		+ "where m.containerId in :containerIds and m.field = :field "
		+ "order by (m.sumValue / m.count) desc")
	List<ServiceMonitoring> getTopContainersByField(@Param("containerIds") List<String> containerIds,
													@Param("field") String field);

}
