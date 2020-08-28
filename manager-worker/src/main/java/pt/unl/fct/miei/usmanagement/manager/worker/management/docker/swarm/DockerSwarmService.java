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

package pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.SwarmJoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.WorkerManagerException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.bash.BashCommandResult;
import pt.unl.fct.miei.usmanagement.manager.worker.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.DockerCoreService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.swarm.nodes.SimpleNode;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.HostsService;

@Service
@Slf4j
public class DockerSwarmService {

  private final DockerCoreService dockerCoreService;
  private final NodesService nodesService;
  private final HostsService hostsService;
  private final BashService bashService;

  public DockerSwarmService(DockerCoreService dockerCoreService,
                            @Lazy NodesService nodesService,
                            @Lazy HostsService hostsService,
                            BashService bashService) {
    this.dockerCoreService = dockerCoreService;
    this.nodesService = nodesService;
    this.hostsService = hostsService;
    this.bashService = bashService;
  }

  public DockerClient getSwarmLeader() {
    String privateIp = hostsService.getHostAddress().getPrivateIpAddress();
    return dockerCoreService.getDockerClient(privateIp);
  }

  public Optional<String> getSwarmManagerNodeId(String hostname) {
    try (var docker = dockerCoreService.getDockerClient(hostname)) {
      return Objects.equals(docker.info().swarm().localNodeState(), "active")
          && docker.info().swarm().controlAvailable()
          ? Optional.of(nodesService.getHostNode(hostname).getId())
          : Optional.empty();
    } catch (DockerException | InterruptedException e) {
      return Optional.empty();
    }
  }

  public Optional<String> getSwarmWorkerNodeId(String hostname) {
    try (var docker = dockerCoreService.getDockerClient(hostname)) {
      return Objects.equals(docker.info().swarm().localNodeState(), "active")
          && !docker.info().swarm().controlAvailable()
          ? Optional.of(docker.info().swarm().nodeId())
          : Optional.empty();
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new WorkerManagerException(e.getMessage());
    }
  }

  public SimpleNode initSwarm() {
    HostAddress hostAddress = hostsService.getHostAddress();
    String advertiseAddress = hostAddress.getPublicIpAddress();
    String listenAddress = hostAddress.getPrivateIpAddress();
    log.info("Initializing docker swarm at {}", advertiseAddress);
    String command = String.format("docker swarm init --advertise-addr %s --listen-addr %s", /*--availability drain*/
        advertiseAddress, listenAddress);
    BashCommandResult result = bashService.executeCommand(command);
    if (!result.isSuccessful()) {
      throw new WorkerManagerException("Unable to init docker swarm at %s: %s", advertiseAddress, result.getError());
    }
    String output = String.join("\n", result.getOutput());
    String nodeIdRegex = "(?<=Swarm initialized: current node \\()(.*)(?=\\) is now a manager)";
    Matcher nodeIdRegexExpression = Pattern.compile(nodeIdRegex).matcher(output);
    if (!nodeIdRegexExpression.find()) {
      throw new WorkerManagerException("Unable to get docker swarm node id");
    }
    String nodeId = nodeIdRegexExpression.group(0);
    nodesService.addLabel(nodeId, NodeConstants.Label.PRIVATE_IP_ADDRESS, listenAddress);
    return nodesService.getNode(nodeId);
  }

  public SimpleNode rejoinSwarm(String nodeId) {
    SimpleNode node = nodesService.getNode(nodeId);
    String publicIpAddress = node.getHostname();
    String privateIpAddress = node.getPrivateIpAddress();
    NodeRole role = node.getRole();
    nodesService.removeNode(nodeId);
    return joinSwarm(publicIpAddress, privateIpAddress, role);
  }

  public SimpleNode joinSwarm(String publicIpAddress, String privateIpAddress, NodeRole role) {
    String leaderAddress = hostsService.getHostAddress().getPublicIpAddress();
    try (DockerClient leaderClient = getSwarmLeader();
         DockerClient nodeClient = dockerCoreService.getDockerClient(publicIpAddress)) {
      leaveSwarm(nodeClient);
      log.info("{} is joining the swarm as {}", publicIpAddress, role);
      String joinToken;
      switch (role) {
        case MANAGER:
          joinToken = leaderClient.inspectSwarm().joinTokens().manager();
          break;
        case WORKER:
          joinToken = leaderClient.inspectSwarm().joinTokens().worker();
          break;
        default:
          throw new UnsupportedOperationException();
      }
      SwarmJoin swarmJoin = SwarmJoin.builder()
          .advertiseAddr(publicIpAddress)
          .listenAddr(privateIpAddress)
          .joinToken(joinToken)
          .remoteAddrs(List.of(leaderAddress))
          .build();
      nodeClient.joinSwarm(swarmJoin);
      String nodeId = nodeClient.info().swarm().nodeId();
      log.info("Host {} ({}) has joined the swarm as node {}", publicIpAddress, privateIpAddress, nodeId);
      nodesService.addLabel(nodeId, NodeConstants.Label.PRIVATE_IP_ADDRESS, privateIpAddress);
      return nodesService.getNode(nodeId);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new WorkerManagerException(e.getMessage());
    }
  }

  public void leaveSwarm(String hostname) {
    try (DockerClient docker = dockerCoreService.getDockerClient(hostname)) {
      leaveSwarm(docker);
    }
  }

  private void leaveSwarm(DockerClient docker) {
    try {
      boolean isNode = !Objects.equals(docker.info().swarm().localNodeState(), "inactive");
      if (isNode) {
        String nodeId = docker.info().swarm().nodeId();
        boolean isManager = docker.info().swarm().controlAvailable();
        Integer managers = docker.info().swarm().managers();
        if (isManager && managers != null && managers > 1) {
          nodesService.changeRole(nodeId, NodeRole.WORKER);
        }
        docker.leaveSwarm(true);
        log.info("{} ({}) left the swarm", docker.getHost(), nodeId);
      }
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
      throw new WorkerManagerException(e.getMessage());
    }
  }

  public void destroy() {
    try {
      getSwarmLeader().listNodes().stream().map(node -> node.status().addr()).forEach(this::leaveSwarm);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
    }
  }

}
