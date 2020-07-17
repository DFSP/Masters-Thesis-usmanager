package pt.unl.fct.microservicemanagement.mastermanager.manager.componenttypes;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.microservicemanagement.mastermanager.util.Validation;

import java.util.List;

@RestController
@RequestMapping("/component-types")
public class ComponentTypesController {

  private final ComponentTypesService componentTypesService;

  public ComponentTypesController(ComponentTypesService componentTypesService) {
    this.componentTypesService = componentTypesService;
  }

  @GetMapping
  public List<ComponentTypeEntity> getComponentTypes() {
    return componentTypesService.getComponentTypes();
  }

  @GetMapping("/{componentTypeId}")
  public ComponentTypeEntity getComponentType(@PathVariable Long componentTypeId) {
    return componentTypesService.getComponentType(componentTypeId);
  }

  @GetMapping("/{componentTypeName}")
  public ComponentTypeEntity getComponentType(@PathVariable String componentTypeName) {
    return componentTypesService.getComponentType(componentTypeName);
  }

  @PostMapping
  public ComponentTypeEntity addComponentType(@RequestBody ComponentTypeEntity componentType) {
    Validation.validatePostRequest(componentType.getId());
    return componentTypesService.addComponentType(componentType);
  }

  @PutMapping("/{componentTypeName}")
  public ComponentTypeEntity updateComponentType(@PathVariable String componentTypeName, @RequestBody ComponentTypeEntity componentType) {
    Validation.validatePutRequest(componentType.getId());
    return componentTypesService.updateComponentType(componentTypeName, componentType);
  }

  @DeleteMapping("/{componentTypeName}")
  public void deleteComponentType(@PathVariable String componentTypeName) {
    componentTypesService.deleteComponentType(componentTypeName);
  }

}
