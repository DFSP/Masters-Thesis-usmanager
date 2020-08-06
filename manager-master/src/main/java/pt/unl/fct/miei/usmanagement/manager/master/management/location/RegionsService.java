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

package pt.unl.fct.miei.usmanagement.manager.master.management.location;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionRepository;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.master.util.ObjectUtils;

@Slf4j
@Service
public class RegionsService {

  private final RegionRepository regions;

  public RegionsService(RegionRepository regions) {
    this.regions = regions;
  }

  public List<RegionEntity> getRegions() {
    return regions.findAll();
  }

  public RegionEntity getRegion(Long id) {
    return regions.findById(id).orElseThrow(() ->
        new EntityNotFoundException(RegionEntity.class, "id", id.toString()));
  }

  public RegionEntity getRegion(String name) {
    return regions.findByNameIgnoreCase(name).orElseThrow(() ->
        new EntityNotFoundException(RegionEntity.class, "name", name));
  }

  public RegionEntity addRegion(RegionEntity region) {
    assertRegionDoesntExist(region);
    log.debug("Saving region {}", ToStringBuilder.reflectionToString(region));
    return regions.save(region);
  }

  public RegionEntity updateRegion(String name, RegionEntity newRegion) {
    var region = getRegion(name);
    log.debug("Updating region {} with {}",
        ToStringBuilder.reflectionToString(region), ToStringBuilder.reflectionToString(newRegion));
    log.debug("Region before copying properties: {}",
        ToStringBuilder.reflectionToString(region));
    ObjectUtils.copyValidProperties(newRegion, region);
    log.debug("Region after copying properties: {}",
        ToStringBuilder.reflectionToString(region));
    return regions.save(region);
  }

  public void deleteRegion(String name) {
    var region = getRegion(name);
    regions.delete(region);
  }

  private void assertRegionDoesntExist(RegionEntity region) {
    var name = region.getName();
    if (regions.hasRegion(name)) {
      throw new DataIntegrityViolationException("Region '" + name + "' already exists");
    }
  }

}
