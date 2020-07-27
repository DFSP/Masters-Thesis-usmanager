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

package pt.unl.fct.microservicemanagement.manager.master.manager.docker;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("docker")
public class DockerProperties {

  private final Api api;
  private final ApiProxy apiProxy;
  private final Hub hub;
  private final Swarm swarm;
  private String installScript;
  private String installScriptPath;
  private String uninstallScript;
  private String uninstallScriptPath;
  private String repository;

  public DockerProperties() {
    this.api = new Api();
    this.apiProxy = new ApiProxy();
    this.hub = new Hub();
    this.swarm = new Swarm();
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter
  @Setter
  public static final class Api {

    private int port;

  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter
  @Setter
  public static final class ApiProxy {

    private String username;
    private String password;
    private int port;

  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter
  @Setter
  public static final class Hub {

    private String username;

  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter
  @Setter
  public static final class Swarm {

    private String manager;
    private int maxWorkers;

  }

}
