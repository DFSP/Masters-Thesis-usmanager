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

package pt.unl.fct.miei.usmanagement.manager.hosts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.PlaceEnum;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class HostAddress implements Serializable {

	private static final long serialVersionUID = -7952567936761304029L;

	private final String username;
	private final String publicDnsName;
	private final String publicIpAddress;
	private final String privateIpAddress;
	private final Coordinates coordinates;
	private final RegionEnum region;
	private final PlaceEnum place;

	public HostAddress() {
		this(null);
	}

	public HostAddress(String publicIpAddress) {
		this(publicIpAddress, null);
	}

	public HostAddress(String publicIpAddress, String privateIpAddress) {
		this(null, publicIpAddress, privateIpAddress);
	}

	public HostAddress(String username, String publicIpAddress, String privateIpAddress) {
		this(username, publicIpAddress, privateIpAddress, null);
	}

	public HostAddress(String publicIpAddress, String privateIpAddress, Coordinates coordinates) {
		this(null, publicIpAddress, privateIpAddress, coordinates);
	}

	public HostAddress(String username, String publicIpAddress, String privateIpAddress, Coordinates coordinates) {
		this(username, publicIpAddress, privateIpAddress, coordinates, null, null);
	}

	public HostAddress(String username, String publicIpAddress, String privateIpAddress, Coordinates coordinates,
					   RegionEnum region) {
		this(username, publicIpAddress, privateIpAddress, coordinates, region, null);
	}

	public HostAddress(String username, String publicIpAddress, String privateIpAddress, Coordinates coordinates,
					   RegionEnum region, PlaceEnum place) {
		this.username = username;
		this.publicDnsName = null;
		this.privateIpAddress = privateIpAddress;
		this.publicIpAddress = publicIpAddress;
		this.coordinates = coordinates;
		this.region = region;
		this.place = place;
	}

	@JsonIgnore
	public String getHostname() {
		return publicDnsName != null ? publicDnsName : publicIpAddress;
	}

	@JsonIgnore
	public boolean hasConnectionInfo() {
		return username != null && publicIpAddress != null/* && privateIpAddress != null*/;
	}

	@JsonIgnore
	public boolean isComplete() {
		return username != null && publicIpAddress != null && privateIpAddress != null && coordinates != null
			&& region != null && place != null;
	}

	@Override
	public String toString() {
		return (username == null ? "" : username + "@")
			+ publicIpAddress
			+ (privateIpAddress == null ? "" : "/" + privateIpAddress)
			+ (publicDnsName == null ? "" : "/" + publicDnsName)
			+ (region == null ? "" : "/" + region.getRegion());
	}

	@JsonIgnore
	public String toSimpleString() {
		return (username == null ? "" : username + "@")
			+ publicIpAddress
			+ (privateIpAddress == null ? "" : "/" + privateIpAddress);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		HostAddress that = (HostAddress) o;
		return Objects.equals(publicIpAddress, that.publicIpAddress);
		// TODO implement equals to differentiate between edge hosts under the same network
		/*return Objects.equals(username, that.username) &&
			Objects.equals(publicDnsName, that.publicDnsName) &&
			Objects.equals(publicIpAddress, that.publicIpAddress) &&
			Objects.equals(privateIpAddress, that.privateIpAddress);*/
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, publicDnsName, publicIpAddress, privateIpAddress);
	}

}
