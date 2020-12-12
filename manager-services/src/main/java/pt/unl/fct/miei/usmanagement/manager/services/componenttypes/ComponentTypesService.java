/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.services.componenttypes;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypes;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ComponentTypesService {

	private final ComponentTypes componentTypes;
	private final KafkaService kafkaService;

	public ComponentTypesService(ComponentTypes componentTypes, KafkaService kafkaService) {
		this.componentTypes = componentTypes;
		this.kafkaService = kafkaService;
	}

	public List<ComponentType> getComponentTypes() {
		return componentTypes.findAll();
	}

	public List<ComponentType> getComponentTypesAndRelations() {
		return componentTypes.getComponentTypesAndRelations();
	}

	public ComponentType getComponentType(Long id) {
		return componentTypes.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ComponentType.class, "id", id.toString()));
	}

	public ComponentType getComponentType(String type) {
		ComponentTypeEnum componentType = ComponentTypeEnum.valueOf(type.toUpperCase());
		return getComponentType(componentType);
	}

	public ComponentType getComponentType(ComponentTypeEnum type) {
		return componentTypes.findByType(type).orElseThrow(() ->
			new EntityNotFoundException(ComponentTypeEnum.class, "type", type.name()));
	}

	public ComponentType addComponentType(ComponentType componentType) {
		checkComponentTypeDoesntExist(componentType);
		componentType = componentTypes.save(componentType);
		/*ComponentType kafkaComponentType = componentType;
		kafkaComponentType.setNew(true);
		kafkaService.sendComponentType(kafkaComponentType);*/
		kafkaService.sendComponentType(componentType);
		return componentType;
	}

	public ComponentType updateComponentType(String componentTypeName, ComponentType newComponentType) {
		ComponentType componentType = getComponentType(componentTypeName);
		log.info("Updating componentType {} with {}", ToStringBuilder.reflectionToString(componentType), ToStringBuilder.reflectionToString(newComponentType));
		EntityUtils.copyValidProperties(newComponentType, componentType);
		componentType = componentTypes.save(componentType);
		kafkaService.sendComponentType(componentType);
		return componentType;
	}

	public ComponentType addOrUpdateComponentType(ComponentType componentType) {
		if (componentType.getId() != null) {
			Optional<ComponentType> componentTypeOptional = componentTypes.findById(componentType.getId());
			if (componentTypeOptional.isPresent()) {
				ComponentType existingComponentType = componentTypeOptional.get();
				Set<pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision> decisions = componentType.getDecisions();
				if (decisions != null) {
					existingComponentType.getDecisions().retainAll(decisions);
					existingComponentType.getDecisions().addAll(decisions);
				}
				EntityUtils.copyValidProperties(componentType, existingComponentType);
				return saveComponentType(existingComponentType);
			}
		}
			return saveComponentType(componentType);
	}

	public ComponentType saveComponentType(ComponentType componentType) {
		log.info("Saving componentType {}", ToStringBuilder.reflectionToString(componentType));
		return componentTypes.save(componentType);
	}

	public void deleteComponentType(Long id) {
		componentTypes.deleteById(id);
	}

	public void deleteComponentType(String componentTypeName) {
		ComponentType componentType = getComponentType(componentTypeName);
		componentTypes.delete(componentType);
	}

	private void checkComponentTypeDoesntExist(ComponentType componentType) {
		String componentTypeName = componentType.getType().name();
		if (componentTypes.hasComponentType(componentTypeName)) {
			throw new DataIntegrityViolationException("Component type '" + componentTypeName + "' already exists");
		}
	}

}
