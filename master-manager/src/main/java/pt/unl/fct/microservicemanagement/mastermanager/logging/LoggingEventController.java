package pt.unl.fct.microservicemanagement.mastermanager.logging;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LoggingEventController {

  private final LoggingEventService loggingEvents;

  public LoggingEventController(LoggingEventService loggingEvents) {
    this.loggingEvents = loggingEvents;
  }

  @GetMapping
  public List<LoggingEventEntity> getLogs() {
    return loggingEvents.getLogs();
  }

}
