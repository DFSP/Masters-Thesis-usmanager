package pt.unl.fct.miei.usmanagement.manager.services.services;

import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencies;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyKey;

@Service
public class ServiceDependenciesService {

	private final ServiceDependencies serviceDependencies;

	public ServiceDependenciesService(ServiceDependencies serviceDependencies) {
		this.serviceDependencies = serviceDependencies;
	}

	public ServiceDependency addDependency(ServiceDependency serviceDependency) {
		return serviceDependencies.save(serviceDependency);
	}

	public ServiceDependency addDependency(pt.unl.fct.miei.usmanagement.manager.services.Service service,
										   pt.unl.fct.miei.usmanagement.manager.services.Service dependency) {
		ServiceDependency serviceDependency = ServiceDependency.builder()
			.id(new ServiceDependencyKey(service.getId(), dependency.getId()))
			.service(service)
			.dependency(dependency)
			.build();
		return addDependency(serviceDependency);
	}

	public boolean hasDependency(pt.unl.fct.miei.usmanagement.manager.services.Service service,
								 pt.unl.fct.miei.usmanagement.manager.services.Service dependency) {
		return hasDependency(service.getServiceName(), dependency.getServiceName());
	}

	public boolean hasDependency(String service, String dependency) {
		return serviceDependencies.hasDependency(service, dependency);
	}

	public boolean hasDependencies(String service) {
		return serviceDependencies.hasDependencies(service);
	}

	public boolean hasDependents(String service) {
		return serviceDependencies.hasDependents(service);
	}
}
