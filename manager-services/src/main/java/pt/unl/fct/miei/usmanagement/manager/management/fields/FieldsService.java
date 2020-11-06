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

package pt.unl.fct.miei.usmanagement.manager.management.fields;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.fields.Fields;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;

@Slf4j
@Service
public class FieldsService {

	private final Fields fields;

	public FieldsService(Fields fields) {
		this.fields = fields;
	}

	public List<Field> getFields() {
		return fields.findAll();
	}

	public Field getField(Long id) {
		return fields.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Field.class, "id", id.toString()));
	}

	public Field getField(String name) {
		return fields.findByNameIgnoreCase(name).orElseThrow(() ->
			new EntityNotFoundException(Field.class, "name", name));
	}

	public Field addField(Field field) {
		checkFieldDoesntExist(field);
		log.info("Saving field {}", ToStringBuilder.reflectionToString(field));
		return fields.save(field);
	}

	public Field updateField(String fieldName, Field newField) {
		Field field = getField(fieldName);
		log.info("Updating field {} with {}",
			ToStringBuilder.reflectionToString(field), ToStringBuilder.reflectionToString(newField));
		log.info("Field before copying properties: {}",
			ToStringBuilder.reflectionToString(field));
		ObjectUtils.copyValidProperties(newField, field);
		log.info("Field after copying properties: {}",
			ToStringBuilder.reflectionToString(field));
		field = fields.save(field);
		return field;
	}

	public void deleteField(String fieldName) {
		Field field = getField(fieldName);
		fields.delete(field);
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
