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

public interface HostMonitorings extends JpaRepository<HostMonitoring, Long> {

	List<HostMonitoring> getByPublicIpAddressAndPrivateIpAddress(String publicIpAddress, String privateIpAddress);

	HostMonitoring getByPublicIpAddressAndPrivateIpAddressAndFieldIgnoreCase(String publicIpAddress, String privateIpAddress,
																			 String field);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.monitoring."
		+ "HostFieldAverage(m.publicIpAddress, m.privateIpAddress, m.field, m.sumValue / m.count, m.count) "
		+ "from HostMonitoring m "
		+ "where m.publicIpAddress = :publicIpAddress and m.privateIpAddress = :privateIpAddress")
	List<HostFieldAverage> getHostMonitoringFieldsAverage(@Param("publicIpAddress") String publicIpAddress,
														  @Param("privateIpAddress") String privateIpAddress);

	@Query("select new pt.unl.fct.miei.usmanagement.manager.monitoring."
		+ "HostFieldAverage(m.publicIpAddress, m.privateIpAddress, m.field, m.sumValue / m.count, m.count) "
		+ "from HostMonitoring m "
		+ "where m.publicIpAddress = :publicIpAddress and m.privateIpAddress = :privateIpAddress and m.field = :field")
	HostFieldAverage getHostMonitoringFieldAverage(@Param("publicIpAddress") String publicIpAddress,
												   @Param("privateIpAddress") String privateIpAddress,
												   @Param("field") String field);
}
