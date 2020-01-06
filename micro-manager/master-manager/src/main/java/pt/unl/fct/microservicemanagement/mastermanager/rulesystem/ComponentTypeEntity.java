/*
 * MIT License
 *
 * Copyright (c) 2020 micro-manager
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

package pt.unl.fct.microservicemanagement.mastermanager.rulesystem;

import pt.unl.fct.microservicemanagement.mastermanager.rulesystem.decision.ComponentDecisionLog;
import pt.unl.fct.microservicemanagement.mastermanager.rulesystem.decision.DecisionEntity;
import pt.unl.fct.microservicemanagement.mastermanager.rulesystem.rule.RuleEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "component_type")
public class ComponentTypeEntity {

  @Id
  @GeneratedValue
  private long id;

  // Possible values:
  // - container; - host
  //TODO enum
  @Column(name = "component_type_name", unique = true)
  private String componentTypeName;

  @JsonIgnore
  @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RuleEntity> rules = new HashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DecisionEntity> decisions = new HashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ComponentDecisionLog> componentDecisionLogs = new HashSet<>();

}