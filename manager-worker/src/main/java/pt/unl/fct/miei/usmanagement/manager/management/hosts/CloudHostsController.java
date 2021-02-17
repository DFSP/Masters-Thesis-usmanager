package pt.unl.fct.miei.usmanagement.manager.management.hosts;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.services.hosts.cloud.CloudHostsService;

@RestController
@RequestMapping("/hosts/cloud")
public class CloudHostsController {

	private final CloudHostsService cloudHostsService;

	public CloudHostsController(CloudHostsService cloudHostsService) {
		this.cloudHostsService = cloudHostsService;
	}

	@PostMapping
	public CloudHost startCloudHost(@RequestBody Coordinates coordinates) {
		return cloudHostsService.launchInstance(coordinates);
	}

}
