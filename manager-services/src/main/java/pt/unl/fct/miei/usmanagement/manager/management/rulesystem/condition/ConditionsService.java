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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Conditions;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;
@Slf4j
@Service
public class ConditionsService {

	private final Conditions conditions;

	public ConditionsService(Conditions conditions) {
		this.conditions = conditions;
	}

	public List<Condition> getConditions() {
		return conditions.findAll();
	}

	public Condition getCondition(Long id) {
		return conditions.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Condition.class, "id", id.toString()));
	}

	public Condition getCondition(String conditionName) {
		return conditions.findByNameIgnoreCase(conditionName).orElseThrow(() ->
			new EntityNotFoundException(Condition.class, "conditionName", conditionName));
	}

	public Condition addCondition(Condition condition) {
		checkConditionDoesntExist(condition);
		log.info("Saving condition {}", ToStringBuilder.reflectionToString(condition));
		return conditions.save(condition);
	}

	public Condition updateCondition(String conditionName, Condition newCondition) {
		Condition condition = getCondition(conditionName);
		ObjectUtils.copyValidProperties(newCondition, condition);
		condition = conditions.save(condition);
		return condition;
	}

	public void deleteCondition(String conditionName) {
		Condition condition = getCondition(conditionName);
		conditions.delete(condition);
	}

	private void checkConditionDoesntExist(Condition condition) {
		String name = condition.getName();
		if (conditions.hasCondition(name)) {
			throw new DataIntegrityViolationException("Condition '" + name + "' already exists");
		}
	}

}
