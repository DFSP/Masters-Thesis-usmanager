package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import java.util.IdentityHashMap;
import java.util.Map;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

@SuppressWarnings("unused")
public class CycleAvoidingMappingContext {

	private final Map<Object, Object> knownInstances;

	public CycleAvoidingMappingContext() {
		knownInstances = new IdentityHashMap<>();
	}

	@BeforeMapping
	public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
		return (T) knownInstances.get(source);
	}

	@BeforeMapping
	public void storeMappedInstance(Object source, @MappingTarget Object target) {
		knownInstances.put(source, target);
	}

}
