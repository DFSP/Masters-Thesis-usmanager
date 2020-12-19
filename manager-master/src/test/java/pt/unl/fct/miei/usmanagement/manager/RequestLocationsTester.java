package pt.unl.fct.miei.usmanagement.manager;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.nodes.NodeConstants;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.services.location.LocationWeight;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class RequestLocationsTester {

	private final LocationRequestsService locationRequestsService;

	public RequestLocationsTester(LocationRequestsService locationRequestsService) {
		this.locationRequestsService = locationRequestsService;
	}

	@Test
	public void testMiddlePoint() {
		List<LocationWeight> locationWeightList = List.of(
			new LocationWeight(Node.builder().labels(Map.of(NodeConstants.Label.COORDINATES, new Gson().toJson(new Coordinates(10, 10)))).build(), 1),
			new LocationWeight(Node.builder().labels(Map.of(NodeConstants.Label.COORDINATES, new Gson().toJson(new Coordinates(30, 30)))).build(), 1)
		);
		Coordinates coordinates = locationRequestsService.getServiceWeightedMiddlePoint(locationWeightList);
		assertThat(coordinates.getLatitude()).isEqualTo(20);
		assertThat(coordinates.getLongitude()).isEqualTo(20);

		locationWeightList = List.of(
			new LocationWeight(Node.builder().labels(Map.of(NodeConstants.Label.COORDINATES, new Gson().toJson(new Coordinates(10, 10)))).build(), 1),
			new LocationWeight(Node.builder().labels(Map.of(NodeConstants.Label.COORDINATES, new Gson().toJson(new Coordinates(30, 30)))).build(), 2)
		);
		locationRequestsService.getServiceWeightedMiddlePoint(locationWeightList);
		assertThat(coordinates.getLatitude()).isEqualTo(25);
		assertThat(coordinates.getLongitude()).isEqualTo(25);
	}

}
