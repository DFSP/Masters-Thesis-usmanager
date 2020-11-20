package pt.unl.fct.miei.usmanagement.manager.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.util.Map;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "nodes")
public class Node {

	@Id
	@NaturalId
	private String id;

	private String publicIpAddress;

	private NodeAvailability availability;

	private NodeRole role;

	private long version;

	private String state;

	private ManagerStatus managerStatus;

	private String managerId;

	@MapKeyColumn(name = "LABEL_KEY", length = 64)
	@Column(name = "LABEL_VALUE", length = 2048)
	@ElementCollection(fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private Map<String, String> labels;

	@JsonIgnore
	public String getPrivateIpAddress() {
		return labels.get(NodeConstants.Label.PRIVATE_IP_ADDRESS);
	}

	@JsonIgnore
	public String getUsername() {
		return labels.get(NodeConstants.Label.USERNAME);
	}

	@JsonIgnore
	public Coordinates getCoordinates() {
		return new Gson().fromJson(labels.get(NodeConstants.Label.COORDINATES), Coordinates.class);
	}

	@JsonIgnore
	public RegionEnum getRegion() {
		String region = labels.get(NodeConstants.Label.REGION);
		return region == null ? null : RegionEnum.getRegion(region);
	}

	@JsonIgnore
	public HostAddress getHostAddress() {
		final String username = getUsername();
		final String privateIpAddress = getPrivateIpAddress();
		final Coordinates coordinates = getCoordinates();
		final RegionEnum region = getRegion();
		return new HostAddress(username, publicIpAddress, privateIpAddress, coordinates, region);
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
		if (!(o instanceof Node)) {
			return false;
		}
		Node other = (Node) o;
		return id != null && id.equals(other.getId());
	}

}
