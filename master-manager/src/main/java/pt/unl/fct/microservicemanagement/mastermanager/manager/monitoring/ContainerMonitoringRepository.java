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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContainerMonitoringRepository extends JpaRepository<ServiceMonitoring, Long> {

  @Query("select COUNT(1) "
      + "from ServiceMonitoring log "
      + "where log.containerId = :containerId and log.field = :field")
  long getCountServiceField(@Param("containerId") String containerId, @Param("field") String field);

  @Query("select log "
      + "from ServiceMonitoring log "
      + "where log.containerId = :containerId and log.field = :field")
  ServiceMonitoring getServiceMonitoringByContainerAndField(@Param("containerId") String containerId,
                                                            @Param("field") String field);

  @Query("select log "
      + "from ServiceMonitoring log "
      + "where log.containerId = :containerId")
  List<ServiceMonitoring> getMonitoringServiceLogByContainer(@Param("containerId") String containerId);

  //TODO fix path
  @Query("select new pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring"
      + ".ServiceFieldAvg(log.serviceName, log.field, log.sumValue / log.count, log.count) "
      + "from ServiceMonitoring log "
      + "where log.serviceName = :serviceName and log.field = :field")
  ServiceFieldAvg getAvgServiceField(@Param("serviceName") String serviceName, @Param("field") String field);

  //TODO fix path
  @Query("select new pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring"
      + ".ServiceFieldAvg(log.serviceName, log.field, log.sumValue / log.count, log.count) "
      + "from ServiceMonitoring log "
      + "where log.serviceName = :serviceName")
  List<ServiceFieldAvg> getAvgServiceFields(@Param("serviceName") String serviceName);

  //TODO fix path
  @Query("select new pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring"
      + ".ContainerFieldAvg(log.containerId, log.field, log.sumValue / log.count, log.count) "
      + "from ServiceMonitoring log "
      + "where log.containerId = :containerId and log.field = :field")
  ContainerFieldAvg getAvgContainerField(@Param("containerId") String containerId, @Param("field") String field);

  //TODO fix path
  @Query("select new pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring"
      + ".ContainerFieldAvg(log.containerId, log.field, log.sumValue / log.count, log.count) "
      + "from ServiceMonitoring log "
      + "where log.containerId = :containerId")
  List<ContainerFieldAvg> getAvgContainerFields(@Param("containerId") String containerId);

  @Query("select log "
      + "from ServiceMonitoring log "
      + "where log.containerId in :containerIds and log.field = :field "
      + "order by (log.sumValue / log.count) desc")
  List<ServiceMonitoring> getTopContainersByField(@Param("containerIds") List<String> containerIds,
                                                  @Param("field") String field);

}
