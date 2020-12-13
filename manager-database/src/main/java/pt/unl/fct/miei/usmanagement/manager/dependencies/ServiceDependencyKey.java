package pt.unl.fct.miei.usmanagement.manager.dependencies;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
public class ServiceDependencyKey implements Serializable {

	private static final long serialVersionUID = 4040413050712579178L;

	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "service_id")
	private Long dependencyId;

}
