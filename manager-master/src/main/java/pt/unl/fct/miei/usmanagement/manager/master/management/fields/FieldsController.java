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

package pt.unl.fct.miei.usmanagement.manager.master.management.fields;

import org.springframework.web.bind.annotation.*;
import pt.unl.fct.miei.usmanagement.manager.database.fields.FieldEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;

import java.util.List;

@RestController
@RequestMapping("/fields")
public class FieldsController {

	private final FieldsService fieldsService;

	public FieldsController(FieldsService fieldsService) {
		this.fieldsService = fieldsService;
	}

	@GetMapping
	public List<FieldEntity> getFields() {
		return fieldsService.getFields();
	}

	@GetMapping("/{fieldName}")
	public FieldEntity getField(@PathVariable String fieldName) {
		return fieldsService.getField(fieldName);
	}

	@PostMapping
	public FieldEntity addField(@RequestBody FieldEntity field) {
		Validation.validatePostRequest(field.getId());
		return fieldsService.addField(field);
	}

	@PutMapping("/{fieldName}")
	public FieldEntity updateField(@PathVariable String fieldName, @RequestBody FieldEntity field) {
		Validation.validatePutRequest(field.getId());
		return fieldsService.updateField(fieldName, field);
	}

	@DeleteMapping("/{fieldName}")
	public void deleteField(@PathVariable String fieldName) {
		fieldsService.deleteField(fieldName);
	}

}
