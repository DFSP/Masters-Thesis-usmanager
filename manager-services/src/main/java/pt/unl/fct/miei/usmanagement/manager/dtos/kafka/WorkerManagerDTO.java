package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.HashSet;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = WorkerManagerDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkerManagerDTO {

	private String id;
	private ContainerDTO container;
	private RegionEnum region;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof WorkerManager)) {
			return false;
		}
		WorkerManager other = (WorkerManager) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "WorkerManagerDTO{" +
			"id='" + id + '\'' +
			", container=" + container +
			", region=" + region +
			'}';
	}
}
