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

package pt.unl.fct.miei.usmanagement.manager.rulesystem.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.hibernate.annotations.GenericGenerator;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
public class HostRule {

	@Id
	@GenericGenerator(name = "IdGenerator", strategy = "pt.unl.fct.miei.usmanagement.manager.IdGenerator")
	@GeneratedValue(generator = "IdGenerator")
	private Long id;

	@NotNull
	@Column(unique = true)
	private String name;

	private int priority;

	@ManyToOne
	@JoinColumn(name = "decision_id")
	private Decision decision;

	private boolean generic;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "hostRule", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<HostRuleCondition> conditions;

	@Singular
	@JsonIgnore
	@ManyToMany(mappedBy = "hostRules", cascade = {CascadeType.MERGE})
	private Set<CloudHost> cloudHosts;

	@Singular
	@JsonIgnore
	@ManyToMany(mappedBy = "hostRules", cascade = {CascadeType.MERGE})
	private Set<EdgeHost> edgeHosts;

	public void removeAssociations() {
		Iterator<CloudHost> cloudHostsIterator = cloudHosts.iterator();
		while (cloudHostsIterator.hasNext()) {
			CloudHost cloudHost = cloudHostsIterator.next();
			cloudHostsIterator.remove();
			cloudHost.getHostRules().remove(this);
		}
		Iterator<EdgeHost> edgeHostsIterator = edgeHosts.iterator();
		while (edgeHostsIterator.hasNext()) {
			EdgeHost edgeHost = edgeHostsIterator.next();
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
		if (!(o instanceof HostRule)) {
			return false;
		}
		HostRule other = (HostRule) o;
		return id != null && id.equals(other.getId());
	}


}
