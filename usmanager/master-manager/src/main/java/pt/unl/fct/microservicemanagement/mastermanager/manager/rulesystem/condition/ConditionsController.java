/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.condition;

import pt.unl.fct.microservicemanagement.mastermanager.util.Validation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conditions")
public class ConditionsController {

  private final ConditionsService conditionsService;

  public ConditionsController(ConditionsService conditionsService) {
    this.conditionsService = conditionsService;
  }

  @GetMapping
  public Iterable<ConditionEntity> getConditions() {
    return conditionsService.getConditions();
  }

  @GetMapping("/{conditionId}")
  public ConditionEntity getCondition(@PathVariable Long conditionId) {
    return conditionsService.getCondition(conditionId);
  }

  @GetMapping("/{conditionName}")
  public ConditionEntity getCondition(@PathVariable String conditionName) {
    return conditionsService.getCondition(conditionName);
  }

  @PostMapping
  public ConditionEntity addCondition(@RequestBody ConditionEntity condition) {
    Validation.validatePostRequest(condition.getId());
    return conditionsService.addCondition(condition);
  }

  @PutMapping("/{conditionName}")
  public ConditionEntity updateCondition(@PathVariable String conditionName, @RequestBody ConditionEntity condition) {
    Validation.validatePutRequest(condition.getId());
    return conditionsService.updateCondition(conditionName, condition);
  }

  @DeleteMapping("/{conditionName}")
  public void deleteCondition(@PathVariable String conditionName) {
    conditionsService.deleteCondition(conditionName);
  }

}
