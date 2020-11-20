package pt.unl.fct.miei.usmanagement.manager.loadbalancers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "load_balancers")
public class LoadBalancer {

	@Id
	private String id;

	@NotNull
	@OneToOne
	private Container container;

	@NotNull
	private RegionEnum region;

	@PrePersist
	private void prePersist() {
		if (this.getId() == null) {
			this.setId(UUID.randomUUID().toString());
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
		if (!(o instanceof LoadBalancer)) {
			return false;
		}
		LoadBalancer other = (LoadBalancer) o;
		return id != null && id.equals(other.getId());
	}

}
