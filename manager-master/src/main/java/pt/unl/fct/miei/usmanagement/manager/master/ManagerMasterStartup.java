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

package pt.unl.fct.miei.usmanagement.manager.master;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jumpmind.symmetric.common.ParameterConstants;
import org.jumpmind.symmetric.web.ServerSymmetricEngine;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.WebConstants;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.MachineAddress;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.ServicesMonitoringService;

@Slf4j
@Component
public class ManagerMasterStartup implements ApplicationListener<ApplicationReadyEvent> {

  private final HostsService hostsService;
  private final ServicesMonitoringService servicesMonitoringService;
  private final HostsMonitoringService hostsMonitoringService;
  private final ServletContext servletContext;
  private final DataSource dataSource;
  private final ApplicationContext applicationContext;
  private final DataSourceProperties dataSourceProperties;

  public ManagerMasterStartup(@Lazy HostsService hostsService,
                              @Lazy ServicesMonitoringService servicesMonitoringService,
                              @Lazy HostsMonitoringService hostsMonitoringService,
                              ServletContext servletContext, DataSource dataSource,
                              ApplicationContext applicationContext, DataSourceProperties dataSourceProperties) {
    this.hostsService = hostsService;
    this.servicesMonitoringService = servicesMonitoringService;
    this.hostsMonitoringService = hostsMonitoringService;
    this.servletContext = servletContext;
    this.dataSource = dataSource;
    this.applicationContext = applicationContext;
    this.dataSourceProperties = dataSourceProperties;
  }

  @SneakyThrows
  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    MachineAddress machineAddress = hostsService.setMachineAddress();
    try {
      hostsService.clusterHosts();
    } catch (MasterManagerException e) {
      log.error(e.getMessage());
    }
    servicesMonitoringService.initServiceMonitorTimer();
    hostsMonitoringService.initHostMonitorTimer();
    this.startSymmetricDSServer(machineAddress);
  }

  private void startSymmetricDSServer(MachineAddress machineAddress) {
    SymmetricEngineHolder holder = new SymmetricEngineHolder();

    Properties properties = new Properties();
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classloader.getResourceAsStream("node.properties")) {
      properties.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }

    properties.setProperty(ParameterConstants.SYNC_URL,
        properties.getProperty(ParameterConstants.SYNC_URL)
            .replace("${hostname}", machineAddress.getPublicIpAddress()));

    properties.setProperty("db.driver", dataSourceProperties.getDriverClassName());
    properties.setProperty("db.url", dataSourceProperties.getUrl());
    properties.setProperty("db.user", dataSourceProperties.getUsername());
    properties.setProperty("db.password", dataSourceProperties.getPassword());

    ServerSymmetricEngine serverEngine =
        new ServerSymmetricEngine(dataSource, applicationContext, properties, false, holder);
    holder.getEngines().put(properties.getProperty(ParameterConstants.EXTERNAL_ID), serverEngine);
    holder.setAutoStart(false);
    servletContext.setAttribute(WebConstants.ATTR_ENGINE_HOLDER, holder);
    serverEngine.setup();
    serverEngine.start();
  }

}
