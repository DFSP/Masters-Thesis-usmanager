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

package pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud;

import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.HostLocation;
import pt.unl.fct.miei.usmanagement.manager.database.monitoring.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.database.workermanagers.WorkerManagerEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "cloud_hosts")
public class CloudHostEntity {

	@Id
	@GeneratedValue
	private Long id;

	@NaturalId
	private String instanceId;

	@NotNull
	private String instanceType;

	@NotNull
	private InstanceState state;

	@NotNull
	private String imageId;

	private String publicDnsName;

	private String publicIpAddress;

	private String privateIpAddress;

	private Placement placement;

	@JsonIgnoreProperties({"edgeHost", "cloudHost"})
	@ManyToOne
	private WorkerManagerEntity managedByWorker;

	@Singular
	@JsonIgnore
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "cloud_host_rule",
		joinColumns = @JoinColumn(name = "cloud_host_id"),
		inverseJoinColumns = @JoinColumn(name = "rule_id")
	)
	private Set<HostRuleEntity> hostRules;

	@Singular
	@JsonIgnore
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "cloud_host_simulated_metric",
		joinColumns = @JoinColumn(name = "cloud_host_id"),
		inverseJoinColumns = @JoinColumn(name = "simulated_metric_id")
	)
	private Set<HostSimulatedMetricEntity> simulatedHostMetrics;

	public void addRule(HostRuleEntity rule) {
		hostRules.add(rule);
		rule.getCloudHosts().add(this);
	}

	public void removeRule(HostRuleEntity rule) {
		hostRules.remove(rule);
		rule.getCloudHosts().remove(this);
	}

	public void addHostSimulatedMetric(HostSimulatedMetricEntity hostMetric) {
		simulatedHostMetrics.add(hostMetric);
		hostMetric.getCloudHosts().add(this);
	}

	public void removeHostSimulatedMetric(HostSimulatedMetricEntity hostMetric) {
		simulatedHostMetrics.remove(hostMetric);
		hostMetric.getCloudHosts().remove(this);
	}

	@JsonIgnore
	public HostAddress getAddress() {
		return new HostAddress("ubuntu", publicDnsName, publicIpAddress, privateIpAddress);
	}

	@JsonIgnore
	public HostLocation getLocation() {
		return new HostLocation("", "", getRegion(), getContinent());
	}

	@JsonIgnore
	public String getRegion() {
		String zone = placement.getAvailabilityZone();
		return Character.isDigit(zone.charAt(zone.length() - 1)) ? zone : zone.substring(0, zone.length() - 1);
	}

	@JsonIgnore
	public String getContinent() {
		String region = getRegion();
		String zone = region.substring(0, region.indexOf('-'));
		if (zone.startsWith("us") || zone.startsWith("ca")) {
			return "na";
		}
		if (region.startsWith("sa") || region.startsWith("eu")) {
			return region;
		}
		if (region.contains("ap-southeast-1")) {
			return "oc";
		}
		if (region.startsWith("ap")) {
			return "as";
		}
		return "";
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
		if (!(o instanceof CloudHostEntity)) {
			return false;
		}
		CloudHostEntity other = (CloudHostEntity) o;
		return id != null && id.equals(other.getId());
	}

}
