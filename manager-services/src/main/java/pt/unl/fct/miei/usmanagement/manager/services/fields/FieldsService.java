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

package pt.unl.fct.miei.usmanagement.manager.services.fields;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.fields.Fields;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.services.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.util.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class FieldsService {

	private final Fields fields;
	private final KafkaService kafkaService;

	public FieldsService(Fields fields, KafkaService kafkaService) {
		this.fields = fields;
		this.kafkaService = kafkaService;
	}

	@Transactional(readOnly = true)
	public List<Field> getFields() {
		return fields.findAll();
	}

	public Field getField(Long id) {
		return fields.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Field.class, "id", id.toString()));
	}

	public Field getField(String name) {
		return fields.findByName(name).orElseThrow(() ->
			new EntityNotFoundException(Field.class, "name", name));
	}

	public Field addField(Field field) {
		checkFieldDoesntExist(field);
		log.info("Saving field {}", ToStringBuilder.reflectionToString(field));
		field = fields.save(field);
		/*Field kafkaField = field;
		kafkaField.setNew(true);
		kafkaService.sendField(kafkaField);*/
		kafkaService.sendField(field);
		return field;
	}

	public Field updateField(String fieldName, Field newField) {
		Field field = getField(fieldName);
		log.info("Updating field {} with {}", ToStringBuilder.reflectionToString(field), ToStringBuilder.reflectionToString(newField));
		EntityUtils.copyValidProperties(newField, field);
		field = saveField(field);
		kafkaService.sendField(field);
		return field;
	}

	public Field saveField(Field field) {
		log.info("Saving field {}", ToStringBuilder.reflectionToString(field));
		return fields.save(field);
	}

	public Field addIfNotPresent(Field field) {
		Optional<Field> fieldOptional = fields.findById(field.getId());
		return fieldOptional.orElseGet(() -> {
			field.clearAssociations();
			return saveField(field);
		});
	}

	public Field addOrUpdateField(Field field) {
		if (field.getId() != null) {
			Optional<Field> fieldOptional = fields.findById(field.getId());
			if (fieldOptional.isPresent()) {
				Field existingField = fieldOptional.get();
				Set<pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition> conditions = field.getConditions();
				if (conditions != null) {
					existingField.getConditions().retainAll(conditions);
					existingField.getConditions().addAll(conditions);
				}
				Set<HostSimulatedMetric> simulatedHostMetrics = field.getSimulatedHostMetrics();
				if (simulatedHostMetrics != null) {
					existingField.getSimulatedHostMetrics().retainAll(simulatedHostMetrics);
					existingField.getSimulatedHostMetrics().addAll(simulatedHostMetrics);
				}
				Set<ServiceSimulatedMetric> simulatedServiceMetrics = field.getSimulatedServiceMetrics();
				if (simulatedServiceMetrics != null) {
					existingField.getSimulatedServiceMetrics().retainAll(simulatedServiceMetrics);
					existingField.getSimulatedServiceMetrics().addAll(simulatedServiceMetrics);
				}
				EntityUtils.copyValidProperties(field, existingField);
				return saveField(existingField);
			}
		}
		return saveField(field);
	}

	public void deleteField(Long id) {
		fields.deleteById(id);
	}

	public void deleteField(String fieldName) {
		Field field = getField(fieldName);
		fields.delete(field);
		kafkaService.sendDeleteField(field);
	}

	public boolean hasField(String fieldName) {
		return fields.hasField(fieldName);
	}

	private void checkFieldDoesntExist(Field field) {
		String fieldName = field.getName();
		if (fields.hasField(fieldName)) {
			throw new DataIntegrityViolationException("Field " + fieldName + " already exists");
		}
	}
}
