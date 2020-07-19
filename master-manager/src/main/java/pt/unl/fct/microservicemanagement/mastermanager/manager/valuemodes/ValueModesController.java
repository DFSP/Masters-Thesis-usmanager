package pt.unl.fct.microservicemanagement.mastermanager.manager.valuemodes;

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
@RequestMapping("/value-modes")
public class ValueModesController {

  private final ValueModesService valueModesService;

  public ValueModesController(ValueModesService valueModesService) {
    this.valueModesService = valueModesService;
  }

  @GetMapping
  public List<ValueModeEntity> getValueModes() {
    return valueModesService.getValueModes();
  }

  @GetMapping("/{valueModeName}")
  public ValueModeEntity getValueMode(@PathVariable String valueModeName) {
    return valueModesService.getValueMode(valueModeName);
  }

  @PostMapping
  public ValueModeEntity addValueMode(@RequestBody ValueModeEntity valueMode) {
    Validation.validatePostRequest(valueMode.getId());
    return valueModesService.addValueMode(valueMode);
  }

  @PutMapping("/{valueModeName}")
  public ValueModeEntity updateValueMode(@PathVariable String valueModeName, @RequestBody ValueModeEntity valueMode) {
    Validation.validatePutRequest(valueMode.getId());
    return valueModesService.updateValueMode(valueModeName, valueMode);
  }

  @DeleteMapping("/{valueModeName}")
  public void deleteValueMode(@PathVariable String valueModeName) {
    valueModesService.deleteValueMode(valueModeName);
  }

}
