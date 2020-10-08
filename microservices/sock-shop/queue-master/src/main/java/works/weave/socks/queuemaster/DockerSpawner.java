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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DockerSpawner {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private DockerClient dc;
	private ExecutorService dockerPool;

	private String imageName = "weaveworksdemos/worker";
	private String imageVersion = "latest";
	private String networkId = "weavedemo_backoffice";
	private int poolSize = 50;

	public void init() {
		if (dc == null) {
			DockerClientConfig.DockerClientConfigBuilder builder = DockerClientConfig.createDefaultConfigBuilder();

			DockerClientConfig config = builder.build();
			dc = DockerClientBuilder.getInstance(config).build();

			dc.pullImageCmd(imageName).withTag(imageVersion).exec(new PullImageResultCallback()).awaitSuccess();
		}
		if (dockerPool == null) {
			dockerPool = Executors.newFixedThreadPool(poolSize);
		}
	}

	public void spawn() {
		dockerPool.execute(new Runnable() {
			public void run() {
				logger.info("Spawning new container");
				try {
					CreateContainerResponse container = dc.createContainerCmd(imageName + ":" + imageVersion).withNetworkMode(networkId).withCmd("ping", "rabbitmq").exec();
					String containerId = container.getId();
					dc.startContainerCmd(containerId).exec();
					logger.info("Spawned container with id: " + container.getId() + " on network: " + networkId);
					// TODO instead of just sleeping, call await on the container and remove once it's completed.
					Thread.sleep(40000);
					try {
						dc.stopContainerCmd(containerId).exec();
					}
					catch (DockerException e) {
						logger.info("Container already stopped. (This is expected).");
					}
					dc.removeContainerCmd(containerId).exec();
					logger.info("Removed Container:" + containerId);
				}
				catch (Exception e) {
					logger.error("Exception trying to launch/remove worker container. " + e);
				}
			}
		});
	}
}
