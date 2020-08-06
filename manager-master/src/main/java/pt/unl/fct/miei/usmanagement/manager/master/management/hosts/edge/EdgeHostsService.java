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

package pt.unl.fct.miei.usmanagement.manager.master.management.hosts.edge;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;
import pt.unl.fct.miei.usmanagement.manager.master.management.bash.BashService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.hosts.HostSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.master.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.master.util.ObjectUtils;

@Slf4j
@Service
public class EdgeHostsService {

  private final HostRulesService hostRulesService;
  private final HostSimulatedMetricsService hostSimulatedMetricsService;
  private final SshService sshService;
  private final BashService bashService;

  private final EdgeHostRepository edgeHosts;

  private final String edgeKeyFilePath;

  public EdgeHostsService(@Lazy HostRulesService hostRulesService,
                          @Lazy HostSimulatedMetricsService hostSimulatedMetricsService,
                          @Lazy SshService sshService, BashService bashService,
                          EdgeHostRepository edgeHosts,
                          EdgeHostsProperties edgeHostsProperties) {
    this.hostRulesService = hostRulesService;
    this.hostSimulatedMetricsService = hostSimulatedMetricsService;
    this.sshService = sshService;
    this.bashService = bashService;
    this.edgeHosts = edgeHosts;
    this.edgeKeyFilePath = edgeHostsProperties.getAccess().getKeyFilePath();
  }

  public String getKeyFilePath(EdgeHostEntity edgeHostEntity) {
    String username = edgeHostEntity.getUsername();
    String hostname = edgeHostEntity.getHostname();
    return String.format("%s/%s/%s_%s", System.getProperty("user.dir"), edgeKeyFilePath, username,
        hostname.replace(".", "_"));
  }

  public List<EdgeHostEntity> getEdgeHosts() {
    return edgeHosts.findAll();
  }

  public EdgeHostEntity getEdgeHost(String hostname) {
    return edgeHosts.findEdgeHost(hostname).orElseThrow(() ->
        new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname));
  }

  public EdgeHostEntity addEdgeHost(AddEdgeHostRequest addEdgeHostRequest) {
    EdgeHostEntity edgeHost = EdgeHostEntity.builder()
        .username(addEdgeHostRequest.getUsername())
        .publicDnsName(addEdgeHostRequest.getPublicDnsName())
        .publicIpAddress(addEdgeHostRequest.getPublicIpAddress())
        .privateIpAddress(addEdgeHostRequest.getPrivateIpAddress())
        .region(addEdgeHostRequest.getRegion())
        .country(addEdgeHostRequest.getCountry())
        .city(addEdgeHostRequest.getCity())
        .build();
    return addEdgeHost(edgeHost, addEdgeHostRequest.getPassword());
  }

  public EdgeHostEntity addEdgeHost(EdgeHostEntity edgeHostEntity) {
    return addEdgeHost(edgeHostEntity, null);
  }

  public EdgeHostEntity addEdgeHost(EdgeHostEntity edgeHost, String password) {
    assertHostDoesntExist(edgeHost);
    if (password != null) {
      setupEdgeHost(edgeHost, password);
    }
    log.debug("Saving edgeHost {}", ToStringBuilder.reflectionToString(edgeHost));
    return edgeHosts.save(edgeHost);
  }

  private void setupEdgeHost(EdgeHostEntity edgeHost, String password) {
    String hostname = edgeHost.getHostname();
    String username = edgeHost.getUsername();
    String keyFilePath = getKeyFilePath(edgeHost);
    log.info("Generating keys for edge host {}@{}", username, hostname);
    String generateKeysCommand = String.format("echo y | ssh-keygen -b 2048 -t rsa -f '%s' -q -N \"\" &&"
        + " sshpass -p '%s' ssh-copy-id -i '%s' '%s'", keyFilePath, password, keyFilePath, hostname);
    SshCommandResult generateKeysResult = sshService.executeCommand(hostname, username, password, generateKeysCommand);
    if (!generateKeysResult.isSuccessful()) {
      deleteEdgeHostConfig(edgeHost);
      throw new MasterManagerException("Unable to generate public/private key pair for '%s': %s", hostname,
          generateKeysResult.getError().get(0));
    }
  }

  public EdgeHostEntity updateEdgeHost(String hostname, EdgeHostEntity newEdgeHost) {
    EdgeHostEntity edgeHost = getEdgeHost(hostname);
    log.debug("Updating edgeHost {} with {}",
        ToStringBuilder.reflectionToString(edgeHost),
        ToStringBuilder.reflectionToString(newEdgeHost));
    ObjectUtils.copyValidProperties(newEdgeHost, edgeHost);
    return edgeHosts.save(edgeHost);
  }

  public void deleteEdgeHost(String hostname) {
    var edgeHost = getEdgeHost(hostname);
    edgeHosts.delete(edgeHost);
    deleteEdgeHostConfig(edgeHost);
  }

  public List<EdgeHostEntity> getHostsByRegion(RegionEntity region) {
    return edgeHosts.findByRegion(region);
  }

  public List<EdgeHostEntity> getHostsByCountry(String country) {
    return edgeHosts.findByCountry(country);
  }

  public List<EdgeHostEntity> getHostsByCity(String city) {
    return edgeHosts.findByCity(city);
  }

  public List<HostRuleEntity> getRules(String hostname) {
    assertHostExists(hostname);
    return edgeHosts.getRules(hostname);
  }

  public HostRuleEntity getRule(String hostname, String ruleName) {
    assertHostExists(hostname);
    return edgeHosts.getRule(hostname, ruleName).orElseThrow(() ->
        new EntityNotFoundException(HostRuleEntity.class, "ruleName", ruleName)
    );
  }

  public void addRule(String hostname, String ruleName) {
    assertHostExists(hostname);
    hostRulesService.addEdgeHost(ruleName, hostname);
  }

  public void addRules(String hostname, List<String> ruleNames) {
    assertHostExists(hostname);
    ruleNames.forEach(rule -> hostRulesService.addEdgeHost(rule, hostname));
  }

  public void removeRule(String hostname, String ruleName) {
    assertHostExists(hostname);
    hostRulesService.removeEdgeHost(ruleName, hostname);
  }

  public void removeRules(String hostname, List<String> ruleNames) {
    assertHostExists(hostname);
    ruleNames.forEach(rule -> hostRulesService.removeEdgeHost(rule, hostname));
  }

  public List<HostSimulatedMetricEntity> getSimulatedMetrics(String hostname) {
    assertHostExists(hostname);
    return edgeHosts.getSimulatedMetrics(hostname);
  }

  public HostSimulatedMetricEntity getSimulatedMetric(String hostname, String simulatedMetricName) {
    assertHostExists(hostname);
    return edgeHosts.getSimulatedMetric(hostname, simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(HostSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
    );
  }

  public void addSimulatedMetric(String hostname, String simulatedMetricName) {
    assertHostExists(hostname);
    hostSimulatedMetricsService.addEdgeHost(simulatedMetricName, hostname);
  }

  public void addSimulatedMetrics(String hostname, List<String> simulatedMetricNames) {
    assertHostExists(hostname);
    simulatedMetricNames.forEach(simulatedMetric ->
        hostSimulatedMetricsService.addEdgeHost(simulatedMetric, hostname));
  }

  public void removeSimulatedMetric(String hostname, String simulatedMetricName) {
    assertHostExists(hostname);
    hostSimulatedMetricsService.addEdgeHost(simulatedMetricName, hostname);
  }

  public void removeSimulatedMetrics(String hostname, List<String> simulatedMetricNames) {
    assertHostExists(hostname);
    simulatedMetricNames.forEach(simulatedMetric ->
        hostSimulatedMetricsService.addEdgeHost(simulatedMetric, hostname));
  }

  public boolean hasEdgeHost(String hostname) {
    return edgeHosts.hasEdgeHost(hostname);
  }

  private void assertHostExists(String hostname) {
    if (!hasEdgeHost(hostname)) {
      throw new EntityNotFoundException(EdgeHostEntity.class, "hostname", hostname);
    }
  }

  private void assertHostDoesntExist(EdgeHostEntity edgeHost) {
    var hostname = edgeHost.getPublicDnsName() == null ? edgeHost.getPublicIpAddress() : edgeHost.getPublicDnsName();
    if (edgeHosts.hasEdgeHost(hostname)) {
      throw new DataIntegrityViolationException("Edge host '" + hostname + "' already exists");
    }
  }

  private void deleteEdgeHostConfig(EdgeHostEntity edgeHost) {
    String privateKeyFilePath = getKeyFilePath(edgeHost);
    String publicKeyFilePath = String.format("%s.pub", privateKeyFilePath);
    String cleanupCommand = String.format("rm -f %s %s", privateKeyFilePath, publicKeyFilePath);
    bashService.executeCommand(cleanupCommand);
  }

}
