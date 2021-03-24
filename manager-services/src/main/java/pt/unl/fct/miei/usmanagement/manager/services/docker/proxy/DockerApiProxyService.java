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

package pt.unl.fct.miei.usmanagement.manager.services.docker.proxy;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.services.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.HostsService;

import java.util.List;

@Slf4j
@org.springframework.stereotype.Service
public class DockerApiProxyService {

	private final HostsService hostsService;

	private final String managerId;
	private final String dockerApiProxyUsername;
	private final String dockerApiProxyPassword;
	private final int dockerApiProxyPort;
	private final int dockerApiPort;
	private final String dockerHubUsername;

	public DockerApiProxyService(@Lazy HostsService hostsService, DockerProperties dockerProperties, Environment environment) {
		this.hostsService = hostsService;
		this.managerId = environment.getProperty(ContainerConstants.Environment.Manager.ID);
		this.dockerApiProxyUsername = dockerProperties.getApiProxy().getUsername();
		this.dockerApiProxyPassword = dockerProperties.getApiProxy().getPassword();
		this.dockerApiProxyPort = dockerProperties.getApiProxy().getPort();
		this.dockerApiPort = dockerProperties.getApi().getPort();
		this.dockerHubUsername = dockerProperties.getHub().getUsername();
	}

	public String launchDockerApiProxy(HostAddress hostAddress) {
		String serviceName = ServiceConstants.Name.DOCKER_API_PROXY;
		String dockerRepository = dockerHubUsername + "/" + serviceName;
		Gson gson = new Gson();
		String command = String.format("DOCKER_API_PROXY=$(docker ps -q -f 'name=%s') && "
				+ "if [ $DOCKER_API_PROXY ]; then echo $DOCKER_API_PROXY; "
				+ "else docker pull %s && "
				+ "docker run -itd --name=%s -p %d:%d --hostname %s --rm "
				+ "-e %s=%s -e %s=%s -e %s=http://%s:%s "
				+ "-l %s=%b -l %s=%s -l %s=%s -l %s=%s -l %s='%s' -l %s=%s -l %s='%s' %s; fi",
			serviceName, dockerRepository, serviceName, dockerApiProxyPort, 80, serviceName,
			ContainerConstants.Environment.Proxy.BASIC_AUTH_USERNAME, dockerApiProxyUsername,
			ContainerConstants.Environment.Proxy.BASIC_AUTH_PASSWORD, dockerApiProxyPassword,
			ContainerConstants.Environment.Proxy.PROXY_PASS, hostAddress.getPrivateIpAddress(), dockerApiPort,
			ContainerConstants.Label.US_MANAGER, true,
			ContainerConstants.Label.CONTAINER_TYPE, ContainerTypeEnum.SINGLETON,
			ContainerConstants.Label.SERVICE_NAME, serviceName,
			ContainerConstants.Label.SERVICE_TYPE, ServiceTypeEnum.SYSTEM,
			ContainerConstants.Label.COORDINATES, gson.toJson(hostAddress.getCoordinates()),
			ContainerConstants.Label.REGION, hostAddress.getRegion().name(),
			ContainerConstants.Label.MANAGER_ID, managerId,
			dockerRepository);
		List<String> output = hostsService.executeCommandSync(command, hostAddress);
		return output.get(output.size() - 1);
	}

	public void stopDockerApiProxy(HostAddress hostAddress) {
		log.info("Stopping docker api proxy on host {}", hostAddress.toSimpleString());
		String command = String.format("docker stop $(docker ps -q -f \"name=%s\")", ServiceConstants.Name.DOCKER_API_PROXY);
		hostsService.executeCommandAsync(command, hostAddress);
	}

}
