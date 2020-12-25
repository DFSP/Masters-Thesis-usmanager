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

package pt.unl.fct.miei.usmanagement.manager.management.nodes;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.exceptions.BadRequestException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.AddNode;
import pt.unl.fct.miei.usmanagement.manager.services.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeRole;
import pt.unl.fct.miei.usmanagement.manager.sync.SyncService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/nodes")
public class NodesController {

	private final NodesService nodesService;
	private final SyncService syncService;

	public NodesController(NodesService nodesService, SyncService syncService) {
		this.nodesService = nodesService;
		this.syncService = syncService;
	}

	@GetMapping
	public List<pt.unl.fct.miei.usmanagement.manager.nodes.Node> getNodes() {
		return nodesService.getNodes();
	}

	@GetMapping("/{id}")
	public pt.unl.fct.miei.usmanagement.manager.nodes.Node getNode(@PathVariable("id") String id) {
		return nodesService.getNode(id);
	}

	@PostMapping
	public List<Node> addNodes(@RequestBody AddNode addNode) {
		NodeRole role = addNode.getRole();
		String host = addNode.getHost();
		List<Coordinates> coordinates = addNode.getCoordinates();
		if (host == null && coordinates == null) {
			throw new BadRequestException("Expected host address or coordinates to start nodes");
		}
		return nodesService.addNodes(role, host, coordinates);
	}

	@PutMapping("/{id}")
	public Node updateNode(@PathVariable String id, @RequestBody Node node) {
		if (!Objects.equals(id, node.getId())) {
			throw new BadRequestException("Invalid request, path id %s and request body %s don't match", id, node.getId());
		}
		return nodesService.updateNodeSpecs(id, node);
	}

	@DeleteMapping("/{id}")
	public void removeNode(@PathVariable("id") String id) {
		nodesService.removeNode(id);
	}

	@PostMapping("/{id}/join")
	public Node rejoinSwarm(@PathVariable("id") String id) {
		return nodesService.rejoinSwarm(id);
	}

	@DeleteMapping("/{hostname}/leave")
	public void leaveSwarm(@PathVariable("hostname") String hostname) {
		nodesService.leaveHost(new HostAddress(hostname));
	}

	@PostMapping("/sync")
	public List<Node> synchronizeNodesDatabase() {
		return syncService.synchronizeNodesDatabase();
	}

}
