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

package pt.unl.fct.miei.usmanagement.manager.management.componenttypes;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.util.Validation;

import java.util.List;

@RestController
@RequestMapping("/component-types")
public class ComponentTypesController {

	private final ComponentTypesService componentTypesService;

	public ComponentTypesController(ComponentTypesService componentTypesService) {
		this.componentTypesService = componentTypesService;
	}

	@GetMapping
	public List<ComponentType> getComponentTypes() {
		return componentTypesService.getComponentTypes();
	}

	@GetMapping("/{componentTypeId}")
	public ComponentType getComponentType(@PathVariable Long componentTypeId) {
		return componentTypesService.getComponentType(componentTypeId);
	}

	@GetMapping("/{componentTypeName}")
	public ComponentType getComponentType(@PathVariable String componentTypeName) {
		return componentTypesService.getComponentType(componentTypeName);
	}

	@PostMapping
	public ComponentType addComponentType(@RequestBody ComponentType componentType) {
		Validation.validatePostRequest(componentType.getId());
		return componentTypesService.addComponentType(componentType);
	}

	@PutMapping("/{componentTypeName}")
	public ComponentType updateComponentType(@PathVariable String componentTypeName, @RequestBody ComponentType componentType) {
		Validation.validatePutRequest(componentType.getId());
		return componentTypesService.updateComponentType(componentTypeName, componentType);
	}

	@DeleteMapping("/{componentTypeName}")
	public void deleteComponentType(@PathVariable String componentTypeName) {
		componentTypesService.deleteComponentType(componentTypeName);
	}

}
