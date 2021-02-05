package pt.unl.fct.miei.usmanagement.manager.apps;

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
public class AppServiceKey implements Serializable {

	private static final long serialVersionUID = -3602299693295159087L;

	@Column(name = "app_id")
	private Long appId;

	@Column(name = "service_name")
	private String serviceName;

}

