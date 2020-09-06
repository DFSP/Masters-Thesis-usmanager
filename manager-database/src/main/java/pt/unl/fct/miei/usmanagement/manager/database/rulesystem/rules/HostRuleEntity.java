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

package pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision.DecisionEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "host_rules")
public class HostRuleEntity {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Column(unique = true)
	private String name;

	private int priority;

	@ManyToOne
	@JoinColumn(name = "decision_id")
	private DecisionEntity decision;

	private boolean generic;

	@Singular
	@JsonIgnore
	@ManyToMany(mappedBy = "hostRules", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private Set<CloudHostEntity> cloudHosts;

	@Singular
	@JsonIgnore
	@ManyToMany(mappedBy = "hostRules", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private Set<EdgeHostEntity> edgeHosts;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "hostRule", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<HostRuleConditionEntity> conditions = new HashSet<>();

	public void removeAssociations() {
		Iterator<CloudHostEntity> cloudHostsIterator = cloudHosts.iterator();
		while (cloudHostsIterator.hasNext()) {
			CloudHostEntity cloudHost = cloudHostsIterator.next();
			cloudHostsIterator.remove();
			cloudHost.getHostRules().remove(this);
		}
		Iterator<EdgeHostEntity> edgeHostsIterator = edgeHosts.iterator();
		while (edgeHostsIterator.hasNext()) {
			EdgeHostEntity edgeHost = edgeHostsIterator.next();
			edgeHostsIterator.remove();
			edgeHost.getHostRules().remove(this);
		}
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
		if (!(o instanceof HostRuleEntity)) {
			return false;
		}
		HostRuleEntity other = (HostRuleEntity) o;
		return id != null && id.equals(other.getId());
	}

}
