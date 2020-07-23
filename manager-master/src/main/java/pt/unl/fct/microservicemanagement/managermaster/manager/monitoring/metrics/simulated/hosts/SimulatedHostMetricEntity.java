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

package pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.metrics.simulated.hosts;


import pt.unl.fct.microservicemanagement.managermaster.manager.fields.FieldEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.hosts.edge.EdgeHostEntity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "simulated_host_metrics")
public class SimulatedHostMetricEntity {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  @Column(unique = true)
  private String name;

  @ManyToOne
  @JoinColumn(name = "field_id")
  private FieldEntity field;

  private double minimumValue;

  private double maximumValue;

  private boolean override;

  private boolean generic;

  @Singular
  @JsonIgnore
  @ManyToMany(mappedBy = "simulatedHostMetrics", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Set<CloudHostEntity> cloudHosts;

  @Singular
  @JsonIgnore
  @ManyToMany(mappedBy = "simulatedHostMetrics", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Set<EdgeHostEntity> edgeHosts;

  public void removeAssociations() {
    Iterator<CloudHostEntity> cloudHostsIterator = cloudHosts.iterator();
    while (cloudHostsIterator.hasNext()) {
      CloudHostEntity cloudHost = cloudHostsIterator.next();
      cloudHostsIterator.remove();
      cloudHost.getSimulatedHostMetrics().remove(this);
    }
    Iterator<EdgeHostEntity> edgeHostsIterator = edgeHosts.iterator();
    while (edgeHostsIterator.hasNext()) {
      EdgeHostEntity edgeHost = edgeHostsIterator.next();
      edgeHostsIterator.remove();
      edgeHost.getSimulatedHostMetrics().remove(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimulatedHostMetricEntity)) {
      return false;
    }
    SimulatedHostMetricEntity other = (SimulatedHostMetricEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

}
