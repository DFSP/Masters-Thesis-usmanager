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

package pt.unl.fct.miei.usmanagement.manager.worker.management.remote.ssh;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.worker.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.aws.AwsProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.edge.EdgeHostsProperties;
import pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.worker.management.monitoring.prometheus.PrometheusProperties;

@Service
@Slf4j
public class SshService {

  private static final int EXECUTE_COMMAND_TIMEOUT = 120000;

  private final EdgeHostsService edgeHostsService;

  private final int connectionTimeout;
  private final String awsKeyFilePath;
  private final String awsUser;
  private final Map<String, String> scriptPaths;

  public SshService(EdgeHostsService edgeHostsService, SshProperties sshProperties,
                    AwsProperties awsProperties, DockerProperties dockerProperties,
                    PrometheusProperties prometheusProperties) {
    this.edgeHostsService = edgeHostsService;
    this.connectionTimeout = sshProperties.getConnectionTimeout();
    this.awsKeyFilePath = awsProperties.getAccess().getKeyFilePath();
    this.awsUser = awsProperties.getAccess().getUsername();
    PrometheusProperties.NodeExporter nodeExporterProperties = prometheusProperties.getNodeExporter();
    this.scriptPaths = Map.of(
        dockerProperties.getInstallScript(), dockerProperties.getInstallScriptPath(),
        dockerProperties.getUninstallScript(), dockerProperties.getUninstallScriptPath(),
        nodeExporterProperties.getInstallScript(), nodeExporterProperties.getInstallScriptPath()
    );
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  private SSHClient initClient(String hostname) throws IOException {
    String username;
    String publicKeyFile;
    try {
      EdgeHostEntity edgeHostEntity = edgeHostsService.getEdgeHost(hostname);
      username = edgeHostEntity.getUsername();
      publicKeyFile = edgeHostsService.getKeyFilePath(edgeHostEntity);
    } catch (EntityNotFoundException e) {
      username = awsUser;
      publicKeyFile = String.format("%s/%s", System.getProperty("user.dir"), awsKeyFilePath);
    }
    return initClient(username, hostname, new File(publicKeyFile));
  }

  private SSHClient initClient(String username, String hostname, File publicKeyFile) throws IOException {
    var sshClient = new SSHClient();
    sshClient.setConnectTimeout(connectionTimeout);
    sshClient.addHostKeyVerifier(new PromiscuousVerifier());
    log.info("Logging in to host '{}@{}' using key '{}'", username, hostname, publicKeyFile);
    sshClient.connect(hostname);
    var keyFile = new PKCS8KeyFile();
    keyFile.init(publicKeyFile);
    sshClient.authPublickey(username, keyFile);
    log.info("Logged in to host '{}@{}'", username, hostname);
    return sshClient;
  }

  private SSHClient initClient(String hostname, String username, String password) throws IOException {
    var sshClient = new SSHClient();
    sshClient.setConnectTimeout(connectionTimeout);
    sshClient.addHostKeyVerifier(new PromiscuousVerifier());
    log.info("Logging in to host '{}@{}' using password", username, hostname);
    sshClient.connect(hostname);
    sshClient.authPassword(username, password);
    log.info("Logged in to host '{}@{}'", username, hostname);
    return sshClient;
  }

  public void uploadFile(String hostname, String filename) {
    try (SSHClient sshClient = initClient(hostname);
         SFTPClient sftpClient = sshClient.newSFTPClient()) {
      String scriptPath = scriptPaths.get(filename);
      if (scriptPath == null) {
        throw new EntityNotFoundException(File.class, "name", filename);
      }
      var file = new File(scriptPath);
      log.info("Transferring file {} to host {}", filename, hostname);
      sftpClient.put(new FileSystemFile(file), filename);
    } catch (IOException e) {
      e.printStackTrace();
      throw new MasterManagerException(e.getMessage());
    }
  }

  public SshCommandResult executeCommand(String hostname, String command) {
    try (SSHClient sshClient = initClient(hostname);
         Session session = sshClient.startSession()) {
      return executeCommand(session, hostname, command);
    } catch (IOException e) {
      e.printStackTrace();
      return new SshCommandResult(hostname, command, -1, List.of(), List.of(e.getMessage()));
    }
  }

  public SshCommandResult executeCommand(String hostname, String username, String password, String command) {
    try (SSHClient sshClient = initClient(hostname, username, password);
         Session session = sshClient.startSession()) {
      return executeCommand(session, hostname, command);
    } catch (IOException e) {
      e.printStackTrace();
      return new SshCommandResult(hostname, command, -1, List.of(), List.of(e.getMessage()));
    }
  }

  private SshCommandResult executeCommand(Session session, String hostname, String command) throws IOException {
    log.info("Executing: {}, at host {}", command, hostname);
    Session.Command cmd = session.exec(command);
    cmd.join(EXECUTE_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS);
    int exitStatus = cmd.getExitStatus();
    List<String> output = Arrays.asList(IOUtils.readFully(cmd.getInputStream()).toString().strip().split("\n"));
    List<String> error = Arrays.asList(IOUtils.readFully(cmd.getErrorStream()).toString().strip().split("\n"));
    log.info("Command exited with\nstatus: {}\noutput: {}\nerror: {}", exitStatus, output, error);
    return new SshCommandResult(hostname, command, exitStatus, output, error);
  }

  public boolean hasConnection(String hostname) {
    log.info("Checking connectivity to {}", hostname);
    try (SSHClient client = initClient(hostname);
         Session ignored = client.startSession()) {
      log.info("Successfully connected to {}", hostname);
      return true;
    } catch (NoRouteToHostException | SocketTimeoutException | ConnectException ignored) {
      // ignored
    } catch (IOException e) {
      e.printStackTrace();
    }
    log.info("Failed to connect to {}", hostname);
    return false;
  }


}
