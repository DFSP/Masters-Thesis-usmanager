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

package pt.unl.fct.miei.usmanagement.manager.management.remote.ssh;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusProperties;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsProperties;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SshService {

	private static final int EXECUTE_COMMAND_TIMEOUT = 120000;

	private final EdgeHostsService edgeHostsService;
	private final CloudHostsService cloudHostsService;

	private final int connectionTimeout;
	private final String awsKeyFilePath;
	private final Map<String, String> scriptPaths;

	public SshService(EdgeHostsService edgeHostsService, CloudHostsService cloudHostsService,
					  SshProperties sshProperties, AwsProperties awsProperties, DockerProperties dockerProperties,
					  PrometheusProperties prometheusProperties) {
		this.edgeHostsService = edgeHostsService;
		this.cloudHostsService = cloudHostsService;
		this.connectionTimeout = sshProperties.getConnectionTimeout();
		this.awsKeyFilePath = awsProperties.getAccess().getKeyFilePath();
		PrometheusProperties.NodeExporter nodeExporterProperties = prometheusProperties.getNodeExporter();
		this.scriptPaths = Map.of(
			dockerProperties.getInstallScript(), dockerProperties.getInstallScriptPath(),
			dockerProperties.getUninstallScript(), dockerProperties.getUninstallScriptPath(),
			dockerProperties.getInstallApiScript(), dockerProperties.getInstallApiScriptPath(),
			nodeExporterProperties.getInstallScript(), nodeExporterProperties.getInstallScriptPath()
		);
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	private SSHClient initClient(HostAddress hostAddress) throws IOException {
		String publicKeyFile;
		try {
			EdgeHostEntity edgeHost = edgeHostsService.getEdgeHostByAddress(hostAddress);
			hostAddress = edgeHost.getAddress();
			publicKeyFile = edgeHostsService.getPrivateKeyFilePath(edgeHost);
		}
		catch (EntityNotFoundException e) {
			try {
				CloudHostEntity cloudHost = cloudHostsService.getCloudHostByAddress(hostAddress);
				hostAddress = cloudHost.getAddress();
			}
			catch (EntityNotFoundException ignored) {
			}
			publicKeyFile = String.format("%s/%s", System.getProperty("user.dir"), awsKeyFilePath);
		}
		return initClient(hostAddress.getUsername(), hostAddress.getPublicIpAddress(), new File(publicKeyFile));
	}

	private SSHClient initClient(String username, String hostname, File publicKeyFile) throws IOException {
		SSHClient sshClient = new SSHClient();
		sshClient.setConnectTimeout(connectionTimeout);
		sshClient.addHostKeyVerifier(new PromiscuousVerifier());
		log.info("Logging in to host {}@{} using key {}", username, hostname, publicKeyFile);
		sshClient.connect(hostname);
		PKCS8KeyFile keyFile = new PKCS8KeyFile();
		keyFile.init(publicKeyFile);
		sshClient.authPublickey(username, keyFile);
		log.info("Successfully logged in to host {}@{}", username, hostname);
		return sshClient;
	}

	private SSHClient initClient(HostAddress hostAddress, String password) throws IOException {
		String hostname = hostAddress.getPublicIpAddress();
		String username = hostAddress.getUsername();
		SSHClient sshClient = new SSHClient();
		sshClient.setConnectTimeout(connectionTimeout);
		sshClient.addHostKeyVerifier(new PromiscuousVerifier());
		log.info("Logging in to host {} using password", hostAddress);
		sshClient.connect(hostname);
		sshClient.authPassword(username, password);
		log.info("Successfully logged in to host {}", hostAddress);
		return sshClient;
	}

	public void uploadFile(HostAddress hostAddress, String filename) {
		try (SSHClient sshClient = initClient(hostAddress);
			 SFTPClient sftpClient = sshClient.newSFTPClient()) {
			String scriptPath = scriptPaths.get(filename);
			if (scriptPath == null) {
				throw new EntityNotFoundException(File.class, "name", filename);
			}
			File file = new File(scriptPath);
			log.info("Transferring file {} to host {}", filename, hostAddress);
			sftpClient.put(new FileSystemFile(file), filename);
		}
		catch (IOException e) {
			log.error("Failed to transfer file {} to {}: {}", filename, hostAddress.toSimpleString(), e.getMessage());
			e.printStackTrace();
			throw new ManagerException(e.getMessage());
		}
	}

	public SshCommandResult executeCommandSync(String command, HostAddress hostAddress) {
		return executeCommand(command, hostAddress, true);
	}

	public void executeCommandAsync(String command, HostAddress hostAddress) {
		executeCommand(command, hostAddress, false);
	}

	private SshCommandResult executeCommand(String command, HostAddress hostAddress, boolean wait) {
		try (SSHClient sshClient = initClient(hostAddress);
			 Session session = sshClient.startSession()) {
			return executeCommand(session, command, hostAddress, wait);
		}
		catch (IOException e) {
			log.error("Failed to execute command {} on host {}: {}", command, hostAddress.toSimpleString(), e.getMessage());
			return new SshCommandResult(hostAddress, command, -1, List.of(), List.of(e.getMessage()));
		}
	}

	public SshCommandResult executeCommandSync(String command, HostAddress hostAddress, String password) {
		return executeCommand(command, hostAddress, password, true);
	}

	public SshCommandResult executeCommandAsync(String command, HostAddress hostAddress, String password) {
		return executeCommand(command, hostAddress, password, false);
	}

	private SshCommandResult executeCommand(String command, HostAddress hostAddress, String password, boolean wait) {
		try (SSHClient sshClient = initClient(hostAddress, password);
			 Session session = sshClient.startSession()) {
			return executeCommand(session, command, hostAddress, wait);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new SshCommandResult(hostAddress, command, -1, List.of(), List.of(e.getMessage()));
		}
	}

	private SshCommandResult executeCommand(Session session, String command, HostAddress hostAddress, boolean wait) throws IOException {
		if (!command.contains("sshpass")) {
			// to avoid logging passwords
			log.info("Executing: {}, at host {}", command, hostAddress);
		}
		Session.Command cmd = session.exec(command);
		if (!wait) {
			return null;
		}
		cmd.join(EXECUTE_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS);
		int exitStatus = cmd.getExitStatus();
		List<String> output = Arrays.asList(IOUtils.readFully(cmd.getInputStream()).toString().strip().split("\n"));
		List<String> error = Arrays.asList(IOUtils.readFully(cmd.getErrorStream()).toString().strip().split("\n"));
		log.info("Command exited with\nstatus: {}\noutput: {}\nerror: {}", exitStatus, output, error);
		return new SshCommandResult(hostAddress, command, exitStatus, output, error);
	}

	public boolean hasConnection(HostAddress hostAddress) {
		log.info("Checking connectivity to {}", hostAddress);
		try (SSHClient client = initClient(hostAddress);
			 Session ignored = client.startSession()) {
			log.info("Successfully connected to {}", hostAddress);
			return true;
		}
		catch (IOException e) {
			log.info("Failed to connect to {}: {}", hostAddress, e.getMessage());
		}
		return false;
	}

	public Set<String> getScripts() {
		return scriptPaths.keySet();
	}

	public void executeCommandInBackground(String command, HostAddress hostAddress, Path outputFilePath) {
		String executeCommand = String.format("nohup %s > %s", command, outputFilePath.toString());
		executeCommandAsync(executeCommand, hostAddress);
	}

	public void executeCommandInBackground(String command, HostAddress hostAddress) {
		String filename = String.valueOf(System.currentTimeMillis());
		String filepath = String.format("%s/logs/commands/%s/%s.log", System.getProperty("user.dir"), hostAddress.getPublicIpAddress(), filename);
		Path path = Path.of(filepath);
		try {
			Files.createDirectories(path.getParent());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yy HH:mm:ss.SSS");
			Files.write(path, (formatter.format(LocalDateTime.now()) + ": " + command + "\n\n").getBytes());
		}
		catch (IOException e) {
			log.error("Failed to store output of background command {}: {}", command, e.getMessage());
			throw new ManagerException("Failed to store output of background command %s: %s", command, e.getMessage());
		}
		executeCommandInBackground(command, hostAddress, path);
	}
}
