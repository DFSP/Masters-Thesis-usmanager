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

package pt.unl.fct.miei.usmanagement.manager.management.valuemodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueModes;

import java.util.List;

@Slf4j
@Service
public class ValueModesService {

	private final ValueModes valueModes;

	public ValueModesService(ValueModes valueModes) {
		this.valueModes = valueModes;
	}

	public List<ValueMode> getValueModes() {
		return valueModes.findAll();
	}

	public ValueMode getValueMode(Long id) {
		return valueModes.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ValueMode.class, "id", id.toString()));
	}

	public ValueMode getValueMode(String valueModeName) {
		return valueModes.findByNameIgnoreCase(valueModeName).orElseThrow(() ->
			new EntityNotFoundException(ValueMode.class, "name", valueModeName));
	}

	public ValueMode addValueMode(ValueMode valueMode) {
		checkValueModeDoesntExist(valueMode);
		log.info("Saving valueMode {}", ToStringBuilder.reflectionToString(valueMode));
		return valueModes.save(valueMode);
	}

	public ValueMode updateValueMode(String valueModeName, ValueMode newValueMode) {
		ValueMode valueMode = getValueMode(valueModeName);
		log.info("Updating valueMode {} with {}",
			ToStringBuilder.reflectionToString(valueMode), ToStringBuilder.reflectionToString(newValueMode));
		log.info("valueMode before copying properties: {}",
			ToStringBuilder.reflectionToString(valueMode));
		ObjectUtils.copyValidProperties(newValueMode, valueMode);
		log.info("valueMode after copying properties: {}",
			ToStringBuilder.reflectionToString(valueMode));
		valueMode = valueModes.save(valueMode);
		return valueMode;
	}

	public void deleteValueMode(String valueModeName) {
		ValueMode valueMode = getValueMode(valueModeName);
		valueModes.delete(valueMode);
	}

	private void checkValueModeDoesntExist(ValueMode valueMode) {
		String valueModeName = valueMode.getName();
		if (valueModes.hasValueMode(valueModeName)) {
			throw new DataIntegrityViolationException("Value mode '" + valueModeName + "' already exists");
		}
	}

}
