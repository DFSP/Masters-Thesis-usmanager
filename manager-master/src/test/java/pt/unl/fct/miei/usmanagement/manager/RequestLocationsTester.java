package pt.unl.fct.miei.usmanagement.manager;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration(classes={LocationRequestsService.class})
public class RequestLocationsTester {

	@Autowired
	private LocationRequestsService locationRequestsService;

	@Test
	public void testMiddlePoint() {
		List<LocationWeight> locationWeightList = List.of(
			new LocationWeight(new Coordinates(10, 10), 1),
			new LocationWeight(new Coordinates(30, 30), 1)
		);
		Coordinates coordinates = locationRequestsService.getServiceWeightedMiddlePoint(locationWeightList);
		assertThat(coordinates.getLatitude()).isEqualTo(20);
		assertThat(coordinates.getLongitude()).isEqualTo(20);

		locationWeightList = List.of(
			new LocationWeight(new Coordinates(10, 10), 1),
			new LocationWeight(new Coordinates(30, 30), 2)
		);
		locationRequestsService.getServiceWeightedMiddlePoint(locationWeightList);
		assertThat(coordinates.getLatitude()).isEqualTo(25);
		assertThat(coordinates.getLongitude()).isEqualTo(25);
	}

}
