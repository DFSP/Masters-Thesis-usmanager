/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package pt.unl.fct.microservicemanagement.mastermanager.manager.docker.proxy;

import org.springframework.context.annotation.Lazy;
import pt.unl.fct.microservicemanagement.mastermanager.manager.containers.ContainerConstants;
import pt.unl.fct.microservicemanagement.mastermanager.manager.docker.DockerProperties;
import pt.unl.fct.microservicemanagement.mastermanager.manager.hosts.HostsService;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServiceEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServiceType;
import pt.unl.fct.microservicemanagement.mastermanager.manager.services.ServicesService;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DockerApiProxyService {

  public static final String DOCKER_API_PROXY = "docker-api-proxy";

  private final ServicesService servicesService;
  private final HostsService hostsService;

  private final String dockerApiProxyUsername;
  private final String dockerApiProxyPassword;
  private final int dockerApiPort;
  private final String dockerHubUsername;

  public DockerApiProxyService(ServicesService servicesService,
                               @Lazy HostsService hostsService,
                               DockerProperties dockerProperties) {
    this.servicesService = servicesService;
    this.hostsService = hostsService;
    this.dockerApiProxyUsername = dockerProperties.getApiProxy().getUsername();
    this.dockerApiProxyPassword = dockerProperties.getApiProxy().getPassword();
    this.dockerApiPort = dockerProperties.getApi().getPort();
    this.dockerHubUsername = dockerProperties.getHub().getUsername();
  }

  public String launchDockerApiProxy(String hostname) {
    ServiceEntity dockerApiProxy = servicesService.getService(DOCKER_API_PROXY);
    String serviceName = dockerApiProxy.getServiceName();
    ServiceType serviceType = dockerApiProxy.getServiceType();
    String externalPort = dockerApiProxy.getDefaultExternalPort();
    String internalPort = dockerApiProxy.getDefaultInternalPort();
    var dockerRepository = String.format("%s/%s", dockerHubUsername, dockerApiProxy.getDockerRepository());
    var command = String.format("DOCKER_API_PROXY=$(docker ps -q -f 'name=%s') && "
            + "if [ $DOCKER_API_PROXY ]; then echo $DOCKER_API_PROXY; "
            + "else PRIVATE_IP=$(/sbin/ip -o -4 addr list docker0 | awk '{print $4}' | cut -d/ -f1) && "
            + "docker pull %s && "
            + "docker run -itd --name=docker-api-proxy -p %s:%s --rm "
            + "-e %s=%s -e %s=%s -e %s=http://$PRIVATE_IP:%s "
            + "-l %s=%s -l %s=%s -l %s=%s:%s -l %s=%s %s; fi",
        serviceName, dockerRepository, externalPort, internalPort,
        ContainerConstants.Environment.BASIC_AUTH_USERNAME, dockerApiProxyUsername,
        ContainerConstants.Environment.BASIC_AUTH_PASSWORD, dockerApiProxyPassword,
        ContainerConstants.Environment.PROXY_PASS, dockerApiPort,
        ContainerConstants.Label.SERVICE_NAME, serviceName,
        ContainerConstants.Label.SERVICE_TYPE, serviceType,
        ContainerConstants.Label.SERVICE_ADDRESS, hostname, externalPort,
        ContainerConstants.Label.SERVICE_HOSTNAME, hostname,
        dockerRepository);
    List<String> output = hostsService.executeCommand(command, hostname);
    return output.get(output.size() - 1);
  }

}
