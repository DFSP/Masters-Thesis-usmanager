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

package pt.unl.fct.miei.usmanagement.manager.database.fields;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.ServiceDecisionValueEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "fields")
public class FieldEntity {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Column(unique = true)
	private String name;

	@JsonIgnore
	@OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<ConditionEntity> conditions = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<HostSimulatedMetricEntity> simulatedHostMetrics = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<ServiceSimulatedMetricEntity> simulatedServiceMetrics = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<ServiceDecisionValueEntity> componentDecisionValueLogs = new HashSet<>();

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FieldEntity)) {
			return false;
		}
		FieldEntity other = (FieldEntity) o;
		return id != null && id.equals(other.getId());
	}

}
