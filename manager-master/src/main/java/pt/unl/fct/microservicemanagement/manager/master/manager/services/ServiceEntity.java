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

package pt.unl.fct.microservicemanagement.manager.master.manager.services;

import pt.unl.fct.microservicemanagement.manager.master.manager.services.dependencies.ServiceDependencyEntity;
import pt.unl.fct.microservicemanagement.manager.master.manager.apps.AppServiceEntity;
import pt.unl.fct.microservicemanagement.manager.master.manager.monitoring.metrics.simulated.services.SimulatedServiceMetricEntity;
import pt.unl.fct.microservicemanagement.manager.master.manager.prediction.ServiceEventPredictionEntity;
import pt.unl.fct.microservicemanagement.manager.master.manager.rulesystem.rules.services.ServiceRuleEntity;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "services")
public class ServiceEntity {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  @Column(unique = true)
  private String serviceName;

  @NotNull
  private String dockerRepository;

  private String defaultExternalPort;

  private String defaultInternalPort;

  private String defaultDb;

  private String launchCommand;

  @Min(0)
  private Integer minReplicas;

  @Min(0)
  private Integer maxReplicas;

  private String outputLabel;

  @Enumerated(EnumType.STRING)
  @NotNull
  private ServiceType serviceType;

  // ##### placement info

  @NotNull
  private Double expectedMemoryConsumption;

  /*@NotNull
  private Double expectedCpuConsumption;

  @NotNull
  private Double expectedStorageConsumption;

  @NotNull
  private Double expectedBandwidthConsumption;

  // Stateful services shouldn't migrate until all database replication/migration components are incorporated
  @NotNull
  private boolean stateful;

  @Enumerated(EnumType.STRING)
  private Place place;*/


  //QoS requirements of ap-
  //plication microservices such as service delivery deadline, through-
  //put

  // #####


  /*@Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "service_affinity",
      joinColumns = @JoinColumn(name = "service_id"),
      inverseJoinColumns = @JoinColumn(name = "affinity_id")
  )
  private Set<ServiceAffinityEntity> affinities;*/

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<AppServiceEntity> appServices;

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ServiceDependencyEntity> dependencies;

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "dependency", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ServiceDependencyEntity> dependents;

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ServiceEventPredictionEntity> eventPredictions;

  @Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "service_rule",
      joinColumns = @JoinColumn(name = "service_id"),
      inverseJoinColumns = @JoinColumn(name = "rule_id")
  )
  private Set<ServiceRuleEntity> serviceRules;

  @Singular
  @JsonIgnore
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "service_simulated_metric",
      joinColumns = @JoinColumn(name = "service_id"),
      inverseJoinColumns = @JoinColumn(name = "simulated_metric_id")
  )
  private Set<SimulatedServiceMetricEntity> simulatedServiceMetrics;

  public void addRule(ServiceRuleEntity rule) {
    serviceRules.add(rule);
    rule.getServices().add(this);
  }

  public void removeRule(ServiceRuleEntity rule) {
    serviceRules.remove(rule);
    rule.getServices().remove(this);
  }

  public void addSimulatedServiceMetric(SimulatedServiceMetricEntity serviceMetric) {
    simulatedServiceMetrics.add(serviceMetric);
    serviceMetric.getServices().add(this);
  }

  public void removeSimulatedServiceMetric(SimulatedServiceMetricEntity serviceMetric) {
    simulatedServiceMetrics.remove(serviceMetric);
    serviceMetric.getServices().remove(this);
  }

  public boolean hasLaunchCommand() {
    return launchCommand != null && !launchCommand.isBlank();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ServiceEntity)) {
      return false;
    }
    ServiceEntity other = (ServiceEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

}
