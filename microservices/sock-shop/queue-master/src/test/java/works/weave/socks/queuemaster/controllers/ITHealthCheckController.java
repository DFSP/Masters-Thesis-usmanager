package works.weave.socks.queuemaster.controllers;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import works.weave.socks.queuemaster.entities.HealthCheck;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ITHealthCheckController {

  @Autowired
  private HealthCheckController healthCheckController;

  @Test
  public void getHealthCheck() throws Exception {
    Map<String, List<HealthCheck>> healthChecks = healthCheckController.getHealth();
    assertThat(healthChecks.get("health").size(), is(equalTo(2)));
  }
}
