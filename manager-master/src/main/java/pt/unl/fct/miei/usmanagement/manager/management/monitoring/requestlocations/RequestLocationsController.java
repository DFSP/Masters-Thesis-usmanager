package pt.unl.fct.miei.usmanagement.manager.management.monitoring.requestlocations;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationWeight;
import pt.unl.fct.miei.usmanagement.manager.services.location.NodeLocationRequests;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/request-locations")
public class RequestLocationsController {

	private final LocationRequestsService locationRequestsService;

	private RequestLocationsController(LocationRequestsService locationRequestsService) {
		this.locationRequestsService = locationRequestsService;
	}

	@GetMapping("/locations")
	public List<NodeLocationRequests> getLocationRequests() {
		return locationRequestsService.getNodesLocationRequests();
	}

	@GetMapping("/locations/weight")
	public Map<String, List<LocationWeight>> getLocationsWeight() {
		return locationRequestsService.getLocationsWeight();
	}

	@GetMapping("/services/middle-point")
	public Map<String, Coordinates> getServicesWeightedMiddlePoint() {
		return locationRequestsService.getServicesWeightedMiddlePoint();
	}

}
