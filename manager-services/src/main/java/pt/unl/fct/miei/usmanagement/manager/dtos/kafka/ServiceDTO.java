package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServiceDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDTO {

	private Long id;
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

	public ServiceDTO(Long id) {
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
		if (!(o instanceof Service)) {
			return false;
		}
		Service other = (Service) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceDTO{" +
			"id=" + id +
			", serviceName='" + serviceName + '\'' +
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
			", dependencies=" + (dependencies == null ? "null" : dependencies.stream().map(ServiceDependencyDTO::getId).collect(Collectors.toSet())) +
			", dependents=" + (dependents == null ? "null" : dependents.stream().map(ServiceDependencyDTO::getId).collect(Collectors.toSet())) +
			", eventPredictions=" + (eventPredictions == null ? "null" : eventPredictions.stream().map(ServiceEventPredictionDTO::getId).collect(Collectors.toSet())) +
			", serviceRules=" + (serviceRules == null ? "null" : serviceRules.stream().map(ServiceRuleDTO::getId).collect(Collectors.toSet())) +
			", simulatedServiceMetrics=" + (simulatedServiceMetrics) +
			'}';
	}
}
