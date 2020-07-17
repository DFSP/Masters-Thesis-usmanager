package pt.unl.fct.microservicemanagement.mastermanager.manager.operators;

import pt.unl.fct.microservicemanagement.mastermanager.util.Validation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/operators")
public class OperatorsController {

  private final OperatorsService operatorsService;

  public OperatorsController(OperatorsService operatorsService) {
    this.operatorsService = operatorsService;
  }

  @GetMapping
  public List<OperatorEntity> getOperators() {
    return operatorsService.getOperators();
  }

  @GetMapping("/{operatorName}")
  public OperatorEntity getOperator(@PathVariable String operatorName) {
    return operatorsService.getOperator(operatorName);
  }

  @PostMapping
  public OperatorEntity addOperator(@RequestBody OperatorEntity operator) {
    Validation.validatePostRequest(operator.getId());
    return operatorsService.addOperator(operator);
  }

  @PutMapping("/{operatorName}")
  public OperatorEntity updateOperator(@PathVariable String operatorName, @RequestBody OperatorEntity operator) {
    Validation.validatePutRequest(operator.getId());
    return operatorsService.updateOperator(operatorName, operator);
  }

  @DeleteMapping("/{operatorName}")
  public void deleteOperator(@PathVariable String operatorName) {
    operatorsService.deleteOperator(operatorName);
  }

}
