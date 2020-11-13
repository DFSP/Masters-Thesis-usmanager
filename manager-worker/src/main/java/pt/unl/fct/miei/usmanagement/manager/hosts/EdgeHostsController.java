package pt.unl.fct.miei.usmanagement.manager.management.hosts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.ExecuteSftpRequest;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.ExecuteSshRequest;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/hosts/edge")
public class EdgeHostsController {

	private final EdgeHostsService edgeHostsService;
	private final SshService sshService;

	public EdgeHostsController(EdgeHostsService edgeHostsService, SshService sshService) {
		this.edgeHostsService = edgeHostsService;
		this.sshService = sshService;
	}

	@GetMapping
	public List<EdgeHost> getEdgeHosts() {
		return edgeHostsService.getEdgeHosts();
	}

	@GetMapping("/{hostname}")
	public EdgeHost getEdgeHost(@PathVariable String hostname) {
		return edgeHostsService.getEdgeHostByHostname(hostname);
	}

	@PostMapping
	public EdgeHost addEdgeHost(@RequestBody AddEdgeHost addEdgeHost) {
		log.info("{}", addEdgeHost);
		return edgeHostsService.addEdgeHost(addEdgeHost.getUsername(), addEdgeHost.getPassword(), addEdgeHost.getPublicIpAddress(),
			addEdgeHost.getPrivateIpAddress(), addEdgeHost.getPublicDnsName(), addEdgeHost.getCoordinates());
	}

	@PutMapping("/{hostname}")
	public EdgeHost updateEdgeHost(@PathVariable String hostname, @RequestBody EdgeHost edgeHost) {
		Validation.validatePutRequest(edgeHost.getId());
		return edgeHostsService.updateEdgeHost(hostname, edgeHost);
	}

	@DeleteMapping("/{hostname}")
	public void deleteEdgeHost(@PathVariable String hostname) {
		edgeHostsService.deleteEdgeHost(hostname);
	}

	@GetMapping("/{hostname}/rules")
	public List<HostRule> getEdgeHostRules(@PathVariable String hostname) {
		return edgeHostsService.getRules(hostname);
	}

	@GetMapping("/{hostname}/rules/{ruleName}")
	public HostRule getEdgeHostRule(@PathVariable String hostname, @PathVariable String ruleName) {
		return edgeHostsService.getRule(hostname, ruleName);
	}

	@PostMapping("/{hostname}/rules")
	public void addEdgeHostRules(@PathVariable String hostname, @RequestBody String[] rules) {
		edgeHostsService.addRules(hostname, Arrays.asList(rules));
	}

	@DeleteMapping("/{hostname}/rules")
	public void removeEdgeHostRules(@PathVariable String hostname, @RequestBody String[] rules) {
		edgeHostsService.removeRules(hostname, Arrays.asList(rules));
	}

	@DeleteMapping("/{hostname}/rules/{ruleName}")
	public void removeEdgeHostRule(@PathVariable String hostname, @PathVariable String ruleName) {
		edgeHostsService.removeRule(hostname, ruleName);
	}

	@GetMapping("/{hostname}/simulated-metrics")
	public List<HostSimulatedMetric> getEdgeHostSimulatedMetrics(@PathVariable String hostname) {
		return edgeHostsService.getSimulatedMetrics(hostname);
	}

	@GetMapping("/{hostname}/simulated-metrics/{simulatedMetricName}")
	public HostSimulatedMetric getEdgeHostSimulatedMetric(@PathVariable String hostname,
														  @PathVariable String simulatedMetricName) {
		return edgeHostsService.getSimulatedMetric(hostname, simulatedMetricName);
	}

	@PostMapping("/{hostname}/simulated-metrics")
	public void addEdgeHostSimulatedMetrics(@PathVariable String hostname, @RequestBody String[] simulatedMetrics) {
		edgeHostsService.addSimulatedMetrics(hostname, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{hostname}/simulated-metrics")
	public void removeEdgeHostSimulatedMetrics(@PathVariable String hostname, @RequestBody String[] simulatedMetrics) {
		edgeHostsService.removeSimulatedMetrics(hostname, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{hostname}/simulated-metrics/{simulatedMetricName}")
	public void removeEdgeHostSimulatedMetric(@PathVariable String hostname, @PathVariable String simulatedMetricName) {
		edgeHostsService.removeSimulatedMetric(hostname, simulatedMetricName);
	}

	@PostMapping("/{hostname}/ssh")
	public SshCommandResult execute(@PathVariable String hostname, @RequestBody ExecuteSshRequest request) {
		String command = request.getCommand();
		HostAddress hostAddress = new HostAddress(hostname);
		if (request.isBackground()) {
			sshService.executeCommandInBackground(command, hostAddress);
			return new SshCommandResult(hostAddress, command, -1, null, null);
		}
		else {
			return sshService.executeCommandSync(command, hostAddress);
		}
	}

	@PostMapping("/{hostname}/sftp")
	public void upload(@PathVariable String hostname, @RequestBody ExecuteSftpRequest request) {
		sshService.uploadFile(new HostAddress(hostname), request.getFilename());
	}

}
