package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencies;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.services.Service;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class ServiceDependencyDTO {

	private Long id;
	private ServiceDTO service;
	private ServiceDTO dependency;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public ServiceDependencyDTO(ServiceDependency serviceDependency) {
		this.id = serviceDependency.getId();
		this.service = new ServiceDTO(serviceDependency.getService());
		this.dependency = new ServiceDTO(serviceDependency.getDependency());
		/*this.isNew = serviceDependency.isNew();*/
	}

	public ServiceDependency toEntity() {
		ServiceDependency serviceDependency = ServiceDependency.builder()
			.id(id)
			.service(service.toEntity())
			.dependency(dependency.toEntity())
			.build();
		/*serviceDependency.setNew(isNew);*/
		return serviceDependency;
	}

}
