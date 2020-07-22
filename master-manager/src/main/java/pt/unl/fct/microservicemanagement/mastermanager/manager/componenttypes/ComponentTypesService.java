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

package pt.unl.fct.microservicemanagement.mastermanager.manager.componenttypes;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ComponentTypesService {

  private final ComponentTypeRepository componentTypes;

  public ComponentTypesService(ComponentTypeRepository componentTypes) {
    this.componentTypes = componentTypes;
  }

  public List<ComponentTypeEntity> getComponentTypes() {
    return componentTypes.findAll();
  }

  public ComponentTypeEntity getComponentType(Long id) {
    return componentTypes.findById(id).orElseThrow(() ->
        new EntityNotFoundException(ComponentTypeEntity.class, "id", id.toString()));
  }

  public ComponentTypeEntity getComponentType(String type) {
    ComponentType componentType = ComponentType.valueOf(type.toUpperCase());
    return componentTypes.findByType(componentType).orElseThrow(() ->
        new EntityNotFoundException(ComponentTypeEntity.class, "type", type));
  }

  public ComponentTypeEntity addComponentType(ComponentTypeEntity componentType) {
    assertComponentTypeDoesntExist(componentType);
    log.debug("Saving componentType {}", ToStringBuilder.reflectionToString(componentType));
    return componentTypes.save(componentType);
  }

  public ComponentTypeEntity updateComponentType(String componentTypeName, ComponentTypeEntity newComponentType) {
    var componentType = getComponentType(componentTypeName);
    log.debug("Updating componentType {} with {}",
        ToStringBuilder.reflectionToString(componentType), ToStringBuilder.reflectionToString(newComponentType));
    log.debug("ComponentType before copying properties: {}",
        ToStringBuilder.reflectionToString(componentType));
    ObjectUtils.copyValidProperties(newComponentType, componentType);
    log.debug("ComponentType after copying properties: {}",
        ToStringBuilder.reflectionToString(componentType));
    componentType = componentTypes.save(componentType);
    return componentType;
  }

  public void deleteComponentType(String componentTypeName) {
    var componentType = getComponentType(componentTypeName);
    componentTypes.delete(componentType);
  }

  private void assertComponentTypeDoesntExist(ComponentTypeEntity componentType) {
    var componentTypeName = componentType.getType().name();
    if (componentTypes.hasComponentType(componentTypeName)) {
      throw new DataIntegrityViolationException("Component type '" + componentTypeName + "' already exists");
    }
  }

}