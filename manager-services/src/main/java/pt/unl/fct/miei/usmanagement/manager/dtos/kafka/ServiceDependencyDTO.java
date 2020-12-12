package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ServiceDependencyDTO {

	private Long id;
	@JsonManagedReference
	private ServiceDTO service;
	@JsonManagedReference
	private ServiceDTO dependency;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	/*public ServiceDependencyDTO(ServiceDependency serviceDependency) {
		this.id = serviceDependency.getId();
		this.service = new ServiceDTO(serviceDependency.getService());
		this.dependency = new ServiceDTO(serviceDependency.getDependency());
		*//*this.isNew = serviceDependency.isNew();*//*
	}

	public ServiceDependency toEntity() {
		ServiceDependency serviceDependency = ServiceDependency.builder()
			.id(id)
			.service(service.toEntity())
			.dependency(dependency.toEntity())
			.build();
		*//*serviceDependency.setNew(isNew);*//*
		return serviceDependency;
	}*/

}
