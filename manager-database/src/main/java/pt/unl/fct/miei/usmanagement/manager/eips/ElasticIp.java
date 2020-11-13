package pt.unl.fct.miei.usmanagement.manager.ips;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "eips")
public class ElasticIp {

	@Id
	@GeneratedValue
	private Long id;

	private RegionEnum region;

	private String allocationId;

	private String publicIp;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ElasticIp)) {
			return false;
		}
		ElasticIp other = (ElasticIp) o;
		return id != null && id.equals(other.getId());
	}

}
