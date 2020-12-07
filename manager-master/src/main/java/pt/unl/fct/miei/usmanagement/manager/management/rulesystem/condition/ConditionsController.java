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

package pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.List;

@RestController
@RequestMapping("/rules/conditions")
public class ConditionsController {

	private final ConditionsService conditionsService;

	public ConditionsController(ConditionsService conditionsService) {
		this.conditionsService = conditionsService;
	}

	@GetMapping
	public List<Condition> getConditions() {
		return conditionsService.getConditions();
	}

	@GetMapping("/{conditionName}")
	public Condition getCondition(@PathVariable String conditionName) {
		return conditionsService.getCondition(conditionName);
	}

	@PostMapping
	public Condition addCondition(@RequestBody Condition condition) {
		Validation.validatePostRequest(condition.getId());
		return conditionsService.addCondition(condition);
	}

	@PutMapping("/{conditionName}")
	public Condition updateCondition(@PathVariable String conditionName, @RequestBody Condition condition) {
		Validation.validatePutRequest(condition.getId());
		return conditionsService.updateCondition(conditionName, condition);
	}

	@DeleteMapping("/{conditionName}")
	public void deleteCondition(@PathVariable String conditionName) {
		conditionsService.deleteCondition(conditionName);
	}

}