package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ElasticIpDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ElasticIpDTO {

	private Long id;
	private RegionEnum region;
	private String allocationId;
	private String publicIp;
	private String associationId;
	private String instanceId;

	public ElasticIpDTO(Long id) {
		this.id = id;
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
		if (!(o instanceof ElasticIp)) {
			return false;
		}
		ElasticIp other = (ElasticIp) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ElasticIpDTO{" +
			"id=" + id +
			", region=" + region +
			", allocationId='" + allocationId + '\'' +
			", publicIp='" + publicIp + '\'' +
			", associationId='" + associationId + '\'' +
			", instanceId='" + instanceId + '\'' +
			'}';
	}
}
