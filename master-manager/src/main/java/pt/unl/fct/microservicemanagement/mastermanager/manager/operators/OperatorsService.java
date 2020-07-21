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

package pt.unl.fct.microservicemanagement.mastermanager.manager.operators;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class OperatorsService {

  private final OperatorRepository operators;

  public OperatorsService(OperatorRepository operators) {
    this.operators = operators;
  }

  public List<OperatorEntity> getOperators() {
    return operators.findAll();
  }

  public OperatorEntity getOperator(Long id) {
    return operators.findById(id).orElseThrow(() ->
        new EntityNotFoundException(OperatorEntity.class, "id", id.toString()));
  }

  public OperatorEntity getOperator(String operatorName) {
    Operator operator = Operator.valueOf(operatorName.toUpperCase());
    return operators.findByOperator(operator).orElseThrow(() ->
        new EntityNotFoundException(OperatorEntity.class, "name", operatorName));
  }

  public OperatorEntity addOperator(OperatorEntity operator) {
    assertOperatorDoesntExist(operator);
    log.debug("Saving operator {}", ToStringBuilder.reflectionToString(operator));
    return operators.save(operator);
  }

  public OperatorEntity updateOperator(String operatorName, OperatorEntity newOperator) {
    var operator = getOperator(operatorName);
    log.debug("Updating operator {} with {}",
        ToStringBuilder.reflectionToString(operator), ToStringBuilder.reflectionToString(newOperator));
    log.debug("operator before copying properties: {}",
        ToStringBuilder.reflectionToString(operator));
    ObjectUtils.copyValidProperties(newOperator, operator);
    log.debug("operator after copying properties: {}",
        ToStringBuilder.reflectionToString(operator));
    operator = operators.save(operator);
    return operator;
  }

  public void deleteOperator(String operatorName) {
    var operator = getOperator(operatorName);
    operators.delete(operator);
  }

  private void assertOperatorDoesntExist(OperatorEntity operator) {
    var op = operator.getOperator();
    if (operators.hasOperator(op)) {
      throw new DataIntegrityViolationException("Operator '" + op + "' already exists");
    }
  }

}
