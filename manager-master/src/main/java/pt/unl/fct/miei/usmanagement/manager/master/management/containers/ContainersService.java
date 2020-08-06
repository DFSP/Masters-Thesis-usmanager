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

package pt.unl.fct.miei.usmanagement.manager.master.management.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.spotify.docker.client.messages.ContainerStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerRepository;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ContainerSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.ContainerRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.containers.DockerContainer;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.containers.DockerContainersService;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.master.management.monitoring.metrics.simulated.containers.ContainerSimulatedMetricsService;
import pt.unl.fct.miei.usmanagement.manager.master.management.rulesystem.rules.ContainerRulesService;

@Service
@Slf4j
public class ContainersService {

  private final DockerContainersService dockerContainersService;
  private final ContainerRulesService containerRulesService;
  private final ContainerSimulatedMetricsService ContainerSimulatedMetricsService;
  private final DockerApiProxyService dockerApiProxyService;

  private final ContainerRepository containers;

  public ContainersService(DockerContainersService dockerContainersService,
                           ContainerRulesService containerRulesService,
                           ContainerSimulatedMetricsService ContainerSimulatedMetricsService,
                           DockerApiProxyService dockerApiProxyService,
                           ContainerRepository containers) {
    this.dockerContainersService = dockerContainersService;
    this.containerRulesService = containerRulesService;
    this.ContainerSimulatedMetricsService = ContainerSimulatedMetricsService;
    this.dockerApiProxyService = dockerApiProxyService;
    this.containers = containers;
  }

  public ContainerEntity addContainerFromDockerContainer(DockerContainer dockerContainer) {
    try {
      return getContainer(dockerContainer.getId());
    } catch (EntityNotFoundException e) {
      ContainerEntity container = ContainerEntity.builder()
          .containerId(dockerContainer.getId())
          .created(dockerContainer.getCreated())
          .names(dockerContainer.getNames())
          .image(dockerContainer.getImage())
          .command(dockerContainer.getCommand())
          .hostname(dockerContainer.getHostname())
          .ports(dockerContainer.getPorts())
          .labels(dockerContainer.getLabels())
          .build();
      return addContainer(container);
    }
  }

  public Optional<ContainerEntity> addContainer(String containerId) {
    return dockerContainersService.getContainer(containerId).map(this::addContainerFromDockerContainer);
  }

  public ContainerEntity addContainer(ContainerEntity container) {
    assertContainerDoesntExist(container);
    log.debug("Saving container {}", ToStringBuilder.reflectionToString(container));
    return containers.save(container);
  }

  public List<ContainerEntity> getContainers() {
    return containers.findAll();
  }

  public ContainerEntity getContainer(String containerId) {
    return containers.findByContainerId(containerId).orElseThrow(() ->
        new EntityNotFoundException(ContainerEntity.class, "containerId", containerId));
  }

  public List<ContainerEntity> getHostContainers(String hostname) {
    return containers.findByHostname(hostname);
  }

  public List<ContainerEntity> getHostContainersWithLabels(String hostname, Set<Pair<String, String>> labels) {
    List<ContainerEntity> containers = getHostContainers(hostname);
    return filterContainersWithLabels(containers, labels);
  }

  public List<ContainerEntity> getContainersWithLabels(Set<Pair<String, String>> labels) {
    List<ContainerEntity> containers = getContainers();
    return filterContainersWithLabels(containers, labels);
  }

  private List<ContainerEntity> filterContainersWithLabels(List<ContainerEntity> containers,
                                                           Set<Pair<String, String>> labels) {
    // TODO try to build a database query instead
    List<String> labelKeys = labels.stream().map(Pair::getFirst).collect(Collectors.toList());
    return containers.stream()
        .filter(container -> {
          for (Map.Entry<String, String> containerLabel : container.getLabels().entrySet()) {
            String key = containerLabel.getKey();
            String value = containerLabel.getValue();
            if (labelKeys.contains(key) && !labels.contains(Pair.of(key, value))) {
              return false;
            }
          }
          return true;
        })
        .collect(Collectors.toList());
  }

  public List<ContainerEntity> reloadContainers() {
    List<ContainerEntity> containers = getContainers();
    List<DockerContainer> dockerContainers = dockerContainersService.getContainers();
    List<String> dockerContainerIds = dockerContainers
        .stream().map(DockerContainer::getId).collect(Collectors.toList());
    Iterator<ContainerEntity> containerIterator = containers.iterator();
    // Remove invalid container entities
    while (containerIterator.hasNext()) {
      ContainerEntity container = containerIterator.next();
      String containerId = container.getContainerId();
      if (!dockerContainerIds.contains(containerId)) {
        deleteContainer(containerId);
        containerIterator.remove();
        log.debug("Removed invalid container {}", containerId);
      }
    }
    // Add missing container entities
    dockerContainers.forEach(dockerContainer -> {
      String isTraceable = dockerContainer.getLabels().get(ContainerConstants.Label.IS_TRACEABLE);
      if (Boolean.parseBoolean(isTraceable)) {
        String containerId = dockerContainer.getId();
        if (!hasContainer(containerId)) {
          ContainerEntity containerEntity = addContainerFromDockerContainer(dockerContainer);
          containers.add(containerEntity);
          log.debug("Added missing container {}", containerId);
        }
      }
    });
    return containers;
  }

  public ContainerEntity launchContainer(String hostname, String serviceName) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, boolean global) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, global);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, List<String> environment) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, environment);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, boolean singleton,
                                         List<String> environment) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, singleton,
        environment);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, Map<String, String> labels) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, labels);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName,
                                         boolean singleton, Map<String, String> labels) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, singleton,
        labels);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, List<String> environment,
                                         Map<String, String> labels) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, environment,
        labels);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, List<String> environment,
                                         Map<String, String> labels, Map<String, String> dynamicLaunchParams) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, environment,
        labels, dynamicLaunchParams);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, boolean singleton,
                                         List<String> environment, Map<String, String> labels) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, singleton,
        environment, labels);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, boolean singleton,
                                         List<String> environment, Map<String, String> labels,
                                         Map<String, String> dynamicLaunchParams) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, singleton,
        environment, labels, dynamicLaunchParams);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, String internalPort,
                                         String externalPort) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, internalPort,
        externalPort);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity launchContainer(String hostname, String serviceName, boolean singleton, String internalPort,
                                         String externalPort) {
    Optional<DockerContainer> container = dockerContainersService.launchContainer(hostname, serviceName, singleton,
        internalPort, externalPort);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public ContainerEntity replicateContainer(String id, String hostname) {
    ContainerEntity containerEntity = getContainer(id);
    Optional<DockerContainer> container = dockerContainersService.replicateContainer(containerEntity, hostname);
    return container.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public List<ContainerEntity> migrateAppContainers(String fromHostname, String toHostname) {
    List<ContainerEntity> containers = getHostContainers(fromHostname).stream()
        .filter(c -> List.of("backend", "frontend").contains(c.getLabels().get(ContainerConstants.Label.SERVICE_TYPE)))
        .collect(Collectors.toList());
    var migratedContainers = new ArrayList<ContainerEntity>(containers.size());
    containers.forEach(c -> migratedContainers.add(migrateContainer(c.getContainerId(), toHostname)));
    return migratedContainers;
  }

  public ContainerEntity migrateContainer(String id, String hostname) {
    ContainerEntity container = getContainer(id);
    Optional<DockerContainer> dockerContainer = dockerContainersService.migrateContainer(container, hostname);
    return dockerContainer.map(this::addContainerFromDockerContainer).orElse(null);
  }

  public Map<String, List<ContainerEntity>> launchApp(List<ServiceEntity> services,
                                                      String region, String country, String city) {
    return dockerContainersService.launchApp(services, region, country, city).entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream().map(this::addContainerFromDockerContainer).collect(Collectors.toList())
        ));
  }

  public void stopContainer(String id) {
    ContainerEntity container = getContainer(id);
    dockerContainersService.stopContainer(container);
    containers.delete(container);
  }

  public void deleteContainer(String id) {
    ContainerEntity container = getContainer(id);
    containers.delete(container);
  }

  public List<ContainerEntity> getAppContainers() {
    return getContainersWithLabels(Set.of(
        Pair.of(ContainerConstants.Label.SERVICE_TYPE, "frontend"),
        Pair.of(ContainerConstants.Label.SERVICE_TYPE, "backend"))
    );
  }

  public List<ContainerEntity> getAppContainers(String hostname) {
    return getHostContainersWithLabels(hostname, Set.of(
        Pair.of(ContainerConstants.Label.SERVICE_TYPE, "frontend"),
        Pair.of(ContainerConstants.Label.SERVICE_TYPE, "backend")));
  }

  public List<ContainerEntity> getDatabaseContainers(String hostname) {
    return getHostContainersWithLabels(hostname, Set.of(
        Pair.of(ContainerConstants.Label.SERVICE_TYPE, "database")));
  }

  public List<ContainerEntity> getSystemContainers(String hostname) {
    return getHostContainersWithLabels(hostname, Set.of(
        Pair.of(ContainerConstants.Label.SERVICE_TYPE, "system")));
  }

  public ContainerStats getContainerStats(String containerId, String hostname) {
    ContainerEntity container = getContainer(containerId);
    return dockerContainersService.getContainerStats(container, hostname);
  }

  public String getLogs(String containerId) {
    ContainerEntity container = getContainer(containerId);
    return dockerContainersService.getContainerLogs(container);
  }

  public List<ContainerRuleEntity> getRules(String containerId) {
    assertContainerExists(containerId);
    return containers.getRules(containerId);
  }

  public void addRule(String containerId, String ruleName) {
    assertContainerExists(containerId);
    containerRulesService.addContainer(ruleName, containerId);
  }

  public void addRules(String containerId, List<String> ruleNames) {
    assertContainerExists(containerId);
    ruleNames.forEach(rule -> containerRulesService.addContainer(rule, containerId));
  }

  public void removeRule(String containerId, String ruleName) {
    assertContainerExists(containerId);
    containerRulesService.removeContainer(ruleName, containerId);
  }

  public void removeRules(String containerId, List<String> ruleNames) {
    assertContainerExists(containerId);
    ruleNames.forEach(rule -> containerRulesService.removeContainer(rule, containerId));
  }

  public List<ContainerSimulatedMetricEntity> getSimulatedMetrics(String containerId) {
    assertContainerExists(containerId);
    return containers.getSimulatedMetrics(containerId);
  }

  public ContainerSimulatedMetricEntity getSimulatedMetric(String containerId, String simulatedMetricName) {
    assertContainerExists(containerId);
    return containers.getSimulatedMetric(containerId, simulatedMetricName).orElseThrow(() ->
        new EntityNotFoundException(ContainerSimulatedMetricEntity.class, "simulatedMetricName", simulatedMetricName)
    );
  }

  public void addSimulatedMetric(String containerId, String simulatedMetricName) {
    assertContainerExists(containerId);
    ContainerSimulatedMetricsService.addContainer(simulatedMetricName, containerId);
  }

  public void addSimulatedMetrics(String containerId, List<String> simulatedMetricNames) {
    assertContainerExists(containerId);
    simulatedMetricNames.forEach(simulatedMetric ->
        ContainerSimulatedMetricsService.addContainer(simulatedMetric, containerId));
  }

  public void removeSimulatedMetric(String containerId, String simulatedMetricName) {
    assertContainerExists(containerId);
    ContainerSimulatedMetricsService.removeContainer(simulatedMetricName, containerId);
  }

  public void removeSimulatedMetrics(String containerId, List<String> simulatedMetricNames) {
    assertContainerExists(containerId);
    simulatedMetricNames.forEach(simulatedMetric ->
        ContainerSimulatedMetricsService.removeContainer(simulatedMetric, containerId));
  }

  public String launchDockerApiProxy(String hostname) {
    return launchDockerApiProxy(hostname, true);
  }

  public String launchDockerApiProxy(String hostname, boolean insertIntoDatabase) {
    String containerId = dockerApiProxyService.launchDockerApiProxy(hostname);
    if (!insertIntoDatabase) {
      addContainer(containerId);
    }
    return containerId;
  }

  public boolean hasContainer(String containerId) {
    return containers.hasContainer(containerId);
  }

  private void assertContainerExists(String containerId) {
    if (!hasContainer(containerId)) {
      throw new EntityNotFoundException(ContainerEntity.class, "containerId", containerId);
    }
  }

  private void assertContainerDoesntExist(ContainerEntity container) {
    var containerId = container.getContainerId();
    if (containers.hasContainer(containerId)) {
      throw new DataIntegrityViolationException("Container '" + containerId + "' already exists");
    }
  }

}
