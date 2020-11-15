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

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Service
public class SymService {


	private final ServletContext servletContext;
	private final DataSource dataSource;
	private final ApplicationContext applicationContext;
	private final DataSourceProperties dataSourceProperties;
	private final Environment environment;

	private ServerSymmetricEngine serverSymmetricEngine;

	public SymService(ServletContext servletContext, DataSource dataSource, ApplicationContext applicationContext,
					  DataSourceProperties dataSourceProperties, Environment environment) {
		this.servletContext = servletContext;
		this.dataSource = dataSource;
		this.applicationContext = applicationContext;
		this.dataSourceProperties = dataSourceProperties;
		this.environment = environment;
	}

	public void startSymmetricDsService(String externalId, String registrationUrl, HostAddress hostAddress) throws IOException {
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
		serverSymmetricEngine.start();
	}

	public void stopSymmetricDSServer() {
		if (serverSymmetricEngine != null) {
			serverSymmetricEngine.stop();
		}
	}

}