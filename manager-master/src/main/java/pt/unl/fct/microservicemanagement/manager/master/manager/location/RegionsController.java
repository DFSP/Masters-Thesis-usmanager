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

package pt.unl.fct.microservicemanagement.manager.master.manager.location;

import pt.unl.fct.microservicemanagement.manager.master.util.Validation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/regions")
public class RegionsController {

  private final RegionsService regionsService;

  public RegionsController(RegionsService regionsService) {
    this.regionsService = regionsService;
  }

  @GetMapping
  public List<RegionEntity> getRegions() {
    return regionsService.getRegions();
  }

  @GetMapping("/{regionName}")
  public RegionEntity getRegion(@PathVariable String regionName) {
    return regionsService.getRegion(regionName);
  }

  @PostMapping
  public RegionEntity addRegion(@RequestBody RegionEntity region) {
    Validation.validatePostRequest(region.getId());
    return regionsService.addRegion(region);
  }

  @PutMapping(value = "/{name}")
  public RegionEntity updateRegion(@PathVariable String name, @RequestBody RegionEntity region) {
    Validation.validatePutRequest(region.getId());
    return regionsService.updateRegion(name, region);
  }

  @DeleteMapping("/{name}")
  public void deleteRegion(@PathVariable String name) {
    regionsService.deleteRegion(name);
  }

}
