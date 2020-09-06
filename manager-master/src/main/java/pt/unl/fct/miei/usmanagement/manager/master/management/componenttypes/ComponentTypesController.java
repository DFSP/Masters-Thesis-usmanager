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

package pt.unl.fct.miei.usmanagement.manager.master.management.componenttypes;

import org.springframework.web.bind.annotation.*;
import pt.unl.fct.miei.usmanagement.manager.database.componenttypes.ComponentTypeEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Validation;

import java.util.List;

@RestController
@RequestMapping("/component-types")
public class ComponentTypesController {

	private final ComponentTypesService componentTypesService;

	public ComponentTypesController(ComponentTypesService componentTypesService) {
		this.componentTypesService = componentTypesService;
	}

	@GetMapping
	public List<ComponentTypeEntity> getComponentTypes() {
		return componentTypesService.getComponentTypes();
	}

	@GetMapping("/{componentTypeId}")
	public ComponentTypeEntity getComponentType(@PathVariable Long componentTypeId) {
		return componentTypesService.getComponentType(componentTypeId);
	}

	@GetMapping("/{componentTypeName}")
	public ComponentTypeEntity getComponentType(@PathVariable String componentTypeName) {
		return componentTypesService.getComponentType(componentTypeName);
	}

	@PostMapping
	public ComponentTypeEntity addComponentType(@RequestBody ComponentTypeEntity componentType) {
		Validation.validatePostRequest(componentType.getId());
		return componentTypesService.addComponentType(componentType);
	}

	@PutMapping("/{componentTypeName}")
	public ComponentTypeEntity updateComponentType(@PathVariable String componentTypeName, @RequestBody ComponentTypeEntity componentType) {
		Validation.validatePutRequest(componentType.getId());
		return componentTypesService.updateComponentType(componentTypeName, componentType);
	}

	@DeleteMapping("/{componentTypeName}")
	public void deleteComponentType(@PathVariable String componentTypeName) {
		componentTypesService.deleteComponentType(componentTypeName);
	}

}
