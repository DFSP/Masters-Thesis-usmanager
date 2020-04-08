/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package pt.unl.fct.microservicemanagement.mastermanager.manager.rule_system.condition;

import pt.unl.fct.microservicemanagement.mastermanager.manager.fields.FieldEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.operators.OperatorEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rule_system.rules.hosts.HostRuleConditionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.rule_system.rules.services.ServiceRuleConditionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.manager.value_modes.ValueModeEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Table(name = "conditions")
public class ConditionEntity {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  @Column(unique = true)
  private String name;

  @ManyToOne
  @JoinColumn(name = "value_mode_id")
  private ValueModeEntity valueMode;

  @ManyToOne
  @JoinColumn(name = "field_id")
  private FieldEntity field;

  @ManyToOne
  @JoinColumn(name = "operator_id")
  private OperatorEntity operator;

  private double value;

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "hostCondition", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<HostRuleConditionEntity> hostRuleConditions = new HashSet<>();

  @Singular
  @JsonIgnore
  @OneToMany(mappedBy = "serviceCondition", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ServiceRuleConditionEntity> serviceConditions = new HashSet<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConditionEntity)) {
      return false;
    }
    ConditionEntity other = (ConditionEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return 31;
  }

}
