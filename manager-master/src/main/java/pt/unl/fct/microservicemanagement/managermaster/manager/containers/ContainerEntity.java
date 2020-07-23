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

package pt.unl.fct.microservicemanagement.managermaster.manager.containers;

import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.metrics.simulated.containers.SimulatedContainerMetricEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.rulesystem.rules.containers.ContainerRuleEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "containers")
public class ContainerEntity {

  @Id
  @GeneratedValue
  private Long id;

  @NaturalId
  private String containerId;

  @NotNull
  private long created;

  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  private List<String> names;

  @NotNull
  private String image;

  private String command;

  @NotNull
  private String hostname;

  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  private List<ContainerPortMapping> ports;

  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  private Map<String, String> labels;

  @Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "container_rule",
      joinColumns = @JoinColumn(name = "container_id"),
      inverseJoinColumns = @JoinColumn(name = "rule_id")
  )
  private Set<ContainerRuleEntity> containerRules;

  @Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "container_simulated_metric",
      joinColumns = @JoinColumn(name = "container_id"),
      inverseJoinColumns = @JoinColumn(name = "simulated_metric_id")
  )
  private Set<SimulatedContainerMetricEntity> simulatedContainerMetrics;

  public void addRule(ContainerRuleEntity rule) {
    containerRules.add(rule);
    rule.getContainers().add(this);
  }

  public void removeRule(ContainerRuleEntity rule) {
    containerRules.remove(rule);
    rule.getContainers().remove(this);
  }

  public void addSimulatedContainerMetric(SimulatedContainerMetricEntity containerMetric) {
    simulatedContainerMetrics.add(containerMetric);
    containerMetric.getContainers().add(this);
  }

  public void removeSimulatedContainerMetric(SimulatedContainerMetricEntity containerMetric) {
    simulatedContainerMetrics.remove(containerMetric);
    containerMetric.getContainers().remove(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ContainerEntity)) {
      return false;
    }
    ContainerEntity other = (ContainerEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

}
