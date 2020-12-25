package pt.unl.fct.miei.usmanagement.manager.management.elasticIps;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.services.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/eips")
public class ElasticIpsController {

	private final ElasticIpsService elasticIpsService;

	public ElasticIpsController(ElasticIpsService elasticIpsService) {
		this.elasticIpsService = elasticIpsService;
	}

	@GetMapping
	private List<ElasticIp> getElasticIps() {
		return elasticIpsService.getElasticIps();
	}

	@GetMapping("/{region}")
	private ElasticIp getElasticIp(@PathVariable String region) {
		return elasticIpsService.getElasticIp(RegionEnum.getRegion(region));
	}

}
