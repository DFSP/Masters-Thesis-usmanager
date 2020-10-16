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

package pt.unl.fct.miei.usmanagement.manager.services.management.rulesystem.condition;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.condition.ConditionRepository;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.util.ObjectUtils;

import java.util.List;

@Slf4j
@Service
public class ConditionsService {

	private final ConditionRepository conditions;

	public ConditionsService(ConditionRepository conditions) {
		this.conditions = conditions;
	}

	public List<ConditionEntity> getConditions() {
		return conditions.findAll();
	}

	public ConditionEntity getCondition(Long id) {
		return conditions.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ConditionEntity.class, "id", id.toString()));
	}

	public ConditionEntity getCondition(String conditionName) {
		return conditions.findByNameIgnoreCase(conditionName).orElseThrow(() ->
			new EntityNotFoundException(ConditionEntity.class, "conditionName", conditionName));
	}

	public ConditionEntity addCondition(ConditionEntity condition) {
		checkConditionDoesntExist(condition);
		log.info("Saving condition {}", ToStringBuilder.reflectionToString(condition));
		return conditions.save(condition);
	}

	public ConditionEntity updateCondition(String conditionName, ConditionEntity newCondition) {
		ConditionEntity condition = getCondition(conditionName);
		ObjectUtils.copyValidProperties(newCondition, condition);
		condition = conditions.save(condition);
		return condition;
	}

	public void deleteCondition(String conditionName) {
		ConditionEntity condition = getCondition(conditionName);
		conditions.delete(condition);
	}

	private void checkConditionDoesntExist(ConditionEntity condition) {
		String name = condition.getName();
		if (conditions.hasCondition(name)) {
			throw new DataIntegrityViolationException("Condition '" + name + "' already exists");
		}
	}

}
