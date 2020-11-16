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
import org.jumpmind.db.model.Table;
import org.jumpmind.symmetric.common.Constants;
import org.jumpmind.symmetric.io.data.CsvData;
import org.jumpmind.symmetric.io.data.DataContext;
import org.jumpmind.symmetric.io.data.DataEventType;
import org.jumpmind.symmetric.io.data.writer.DatabaseWriterFilterAdapter;
import org.jumpmind.symmetric.io.data.writer.IDatabaseWriterErrorHandler;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagersService;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
class SymDatabaseMonitor extends DatabaseWriterFilterAdapter implements IDatabaseWriterErrorHandler {


	private final HostsService hostsService;
	private final CloudHostsService cloudHostsService;
	private final EdgeHostsService edgeHostsService;
	private final WorkerManagersService workerManagersService;
	private final Environment environment;

	private boolean setupScheduled;
	private CloudHost oldCloudHost;
	private EdgeHost oldEdgeHost;

	SymDatabaseMonitor(HostsService hostsService, CloudHostsService cloudHostsService,
					   EdgeHostsService edgeHostsService, WorkerManagersService workerManagersService,
					   Environment environment) {
		this.hostsService = hostsService;
		this.cloudHostsService = cloudHostsService;
		this.edgeHostsService = edgeHostsService;
		this.workerManagersService = workerManagersService;
		this.environment = environment;
		this.setupScheduled = false;
	}

	@Override
	public boolean beforeWrite(DataContext context, Table table, CsvData data) {
		String channelId = context.getBatch().getChannelId();
		if (!channelId.equals(Constants.CHANNEL_RELOAD) && !channelId.equals(Constants.CHANNEL_DEFAULT)) {
			return true;
		}
		String tableName = table.getName();
		if ("cloud_hosts".equalsIgnoreCase(tableName)) {
			Map<String, String> newCloudHost = data.toColumnNameValuePairs(table.getColumnNames(), CsvData.ROW_DATA);
			log.info(newCloudHost.toString());
			String instanceId = newCloudHost.get("INSTANCE_ID");
			try {
				oldCloudHost = cloudHostsService.getCloudHostById(instanceId);
			}
			catch (EntityNotFoundException e) {
				log.error("Cloud host not found {}", instanceId);
			}
		}
		else if ("edge_hosts".equalsIgnoreCase(tableName)) {
			Map<String, String> newEdgeHost = data.toColumnNameValuePairs(table.getColumnNames(), CsvData.ROW_DATA);
			String publicIpAddress = newEdgeHost.get("PUBLIC_IP_ADDRESS");
			String privateIpAddress = newEdgeHost.get("PRIVATE_IP_ADDRESS");
			HostAddress hostAddress = new HostAddress(publicIpAddress, privateIpAddress);
			try {
				oldEdgeHost = edgeHostsService.getEdgeHostByAddress(hostAddress);
			}
			catch (EntityNotFoundException e) {
				log.error("Edge host not found {}", hostAddress.toSimpleString());
			}
		}
		return true;
	}

	@Override
	public void afterWrite(DataContext context, Table table, CsvData data) {
		final String channelId = context.getBatch().getChannelId();
		if (channelId.equals(Constants.CHANNEL_RELOAD) || channelId.equals(Constants.CHANNEL_DEFAULT)) {
			final String tableName = table.getName();
			if ("cloud_hosts".equalsIgnoreCase(tableName) || "edge_hosts".equalsIgnoreCase(tableName)) {
				final Map<String, String> columns =
					data.toColumnNameValuePairs(table.getColumnNames(), CsvData.ROW_DATA);
				final String publicIpAddress = columns.get("PUBLIC_IP_ADDRESS");
				final String privateIpAddress = columns.get("PRIVATE_IP_ADDRESS");
				final String newWorkerId = columns.get("MANAGED_BY_WORKER_ID");
				String oldWorkerId = null;
				if ("cloud_hosts".equalsIgnoreCase(tableName)) {
					if (oldCloudHost != null && oldCloudHost.getManagedByWorker() != null) {
						oldWorkerId = oldCloudHost.getManagedByWorker().getId();
					}
				}
				else if ("edge_hosts".equalsIgnoreCase(tableName)) {
					if (oldEdgeHost != null && oldEdgeHost.getManagedByWorker() != null) {
						oldWorkerId = oldEdgeHost.getManagedByWorker().getId();
					}
				}
				HostAddress hostAddress = new HostAddress(publicIpAddress, privateIpAddress);
				updateHost(hostAddress, oldWorkerId, newWorkerId);
			}
			else if (!setupScheduled && channelId.equals(Constants.CHANNEL_RELOAD)
				&& "services".equalsIgnoreCase(tableName) && data.getDataEventType().equals(DataEventType.INSERT)) {
				String serviceName = data.toColumnNameValuePairs(table.getColumnNames(), CsvData.ROW_DATA).get("SERVICE_NAME");
				if (serviceName != null && serviceName.contains("nginx-basic-auth-proxy")) {
					workerManagersService.init();
					setupScheduled = true;
				}
			}
		}
	}

	private void updateHost(HostAddress hostAddress, String oldWorkerId, String newWorkerId) {
		String externalId = environment.getProperty(ContainerConstants.Environment.WorkerManager.EXTERNAL_ID);
		log.info("Inserted a host {}: {} -> {} ({})", hostAddress.toSimpleString(), oldWorkerId, newWorkerId, externalId);
		if (hostAddress.getPublicIpAddress() != null) {
			// host is running
			if (Objects.equals(externalId, newWorkerId)) {
				// is a host managed by this worker
				if (!Objects.equals(externalId, oldWorkerId)) {
					// is a newly added host
					hostsService.setupHost(hostAddress, NodeRole.WORKER);
				}
			}
			else if (Objects.equals(externalId, oldWorkerId)) {
				// is not a host managed by this worker, but used to be
				hostsService.removeHost(hostAddress);
			}
		}
	}

	@Override
	public boolean handleError(DataContext context, Table table, CsvData data, Exception ex) {
		throw new ManagerException(ex.getMessage());
	}

}
