package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.prediction.ServiceEventPrediction;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceEventPredictionDTO {

	private Long id;
	private String name;
	private String description;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate startDate;
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate endDate;
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;
	private int minimumReplicas;
	private ServiceDTO service;
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
		if (!(o instanceof ServiceEventPrediction)) {
			return false;
		}
		ServiceEventPrediction other = (ServiceEventPrediction) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public String toString() {
		return "ServiceEventPredictionDTO{" +
			"id=" + id +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", startDate=" + startDate +
			", startTime=" + startTime +
			", endDate=" + endDate +
			", endTime=" + endTime +
			", minimumReplicas=" + minimumReplicas +
			", service=" + service +
			", lastUpdate=" + lastUpdate +
			'}';
	}
}
