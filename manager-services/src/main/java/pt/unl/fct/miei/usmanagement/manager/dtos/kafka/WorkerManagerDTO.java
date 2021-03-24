package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = WorkerManagerDTO.class)
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
		if (!(o instanceof WorkerManagerDTO)) {
			return false;
		}
		WorkerManagerDTO other = (WorkerManagerDTO) o;
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
