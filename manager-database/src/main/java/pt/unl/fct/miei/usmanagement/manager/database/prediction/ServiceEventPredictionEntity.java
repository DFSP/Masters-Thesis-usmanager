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

package pt.unl.fct.miei.usmanagement.manager.database.prediction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pt.unl.fct.miei.usmanagement.manager.database.services.ServiceEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "service_event_predictions")
public class ServiceEventPredictionEntity {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Column(unique = true)
	private String name;

	private String description;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate startDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate endDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;

	private int minimumReplicas;

	@JsonIgnore
	@JoinColumn(name = "service_id")
	@ManyToOne
	private ServiceEntity service;

	@JsonIgnore
	private Timestamp lastUpdate;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceEventPredictionEntity)) {
			return false;
		}
		ServiceEventPredictionEntity other = (ServiceEventPredictionEntity) o;
		return id != null && id.equals(other.getId());
	}

}
