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
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class HostAddress implements Serializable {

	private final String username;
	private final String publicDnsName;
	private final String publicIpAddress;
	private final String privateIpAddress;
	private final Coordinates coordinates;

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
		this.username = username;
		this.publicDnsName = null;
		this.privateIpAddress = privateIpAddress;
		this.publicIpAddress = publicIpAddress;
		this.coordinates = coordinates;
	}

	public String getHostname() {
		return publicDnsName != null ? publicDnsName : publicIpAddress;
	}

	@Override
	public String toString() {
		return (username == null ? "" : username + "@")
			+ publicIpAddress
			+ (privateIpAddress == null ? "" : "/" + privateIpAddress)
			+ (publicDnsName == null ? "" : "/" + publicDnsName);
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
