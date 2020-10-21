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

package pt.unl.fct.miei.usmanagement.manager.database.hosts;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Coordinates implements Serializable {

	private final String label;
	private final String title;
	private final double latitude;
	private final double longitude;

	public Coordinates() {
		this(null, null, 0, 0);
	}

	public Coordinates(double latitude, double longitude) {
		this(null, null, latitude, longitude);
	}

	public Coordinates(String label, double latitude, double longitude) {
		this(label, null, latitude, longitude);
	}

	public double distanceTo(Coordinates coordinates) {
		// Spherical Law of Cosines formula. Great-circle distance
		// d = acos( sin φ1 ⋅ sin φ2 + cos φ1 ⋅ cos φ2 ⋅ cos Δλ ) ⋅ R
		double ph1 = this.latitude * Math.PI / 180;
		double ph2 = coordinates.latitude * Math.PI / 180;
		double deltaY = (coordinates.longitude - this.longitude) * Math.PI / 180;
		double r = 6371e3;
		return Math.acos(Math.sin(ph1) * Math.sin(ph2) + Math.cos(ph1) * Math.cos(ph2) * Math.cos(deltaY)) * r;
	}

}
