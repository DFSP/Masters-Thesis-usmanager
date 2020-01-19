/*
 * MIT License
 *
 * Copyright (c) 2020 micro-manager
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

package pt.unl.fct.microservicemanagement.mastermanager.docker;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.springframework.stereotype.Service;

@Service
public class DockerCoreService {

  //TODO check if this time can be lower 10/10?
  private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long READ_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

  private final String dockerAuthorization;
  private final int dockerApiPort;

  public DockerCoreService(DockerProperties dockerProperties) {
    String dockerApiProxyUsername = dockerProperties.getApiProxy().getUsername();
    String dockerApiProxyPassword = dockerProperties.getApiProxy().getPassword();
    var auth = String.format("%s:%s", dockerApiProxyUsername, dockerApiProxyPassword).getBytes();
    this.dockerAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
    this.dockerApiPort = dockerProperties.getApiProxy().getPort();
  }

  public DockerClient getDockerClient(String hostname) {
    final String uri = String.format("http://%s:%d", hostname, dockerApiPort);
    return DefaultDockerClient.builder().uri(uri).header("Authorization", dockerAuthorization)
        .connectTimeoutMillis(CONNECTION_TIMEOUT).readTimeoutMillis(READ_TIMEOUT).build();
  }

}
