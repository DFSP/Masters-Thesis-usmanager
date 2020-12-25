package pt.unl.fct.miei.usmanagement.manager.management.hosts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import pt.unl.fct.miei.usmanagement.manager.services.hosts.AddEdgeHost;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.services.remote.ssh.ExecuteSftpRequest;
import pt.unl.fct.miei.usmanagement.manager.services.remote.ssh.ExecuteSshRequest;
import pt.unl.fct.miei.usmanagement.manager.services.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.services.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.util.validate.Validation;

import java.util.Arrays;
import java.util.List;

@Slf4j
@CrossOrigin
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

	@GetMapping("/{publicIpAddress}/{privateIpAddress}")
	public EdgeHost getEdgeHost(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		return edgeHostsService.getEdgeHostByAddress(new HostAddress(publicIpAddress, privateIpAddress));
	}

	@PostMapping
	public EdgeHost addEdgeHost(@RequestBody AddEdgeHost addEdgeHost) {
		log.info("{}", addEdgeHost);
		return edgeHostsService.addEdgeHost(addEdgeHost.getUsername(), addEdgeHost.getPassword(), addEdgeHost.getPublicIpAddress(),
			addEdgeHost.getPrivateIpAddress(), addEdgeHost.getPublicDnsName(), addEdgeHost.getCoordinates());
	}

	@PutMapping("/{publicIpAddress}/{privateIpAddress}")
	public EdgeHost updateEdgeHost(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody EdgeHost edgeHost) {
		Validation.validatePutRequest(edgeHost.getId());
		return edgeHostsService.updateEdgeHost(new HostAddress(publicIpAddress, privateIpAddress), edgeHost);
	}

	@DeleteMapping("/{publicIpAddress}/{privateIpAddress}")
	public void deleteEdgeHost(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		edgeHostsService.deleteEdgeHost(new HostAddress(publicIpAddress, privateIpAddress));
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}/rules")
	public List<HostRule> getEdgeHostRules(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		return edgeHostsService.getRules(new HostAddress(publicIpAddress, privateIpAddress));
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}/rules/{ruleName}")
	public HostRule getEdgeHostRule(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @PathVariable String ruleName) {
		return edgeHostsService.getRule(new HostAddress(publicIpAddress, privateIpAddress), ruleName);
	}

	@PostMapping("/{publicIpAddress}/{privateIpAddress}/rules")
	public void addEdgeHostRules(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody String[] rules) {
		edgeHostsService.addRules(new HostAddress(publicIpAddress, privateIpAddress), Arrays.asList(rules));
	}

	@DeleteMapping("/{publicIpAddress}/{privateIpAddress}/rules")
	public void removeEdgeHostRules(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody String[] rules) {
		edgeHostsService.removeRules(new HostAddress(publicIpAddress, privateIpAddress), Arrays.asList(rules));
	}

	@DeleteMapping("/{publicIpAddress}/{privateIpAddress}/rules/{ruleName}")
	public void removeEdgeHostRule(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @PathVariable String ruleName) {
		edgeHostsService.removeRule(new HostAddress(publicIpAddress, privateIpAddress), ruleName);
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}/simulated-metrics")
	public List<HostSimulatedMetric> getEdgeHostSimulatedMetrics(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress) {
		return edgeHostsService.getSimulatedMetrics(new HostAddress(publicIpAddress, privateIpAddress));
	}

	@GetMapping("/{publicIpAddress}/{privateIpAddress}/simulated-metrics/{simulatedMetricName}")
	public HostSimulatedMetric getEdgeHostSimulatedMetric(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress,
														  @PathVariable String simulatedMetricName) {
		return edgeHostsService.getSimulatedMetric(new HostAddress(publicIpAddress, privateIpAddress), simulatedMetricName);
	}

	@PostMapping("/{publicIpAddress}/{privateIpAddress}/simulated-metrics")
	public void addEdgeHostSimulatedMetrics(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody String[] simulatedMetrics) {
		edgeHostsService.addSimulatedMetrics(new HostAddress(publicIpAddress, privateIpAddress), Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{publicIpAddress}/{privateIpAddress}/simulated-metrics")
	public void removeEdgeHostSimulatedMetrics(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody String[] simulatedMetrics) {
		edgeHostsService.removeSimulatedMetrics(new HostAddress(publicIpAddress, privateIpAddress), Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{publicIpAddress}/{privateIpAddress}/simulated-metrics/{simulatedMetricName}")
	public void removeEdgeHostSimulatedMetric(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @PathVariable String simulatedMetricName) {
		edgeHostsService.removeSimulatedMetric(new HostAddress(publicIpAddress, privateIpAddress), simulatedMetricName);
	}

	@PostMapping("/{publicIpAddress}/{privateIpAddress}/ssh")
	public SshCommandResult execute(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody ExecuteSshRequest request) {
		String command = request.getCommand();
		HostAddress hostAddress = new HostAddress(publicIpAddress, privateIpAddress);
		if (request.isBackground()) {
			sshService.executeBackgroundProcess(command, hostAddress, null);
			return new SshCommandResult(hostAddress, command, -1, null, null);
		}
		else {
			return sshService.executeCommandSync(command, hostAddress);
		}
	}

	@PostMapping("/{publicIpAddress}/{privateIpAddress}/sftp")
	public void upload(@PathVariable String publicIpAddress, @PathVariable String privateIpAddress, @RequestBody ExecuteSftpRequest request) {
		sshService.uploadFile(new HostAddress(publicIpAddress, privateIpAddress), request.getFilename());
	}

}
