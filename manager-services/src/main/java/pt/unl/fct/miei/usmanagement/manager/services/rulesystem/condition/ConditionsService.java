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

package pt.unl.fct.miei.usmanagement.manager.services.rulesystem.condition;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.services.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.services.operators.OperatorsService;
import pt.unl.fct.miei.usmanagement.manager.services.valuemodes.ValueModesService;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Conditions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ConditionsService {

	private final ValueModesService valueModesService;
	private final FieldsService fieldsService;
	private final OperatorsService operatorsService;
	private final KafkaService kafkaService;

	private final Conditions conditions;

	public ConditionsService(ValueModesService valueModesService, FieldsService fieldsService,
							 OperatorsService operatorsService, Conditions conditions, KafkaService kafkaService) {
		this.valueModesService = valueModesService;
		this.fieldsService = fieldsService;
		this.operatorsService = operatorsService;
		this.conditions = conditions;
		this.kafkaService = kafkaService;
	}

	public List<Condition> getConditions() {
		return conditions.findAll();
	}

	public List<Condition> getConditionsAndRelations() {
		return conditions.getConditionsAndRelations();
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
		condition = saveCondition(condition);
		/*Condition kafkaCondition = condition;
		kafkaCondition.setNew(true);
		kafkaService.sendCondition(kafkaCondition);*/
		kafkaService.sendCondition(condition);
		return condition;
	}

	public Condition updateCondition(String conditionName, Condition newCondition) {
		Condition condition = getCondition(conditionName);
		log.info("Updating condition {} with {}", ToStringBuilder.reflectionToString(condition), ToStringBuilder.reflectionToString(newCondition));
		EntityUtils.copyValidProperties(newCondition, condition);
		condition.setField(fieldsService.getField(condition.getField().getName()));
		condition = conditions.save(condition);
		kafkaService.sendCondition(condition);
		return condition;
	}

	public Condition saveCondition(Condition condition) {
		log.info("Saving condition {}", ToStringBuilder.reflectionToString(condition));
		return conditions.save(condition);
	}

	public Condition addOrUpdateCondition(Condition condition) {
		if (condition.getId() != null) {
			Optional<Condition> optionalCondition = conditions.findById(condition.getId());
			if (optionalCondition.isPresent()) {
				Condition existingCondition = optionalCondition.get();
				Set<HostRuleCondition> hostConditions = condition.getHostConditions();
				if (hostConditions != null) {
					existingCondition.getHostConditions().retainAll(hostConditions);
					existingCondition.getHostConditions().addAll(hostConditions);
				}
				Set<AppRuleCondition> appConditions = condition.getAppConditions();
				if (appConditions != null) {
					existingCondition.getAppConditions().retainAll(appConditions);
					existingCondition.getAppConditions().addAll(appConditions);
				}
				Set<ServiceRuleCondition> serviceConditions = condition.getServiceConditions();
				if (serviceConditions != null) {
					existingCondition.getServiceConditions().retainAll(serviceConditions);
					existingCondition.getServiceConditions().addAll(serviceConditions);
				}
				Set<ContainerRuleCondition> containerConditions = condition.getContainerConditions();
				if (containerConditions != null) {
					existingCondition.getContainerConditions().retainAll(containerConditions);
					existingCondition.getContainerConditions().addAll(containerConditions);
				}
				EntityUtils.copyValidProperties(condition, existingCondition);
				return saveCondition(existingCondition);
			}
		}
		return saveCondition(condition);
	}

	public void deleteCondition(Long id) {
		conditions.deleteById(id);
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
