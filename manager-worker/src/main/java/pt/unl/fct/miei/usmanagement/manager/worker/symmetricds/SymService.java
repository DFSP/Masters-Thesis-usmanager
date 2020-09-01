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

package pt.unl.fct.miei.usmanagement.manager.worker.symmetricds;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.db.model.Table;
import org.jumpmind.symmetric.common.Constants;
import org.jumpmind.symmetric.common.ParameterConstants;
import org.jumpmind.symmetric.io.data.CsvData;
import org.jumpmind.symmetric.io.data.DataContext;
import org.jumpmind.symmetric.io.data.writer.DatabaseWriterFilterAdapter;
import org.jumpmind.symmetric.io.data.writer.IDatabaseWriterErrorHandler;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.web.ServerSymmetricEngine;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.WebConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.util.Timing;

@Slf4j
@Service
public class SymService {

  private final HostsService hostsService;

  private final ServletContext servletContext;
  private final DataSource dataSource;
  private final ApplicationContext applicationContext;
  private final DataSourceProperties dataSourceProperties;

  @Value("${external-id}")
  private String id;

  @Value("${master:127.0.0.1}")
  private String master;

  private int tableCount;

  private ServerSymmetricEngine serverSymmetricEngine;

  public SymService(HostsService hostsService, ServletContext servletContext, DataSource dataSource,
                    ApplicationContext applicationContext, DataSourceProperties dataSourceProperties) {
    this.hostsService = hostsService;
    this.servletContext = servletContext;
    this.dataSource = dataSource;
    this.applicationContext = applicationContext;
    this.dataSourceProperties = dataSourceProperties;
  }

  public void startSymmetricDSServer() {
    final SymmetricEngineHolder engineHolder = new SymmetricEngineHolder();

    final Properties properties = new Properties();
    final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classloader.getResourceAsStream("sym/sym-node.properties")) {
      properties.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }

    properties.setProperty(ParameterConstants.REGISTRATION_URL,
        properties.getProperty(ParameterConstants.REGISTRATION_URL).replace("${master}", master));
    properties.setProperty(ParameterConstants.EXTERNAL_ID,
        properties.getProperty(ParameterConstants.EXTERNAL_ID).replace("${external-id}", id));

    properties.setProperty("db.driver", dataSourceProperties.getDriverClassName());
    properties.setProperty("db.url", dataSourceProperties.getUrl());
    properties.setProperty("db.user", dataSourceProperties.getUsername());
    properties.setProperty("db.password", dataSourceProperties.getPassword());

    serverSymmetricEngine = new ServerSymmetricEngine(dataSource, applicationContext, properties, false, engineHolder);
    engineHolder.getEngines().put(properties.getProperty(ParameterConstants.EXTERNAL_ID), serverSymmetricEngine);
    engineHolder.setAutoStart(false);
    servletContext.setAttribute(WebConstants.ATTR_ENGINE_HOLDER, engineHolder);

    serverSymmetricEngine.setup();

    /*final MonitorFilter monitor = new MonitorFilter();
    serverSymmetricEngine.getExtensionService().addExtensionPoint(monitor);*/

    final SymDatabaseMonitor databaseMonitor = new SymDatabaseMonitor(hostsService);
    serverSymmetricEngine.getExtensionService().addExtensionPoint(databaseMonitor);

    serverSymmetricEngine.start();

    //waitForInitialLoad(monitor);


  }

  private void waitForInitialLoad(MonitorFilter monitor) {

    while (!serverSymmetricEngine.isRegistered()) {
      System.err.println("Waiting for engine to register");
      Timing.wait(2, TimeUnit.SECONDS);
    }

    INodeService nodeService = serverSymmetricEngine.getNodeService();
    while (!nodeService.isDataLoadStarted()) {
      System.err.println("Waiting for data load to start");
      Timing.wait(1, TimeUnit.SECONDS);
    }

    long ts = System.currentTimeMillis();
    DateFormat dateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);
    if (!nodeService.isDataLoadCompleted()) {
      System.err.println(String.format("Data load started at %s", dateFormat.format(new Date())));
    }

    Exception error = null;
    while (!nodeService.isDataLoadCompleted()) {
      printTables(monitor.tableQueue);
      if ((error == null && monitor.error != null)
          || (error != null && monitor.error != null && !error.equals(monitor.error))) {
        error = monitor.error;
        System.err.println("Error occurred");
      }

      Timing.wait(500, TimeUnit.MILLISECONDS);
    }

    printTables(monitor.tableQueue);

    System.err.println("******************************************");
    System.err.println(
        String.format("Data load complete at %s in %s seconds",
            dateFormat.format(new Date()), (System.currentTimeMillis() - ts) / 1000));
    System.err.println("******************************************");
  }

  private void printTables(List<String> tableQueue) {
    while (tableQueue.size() > 0) {
      String table = tableQueue.remove(0);
      System.err.println("******************************************");
      System.err.println("Loading " + table + ".  Current table count=" + ++tableCount);
      System.err.println("******************************************");
    }
  }

  public void stopSymmetricDSServer() {
    serverSymmetricEngine.stop();
  }

  static class MonitorFilter extends DatabaseWriterFilterAdapter implements IDatabaseWriterErrorHandler {

    private final Set<String> tablesEncountered = new HashSet<>();

    private final List<String> tableQueue = Collections.synchronizedList(new ArrayList<>());

    private Exception error;

    @Override
    public boolean beforeWrite(DataContext context, Table table, CsvData data) {
      error = null;
      if (context.getBatch().getChannelId().equals(Constants.CHANNEL_RELOAD)) {
        if (!tablesEncountered.contains(table.getName())) {
          tablesEncountered.add(table.getName());
          tableQueue.add(table.getName());
        }
      }
      return true;
    }

    @Override
    public void afterWrite(DataContext context, Table table, CsvData data) {

    }

    @Override
    public boolean handleError(DataContext context, Table table, CsvData data, Exception ex) {
      error = ex;
      return false;
    }
  }

}
