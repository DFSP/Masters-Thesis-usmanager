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

package pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.nodes;

import org.springframework.web.bind.annotation.*;
import pt.unl.fct.miei.usmanagement.manager.database.regions.RegionEntity;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.master.management.docker.swarm.DockerSwarmService;
import pt.unl.fct.miei.usmanagement.manager.master.management.hosts.HostsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/nodes")
public class NodesController {

	private final NodesService nodesService;
	private final HostsService hostsService;
	private final DockerSwarmService dockerSwarmService;

	public NodesController(NodesService nodesService, HostsService hostsService, DockerSwarmService dockerSwarmService) {
		this.nodesService = nodesService;
		this.hostsService = hostsService;
		this.dockerSwarmService = dockerSwarmService;
	}

	@GetMapping
	public List<SimpleNode> getNodes() {
		return nodesService.getNodes();
	}

	@GetMapping("/{id}")
	public SimpleNode getNode(@PathVariable("id") String id) {
		return nodesService.getNode(id);
	}

	@PostMapping
	public List<SimpleNode> addNodes(@RequestBody AddNode addNode) {
		NodeRole role = addNode.getRole();
		int quantity = addNode.getQuantity();
		String host = addNode.getHost();
		List<SimpleNode> nodes = new ArrayList<>(addNode.getQuantity());
		if (host != null) {
			SimpleNode node = hostsService.addHost(host, role);
			nodes.add(node);
		}
		else {
			RegionEntity region = addNode.getRegion();
			String country = addNode.getCountry();
			String city = addNode.getCity();
			for (var i = 0; i < quantity; i++) {
				SimpleNode node = hostsService.addHost(region, country, city, role);
				nodes.add(node);
			}
		}
		return nodes;
	}

	@PutMapping("/{id}")
	public SimpleNode updateNode(@PathVariable String id, @RequestBody SimpleNode node) {
		if (!Objects.equals(id, node.getId())) {
			throw new BadRequestException("Invalid request, path id %s and request body %s don't match", id, node.getId());
		}
		return nodesService.updateNode(id, node);
	}

	@DeleteMapping("/{id}")
	public void removeNode(@PathVariable("id") String id) {
		nodesService.removeNode(id);
	}

	@PostMapping("/{id}/join")
	public SimpleNode rejoinSwarm(@PathVariable("id") String id) {
		return dockerSwarmService.rejoinSwarm(id);
	}

	@DeleteMapping("/{hostname}/leave")
	public void leaveSwarm(@PathVariable("hostname") String hostname) {
		dockerSwarmService.leaveSwarm(hostname);
	}

}
