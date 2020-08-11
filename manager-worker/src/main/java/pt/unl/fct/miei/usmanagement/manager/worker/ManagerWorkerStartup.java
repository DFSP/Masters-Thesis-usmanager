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

package pt.unl.fct.miei.usmanagement.manager.worker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.jumpmind.symmetric.common.ParameterConstants;
import org.jumpmind.symmetric.web.ServerSymmetricEngine;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.WebConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ManagerWorkerStartup implements ApplicationListener<ApplicationReadyEvent> {

  private final ServletContext servletContext;
  private final DataSource dataSource;
  private final ApplicationContext applicationContext;
  private final DataSourceProperties dataSourceProperties;

  @Value("${id}")
  private String id;

  @Value("${master:localhost}")
  private String master;

  public ManagerWorkerStartup(ServletContext servletContext, DataSource dataSource,
                              ApplicationContext applicationContext, DataSourceProperties dataSourceProperties) {
    this.servletContext = servletContext;
    this.dataSource = dataSource;
    this.applicationContext = applicationContext;
    this.dataSourceProperties = dataSourceProperties;
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    // 1. start zookeeper process
    // 2. start kafka process
    // 3. consume topic 'init-local-manager'
    this.startSymmetricDSServer();
  }


  private void startSymmetricDSServer() {
    SymmetricEngineHolder holder = new SymmetricEngineHolder();

    Properties properties = new Properties();
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classloader.getResourceAsStream("sym/sym-node.properties")) {
      properties.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }

    properties.setProperty(ParameterConstants.REGISTRATION_URL,
        properties.getProperty(ParameterConstants.REGISTRATION_URL).replace("${master}", master));
    properties.setProperty(ParameterConstants.EXTERNAL_ID,
        properties.getProperty(ParameterConstants.EXTERNAL_ID).replace("${id}", id));

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
