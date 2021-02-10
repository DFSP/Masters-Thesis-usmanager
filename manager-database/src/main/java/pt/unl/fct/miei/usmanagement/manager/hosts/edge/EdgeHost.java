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

package pt.unl.fct.miei.usmanagement.manager.hosts.edge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.services.PlaceEnum;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
@Table(name = "edge_hosts")
public class EdgeHost /*extends AbstractEntity<Long> */ {

	@Id
	@GenericGenerator(name = "IdGenerator", strategy = "pt.unl.fct.miei.usmanagement.manager.IdGenerator")
	@GeneratedValue(generator = "IdGenerator")
	private Long id;

	@NotNull
	private String username;

	@NotNull
	private String publicIpAddress;

	@NotNull
	private String privateIpAddress;

	@NaturalId
	private String publicDnsName;

	@NotNull
	private RegionEnum region;

	@NotNull
	private Coordinates coordinates;

	@JsonIgnore
	private Boolean local;

	@JsonIgnoreProperties({"edgeHost", "cloudHost"})
	@ManyToOne
	@JoinColumn(name = "managedByWorker_id")
	private WorkerManager managedByWorker;

	@Singular
	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "edge_host_rules",
		joinColumns = @JoinColumn(name = "edge_host_id"),
		inverseJoinColumns = @JoinColumn(name = "rule_id")
	)
	private Set<HostRule> hostRules;

	@Singular
	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "edge_host_simulated_metrics",
		joinColumns = @JoinColumn(name = "edge_host_id"),
		inverseJoinColumns = @JoinColumn(name = "simulated_metric_id")
	)
	private Set<HostSimulatedMetric> simulatedHostMetrics;

	@JsonIgnore
	public String getHostname() {
		return this.publicDnsName == null ? this.publicIpAddress : this.publicDnsName;
	}

	public void addRule(HostRule rule) {
		hostRules.add(rule);
		rule.getEdgeHosts().add(this);
	}

	public void removeRule(HostRule rule) {
		hostRules.remove(rule);
		rule.getEdgeHosts().remove(this);
	}

	public void addHostSimulatedMetric(HostSimulatedMetric hostMetric) {
		simulatedHostMetrics.add(hostMetric);
		hostMetric.getEdgeHosts().add(this);
	}

	public void removeHostSimulatedMetric(HostSimulatedMetric hostMetric) {
		simulatedHostMetrics.remove(hostMetric);
		hostMetric.getEdgeHosts().remove(this);
	}

	@JsonIgnore
	public HostAddress getAddress() {
		return new HostAddress(username, publicDnsName, publicIpAddress, privateIpAddress, coordinates, region, PlaceEnum.EDGE);
	}

	public void clearAssociations() {
		if (hostRules != null) {
			hostRules.clear();
		}
		if (simulatedHostMetrics != null) {
			simulatedHostMetrics.clear();
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
		if (!(o instanceof EdgeHost)) {
			return false;
		}
		EdgeHost other = (EdgeHost) o;
		return id != null && id.equals(other.getId());
	}

}
