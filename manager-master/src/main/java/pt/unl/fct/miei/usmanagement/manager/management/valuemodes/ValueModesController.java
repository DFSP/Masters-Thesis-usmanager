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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.services.valuemodes.ValueModesService;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.List;

@RestController
@RequestMapping("/value-modes")
public class ValueModesController {

	private final ValueModesService valueModesService;

	public ValueModesController(ValueModesService valueModesService) {
		this.valueModesService = valueModesService;
	}

	@GetMapping
	public List<ValueMode> getValueModes() {
		return valueModesService.getValueModes();
	}

	@GetMapping("/{valueModeName}")
	public ValueMode getValueMode(@PathVariable String valueModeName) {
		return valueModesService.getValueMode(valueModeName);
	}

	@PostMapping
	public ValueMode addValueMode(@RequestBody ValueMode valueMode) {
		Validation.validatePostRequest(valueMode.getId());
		return valueModesService.addValueMode(valueMode);
	}

	@PutMapping("/{valueModeName}")
	public ValueMode updateValueMode(@PathVariable String valueModeName, @RequestBody ValueMode valueMode) {
		Validation.validatePutRequest(valueMode.getId());
		return valueModesService.updateValueMode(valueModeName, valueMode);
	}

	@DeleteMapping("/{valueModeName}")
	public void deleteValueMode(@PathVariable String valueModeName) {
		valueModesService.deleteValueMode(valueModeName);
	}

}