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

package pt.unl.fct.miei.usmanagement.manager.management.docker.swarm.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;

import java.util.Map;

@Data
@AllArgsConstructor
public final class SimpleNode {

	private final String id;
	private final String publicIpAddress;
	private final NodeAvailability availability;
	private final NodeRole role;
	private final long version;
	private final Map<String, String> labels;
	private String state;

	@JsonIgnore
	public String getPrivateIpAddress() {
		return labels.get(NodeConstants.Label.PRIVATE_IP_ADDRESS);
	}

	@JsonIgnore
	public String getUsername() {
		return labels.get(NodeConstants.Label.USERNAME);
	}

	@JsonIgnore
	public Coordinates getCoordinates() {
		return new Gson().fromJson(labels.get(NodeConstants.Label.COORDINATES), Coordinates.class);
	}

	@JsonIgnore
	public Region getRegion() {
		return Region.getRegion(labels.get(NodeConstants.Label.REGION));
	}

	@JsonIgnore
	public HostAddress getHostAddress() {
		final String username = getUsername();
		final String privateIpAddress = getPrivateIpAddress();
		final Coordinates coordinates = getCoordinates();
		return new HostAddress(username, publicIpAddress, privateIpAddress, coordinates);
	}

}