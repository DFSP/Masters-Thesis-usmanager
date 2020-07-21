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

package works.weave.socks.queuemaster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DockerSpawner {

  private static final String IMAGE_NAME = "weaveworksdemos/worker";
  private static final String IMAGE_VERSION = "latest";
  private static final String NETWORK_ID = "weavedemo_backoffice";
  private static final int POOL_SIZE = 50;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private DockerClient dockerClient;
  private ExecutorService dockerPool;

  public void init() {
    if (dockerClient == null) {
      DockerClientConfig.DockerClientConfigBuilder builder = DockerClientConfig.createDefaultConfigBuilder();
      DockerClientConfig config = builder.build();
      dockerClient = DockerClientBuilder.getInstance(config).build();
      dockerClient.pullImageCmd(IMAGE_NAME).withTag(IMAGE_VERSION).exec(new PullImageResultCallback()).awaitSuccess();
    }
    if (dockerPool == null) {
      dockerPool = Executors.newFixedThreadPool(POOL_SIZE);
    }
  }

  public void spawn() {
    dockerPool.execute(() -> {
      logger.info("Spawning new container");
      try {
        CreateContainerResponse container = dockerClient.createContainerCmd(IMAGE_NAME + ":" + IMAGE_VERSION)
            .withNetworkMode(NETWORK_ID).withCmd("ping", "rabbitmq").exec();
        String containerId = container.getId();
        dockerClient.startContainerCmd(containerId).exec();
        logger.info("Spawned container with id: " + container.getId() + " on network: " + NETWORK_ID);
        // TODO instead of just sleeping, call await on the container and remove once it's completed.
        Thread.sleep(40000);
        try {
          dockerClient.stopContainerCmd(containerId).exec();
        } catch (DockerException e) {
          logger.info("Container already stopped. (This is expected).");
        }
        dockerClient.removeContainerCmd(containerId).exec();
        logger.info("Removed Container:" + containerId);
      } catch (Exception e) {
        logger.error("Exception trying to launch/remove worker container. " + e);
      }
    });
  }

}
