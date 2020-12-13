package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = HostEventDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HostEventDTO {

	private Long id;
	private String publicIpAddress;
	private String privateIpAddress;
	private String managerPublicIpAddress;
	private String managerPrivateIpAddress;
	private DecisionDTO decision;
	private int count;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HostEvent)) {
			return false;
		}
		HostEvent other = (HostEvent) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "HostEventDTO{" +
			"id=" + id +
			", publicIpAddress='" + publicIpAddress + '\'' +
			", privateIpAddress='" + privateIpAddress + '\'' +
			", managerPublicIpAddress='" + managerPublicIpAddress + '\'' +
			", managerPrivateIpAddress='" + managerPrivateIpAddress + '\'' +
			", decision=" + decision +
			", count=" + count +
			'}';
	}
}
