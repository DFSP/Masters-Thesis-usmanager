package pt.unl.fct.miei.usmanagement.manager.management.monitoring;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public final class ContainerRecovery implements Serializable {

	private static final long serialVersionUID = -6022457816717698204L;

	private String containerId;
	private Long timestamp;

}
