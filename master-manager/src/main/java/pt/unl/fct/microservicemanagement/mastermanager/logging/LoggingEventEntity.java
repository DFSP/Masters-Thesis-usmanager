package pt.unl.fct.microservicemanagement.mastermanager.logging;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "logging_event")
public class LoggingEventEntity {

  @NotNull
  private long timestmp;

  @NotNull
  private String formattedMessage;

  @NotNull
  private String loggerName;

  @NotNull
  private String levelString;

  private String threadName;

  private Short referenceFlag;

  private String arg0;

  private String arg1;

  private String arg2;

  private String arg3;

  private String callerFilename;

  private String callerClass;

  private String callerMethod;

  private String callerLine;

  @GeneratedValue
  @Id
  private Long eventId;

}
