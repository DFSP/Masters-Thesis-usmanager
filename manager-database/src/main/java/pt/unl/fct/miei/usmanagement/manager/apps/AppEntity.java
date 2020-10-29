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

package pt.unl.fct.miei.usmanagement.manager.apps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "apps")
public class AppEntity {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Column(unique = true)
	private String name;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<AppServiceEntity> appServices;

	@Singular
	@JsonIgnore
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "app_rule",
		joinColumns = @JoinColumn(name = "app_id"),
		inverseJoinColumns = @JoinColumn(name = "rule_id")
	)
	private Set<AppRuleEntity> appRules;

	@Singular
	@JsonIgnore
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "app_simulated_metric",
		joinColumns = @JoinColumn(name = "app_id"),
		inverseJoinColumns = @JoinColumn(name = "simulated_metric_id")
	)
	private Set<AppSimulatedMetricEntity> simulatedAppMetrics;

	public void addRule(AppRuleEntity rule) {
		appRules.add(rule);
		rule.getApps().add(this);
	}

	public void removeRule(AppRuleEntity rule) {
		appRules.remove(rule);
		rule.getApps().remove(this);
	}

	public void addAppSimulatedMetric(AppSimulatedMetricEntity appMetric) {
		simulatedAppMetrics.add(appMetric);
		appMetric.getApps().add(this);
	}

	public void removeAppSimulatedMetric(AppSimulatedMetricEntity appMetric) {
		simulatedAppMetrics.remove(appMetric);
		appMetric.getApps().remove(this);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AppEntity)) {
			return false;
		}
		AppEntity other = (AppEntity) o;
		return id != null && id.equals(other.getId());
	}

}
