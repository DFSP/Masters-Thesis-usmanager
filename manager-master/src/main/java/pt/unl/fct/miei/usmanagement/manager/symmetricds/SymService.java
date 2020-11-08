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
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
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
public class SymService {

	private final SymTriggerRoutersRepository symTriggerRoutersRepository;
	private final SymTriggersRepository symTriggersRepository;
	private final SymRoutersRepository symRoutersRepository;
	private final SymNodeGroupLinksRepository symNodeGroupLinksRepository;
	private final SymNodeGroupsRepository symNodeGroupsRepository;
	private final SymNodeHostsRepository symNodeHostsRepository;
	private final SymNodeIdentitiesRepository symNodeIdentitiesRepository;
	private final SymNodesSecurityRepository symNodesSecurityRepository;
	private final SymNodesRepository symNodesRepository;

	private final ServletContext servletContext;
	private final DataSource dataSource;
	private final ApplicationContext applicationContext;
	private final DataSourceProperties dataSourceProperties;
	private final SymmetricDSProperties symmetricDSProperties;

	private ServerSymmetricEngine serverSymmetricEngine;

	public SymService(SymTriggerRoutersRepository symTriggerRoutersRepository,
					  SymTriggersRepository symTriggersRepository,
					  SymRoutersRepository symRoutersRepository,
					  SymNodeGroupLinksRepository symNodeGroupLinksRepository,
					  SymNodeGroupsRepository symNodeGroupsRepository,
					  SymNodeHostsRepository symNodeHostsRepository,
					  SymNodeIdentitiesRepository symNodeIdentitiesRepository,
					  SymNodesSecurityRepository symNodesSecurityRepository,
					  SymNodesRepository symNodesRepository, ServletContext servletContext, DataSource dataSource,
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
		this.servletContext = servletContext;
		this.dataSource = dataSource;
		this.applicationContext = applicationContext;
		this.dataSourceProperties = dataSourceProperties;
		this.symmetricDSProperties = symmetricDSProperties;
	}

	public void startSymmetricDSServer(HostAddress hostAddress) throws SQLException, IOException {
		SymmetricEngineHolder holder = new SymmetricEngineHolder();

		Properties properties = new Properties();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (InputStream is = classloader.getResourceAsStream("sym/sym-node.properties")) {
			properties.load(is);
		}
		catch (IOException e) {
			log.error("Failed load sym-node properties: {}", e.getMessage());
			throw e;
		}

		properties.setProperty(ParameterConstants.SYNC_URL,
			properties.getProperty(ParameterConstants.SYNC_URL)
				.replace("${hostname}", hostAddress.getPublicIpAddress()));
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


	private void loadTriggerRouters() throws SQLException {
		this.getDatabaseTablesToReplicate().forEach(table ->
			symTriggerRoutersRepository.save(SymTriggerRouterEntity.builder()
				.triggerId(table)
				.routerId("master-to-worker")
				.initialLoadOrder(1)
				.createTime(LocalDateTime.now())
				.lastUpdateTime(LocalDateTime.now())
				.build())
		);
    /*insert into sym_trigger_router (trigger_id, router_id, initial_load_order, initial_load_select, create_time,
        last_update_time)
    values ('rules', 'master-to-one-worker', 1, 'worker_id=''$(externalId)''', current_timestamp, current_timestamp);*/

    /*insert into sym_trigger_router (trigger_id, router_id, initial_load_order, last_update_time, create_time)
    values ('apps', 'worker-to-master', 2, current_timestamp, current_timestamp);

    insert into sym_trigger_router (trigger_id, router_id, initial_load_order, last_update_time, create_time)
    values ('rules', 'worker-to-master', 2, current_timestamp, current_timestamp);*/
		// worker-to-master
		// - containers
		// - hosts
		// - events/decisions/metrics
	}

	private void loadRouters() {
		// Default router sends all data from master to workers
		symRoutersRepository.save(SymRouterEntity.builder()
			.routerId("master-to-worker")
			.sourceNodeGroupId("master-manager")
			.targetNodeGroupId("worker-manager")
			.routerType("default")
			.createTime(LocalDateTime.now())
			.lastUpdateTime(LocalDateTime.now())
			.build());
		// Column match router will subset data from master-manager to a specific worker
		symRoutersRepository.save(SymRouterEntity.builder()
			.routerId("master-to-one-worker")
			.sourceNodeGroupId("master-manager")
			.targetNodeGroupId("worker-manager")
			.routerType("column")
			.routerExpression("WORKER_ID=:EXTERNAL_ID or OLD_WORKER_ID=:EXTERNAL_ID")
			.createTime(LocalDateTime.now())
			.lastUpdateTime(LocalDateTime.now())
			.build());
		// Default router sends all data from workers to manager
		symRoutersRepository.save(SymRouterEntity.builder()
			.routerId("worker-to-master")
			.sourceNodeGroupId("worker-manager")
			.targetNodeGroupId("master-manager")
			.routerType("default")
			.createTime(LocalDateTime.now())
			.lastUpdateTime(LocalDateTime.now())
			.build());
	}

	private void loadTriggers() throws SQLException {
		this.getDatabaseTablesToReplicate().forEach(table ->
			symTriggersRepository.save(SymTriggerEntity.builder()
				.triggerId(table)
				.sourceTableName(table)
				.channelId("default")
				.lastUpdateTime(LocalDateTime.now())
				.createTime(LocalDateTime.now())
				.build())
		);
	}

	private void loadNodeGroupLinks() {
		// master-manager sends changes to worker-manager when worker pulls from the master
		symNodeGroupLinksRepository.save(SymNodeGroupLinkEntity.builder()
			.sourceNodeGroupId("master-manager")
			.targetNodeGroupId("worker-manager")
			.dataEventAction("W")
			.build());
		// worker-manager sends changes to master-manager when worker pushes to the master
		symNodeGroupLinksRepository.save(SymNodeGroupLinkEntity.builder()
			.sourceNodeGroupId("worker-manager")
			.targetNodeGroupId("master-manager")
			.dataEventAction("P")
			.build());
	}

	private void loadNodeGroups() {
		symNodeGroupsRepository.save(SymNodeGroupEntity.builder()
			.nodeGroupId("master-manager")
			.description("A master manager node. Available at the cloud.")
			.build());
		symNodeGroupsRepository.save(SymNodeGroupEntity.builder()
			.nodeGroupId("worker-manager")
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

	private List<String> getDatabaseTablesToReplicate() throws SQLException {
		DatabaseMetaData metaData;
		metaData = dataSource.getConnection().getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		List<String> tablesNames = new LinkedList<>();
		List<String> excludingStartsWith = symmetricDSProperties.getTables().getExclude().getStartsWith();
		List<String> excludingEndsWith = symmetricDSProperties.getTables().getExclude().getEndsWith();
		List<String> excludingContains = symmetricDSProperties.getTables().getExclude().getContains();
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

}
