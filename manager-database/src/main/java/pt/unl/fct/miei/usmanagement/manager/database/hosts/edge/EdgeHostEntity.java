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

package pt.unl.fct.miei.usmanagement.manager.database.hosts.edge;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;

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

  @NotNull
  private String country;

  @NotNull
  private String city;

  //@NotNull
  private String address;

  //@NotNull
  private Double latitude;

  //@NotNull
  private Double longitude;

  @JsonIgnoreProperties({"edgeHost", "cloudHost"})
  @ManyToOne
  private WorkerManagerEntity managedByWorker;

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
  private Set<HostSimulatedMetricEntity> simulatedHostMetrics;

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

  public void addHostSimulatedMetric(HostSimulatedMetricEntity hostMetric) {
    simulatedHostMetrics.add(hostMetric);
    hostMetric.getEdgeHosts().add(this);
  }

  public void removeHostSimulatedMetric(HostSimulatedMetricEntity hostMetric) {
    simulatedHostMetrics.remove(hostMetric);
    hostMetric.getEdgeHosts().remove(this);
  }

  public HostAddress getAddress() {
    return new HostAddress(username, publicDnsName, publicIpAddress, privateIpAddress);
  }

  public HostLocation getLocation() {
    return new HostLocation(city, country, region.getName(), getContinent());
  }

  @JsonIgnore
  public String getContinent() {
    if (region == null) {
      return "";
    }
    String regionName = region.getName();
    if (regionName.isEmpty()) {
      return regionName;
    }
    String zone = regionName.substring(0, regionName.indexOf('-'));
    if (zone.startsWith("us") || zone.startsWith("ca")) {
      return "na";
    }
    if (regionName.startsWith("sa") || regionName.startsWith("eu")) {
      return regionName;
    }
    if (regionName.contains("ap-southeast-1")) {
      return "oc";
    }
    if (regionName.startsWith("ap")) {
      return "as";
    }
    return "";
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
