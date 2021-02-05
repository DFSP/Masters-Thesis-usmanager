package pt.unl.fct.miei.usmanagement.manager.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;

import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = ServiceDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDTO {

	private String serviceName;
	private String dockerRepository;
	private Integer defaultExternalPort;
	private Integer defaultInternalPort;
	private String defaultDb;
	private String launchCommand;
	private Integer minimumReplicas;
	private Integer maximumReplicas;
	private String outputLabel;
	private ServiceTypeEnum serviceType;
	private String environment;
	private String volumes;
	private Double expectedMemoryConsumption;

	@Override
	public int hashCode() {
		return Objects.hashCode(getServiceName());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Service)) {
			return false;
		}
		Service other = (Service) o;
		return serviceName != null && serviceName.equals(other.getServiceName());
	}

	@Override
	public String toString() {
		return "ServiceDTO{" +
			"serviceName='" + serviceName + '\'' +
			", dockerRepository='" + dockerRepository + '\'' +
			", defaultExternalPort=" + defaultExternalPort +
			", defaultInternalPort=" + defaultInternalPort +
			", defaultDb='" + defaultDb + '\'' +
			", launchCommand='" + launchCommand + '\'' +
			", minimumReplicas=" + minimumReplicas +
			", maximumReplicas=" + maximumReplicas +
			", outputLabel='" + outputLabel + '\'' +
			", serviceType=" + serviceType +
			", environment=" + environment +
			", volumes=" + volumes +
			", expectedMemoryConsumption=" + expectedMemoryConsumption +
			'}';
	}

}
