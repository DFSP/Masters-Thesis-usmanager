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
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.SymNodesRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.group.SymNodeGroupEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.group.SymNodeGroupsRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.group.link.SymNodeGroupLinkEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.group.link.SymNodeGroupLinksRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.host.SymNodeHostsRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.identity.SymNodeIdentitiesRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.node.security.SymNodesSecurityRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.router.SymRouterEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.router.SymRoutersRepository;
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
public class MasterSymService {

	private final SymTriggerRoutersRepository symTriggerRoutersRepository;
	private final SymTriggersRepository symTriggersRepository;
	private final SymRoutersRepository symRoutersRepository;
	private final SymNodeGroupLinksRepository symNodeGroupLinksRepository;
	private final SymNodeGroupsRepository symNodeGroupsRepository;
	private final SymNodeHostsRepository symNodeHostsRepository;
	private final SymNodeIdentitiesRepository symNodeIdentitiesRepository;
	private final SymNodesSecurityRepository symNodesSecurityRepository;
	private final SymNodesRepository symNodesRepository;
	private final Environment environment;

	private final ServletContext servletContext;
	private final DataSource dataSource;
	private final ApplicationContext applicationContext;
	private final DataSourceProperties dataSourceProperties;
	private final SymmetricDSProperties symmetricDSProperties;

	private ServerSymmetricEngine serverSymmetricEngine;

	public MasterSymService(SymTriggerRoutersRepository symTriggerRoutersRepository,
							SymTriggersRepository symTriggersRepository,
							SymRoutersRepository symRoutersRepository,
							SymNodeGroupLinksRepository symNodeGroupLinksRepository,
							SymNodeGroupsRepository symNodeGroupsRepository,
							SymNodeHostsRepository symNodeHostsRepository,
							SymNodeIdentitiesRepository symNodeIdentitiesRepository,
							SymNodesSecurityRepository symNodesSecurityRepository,
							SymNodesRepository symNodesRepository, Environment environment, ServletContext servletContext, DataSource dataSource,
							ApplicationContext applicationContext, DataSourceProperties dataSourceProperties,
							SymmetricDSProperties symmetricDSProperties) {
		this.symTriggerRoutersRepository = symTriggerRoutersRepository;
		this.symTriggersRepository = symTriggersRepository;
		this.symRoutersRepository = symRoutersRepository;
		this.symNodeGroupLinksRepository = symNodeGroupLinksRepository;
		this.symNodeGroupsRepository = symNodeGroupsRepository;
		this.symNodeHostsRepository = symNodeHostsRepository;
		this.symNodeIdentitiesRepository = symNodeIdentitiesRepository;
		this.symNodesSecurityRepository = symNodesSecurityRepository;
		this.symNodesRepository = symNodesRepository;
		this.environment = environment;
		this.servletContext = servletContext;
		this.dataSource = dataSource;
		this.applicationContext = applicationContext;
		this.dataSourceProperties = dataSourceProperties;
		this.symmetricDSProperties = symmetricDSProperties;
	}

	public void startSymmetricDsService(HostAddress hostAddress) throws SQLException, IOException {
		SymmetricEngineHolder holder = new SymmetricEngineHolder();

		Properties properties = new Properties();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (InputStream is = classloader.getResourceAsStream("sym/sym-node.properties")) {
			properties.load(is);
		}
		catch (IOException e) {
			log.error("Failed to load sym-node properties: {}", e.getMessage());
			throw e;
		}

		String port = environment.getProperty("local.server.port");
		properties.setProperty(ParameterConstants.SYNC_URL, String.format("http://%s:%s/api/sync", hostAddress.getPublicIpAddress(), port));
		properties.setProperty("db.driver", dataSourceProperties.getDriverClassName());
		properties.setProperty("db.url", dataSourceProperties.getUrl());
		properties.setProperty("db.user", dataSourceProperties.getUsername());
		properties.setProperty("db.password", dataSourceProperties.getPassword());

		serverSymmetricEngine = new ServerSymmetricEngine(dataSource, applicationContext, properties, false, holder);
		serverSymmetricEngine.setDeploymentType("server");
		holder.getEngines().put(properties.getProperty(ParameterConstants.EXTERNAL_ID), serverSymmetricEngine);
		holder.setAutoStart(false);
		servletContext.setAttribute(WebConstants.ATTR_ENGINE_HOLDER, holder);
		serverSymmetricEngine.setup();
		this.loadSymmetricDS();
		serverSymmetricEngine.start();
	}

	public void stopSymmetricDSServer() {
		if (serverSymmetricEngine != null) {
			serverSymmetricEngine.stop();
		}
	}

	private void loadSymmetricDS() throws SQLException {
		this.clearSymmetricDS();
		this.loadNodeGroups();
		this.loadNodeGroupLinks();
		this.loadTriggers();
		this.loadRouters();
		this.loadTriggerRouters();
	}

	private String initialLoadCondition(String table) {
		return null;
		/*String initialLoadCondition = table.equalsIgnoreCase("containers") ? "(NAME like 'registration-server%' or NAME like 'load-balancer%')" : null;
		if (table.equalsIgnoreCase("containers")) {
			return "(NAME like 'registration-server%' or NAME like 'load-balancer%')";
		}
		if (List.of("container_labels", "container_mounts", "container_ports", "container_rule", "container_simulated_metric").contains(table)) {
			return "(OLD_CONTAINER_ID = '' or NEW_CONTAINER_ID = '')"; // will be updated dynamically with container ids
		}
		return initialLoadCondition;*/
	}

	private void loadTriggerRouters() throws SQLException {
		getMasterToWorkerTables().forEach(table -> {
			symTriggerRoutersRepository.save(SymTriggerRouterEntity.builder()
				.triggerId("master-" + table)
				.routerId("master-to-worker")
				.initialLoadOrder(1)
				.initialLoadSelect(initialLoadCondition(table))
				.createTime(LocalDateTime.now())
				.lastUpdateTime(LocalDateTime.now())
				.build());
			}
		);
		getWorkerToMasterTables().forEach(table ->
			symTriggerRoutersRepository.save(SymTriggerRouterEntity.builder()
				.triggerId("worker-" + table)
				.routerId("worker-to-master")
				.createTime(LocalDateTime.now())
				.lastUpdateTime(LocalDateTime.now())
				.build())
		);
	}

	private void loadRouters() {
		// Default router sends all data from master to workers
		symRoutersRepository.save(SymRouterEntity.builder()
			.routerId("master-to-worker")
			.sourceNodeGroupId("manager-master")
			.targetNodeGroupId("manager-worker")
			.routerType("default")
			.createTime(LocalDateTime.now())
			.lastUpdateTime(LocalDateTime.now())
			.build());
		// Column match router will subset data from manager-master to a specific worker
		symRoutersRepository.save(SymRouterEntity.builder()
			.routerId("master-to-one-worker")
			.sourceNodeGroupId("manager-master")
			.targetNodeGroupId("manager-worker")
			.routerType("column")
			.routerExpression("WORKER_ID=:EXTERNAL_ID or OLD_WORKER_ID=:EXTERNAL_ID")
			.createTime(LocalDateTime.now())
			.lastUpdateTime(LocalDateTime.now())
			.build());
		// Default router sends all data from workers to master
		symRoutersRepository.save(SymRouterEntity.builder()
			.routerId("worker-to-master")
			.sourceNodeGroupId("manager-worker")
			.targetNodeGroupId("manager-master")
			.routerType("default")
			.createTime(LocalDateTime.now())
			.lastUpdateTime(LocalDateTime.now())
			.build());
	}

	private void loadTriggers() throws SQLException {
		getMasterToWorkerTables().forEach(table -> {
				log.info("Added master synchronize trigger to table {}", table);
				String syncCondition = /*table.equalsIgnoreCase("containers")
					? "(OLD_NAME like 'registration-server%' or OLD_NAME like 'load-balancer%' or NEW_NAME like 'registration-server%' or NEW_NAME like 'load-balancer%')"
					: */null;
				symTriggersRepository.save(SymTriggerEntity.builder()
					.triggerId("master-" + table)
					.sourceTableName(table)
					.channelId("default")
					.syncOnInsertCondition(syncCondition)
					.syncOnUpdateCondition(syncCondition)
					.syncOnDeleteCondition(syncCondition)
					.lastUpdateTime(LocalDateTime.now())
					.createTime(LocalDateTime.now())
					.build());
			}
		);
		getWorkerToMasterTables().forEach(table -> {
				log.info("Added worker synchronize trigger to table {}", table);
				String syncCondition = /*List.of("containers", "container_labels", "container_mounts", "container_ports", "nodes", "node_labels").contains(table)
					? "(OLD_MANAGER_ID = '$(externalId)' or NEW_MANAGER_ID = '$(externalId)')" :*/ null;
				String externalSelect = /*List.of("container_labels", "container_mounts", "container_ports", "node_labels").contains(table)
					? "select manager_id from containers where id=$(curTriggerValue).$(curColumnPrefix)container_id" :*/ null;
				symTriggersRepository.save(SymTriggerEntity.builder()
					.triggerId("worker-" + table)
					.sourceTableName(table)
					.channelId("default")
					.syncOnInsertCondition(syncCondition)
					.syncOnUpdateCondition(syncCondition)
					.syncOnDeleteCondition(syncCondition)
					.externalSelect(externalSelect)
					.lastUpdateTime(LocalDateTime.now())
					.createTime(LocalDateTime.now())
					.build());
			}
		);
	}

	private void loadNodeGroupLinks() {
		// manager-master sends changes to manager-worker when worker pulls from the master
		symNodeGroupLinksRepository.save(SymNodeGroupLinkEntity.builder()
			.sourceNodeGroupId("manager-master")
			.targetNodeGroupId("manager-worker")
			.dataEventAction("W")
			.build());
		// manager-worker sends changes to manager-master when worker pushes to the master
		symNodeGroupLinksRepository.save(SymNodeGroupLinkEntity.builder()
			.sourceNodeGroupId("manager-worker")
			.targetNodeGroupId("manager-master")
			.dataEventAction("P")
			.build());
	}

	private void loadNodeGroups() {
		symNodeGroupsRepository.save(SymNodeGroupEntity.builder()
			.nodeGroupId("manager-master")
			.description("A master manager node. Available at the cloud.")
			.build());
		symNodeGroupsRepository.save(SymNodeGroupEntity.builder()
			.nodeGroupId("manager-worker")
			.description("A worker manager node. Available at the edge.")
			.build());
	}

	private void clearSymmetricDS() {
		symTriggerRoutersRepository.deleteAll();
		symTriggersRepository.deleteAll();
		symRoutersRepository.deleteAll();
		symNodeGroupLinksRepository.deleteAll();
		symNodeGroupsRepository.deleteAll();
		symNodeHostsRepository.deleteAll();
		symNodeIdentitiesRepository.deleteAll();
		symNodesSecurityRepository.deleteAll();
		symNodesRepository.deleteAll();
	}

	private List<String> getMasterToWorkerTables() throws SQLException {
		DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		List<String> tablesNames = new LinkedList<>();
		List<String> excludingStartsWith = symmetricDSProperties.getMaster().getTables().getExclude().getStartsWith();
		List<String> excludingEndsWith = symmetricDSProperties.getMaster().getTables().getExclude().getEndsWith();
		List<String> excludingContains = symmetricDSProperties.getMaster().getTables().getExclude().getContains();
		while (tables.next()) {
			String tableName = tables.getString("TABLE_NAME").toLowerCase();
			if ((excludingStartsWith == null || excludingStartsWith.stream().noneMatch(tableName::startsWith))
				&& (excludingEndsWith == null || excludingEndsWith.stream().noneMatch(tableName::endsWith))
				&& (excludingContains == null || excludingContains.stream().noneMatch(tableName::contains))) {
				tablesNames.add(tableName);
			}
		}
		return tablesNames;
	}

	private List<String> getWorkerToMasterTables() throws SQLException {
		DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		List<String> tablesNames = new LinkedList<>();
		List<String> includingStartsWith = symmetricDSProperties.getWorker().getTables().getInclude().getStartsWith();
		List<String> includingContains = symmetricDSProperties.getWorker().getTables().getInclude().getContains();
		List<String> includingEndsWith = symmetricDSProperties.getWorker().getTables().getInclude().getEndsWith();
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

}
