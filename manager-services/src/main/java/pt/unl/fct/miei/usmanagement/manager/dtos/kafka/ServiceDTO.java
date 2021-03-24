package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
	private Set<String> environment;
	private Set<String> volumes;
	private Double expectedMemoryConsumption;
	private Set<AppServiceDTO> appServices;
	private Set<ServiceDependencyDTO> dependencies;
	private Set<ServiceDependencyDTO> dependents;
	private Set<ServiceEventPredictionDTO> eventPredictions;
	private Set<ServiceRuleDTO> serviceRules;
	private Set<ServiceSimulatedMetricDTO> simulatedServiceMetrics;

	public ServiceDTO(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getServiceName());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceDTO)) {
			return false;
		}
		ServiceDTO other = (ServiceDTO) o;
		return serviceName != null && serviceName.equals(other.getServiceName());
	}

	@Override
	public String toString() {
		return "ServiceDTO{" +
			/*"id=" + id +
			", */"serviceName='" + serviceName + '\'' +
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
			", appServices=" + (appServices == null ? "null" : appServices.stream().map(AppServiceDTO::toString).collect(Collectors.toSet())) +
			", dependencies=" + (dependencies == null ? "null" : dependencies.stream().map(ServiceDependencyDTO::toString).collect(Collectors.toSet())) +
			", dependents=" + (dependents == null ? "null" : dependents.stream().map(ServiceDependencyDTO::toString).collect(Collectors.toSet())) +
			", eventPredictions=" + (eventPredictions == null ? "null" : eventPredictions.stream().map(ServiceEventPredictionDTO::getId).collect(Collectors.toSet())) +
			", serviceRules=" + (serviceRules == null ? "null" : serviceRules.stream().map(ServiceRuleDTO::getId).collect(Collectors.toSet())) +
			", simulatedServiceMetrics=" + (simulatedServiceMetrics == null ? "null" : simulatedServiceMetrics.stream().map(ServiceSimulatedMetricDTO::getId).collect(Collectors.toSet())) +
			'}';
	}
}
