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

package pt.unl.fct.microservicemanagement.managermaster.manager.rulesystem.decision;

import pt.unl.fct.microservicemanagement.managermaster.manager.componenttypes.ComponentTypeEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.event.HostEventEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.monitoring.event.ServiceEventEntity;
import pt.unl.fct.microservicemanagement.managermaster.manager.rulesystem.rules.RuleDecision;

import java.util.HashSet;
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
@Table(name = "decisions")
public class DecisionEntity {

  @Id
  @GeneratedValue
  private Long id;

  @Enumerated(EnumType.STRING)
  private RuleDecision value;

  @ManyToOne
  @JoinColumn(name = "component_type_id")
  private ComponentTypeEntity componentType;

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ServiceEventEntity> serviceEvents = new HashSet<>();

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<HostEventEntity> hostEvents = new HashSet<>();

  /*@Singular
  @JsonIgnore
  @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ServiceDecisionEntity> componentDecisionLogs = new HashSet<>();*/

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DecisionEntity)) {
      return false;
    }
    DecisionEntity other = (DecisionEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

}
