package pt.unl.fct.miei.usmanagement.manager.management.hosts;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.ExecuteHostSftpRequest;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.ExecuteHostSshRequest;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.ExecuteSftpRequest;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.ExecuteSshRequest;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshCommandResult;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.HostSimulatedMetricEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleEntity;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/hosts/cloud")
public class CloudHostsController {

	private final CloudHostsService cloudHostsService;
	private final SshService sshService;

	public CloudHostsController(CloudHostsService cloudHostsService, SshService sshService) {
		this.cloudHostsService = cloudHostsService;
		this.sshService = sshService;
	}

	@PostMapping
	public CloudHostEntity startCloudHost(@RequestBody AddCloudInstance addCloudInstance) {
		return cloudHostsService.launchInstance(addCloudInstance.getCoordinates());
	}

	@GetMapping
	public List<CloudHostEntity> getCloudHosts() {
		return cloudHostsService.getCloudHosts();
	}

	@PostMapping("/sync")
	public List<CloudHostEntity> synchronizeDatabaseCloudHosts() {
		return cloudHostsService.synchronizeDatabaseCloudHosts();
	}

	@GetMapping("/{instanceId}")
	public CloudHostEntity getCloudHost(@PathVariable String instanceId) {
		return cloudHostsService.getCloudHostById(instanceId);
	}

	@PutMapping("/{instanceId}/start")
	public CloudHostEntity startCloudInstance(@PathVariable String instanceId) {
		return cloudHostsService.startInstance(instanceId, true);
	}

	@PutMapping("/{instanceId}/stop")
	public CloudHostEntity stopCloudInstance(@PathVariable String instanceId) {
		return cloudHostsService.stopInstance(instanceId);
	}

	@DeleteMapping("/{instanceId}")
	public void terminateCloudInstance(@PathVariable String instanceId) {
		cloudHostsService.terminateInstance(instanceId, true);
	}

	@GetMapping("/{instanceId}/rules/{ruleName}")
	public HostRuleEntity getCloudHostRule(@PathVariable String instanceId, String ruleName) {
		return cloudHostsService.getRule(instanceId, ruleName);
	}

	@GetMapping("/{instanceId}/rules")
	public List<HostRuleEntity> getCloudHostRules(@PathVariable String instanceId) {
		return cloudHostsService.getRules(instanceId);
	}

	@PostMapping("/{instanceId}/rules")
	public void addCloudHostRules(@PathVariable String instanceId, @RequestBody String[] rules) {
		cloudHostsService.addRules(instanceId, Arrays.asList(rules));
	}

	@DeleteMapping("/{instanceId}/rules")
	public void removeCloudHostRules(@PathVariable String instanceId, @RequestBody String[] rules) {
		cloudHostsService.removeRules(instanceId, Arrays.asList(rules));
	}

	@DeleteMapping("/{instanceId}/rules/{ruleName}")
	public void removeCloudHostRule(@PathVariable String instanceId, @PathVariable String ruleName) {
		cloudHostsService.removeRule(instanceId, ruleName);
	}

	@GetMapping("/{instanceId}/simulated-metrics")
	public List<HostSimulatedMetricEntity> getCloudHostSimulatedMetrics(@PathVariable String instanceId) {
		return cloudHostsService.getSimulatedMetrics(instanceId);
	}

	@GetMapping("/{instanceId}/simulated-metrics/{simulatedMetricName}")
	public HostSimulatedMetricEntity getCloudHostSimulatedMetric(@PathVariable String instanceId,
																 @PathVariable String simulatedMetricName) {
		return cloudHostsService.getSimulatedMetric(instanceId, simulatedMetricName);
	}

	@PostMapping("/{instanceId}/simulated-metrics")
	public void addCloudHostSimulatedMetrics(@PathVariable String instanceId, @RequestBody String[] simulatedMetrics) {
		cloudHostsService.addSimulatedMetrics(instanceId, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{instanceId}/simulated-metrics")
	public void removeCloudHostSimulatedMetrics(@PathVariable String instanceId, @RequestBody String[] simulatedMetrics) {
		cloudHostsService.removeSimulatedMetrics(instanceId, Arrays.asList(simulatedMetrics));
	}

	@DeleteMapping("/{instanceId}/simulated-metrics/{simulatedMetricName}")
	public void removeCloudHostSimulatedMetric(@PathVariable String instanceId, @PathVariable String simulatedMetricName) {
		cloudHostsService.removeSimulatedMetric(instanceId, simulatedMetricName);
	}

	@GetMapping("/regions")
	public AwsRegion[] getCloudRegions() {
		return AwsRegion.values();
	}

	@PostMapping("/{publicIpAddress}/ssh")
	public SshCommandResult execute(@PathVariable String publicIpAddress, @RequestBody ExecuteSshRequest request) {
		String command = request.getCommand();
		HostAddress hostAddress = new HostAddress(publicIpAddress);
		if (request.isBackground()) {
			sshService.executeCommandInBackground(command, hostAddress);
			return new SshCommandResult(hostAddress, command, -1, null, null);
		}
		else {
			return sshService.executeCommandSync(command, hostAddress);
		}
	}

	@PostMapping("/{publicIpAddress}/sftp")
	public void upload(@PathVariable String publicIpAddress, @RequestBody ExecuteSftpRequest request) {
		sshService.uploadFile(new HostAddress(publicIpAddress), request.getFilename());
	}

}
