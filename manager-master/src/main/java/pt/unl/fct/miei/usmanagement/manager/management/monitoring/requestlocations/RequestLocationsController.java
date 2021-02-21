package pt.unl.fct.miei.usmanagement.manager.management.monitoring.requestlocations;

import org.springframework.web.bind.annotation.*;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationRequest;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationWeight;

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
	public Map<Node, Map<String, LocationRequest>> getLocationRequests(@RequestParam(required = false) Long interval) {
		return locationRequestsService.getNodesLocationRequests(interval, true);
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
