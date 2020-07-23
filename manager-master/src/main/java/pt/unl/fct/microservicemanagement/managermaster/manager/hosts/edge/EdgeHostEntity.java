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

package pt.unl.fct.microservicemanagement.managermaster.manager.hosts.edge;

import pt.unl.fct.microservicemanagement.managermaster.manager.location.RegionEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.metrics.simulated.hosts.SimulatedHostMetricEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.rulesystem.rules.hosts.HostRuleEntity;

import java.util.Objects;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;


@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "edge_hosts")
public class EdgeHostEntity {

  @Id
  @GeneratedValue
  private Long id;

  private String username;

  private String publicDnsName;

  private String privateIpAddress;

  private String publicIpAddress;

  @ManyToOne
  private RegionEntity region;

  private String country;

  private String city;

  @Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "edge_host_rule",
      joinColumns = @JoinColumn(name = "edge_host_id"),
      inverseJoinColumns = @JoinColumn(name = "rule_id")
  )
  private Set<HostRuleEntity> hostRules;

  @Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "edge_host_simulated_metric",
      joinColumns = @JoinColumn(name = "edge_host_id"),
      inverseJoinColumns = @JoinColumn(name = "simulated_metric_id")
  )
  private Set<SimulatedHostMetricEntity> simulatedHostMetrics;

  @JsonIgnore
  public String getHostname() {
    return this.publicDnsName == null ? this.publicIpAddress : this.publicDnsName;
  }

  public void addRule(HostRuleEntity rule) {
    hostRules.add(rule);
    rule.getEdgeHosts().add(this);
  }

  public void removeRule(HostRuleEntity rule) {
    hostRules.remove(rule);
    rule.getEdgeHosts().remove(this);
  }

  public void addSimulatedHostMetric(SimulatedHostMetricEntity hostMetric) {
    simulatedHostMetrics.add(hostMetric);
    hostMetric.getEdgeHosts().add(this);
  }

  public void removeSimulatedHostMetric(SimulatedHostMetricEntity hostMetric) {
    simulatedHostMetrics.remove(hostMetric);
    hostMetric.getEdgeHosts().remove(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EdgeHostEntity)) {
      return false;
    }
    EdgeHostEntity other = (EdgeHostEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

}