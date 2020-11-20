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

package pt.unl.fct.miei.usmanagement.manager.rulesystem.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

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
import java.util.Objects;
import java.util.Set;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "conditions")
public class Condition {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Column(unique = true)
	private String name;

	@ManyToOne
	@JoinColumn(name = "value_mode_id")
	private ValueMode valueMode;

	@ManyToOne
	@JoinColumn(name = "field_id")
	private Field field;

	@ManyToOne
	@JoinColumn(name = "operator_id")
	private Operator operator;

	private double value;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "hostCondition", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<HostRuleCondition> hostRuleConditions;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "serviceCondition", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ServiceRuleCondition> serviceConditions;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "containerCondition", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ContainerRuleCondition> containerConditions;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Condition)) {
			return false;
		}
		Condition other = (Condition) o;
		return id != null && id.equals(other.getId());
	}

}
