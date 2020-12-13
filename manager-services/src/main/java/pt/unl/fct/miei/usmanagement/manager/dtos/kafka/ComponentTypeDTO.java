package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ComponentTypeDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ComponentTypeDTO {

	private Long id;
	private ComponentTypeEnum type;
	private Set<DecisionDTO> decisions;

	public ComponentTypeDTO(Long id) {
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
		if (!(o instanceof ComponentType)) {
			return false;
		}
		ComponentType other = (ComponentType) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ComponentTypeDTO{" +
			"id=" + id +
			", type=" + type +
			", decisions=" + (decisions == null ? "null" : decisions.stream().map(DecisionDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
