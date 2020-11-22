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

package pt.unl.fct.miei.usmanagement.manager.symmetricds;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.symmetric.common.ParameterConstants;
import org.jumpmind.symmetric.web.ServerSymmetricEngine;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.WebConstants;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.symmetricds.SymmetricDSProperties;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.SymTriggerEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.SymTriggersRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.router.SymTriggerRouterEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.router.SymTriggerRoutersRepository;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class WorkerSymService {

	private final SymTriggersRepository symTriggersRepository;
	private final SymTriggerRoutersRepository symTriggerRoutersRepository;
	private final SymmetricDSProperties symmetricDSProperties;

	private final ServletContext servletContext;
	private final DataSource dataSource;
	private final ApplicationContext applicationContext;
	private final DataSourceProperties dataSourceProperties;
	private final Environment environment;

	private ServerSymmetricEngine serverSymmetricEngine;

	public WorkerSymService(SymTriggersRepository symTriggersRepository, SymTriggerRoutersRepository symTriggerRoutersRepository,
							SymmetricDSProperties symmetricDSProperties, ServletContext servletContext, DataSource dataSource,
							ApplicationContext applicationContext, DataSourceProperties dataSourceProperties, Environment environment) {
		this.symTriggersRepository = symTriggersRepository;
		this.symTriggerRoutersRepository = symTriggerRoutersRepository;
		this.symmetricDSProperties = symmetricDSProperties;
		this.servletContext = servletContext;
		this.dataSource = dataSource;
		this.applicationContext = applicationContext;
		this.dataSourceProperties = dataSourceProperties;
		this.environment = environment;
	}

	public void startSymmetricDsService(String externalId, String registrationUrl, HostAddress hostAddress) throws IOException, SQLException {
		SymmetricEngineHolder engineHolder = new SymmetricEngineHolder();

		Properties properties = new Properties();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (InputStream is = classloader.getResourceAsStream("sym/sym-node.properties")) {
			properties.load(is);
		}
		catch (IOException e) {
			log.error("Failed to load sym-node properties: {}", e.getMessage());
			throw e;
		}

		properties.setProperty(ParameterConstants.EXTERNAL_ID, externalId);
		properties.setProperty(ParameterConstants.REGISTRATION_URL, registrationUrl);
		String port = environment.getProperty("local.server.port");
		properties.setProperty(ParameterConstants.SYNC_URL, String.format("http://%s:%s/api/sync", hostAddress.getPublicIpAddress(), port));

		properties.setProperty("db.driver", dataSourceProperties.getDriverClassName());
		properties.setProperty("db.url", dataSourceProperties.getUrl());
		properties.setProperty("db.user", dataSourceProperties.getUsername());
		properties.setProperty("db.password", dataSourceProperties.getPassword());

		serverSymmetricEngine = new ServerSymmetricEngine(dataSource, applicationContext, properties, false, engineHolder);
		engineHolder.getEngines().put(properties.getProperty(ParameterConstants.EXTERNAL_ID), serverSymmetricEngine);
		engineHolder.setAutoStart(false);
		servletContext.setAttribute(WebConstants.ATTR_ENGINE_HOLDER, engineHolder);
		serverSymmetricEngine.setup();
		this.loadSymmetricDs();
		serverSymmetricEngine.start();
	}

	private void loadSymmetricDs() throws SQLException {
		this.loadTriggers();
		this.loadTriggerRouters();
	}

	private void loadTriggers() throws SQLException {
		getWorkerToMasterTables().forEach(table -> {
			log.info("Added synchronize trigger to table {}", table);
			symTriggersRepository.save(SymTriggerEntity.builder()
				.triggerId(table)
				.sourceTableName(table)
				.channelId("default")
				.lastUpdateTime(LocalDateTime.now())
				.createTime(LocalDateTime.now())
				.build());
			}
		);
	}

	private void loadTriggerRouters() throws SQLException {
		getWorkerToMasterTables().forEach(table ->
			symTriggerRoutersRepository.save(SymTriggerRouterEntity.builder()
				.triggerId(table)
				.routerId("worker-to-master")
				.createTime(LocalDateTime.now())
				.lastUpdateTime(LocalDateTime.now())
				.build())
		);
	}

	private List<String> getWorkerToMasterTables() throws SQLException {
		DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		List<String> tablesNames = new LinkedList<>();
		List<String> includingStartsWith = symmetricDSProperties.getTables().getInclude().getStartsWith();
		List<String> includingContains = symmetricDSProperties.getTables().getInclude().getContains();
		List<String> includingEndsWith = symmetricDSProperties.getTables().getInclude().getEndsWith();
		while (tables.next()) {
			String tableName = tables.getString("TABLE_NAME").toLowerCase();
			if ((includingStartsWith != null && includingStartsWith.stream().anyMatch(tableName::startsWith))
				|| (includingContains != null && includingContains.stream().anyMatch(tableName::contains))
				|| (includingEndsWith != null && includingEndsWith.stream().anyMatch(tableName::endsWith))) {
				tablesNames.add(tableName);
			}
		}
		return tablesNames;
	}

/*	private List<String> getWorkerToMasterTables() throws SQLException {
		DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		List<String> tablesNames = new LinkedList<>();
		while (tables.next()) {
			String tableName = tables.getString("TABLE_NAME").toLowerCase();
			if (tableName.startsWith("container")
				|| tableName.startsWith("node")
				|| tableName.contains("event")
				|| tableName.equalsIgnoreCase("host_monitoring_logs")
				|| tableName.equalsIgnoreCase("service_monitoring_logs")
				|| tableName.equalsIgnoreCase("host_decisions")
				|| tableName.equalsIgnoreCase("host_decision_values")
				|| tableName.equalsIgnoreCase("service_decisions")
				|| tableName.equalsIgnoreCase("service_decision_values")) {
				tablesNames.add(tableName);
			}
		}
		return tablesNames;
	}*/

	public void stopSymmetricDSServer() {
		if (serverSymmetricEngine != null) {
			serverSymmetricEngine.stop();
		}
	}

}