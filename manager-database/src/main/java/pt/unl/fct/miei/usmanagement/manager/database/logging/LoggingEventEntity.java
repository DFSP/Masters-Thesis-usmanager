/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.database.logging;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "logging_event")
public class LoggingEventEntity {

	@NotNull
	private Long timestmp;

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

	@OneToOne
	private LoggingEventPropertyEntity loggingEventProperty;

	@OneToOne
	private LoggingEventExceptionEntity loggingEventException;

	@Override
	public int hashCode() {
		return Objects.hashCode(getEventId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LoggingEventEntity)) {
			return false;
		}
		LoggingEventEntity other = (LoggingEventEntity) o;
		return eventId != null && eventId.equals(other.getEventId());
	}

}
