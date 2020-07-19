package pt.unl.fct.microservicemanagement.mastermanager.manager.fields;

import pt.unl.fct.microservicemanagement.mastermanager.util.Validation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fields")
public class FieldsController {

  private final FieldsService fieldsService;

  public FieldsController(FieldsService fieldsService) {
    this.fieldsService = fieldsService;
  }

  @GetMapping
  public List<FieldEntity> getFields() {
    return fieldsService.getFields();
  }

  @GetMapping("/{fieldName}")
  public FieldEntity getField(@PathVariable String fieldName) {
    return fieldsService.getField(fieldName);
  }

  @PostMapping
  public FieldEntity addField(@RequestBody FieldEntity field) {
    Validation.validatePostRequest(field.getId());
    return fieldsService.addField(field);
  }

  @PutMapping("/{fieldName}")
  public FieldEntity updateField(@PathVariable String fieldName, @RequestBody FieldEntity field) {
    Validation.validatePutRequest(field.getId());
    return fieldsService.updateField(fieldName, field);
  }

  @DeleteMapping("/{fieldName}")
  public void deleteField(@PathVariable String fieldName) {
    fieldsService.deleteField(fieldName);
  }

}
