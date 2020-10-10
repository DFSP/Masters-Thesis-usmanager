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

package pt.unl.fct.miei.usmanagement.manager.services.management.location;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class LocationCount implements Comparable<LocationCount> {

	private final String locationKey;
	private final String city;
	private final String country;
	private final String region;
	private final double countPercentage;
	private final int localContainers;
	private final int countryContainers;
	private final int regionContainers;

	@Override
	public int compareTo(LocationCount o) {
		return compareByRegion(o);
	}

	private int compareByRegion(LocationCount o) {
		if (this.getRegion().equals(o.getRegion())) {
			return compareByCountry(o);
		}
		else if (this.getRegionContainers() == 0 && o.getRegionContainers() > 0) {
			return -1;
		}
		else if (this.getRegionContainers() > 0 && o.getRegionContainers() == 0) {
			return 1;
		}
		else {
			double thisPercentageOfRegionContainers = this.getCountPercentage() / Math.max(1, this.getRegionContainers());
			double otherPercentageOfRegionContainers = o.getCountPercentage() / Math.max(1, o.getRegionContainers());
			return Double.compare(otherPercentageOfRegionContainers, thisPercentageOfRegionContainers);
		}
	}

	private int compareByCountry(LocationCount o) {
		if (!this.getCountry().isEmpty() && !o.getCountry().isEmpty()) {
			if (this.getCountry().equals(o.getCountry())) {
				return compareByCity(o);
			}
			else if (this.getCountryContainers() == 0 && o.getCountryContainers() > 0) {
				return -1;
			}
			else if (this.getCountryContainers() > 0 && o.getCountryContainers() == 0) {
				return 1;
			}
			else {
				double thisPercentageOfCountryContainers = this.getCountPercentage() / Math.max(1, this.getCountryContainers());
				double otherPercentageOfCountryContainers = o.getCountPercentage() / Math.max(1, o.getCountryContainers());
				return Double.compare(otherPercentageOfCountryContainers, thisPercentageOfCountryContainers);
			}
		}
		else if (this.getRegionContainers() == 0) {
			if (this.getCountry().isEmpty() && !o.getCountry().isEmpty()) {
				return -1;
			}
			else if (!this.getCountry().isEmpty() && o.getCountry().isEmpty()) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			if (!this.getCountry().isEmpty() && o.getCountry().isEmpty()) {
				return -1;
			}
			else if (this.getCountry().isEmpty() && !o.getCountry().isEmpty()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	private int compareByCity(LocationCount o) {
		if (!this.getCity().isEmpty() && !o.getCity().isEmpty()) {
			if (this.getCity().equals(o.getCity())) {
				return 0;
			}
			else if (this.getLocalContainers() == 0 && o.getLocalContainers() > 0) {
				return -1;
			}
			else if (this.getLocalContainers() > 0 && o.getLocalContainers() == 0) {
				return 1;
			}
			else {
				double thisPercentageOfLocalContainers = this.getCountPercentage() / Math.max(1, this.getLocalContainers());
				double otherPercentageOfLocalContainers = o.getCountPercentage() / Math.max(1, o.getLocalContainers());
				return Double.compare(thisPercentageOfLocalContainers, otherPercentageOfLocalContainers);
			}
		}
		else if (this.getCountryContainers() == 0) {
			if (this.getCity().isEmpty() && !o.getCity().isEmpty()) {
				return -1;
			}
			else if (!this.getCity().isEmpty() && o.getCity().isEmpty()) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			if (!this.getCity().isEmpty() && o.getCity().isEmpty()) {
				return -1;
			}
			else if (this.getCity().isEmpty() && !o.getCity().isEmpty()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

}
