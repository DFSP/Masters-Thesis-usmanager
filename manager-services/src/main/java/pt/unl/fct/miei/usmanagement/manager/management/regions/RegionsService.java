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

package pt.unl.fct.miei.usmanagement.manager.management.regions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

@Slf4j
@Service
public class RegionsService {

	/*private final RegionRepository regions;

	public RegionsService(RegionRepository regions) {
		this.regions = regions;
	}

	public List<Region> getRegions() {
		return regions.findAll();
	}

	public Region getRegion(Long id) {
		return regions.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Region.class, "id", id.toString()));
	}

	public Region getRegion(String name) {
		Region region = Region.valueOf(name.toUpperCase().replace(" ", "_"));
		return regions.findByRegion(name).orElseThrow(() ->
			new EntityNotFoundException(Region.class, "name", name));
	}

	public Region getClosestRegion(Coordinates coordinates) {
		List<Region> regions = getRegions();
		if (regions.size() < 1) {
			throw new EntityNotFoundException(Region.class);
		}
		regions.sort((oneRegion, anotherRegion) -> {
			double oneDistance = oneRegion.getRegion().getCoordinates().distanceTo(coordinates);
			double anotherDistance = anotherRegion.getRegion().getCoordinates().distanceTo(coordinates);
			return Double.compare(oneDistance, anotherDistance);
		});
		return regions.get(0);
	}

	public Region addRegion(Region region) {
		checkRegionDoesntExist(region);
		log.info("Saving region {}", ToStringBuilder.reflectionToString(region));
		return regions.save(region);
	}

	public Region updateRegion(String name, Region newRegion) {
		Region region = getRegion(name);
		log.info("Updating region {} with {}", ToStringBuilder.reflectionToString(region), ToStringBuilder.reflectionToString(newRegion));
		ObjectUtils.copyValidProperties(newRegion, region);
		return regions.save(region);
	}

	public void deleteRegion(String name) {
		Region region = getRegion(name);
		regions.delete(region);
	}

	public boolean hasRegion(Region region) {
		return regions.hasRegion(region);
	}

	private void checkRegionDoesntExist(Region region) {
		if (regions.hasRegion(region.getRegion())) {
			throw new DataIntegrityViolationException("Region " + region.getRegion().getName() + " already exists");
		}
	}*/

	public AwsRegion mapToAwsRegion(RegionEnum region) {
		for (AwsRegion awsRegion : AwsRegion.getAwsRegions()) {
			if (region == awsRegion.getRegion()) {
				return awsRegion;
			}
		}
		return null;
	}

}
